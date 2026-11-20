package com.example.data.api

import com.example.data.preferences.AdminPreferences
import com.example.data.preferences.SupabaseAdminSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Central Supabase GoTrue Auth Manager for Zero-Trust Admin Authentication.
 *
 * - Authenticates via `POST {SUPABASE_URL}/auth/v1/token?grant_type=password` using `SUPABASE_ANON_KEY`.
 * - Verifies admin membership against `public.admin_users` (and fallback RPCs `is_admin` / `check_is_admin`).
 * - Thread-safely refreshes tokens (`POST /auth/v1/token?grant_type=refresh_token`) before expiry (< 5 min)
 *   or on `401 Unauthorized`.
 * - Logs out via `POST /auth/v1/logout` and clears local encrypted session storage.
 */
class SupabaseAuthManager(private val adminPreferences: AdminPreferences) {

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val REFRESH_BUFFER_SECONDS = 300L // 5 minutes
        private val REFRESH_LOCK = Any()

        // Dedicated lightweight OkHttpClient without the Retrofit auth interceptor to avoid recursion
        private val authHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }

    private fun baseUrl(): String = adminPreferences.getNormalizedSupabaseUrl().trimEnd('/')

    private fun anonKey(): String = adminPreferences.getSupabaseAnonKey().trim()

    /**
     * Authenticates an admin using Supabase Email & Password, then verifies `public.admin_users`.
     */
    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<SupabaseAdminSession> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val cleanPassword = password
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return@withContext Result.failure(Exception("সঠিক অ্যাডমিন ইমেইল লিখুন।"))
        }
        if (cleanPassword.isBlank()) {
            return@withContext Result.failure(Exception("পাসওয়ার্ড লিখুন।"))
        }

        val apiKey = anonKey()
        if (apiKey.isBlank() || AdminPreferences.isServiceRoleJwt(apiKey)) {
            return@withContext Result.failure(
                Exception("ভ্যালিড SUPABASE_ANON_KEY সেট করা নেই। নিচের কানেকশন সেটিংসে ANON KEY সেট করুন।")
            )
        }

        val url = "${baseUrl()}/auth/v1/token?grant_type=password"
        val payload = JSONObject().apply {
            put("email", cleanEmail)
            put("password", cleanPassword)
        }.toString()

        try {
            val request = Request.Builder()
                .url(url)
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .header("apikey", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()

            authHttpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errMsg = parseSupabaseError(rawBody, response.code)
                    return@withContext Result.failure(Exception(errMsg))
                }

                val parsed = parseTokenResponse(rawBody, fallbackEmail = cleanEmail)
                    ?: return@withContext Result.failure(
                        Exception("সার্ভার থেকে সঠিক সেশন টোকেন পাওয়া যায়নি।")
                    )

                // Verify that this authenticated user is listed as an active admin in `public.admin_users`
                val adminCheckResult = verifyUserIsAdminBlocking(
                    accessToken = parsed.accessToken,
                    userId = parsed.userId,
                    email = parsed.email
                )

                if (adminCheckResult.isFailure) {
                    // Revoke remote token and clear any local session immediately
                    revokeAccessTokenSilently(parsed.accessToken, apiKey)
                    adminPreferences.clearAuthSession()
                    return@withContext Result.failure(
                        adminCheckResult.exceptionOrNull()
                            ?: Exception("এই অ্যাকাউন্টটি অ্যাডমিন হিসেবে অনুমোদিত নয়।")
                    )
                }

                val verifiedSession = parsed.copy(isAdminVerified = true)
                adminPreferences.saveAuthSession(verifiedSession)
                Result.success(verifiedSession)
            }
        } catch (e: Exception) {
            Result.failure(Exception("লগইন সংযোগ ব্যর্থ: ${e.message ?: "ইন্টারনেট সংযোগ চেক করুন"}"))
        }
    }

    /**
     * Checks `public.admin_users` (`user_id`, `email`, `role`, `created_at`) and live RPCs
     * (`is_admin_user`, `is_admin`, `check_is_admin`) using the newly issued Bearer `access_token`.
     */
    private fun verifyUserIsAdminBlocking(
        accessToken: String,
        userId: String,
        email: String
    ): Result<Boolean> {
        val apiKey = anonKey()
        val encodedUserId = URLEncoder.encode(userId, "UTF-8")

        // 1. Primary check: `public.admin_users` exact live columns (`user_id`, `email`, `role`, `created_at`)
        // RLS allows a signed-in admin to SELECT only their own row (`user_id = auth.uid()`).
        val tableQueries = listOf(
            "${baseUrl()}/rest/v1/admin_users?select=user_id,email,role,created_at&user_id=eq.$encodedUserId&limit=1",
            "${baseUrl()}/rest/v1/admin_users?select=user_id,email,role,created_at&limit=1"
        )

        var tableReachable = false
        for (queryUrl in tableQueries) {
            try {
                val req = Request.Builder()
                    .url(queryUrl)
                    .get()
                    .header("apikey", apiKey)
                    .header("Authorization", "Bearer $accessToken")
                    .header("Accept", "application/json")
                    .build()

                authHttpClient.newCall(req).execute().use { res ->
                    if (res.isSuccessful) {
                        tableReachable = true
                        val body = res.body?.string().orEmpty().trim()
                        if (body.startsWith("[")) {
                            val arr = JSONArray(body)
                            for (i in 0 until arr.length()) {
                                val row = arr.optJSONObject(i) ?: continue
                                val rowUid = row.optString("user_id", "")
                                val rowEmail = row.optString("email", "")
                                val matchesUser =
                                    (rowUid.isNotBlank() && rowUid.equals(userId, ignoreCase = true)) ||
                                    (rowEmail.isNotBlank() && rowEmail.equals(email, ignoreCase = true))
                                if (matchesUser) {
                                    return Result.success(true)
                                }
                            }
                        }
                    }
                }
            } catch (_: Throwable) {
            }
        }

        // 2. Fallback & live RPC check: `is_admin_user()`, `is_admin()`, `check_is_admin()` (return BOOLEAN)
        val rpcEndpoints = listOf(
            "${baseUrl()}/rest/v1/rpc/is_admin_user",
            "${baseUrl()}/rest/v1/rpc/is_admin",
            "${baseUrl()}/rest/v1/rpc/check_is_admin"
        )
        for (rpcUrl in rpcEndpoints) {
            try {
                val req = Request.Builder()
                    .url(rpcUrl)
                    .post("{}".toRequestBody(JSON_MEDIA_TYPE))
                    .header("apikey", apiKey)
                    .header("Authorization", "Bearer $accessToken")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build()

                authHttpClient.newCall(req).execute().use { res ->
                    if (res.isSuccessful) {
                        val body = res.body?.string().orEmpty().trim()
                        if (body.equals("true", ignoreCase = true)) {
                            return Result.success(true)
                        }
                        if (body.startsWith("{")) {
                            val obj = JSONObject(body)
                            if (obj.optBoolean("is_admin_user", false) ||
                                obj.optBoolean("is_admin", false) ||
                                obj.optBoolean("isAdmin", false) ||
                                obj.optBoolean("admin", false)
                            ) {
                                return Result.success(true)
                            }
                        }
                    }
                }
            } catch (_: Throwable) {
            }
        }

        return if (tableReachable) {
            Result.failure(
                Exception("অ্যাক্সেস অস্বীকৃত: এই অ্যাকাউন্টটি `public.admin_users` টেবিলে অ্যাডমিন হিসেবে অনুমোদিত নয়।")
            )
        } else {
            Result.failure(
                Exception("অ্যাডমিন যাচাই ব্যর্থ: `public.admin_users` টেবিল বা `is_admin_user()` RPC অ্যাক্সেস করা যায়নি।")
            )
        }
    }

    /**
     * Verifies the currently stored session against live `public.admin_users` / `is_admin_user()`.
     */
    suspend fun verifyCurrentSessionIsAdmin(): Result<Boolean> = withContext(Dispatchers.IO) {
        val current = adminPreferences.getAuthSession()
            ?: return@withContext Result.failure(Exception("কোনো অ্যাক্টিভ অ্যাডমিন সেশন নেই।"))
        val token = ensureValidAccessTokenBlocking()
            ?: return@withContext Result.failure(Exception("সেশন টোকেন রিফ্রেশ ব্যর্থ হয়েছে।"))
        verifyUserIsAdminBlocking(
            accessToken = token,
            userId = current.userId,
            email = current.email
        )
    }

    /**
     * Ensures a valid, non-expiring `access_token` is available before any Supabase REST/RPC call.
     * Automatically refreshes the token if expired or expiring within 5 minutes.
     */
    fun ensureValidAccessTokenBlocking(): String? {
        val current = adminPreferences.getAuthSession() ?: return null
        if (!current.isExpiringSoon(REFRESH_BUFFER_SECONDS)) {
            return current.accessToken
        }
        val refreshed = refreshSessionBlocking(failedAccessToken = current.accessToken)
        return refreshed?.accessToken
    }

    /**
     * Thread-safe blocking refresh using `POST /auth/v1/token?grant_type=refresh_token`.
     * Prevents concurrent refresh races when multiple API calls detect expiry or 401 simultaneously.
     */
    fun refreshSessionBlocking(failedAccessToken: String? = null): SupabaseAdminSession? {
        synchronized(REFRESH_LOCK) {
            val current = adminPreferences.getAuthSession() ?: return null

            // If another thread already refreshed the token while we waited for the lock, reuse it!
            if (!failedAccessToken.isNullOrBlank() &&
                current.accessToken != failedAccessToken &&
                !current.isExpiringSoon(REFRESH_BUFFER_SECONDS)
            ) {
                return current
            }

            val apiKey = anonKey()
            if (apiKey.isBlank() || current.refreshToken.isBlank()) {
                adminPreferences.clearAuthSession("অ্যাডমিন সেশনের মেয়াদ শেষ হয়েছে। অনুগ্রহ করে আবার লগইন করুন।")
                return null
            }

            val url = "${baseUrl()}/auth/v1/token?grant_type=refresh_token"
            val payload = JSONObject().apply {
                put("refresh_token", current.refreshToken)
            }.toString()

            return try {
                val request = Request.Builder()
                    .url(url)
                    .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                    .header("apikey", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build()

                authHttpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val parsed = parseTokenResponse(rawBody, fallbackEmail = current.email)
                        if (parsed != null) {
                            val updated = parsed.copy(
                                userId = parsed.userId.ifBlank { current.userId },
                                email = parsed.email.ifBlank { current.email },
                                isAdminVerified = true
                            )
                            adminPreferences.saveAuthSession(updated)
                            return updated
                        }
                    }

                    // If 400/401/403 from Supabase Auth, the refresh token is invalid or revoked -> force re-login
                    if (response.code in listOf(400, 401, 403)) {
                        adminPreferences.clearAuthSession("সেশনের মেয়াদ শেষ হয়েছে। অনুগ্রহ করে পুনরায় লগইন করুন।")
                    }
                    null
                }
            } catch (_: Throwable) {
                // Transient network error: do not wipe local session unless the token is already expired
                if (!current.isExpiringSoon(0L)) current else null
            }
        }
    }

    /**
     * Calls `POST {SUPABASE_URL}/auth/v1/logout` and wipes local encrypted tokens.
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        val current = adminPreferences.getAuthSession()
        val apiKey = anonKey()
        if (current != null && current.accessToken.isNotBlank() && apiKey.isNotBlank()) {
            revokeAccessTokenSilently(current.accessToken, apiKey)
        }
        adminPreferences.clearAuthSession()
        Result.success(Unit)
    }

    private fun revokeAccessTokenSilently(accessToken: String, apiKey: String) {
        try {
            val req = Request.Builder()
                .url("${baseUrl()}/auth/v1/logout")
                .post("{}".toRequestBody(JSON_MEDIA_TYPE))
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .build()
            authHttpClient.newCall(req).execute().close()
        } catch (_: Throwable) {
        }
    }

    private fun parseTokenResponse(
        rawBody: String,
        fallbackEmail: String
    ): SupabaseAdminSession? {
        return try {
            val json = JSONObject(rawBody)
            val accessToken = json.optString("access_token", "").trim()
            val refreshToken = json.optString("refresh_token", "").trim()
            if (accessToken.isBlank() || refreshToken.isBlank()) return null

            val expiresAt = if (json.has("expires_at") && !json.isNull("expires_at")) {
                json.optLong("expires_at", 0L)
            } else {
                val expiresIn = json.optLong("expires_in", 3600L)
                (System.currentTimeMillis() / 1000L) + expiresIn
            }

            val userObj = json.optJSONObject("user")
            val userId = userObj?.optString("id", "")?.trim().orEmpty()
            val userEmail = userObj?.optString("email", "")?.trim().orEmpty().ifBlank { fallbackEmail }

            SupabaseAdminSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAtSeconds = expiresAt,
                userId = userId,
                email = userEmail,
                isAdminVerified = false
            )
        } catch (_: Throwable) {
            null
        }
    }

    private fun parseSupabaseError(rawBody: String, statusCode: Int): String {
        val extracted = try {
            val json = JSONObject(rawBody)
            json.optString("error_description")
                .ifBlank { json.optString("msg") }
                .ifBlank { json.optString("message") }
                .ifBlank { json.optString("error") }
        } catch (_: Throwable) {
            ""
        }

        return when {
            extracted.contains("Invalid login credentials", ignoreCase = true) ->
                "ইমেইল বা পাসওয়ার্ড ভুল হয়েছে। অনুগ্রহ করে সঠিক তথ্য দিয়ে চেষ্টা করুন।"
            extracted.contains("Email not confirmed", ignoreCase = true) ->
                "এই ইমেইলটি এখনো কনফার্ম করা হয়নি। Supabase Auth থেকে ইউজার কনফার্ম করুন।"
            extracted.isNotBlank() ->
                "লগইন ব্যর্থ ($statusCode): $extracted"
            else ->
                "লগইন ব্যর্থ হয়েছে (HTTP $statusCode)"
        }
    }
}
