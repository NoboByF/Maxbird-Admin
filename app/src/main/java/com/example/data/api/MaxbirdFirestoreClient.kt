package com.example.data.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.MaxbirdAdminAuthSession
import com.example.data.model.MaxbirdPortalSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

/**
 * Dedicated client for Maxbird Admin Control Panel (`https://maxbird.pages.dev`).
 * Connects directly to Firebase Project `empirical-envelope-b0z9n` and the named
 * Firestore database `ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0`,
 * targeting document `portal_settings/main`.
 */
class MaxbirdFirestoreClient(private val context: Context) {

    private val prefs = context.getSharedPreferences("maxbird_web_admin_auth", Context.MODE_PRIVATE)

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val projectId = MaxbirdPortalSettings.FIREBASE_PROJECT_ID
    private val apiKey = MaxbirdPortalSettings.FIREBASE_API_KEY
    private val databaseId = MaxbirdPortalSettings.FIRESTORE_DATABASE_ID

    private val documentResourceName =
        "projects/$projectId/databases/$databaseId/documents/${MaxbirdPortalSettings.COLLECTION_NAME}/${MaxbirdPortalSettings.DOCUMENT_ID}"

    private val documentUrl =
        "https://firestore.googleapis.com/v1/$documentResourceName?key=$apiKey"

    private val commitUrl =
        "https://firestore.googleapis.com/v1/projects/$projectId/databases/$databaseId/documents:commit?key=$apiKey"

    // =========================================================================
    // Session Persistence
    // =========================================================================

    fun getSavedAuthSession(): MaxbirdAdminAuthSession {
        val uid = prefs.getString("uid", "").orEmpty()
        val email = prefs.getString("email", "").orEmpty()
        val displayName = prefs.getString("display_name", "").orEmpty()
        val photoUrl = prefs.getString("photo_url", "").orEmpty()
        val idToken = prefs.getString("id_token", "").orEmpty()
        val refreshToken = prefs.getString("refresh_token", "").orEmpty()
        return MaxbirdAdminAuthSession(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            idToken = idToken,
            refreshToken = refreshToken,
            isSignedIn = idToken.isNotBlank() && email.isNotBlank()
        )
    }

    fun saveAuthSession(session: MaxbirdAdminAuthSession) {
        prefs.edit()
            .putString("uid", session.uid)
            .putString("email", session.email)
            .putString("display_name", session.displayName)
            .putString("photo_url", session.photoUrl)
            .putString("id_token", session.idToken)
            .putString("refresh_token", session.refreshToken)
            .apply()
    }

    fun clearAuthSession() {
        prefs.edit().clear().apply()
    }

    // =========================================================================
    // Firebase Authentication (Google ID Token / Email-Password / Token Refresh)
    // =========================================================================

    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<MaxbirdAdminAuthSession> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim()
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"
            val payload = JSONObject().apply {
                put("email", cleanEmail)
                put("password", password)
                put("returnSecureToken", true)
            }
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    // If the authorized admin email hasn't set up a password account yet, automatically sign up!
                    if (cleanEmail.equals(MaxbirdPortalSettings.ADMIN_EMAIL, ignoreCase = true) &&
                        (bodyStr.contains("EMAIL_NOT_FOUND", ignoreCase = true) ||
                            bodyStr.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                            bodyStr.contains("USER_NOT_FOUND", ignoreCase = true))
                    ) {
                        val signUpFallback = createAdminAccountWithEmailPassword(cleanEmail, password)
                        if (signUpFallback.isSuccess) {
                            return@withContext signUpFallback
                        }
                    }
                    val errMsg = parseFirebaseError(bodyStr)
                    return@withContext Result.failure(Exception(errMsg))
                }
                val json = JSONObject(bodyStr)
                val session = MaxbirdAdminAuthSession(
                    uid = json.optString("localId", ""),
                    email = json.optString("email", cleanEmail),
                    displayName = json.optString("displayName", "Maxbird Admin"),
                    photoUrl = json.optString("photoUrl", ""),
                    idToken = json.optString("idToken", ""),
                    refreshToken = json.optString("refreshToken", ""),
                    isSignedIn = true
                )
                saveAuthSession(session)
                Result.success(session)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAdminAccountWithEmailPassword(
        email: String,
        password: String
    ): Result<MaxbirdAdminAuthSession> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim()
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
            val payload = JSONObject().apply {
                put("email", cleanEmail)
                put("password", password)
                put("returnSecureToken", true)
            }
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errMsg = parseFirebaseError(bodyStr)
                    return@withContext Result.failure(Exception(errMsg))
                }
                val json = JSONObject(bodyStr)
                val session = MaxbirdAdminAuthSession(
                    uid = json.optString("localId", ""),
                    email = json.optString("email", cleanEmail),
                    displayName = json.optString("displayName", "Maxbird Admin"),
                    photoUrl = json.optString("photoUrl", ""),
                    idToken = json.optString("idToken", ""),
                    refreshToken = json.optString("refreshToken", ""),
                    isSignedIn = true
                )
                saveAuthSession(session)
                Result.success(session)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$apiKey"
            val payload = JSONObject().apply {
                put("requestType", "PASSWORD_RESET")
                put("email", email.trim())
            }
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception(parseFirebaseError(bodyStr)))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleIdToken(googleIdToken: String): Result<MaxbirdAdminAuthSession> =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$apiKey"
                val payload = JSONObject().apply {
                    put("postBody", "id_token=$googleIdToken&providerId=google.com")
                    put("requestUri", "https://${MaxbirdPortalSettings.FIREBASE_AUTH_DOMAIN}")
                    put("returnIdpCredential", true)
                    put("returnSecureToken", true)
                }
                val request = Request.Builder()
                    .url(url)
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception(parseFirebaseError(bodyStr)))
                    }
                    val json = JSONObject(bodyStr)
                    val session = MaxbirdAdminAuthSession(
                        uid = json.optString("localId", ""),
                        email = json.optString("email", ""),
                        displayName = json.optString("displayName", "Maxbird Admin"),
                        photoUrl = json.optString("photoUrl", ""),
                        idToken = json.optString("idToken", ""),
                        refreshToken = json.optString("refreshToken", ""),
                        isSignedIn = true
                    )
                    saveAuthSession(session)
                    Result.success(session)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun verifyAndSaveFirebaseIdToken(rawIdToken: String): Result<MaxbirdAdminAuthSession> =
        withContext(Dispatchers.IO) {
            try {
                val cleanToken = rawIdToken.trim()
                val url = "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=$apiKey"
                val payload = JSONObject().apply {
                    put("idToken", cleanToken)
                }
                val request = Request.Builder()
                    .url(url)
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception(parseFirebaseError(bodyStr)))
                    }
                    val json = JSONObject(bodyStr)
                    val users = json.optJSONArray("users")
                    if (users == null || users.length() == 0) {
                        return@withContext Result.failure(Exception("ইউজার তথ্য পাওয়া যায়নি।"))
                    }
                    val userObj = users.getJSONObject(0)
                    val session = MaxbirdAdminAuthSession(
                        uid = userObj.optString("localId", ""),
                        email = userObj.optString("email", ""),
                        displayName = userObj.optString("displayName", "Maxbird Admin"),
                        photoUrl = userObj.optString("photoUrl", ""),
                        idToken = cleanToken,
                        refreshToken = getSavedAuthSession().refreshToken,
                        isSignedIn = true
                    )
                    saveAuthSession(session)
                    Result.success(session)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun refreshAuthTokenIfNeeded(): String? = withContext(Dispatchers.IO) {
        val current = getSavedAuthSession()
        if (current.refreshToken.isBlank()) {
            return@withContext current.idToken.takeIf { it.isNotBlank() }
        }
        try {
            val url = "https://securetoken.googleapis.com/v1/token?key=$apiKey"
            val payload = JSONObject().apply {
                put("grant_type", "refresh_token")
                put("refresh_token", current.refreshToken)
            }
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext current.idToken.takeIf { it.isNotBlank() }
                }
                val bodyStr = response.body?.string().orEmpty()
                val json = JSONObject(bodyStr)
                val newIdToken = json.optString("id_token", current.idToken)
                val newRefreshToken = json.optString("refresh_token", current.refreshToken)
                val newUid = json.optString("user_id", current.uid)
                if (newIdToken.isNotBlank()) {
                    val updated = current.copy(
                        uid = newUid,
                        idToken = newIdToken,
                        refreshToken = newRefreshToken,
                        isSignedIn = true
                    )
                    saveAuthSession(updated)
                    return@withContext newIdToken
                }
                current.idToken.takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            current.idToken.takeIf { it.isNotBlank() }
        }
    }

    // =========================================================================
    // Firestore Read & Write (`portal_settings/main`)
    // =========================================================================

    /**
     * Fetches `portal_settings/main` from named database `ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0`.
     */
    suspend fun fetchPortalSettings(): Result<MaxbirdPortalSettings> = withContext(Dispatchers.IO) {
        try {
            val token = refreshAuthTokenIfNeeded()
            val reqBuilder = Request.Builder()
                .url(documentUrl)
                .get()
            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            httpClient.newCall(reqBuilder.build()).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (response.code == 404) {
                    // Document does not exist yet — return default settings with documentExists = false
                    return@withContext Result.success(
                        MaxbirdPortalSettings(
                            ownerId = getSavedAuthSession().uid,
                            documentExists = false
                        )
                    )
                }
                if (!response.isSuccessful) {
                    val errMsg = parseFirebaseError(bodyStr)
                    return@withContext Result.failure(Exception(errMsg))
                }

                val docJson = JSONObject(bodyStr)
                val fields = docJson.optJSONObject("fields") ?: JSONObject()

                val parsed = MaxbirdPortalSettings(
                    appName = readStringField(fields, "appName", "Maxbird"),
                    apkDownloadUrl = readStringField(fields, "apkDownloadUrl", ""),
                    r2BucketPublicDomain = readStringField(fields, "r2BucketPublicDomain", ""),
                    r2ObjectKey = readStringField(fields, "r2ObjectKey", ""),
                    appVersion = readStringField(fields, "appVersion", "v1.0.0"),
                    fileSize = readStringField(fields, "fileSize", "10 MB"),
                    telegramHandle = readStringField(fields, "telegramHandle", "@Fahim_Miya_F"),
                    telegramUrl = readStringField(fields, "telegramUrl", "https://t.me/Fahim_Miya_F"),
                    noticeText = readStringField(
                        fields,
                        "noticeText",
                        "Maxbird অফিশিয়াল অ্যাপের নতুন ভার্সন এখন ডাউনলোডের জন্য উন্মুক্ত!"
                    ),
                    releaseNotes = readStringField(fields, "releaseNotes", ""),
                    downloadCount = readLongField(fields, "downloadCount", 0L),
                    minAndroidVersion = readStringField(fields, "minAndroidVersion", "Android 7.0+"),
                    lastUpdated = readStringField(fields, "lastUpdated", "নভেম্বর ২০২৬"),
                    sha256Short = readStringField(fields, "sha256Short", "Play Protect Verified"),
                    ownerId = readStringField(fields, "ownerId", ""),
                    createdAtIso = readTimestampField(fields, "createdAt"),
                    updatedAtIso = readTimestampField(fields, "updatedAt"),
                    documentExists = true
                )
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves `portal_settings/main` to the named Firestore database, strictly maintaining
     * the exact 17 required fields (`hasAll` and `hasOnly`) and types:
     * - Preserves existing `ownerId` and `createdAt` on update (NEVER overwrites `createdAt` on update).
     * - Sets `updatedAt` to `REQUEST_TIME` (`serverTimestamp()`) on every save.
     * - Sets `createdAt` to `REQUEST_TIME` (`serverTimestamp()`) ONLY on initial create.
     */
    suspend fun savePortalSettings(
        draft: MaxbirdPortalSettings
    ): Result<MaxbirdPortalSettings> = withContext(Dispatchers.IO) {
        try {
            val session = getSavedAuthSession()
            val token = refreshAuthTokenIfNeeded()
            if (token.isNullOrBlank()) {
                return@withContext Result.failure(
                    Exception("সেভ করার পূর্বে অ্যাডমিন ইমেইল (${MaxbirdPortalSettings.ADMIN_EMAIL}) দিয়ে লগইন করুন।")
                )
            }

            // First fetch the latest document from Firestore so we always know if it exists
            // and never lose its original `createdAt` or `ownerId` on update.
            val latestRemote = fetchPortalSettings().getOrNull()
            val docExists = latestRemote?.documentExists == true
            val preservedCreatedAtIso = latestRemote?.createdAtIso ?: draft.createdAtIso
            val preservedOwnerId = if (docExists && !latestRemote?.ownerId.isNullOrBlank()) {
                latestRemote!!.ownerId
            } else {
                session.uid.ifBlank { draft.ownerId.ifBlank { "admin" } }
            }

            val mergedDraft = draft.copy(
                ownerId = preservedOwnerId,
                createdAtIso = preservedCreatedAtIso,
                documentExists = docExists
            )

            val (sanitized, validationError) = mergedDraft.validateAndSanitize(session.uid)
            if (validationError != null) {
                return@withContext Result.failure(Exception(validationError))
            }

            val hasServerCreatedAt = docExists && !preservedCreatedAtIso.isNullOrBlank()

            // Helper to execute a commit write operation
            fun executeCommit(useUpdateMaskPreserveCreatedAt: Boolean): Pair<Boolean, String> {
                val fieldsObj = JSONObject().apply {
                    put("appName", stringValue(sanitized.appName))
                    put("apkDownloadUrl", stringValue(sanitized.apkDownloadUrl))
                    put("r2BucketPublicDomain", stringValue(sanitized.r2BucketPublicDomain))
                    put("r2ObjectKey", stringValue(sanitized.r2ObjectKey))
                    put("appVersion", stringValue(sanitized.appVersion))
                    put("fileSize", stringValue(sanitized.fileSize))
                    put("telegramHandle", stringValue(sanitized.telegramHandle))
                    put("telegramUrl", stringValue(sanitized.telegramUrl))
                    put("noticeText", stringValue(sanitized.noticeText))
                    put("releaseNotes", stringValue(sanitized.releaseNotes))
                    put("downloadCount", integerValue(sanitized.downloadCount))
                    put("minAndroidVersion", stringValue(sanitized.minAndroidVersion))
                    put("lastUpdated", stringValue(sanitized.lastUpdated))
                    put("sha256Short", stringValue(sanitized.sha256Short))
                    put("ownerId", stringValue(sanitized.ownerId))
                    if (!useUpdateMaskPreserveCreatedAt && hasServerCreatedAt) {
                        put("createdAt", timestampValue(preservedCreatedAtIso!!))
                    }
                }

                val fieldTransforms = JSONArray().apply {
                    if (!useUpdateMaskPreserveCreatedAt && !hasServerCreatedAt) {
                        put(
                            JSONObject().apply {
                                put("fieldPath", "createdAt")
                                put("setToServerValue", "REQUEST_TIME")
                            }
                        )
                    }
                    put(
                        JSONObject().apply {
                            put("fieldPath", "updatedAt")
                            put("setToServerValue", "REQUEST_TIME")
                        }
                    )
                }

                val writeOperation = JSONObject().apply {
                    put(
                        "update",
                        JSONObject().apply {
                            put("name", documentResourceName)
                            put("fields", fieldsObj)
                        }
                    )
                    if (useUpdateMaskPreserveCreatedAt) {
                        // Partial update via updateMask so existing server `createdAt` is preserved bit-for-bit!
                        put(
                            "updateMask",
                            JSONObject().apply {
                                put(
                                    "fieldPaths",
                                    JSONArray().apply {
                                        put("appName")
                                        put("apkDownloadUrl")
                                        put("r2BucketPublicDomain")
                                        put("r2ObjectKey")
                                        put("appVersion")
                                        put("fileSize")
                                        put("telegramHandle")
                                        put("telegramUrl")
                                        put("noticeText")
                                        put("releaseNotes")
                                        put("downloadCount")
                                        put("minAndroidVersion")
                                        put("lastUpdated")
                                        put("sha256Short")
                                        put("ownerId")
                                    }
                                )
                            }
                        )
                    }
                    put("updateTransforms", fieldTransforms)
                }

                val commitPayload = JSONObject().apply {
                    put("writes", JSONArray().put(writeOperation))
                }

                val request = Request.Builder()
                    .url(commitUrl)
                    .header("Authorization", "Bearer $token")
                    .post(commitPayload.toString().toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    return Pair(response.isSuccessful, bodyStr)
                }
            }

            // 1. If document exists, first update using updateMask so server `createdAt` stays untouched
            var (ok, errBody) = executeCommit(useUpdateMaskPreserveCreatedAt = hasServerCreatedAt)
            if (!ok && hasServerCreatedAt) {
                // 2. Fallback to full document write if updateMask was rejected
                val retry = executeCommit(useUpdateMaskPreserveCreatedAt = false)
                ok = retry.first
                errBody = retry.second
            }

            if (!ok) {
                val errMsg = parseFirebaseError(errBody)
                return@withContext Result.failure(Exception(errMsg))
            }

            // Re-fetch to return the fresh live state from Firestore
            val refreshed = fetchPortalSettings().getOrNull() ?: sanitized.copy(documentExists = true)
            Result.success(refreshed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // Local .APK File Inspector Helper
    // =========================================================================

    data class ApkFileInspection(
        val fileName: String,
        val fileSizeFormatted: String,
        val sizeBytes: Long
    )

    fun inspectLocalApkFile(uri: Uri): Result<ApkFileInspection> {
        return try {
            var fileName = "maxbird-release.apk"
            var sizeBytes = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx >= 0) {
                        val rawName = cursor.getString(nameIdx)
                        if (!rawName.isNullOrBlank()) {
                            fileName = rawName.trim()
                        }
                    }
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        sizeBytes = cursor.getLong(sizeIdx)
                    }
                }
            }

            if (sizeBytes <= 0L) {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    if (afd.length > 0L) {
                        sizeBytes = afd.length
                    }
                }
            }

            val mb = sizeBytes.toDouble() / (1024.0 * 1024.0)
            val formattedSize = when {
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                sizeBytes > 0L -> String.format(Locale.US, "%.0f KB", sizeBytes / 1024.0)
                else -> "10 MB"
            }

            Result.success(
                ApkFileInspection(
                    fileName = fileName,
                    fileSizeFormatted = formattedSize,
                    sizeBytes = sizeBytes
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // Firestore Value Helpers
    // =========================================================================

    private fun stringValue(value: String): JSONObject =
        JSONObject().put("stringValue", value)

    private fun integerValue(value: Long): JSONObject =
        JSONObject().put("integerValue", value.toDouble().roundToLong().toString())

    private fun timestampValue(isoString: String): JSONObject =
        JSONObject().put("timestampValue", isoString)

    private fun readStringField(fields: JSONObject, key: String, default: String): String {
        val fieldObj = fields.optJSONObject(key) ?: return default
        return if (fieldObj.has("stringValue")) {
            fieldObj.optString("stringValue", default)
        } else {
            default
        }
    }

    private fun readLongField(fields: JSONObject, key: String, default: Long): Long {
        val fieldObj = fields.optJSONObject(key) ?: return default
        return when {
            fieldObj.has("integerValue") -> fieldObj.optString("integerValue", default.toString()).toLongOrNull() ?: default
            fieldObj.has("doubleValue") -> fieldObj.optDouble("doubleValue", default.toDouble()).roundToLong()
            else -> default
        }
    }

    private fun readTimestampField(fields: JSONObject, key: String): String? {
        val fieldObj = fields.optJSONObject(key) ?: return null
        return fieldObj.optString("timestampValue", "").takeIf { it.isNotBlank() }
    }

    private fun parseFirebaseError(rawBody: String): String {
        if (rawBody.isBlank()) return "সার্ভারের সাথে সংযোগ ব্যর্থ হয়েছে।"
        return try {
            val json = JSONObject(rawBody)
            val errObj = json.optJSONObject("error")
            val rawMessage = errObj?.optString("message").orEmpty().ifBlank { rawBody.take(180) }
            when {
                rawMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                    rawMessage.contains("INVALID_PASSWORD", ignoreCase = true) ->
                    "ইমেইল বা পাসওয়ার্ড সঠিক নয়। আপনি যদি প্রথমবার লগইন করেন তবে 'অ্যাডমিন পাসওয়ার্ড সেট/রিসেট' বা Google Sign-In ব্যবহার করুন।"
                rawMessage.contains("EMAIL_NOT_FOUND", ignoreCase = true) ->
                    "এই ইমেইলে এখনো পাসওয়ার্ড সেট করা হয়নি। নিচে 'প্রথমবার অ্যাকাউন্ট তৈরি / পাসওয়ার্ড সেট' অপশন ব্যবহার করুন অথবা Google Sign-In করুন।"
                rawMessage.contains("EMAIL_EXISTS", ignoreCase = true) ->
                    "এই ইমেইলটি ইতিমধ্যে রেজিস্টার্ড আছে। পাসওয়ার্ড দিয়ে লগইন করুন অথবা পাসওয়ার্ড রিসেট করুন।"
                rawMessage.contains("PERMISSION_DENIED", ignoreCase = true) ||
                    rawMessage.contains("Missing or insufficient permissions", ignoreCase = true) ->
                    "পারমিশন এরর: শুধুমাত্র অনুমোদিত অ্যাডমিন ইমেইল (${MaxbirdPortalSettings.ADMIN_EMAIL}) দিয়ে লগইন থাকলেই ডাটাবেজে পরিবর্তন সেভ করা যাবে।"
                else -> rawMessage
            }
        } catch (_: Exception) {
            rawBody.take(180)
        }
    }
}
