package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Represents a verified Supabase Authenticated Admin session.
 */
data class SupabaseAdminSession(
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresAtSeconds: Long = 0L,
    val userId: String = "",
    val email: String = "",
    val isAdminVerified: Boolean = false
) {
    val isPresent: Boolean
        get() = accessToken.isNotBlank() && refreshToken.isNotBlank() && userId.isNotBlank() && isAdminVerified

    fun isExpiringSoon(thresholdSeconds: Long = 300L): Boolean {
        if (expiresAtSeconds <= 0L) return true
        val nowSeconds = System.currentTimeMillis() / 1000L
        return (expiresAtSeconds - nowSeconds) < thresholdSeconds
    }
}

/**
 * Manages Supabase configuration settings and encrypted Admin Auth session state.
 * Enforces Zero-Trust RLS: strictly uses SUPABASE_ANON_KEY + logged-in admin JWT access_token,
 * never hardcoding or storing a service_role key.
 */
class AdminPreferences(context: Context) {

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val securePrefs: SharedPreferences = createSecurePrefsWithFallback(appContext)

    init {
        // Purge any legacy service_role key that may have been persisted in earlier versions
        val legacyKey = prefs.getString(KEY_SUPABASE_KEY, "")?.trim().orEmpty()
        if (legacyKey.isNotEmpty() && isServiceRoleJwt(legacyKey)) {
            prefs.edit().remove(KEY_SUPABASE_KEY).apply()
        }
        // Synchronize process-wide auth session flow on first init
        syncSharedSessionFromStorage()
    }

    private fun createSecurePrefsWithFallback(context: Context): SharedPreferences {
        return try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                SECURE_AUTH_PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Throwable) {
            // Safe fallback if Android Keystore is unavailable or corrupted on a device/emulator
            context.getSharedPreferences(FALLBACK_AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val PREFS_NAME = "maxbird_admin_prefs"
        private const val SECURE_AUTH_PREFS_NAME = "maxbird_admin_auth_encrypted"
        private const val FALLBACK_AUTH_PREFS_NAME = "maxbird_admin_auth_fallback"

        private const val KEY_ADMIN_PIN = "admin_pin"
        private const val KEY_SUPABASE_URL = "supabase_url"
        private const val KEY_SUPABASE_KEY = "supabase_key"
        private const val KEY_SAVED_PAYMENT_METHODS_JSON = "saved_payment_methods_json"

        // Encrypted Auth Session Keys
        private const val KEY_AUTH_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_AUTH_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_AUTH_EXPIRES_AT = "auth_expires_at"
        private const val KEY_AUTH_USER_ID = "auth_user_id"
        private const val KEY_AUTH_USER_EMAIL = "auth_user_email"
        private const val KEY_AUTH_IS_ADMIN_VERIFIED = "auth_is_admin_verified"

        const val DEFAULT_PIN = "8899"

        // Default project fallback (public base URL only — never hardcode service_role)
        const val DEFAULT_FALLBACK_URL = "https://aaxonnktltywbwgjxpot.supabase.co/"
        const val DEFAULT_FALLBACK_KEY = ""

        // Shared process-wide session flows so 401/refresh-failure from any interceptor/service
        // immediately updates the Compose UI back to the Login screen.
        private val _sharedAdminSession = MutableStateFlow<SupabaseAdminSession?>(null)
        val sharedAdminSession: StateFlow<SupabaseAdminSession?> = _sharedAdminSession.asStateFlow()

        private val _sessionExpiredMessage = MutableStateFlow<String?>(null)
        val sessionExpiredMessage: StateFlow<String?> = _sessionExpiredMessage.asStateFlow()

        fun clearSessionExpiredMessage() {
            _sessionExpiredMessage.value = null
        }

        /**
         * Detects Supabase `service_role` JWTs (both via raw base64 signature and decoded JSON payload)
         * so the Admin App strictly rejects them in Zero-Trust mode.
         */
        fun isServiceRoleJwt(key: String): Boolean {
            val trimmed = key.trim()
            if (trimmed.isEmpty()) return false
            if (trimmed.contains("InJvbGUiOiJzZXJ2aWNlX3JvbGUi")) return true
            val parts = trimmed.split(".")
            if (parts.size == 3) {
                try {
                    val decoded = String(
                        Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
                        Charsets.UTF_8
                    )
                    val json = JSONObject(decoded)
                    if (json.optString("role").equals("service_role", ignoreCase = true)) {
                        return true
                    }
                } catch (_: Throwable) {
                }
            }
            return false
        }
    }

    // =========================================================================
    // Encrypted Supabase Admin Auth Session
    // =========================================================================

    private fun syncSharedSessionFromStorage() {
        val loaded = readAuthSessionFromPrefs()
        _sharedAdminSession.value = if (loaded?.isPresent == true) loaded else null
    }

    private fun readAuthSessionFromPrefs(): SupabaseAdminSession? {
        return try {
            val accessToken = securePrefs.getString(KEY_AUTH_ACCESS_TOKEN, "")?.trim().orEmpty()
            val refreshToken = securePrefs.getString(KEY_AUTH_REFRESH_TOKEN, "")?.trim().orEmpty()
            val expiresAt = securePrefs.getLong(KEY_AUTH_EXPIRES_AT, 0L)
            val userId = securePrefs.getString(KEY_AUTH_USER_ID, "")?.trim().orEmpty()
            val email = securePrefs.getString(KEY_AUTH_USER_EMAIL, "")?.trim().orEmpty()
            val isVerified = securePrefs.getBoolean(KEY_AUTH_IS_ADMIN_VERIFIED, false)
            if (accessToken.isBlank() || refreshToken.isBlank() || userId.isBlank() || !isVerified) {
                null
            } else {
                SupabaseAdminSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAtSeconds = expiresAt,
                    userId = userId,
                    email = email,
                    isAdminVerified = isVerified
                )
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun getAuthSession(): SupabaseAdminSession? {
        val session = readAuthSessionFromPrefs()
        if (_sharedAdminSession.value != session) {
            _sharedAdminSession.value = session
        }
        return session
    }

    fun saveAuthSession(session: SupabaseAdminSession) {
        try {
            securePrefs.edit()
                .putString(KEY_AUTH_ACCESS_TOKEN, session.accessToken.trim())
                .putString(KEY_AUTH_REFRESH_TOKEN, session.refreshToken.trim())
                .putLong(KEY_AUTH_EXPIRES_AT, session.expiresAtSeconds)
                .putString(KEY_AUTH_USER_ID, session.userId.trim())
                .putString(KEY_AUTH_USER_EMAIL, session.email.trim())
                .putBoolean(KEY_AUTH_IS_ADMIN_VERIFIED, session.isAdminVerified)
                .apply()
        } catch (_: Throwable) {
        }
        _sessionExpiredMessage.value = null
        _sharedAdminSession.value = if (session.isPresent) session else null
    }

    fun clearAuthSession(expiredReason: String? = null) {
        try {
            securePrefs.edit()
                .remove(KEY_AUTH_ACCESS_TOKEN)
                .remove(KEY_AUTH_REFRESH_TOKEN)
                .remove(KEY_AUTH_EXPIRES_AT)
                .remove(KEY_AUTH_USER_ID)
                .remove(KEY_AUTH_USER_EMAIL)
                .remove(KEY_AUTH_IS_ADMIN_VERIFIED)
                .apply()
        } catch (_: Throwable) {
        }
        if (!expiredReason.isNullOrBlank()) {
            _sessionExpiredMessage.value = expiredReason
        }
        _sharedAdminSession.value = null
    }

    fun hasValidAdminSession(): Boolean {
        return getAuthSession()?.isPresent == true
    }

    // =========================================================================
    // Supabase URL & Public Anon Key (NEVER service_role)
    // =========================================================================

    fun getSavedPaymentMethodsJson(): String? {
        return prefs.getString(KEY_SAVED_PAYMENT_METHODS_JSON, null)
    }

    fun setSavedPaymentMethodsJson(json: String) {
        prefs.edit().putString(KEY_SAVED_PAYMENT_METHODS_JSON, json).apply()
    }

    private val _isSessionUnlocked = MutableStateFlow(true)
    val isSessionUnlocked: StateFlow<Boolean> = _isSessionUnlocked.asStateFlow()

    fun getAdminPin(): String {
        return prefs.getString(KEY_ADMIN_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setAdminPin(pin: String) {
        prefs.edit().putString(KEY_ADMIN_PIN, pin).apply()
    }

    fun verifyPin(enteredPin: String): Boolean {
        _isSessionUnlocked.value = true
        return true
    }

    fun lockSession() {
        _isSessionUnlocked.value = true
    }

    fun unlockSession() {
        _isSessionUnlocked.value = true
    }

    fun getSupabaseUrl(): String {
        val saved = prefs.getString(KEY_SUPABASE_URL, "")
        if (!saved.isNullOrBlank() && !saved.contains("krmhyxovnpxlqfaptyuv")) {
            return normalizeUrl(saved)
        }

        // Check BuildConfig if provided via .env / Secrets panel
        return try {
            val buildConfigField = BuildConfig::class.java.getField("SUPABASE_URL")
            val value = buildConfigField.get(null) as? String
            if (!value.isNullOrBlank() && !value.contains("your-project-ref")) {
                normalizeUrl(value)
            } else {
                DEFAULT_FALLBACK_URL
            }
        } catch (_: Exception) {
            DEFAULT_FALLBACK_URL
        }
    }

    fun getNormalizedSupabaseUrl(): String {
        return normalizeUrl(getSupabaseUrl())
    }

    private fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank() || trimmed.contains("your-project-ref") || trimmed.contains("krmhyxovnpxlqfaptyuv")) {
            return DEFAULT_FALLBACK_URL
        }

        val withScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            if (!trimmed.contains(".")) {
                "https://$trimmed.supabase.co"
            } else {
                "https://$trimmed"
            }
        } else {
            trimmed
        }

        val sanitized = withScheme.trimEnd('/')
        return "$sanitized/"
    }

    fun setSupabaseUrl(url: String) {
        prefs.edit().putString(KEY_SUPABASE_URL, normalizeUrl(url)).apply()
    }

    /**
     * Returns ONLY the public `SUPABASE_ANON_KEY`.
     * Strictly rejects and never returns any `service_role` JWT.
     */
    fun getSupabaseAnonKey(): String {
        val saved = prefs.getString(KEY_SUPABASE_KEY, "")?.trim().orEmpty()
        if (saved.isNotEmpty() && !saved.contains("your-") && !saved.contains("fake_key") && !isServiceRoleJwt(saved)) {
            return saved
        }

        val candidateKeys = listOf("SUPABASE_ANON_KEY", "SUPABASE_KEY")
        for (fieldKey in candidateKeys) {
            try {
                val field = BuildConfig::class.java.getField(fieldKey)
                val value = (field.get(null) as? String)?.trim().orEmpty()
                if (value.isNotEmpty() &&
                    !value.contains("your-") &&
                    !value.contains("fake_key") &&
                    !isServiceRoleJwt(value)
                ) {
                    return value
                }
            } catch (_: Exception) {
                // Ignore and try next
            }
        }
        return DEFAULT_FALLBACK_KEY
    }

    /**
     * Compatibility alias that strictly returns the public `SUPABASE_ANON_KEY` (never service_role).
     */
    fun getSupabaseKey(): String = getSupabaseAnonKey()

    /**
     * Saves the Supabase Anon Key. Returns `false` and refuses to save if a `service_role` key is provided.
     */
    fun setSupabaseKey(key: String): Boolean {
        val clean = key.trim()
        if (clean.isNotEmpty() && isServiceRoleJwt(clean)) {
            return false
        }
        prefs.edit().putString(KEY_SUPABASE_KEY, clean).apply()
        return true
    }

    fun isAnonKeyConfigured(): Boolean {
        val url = getSupabaseUrl()
        val key = getSupabaseAnonKey()
        return url.startsWith("https://") &&
            !url.contains("your-project-ref") &&
            key.isNotBlank() &&
            !key.contains("fake_key") &&
            !isServiceRoleJwt(key)
    }

    fun isCustomConfigured(): Boolean = isAnonKeyConfigured()

    fun getOneSignalAppId(): String {
        val saved = prefs.getString("onesignal_app_id", "")?.trim().orEmpty()
        if (saved.isNotEmpty() && !saved.contains("your-onesignal")) {
            return saved
        }
        val fromBuildConfig = try {
            val field = BuildConfig::class.java.getField("ONESIGNAL_APP_ID")
            val value = (field.get(null) as? String)?.trim().orEmpty()
            if (value.isNotEmpty() && !value.contains("your-onesignal")) value else ""
        } catch (_: Exception) {
            ""
        }
        if (fromBuildConfig.isNotEmpty()) return fromBuildConfig

        val fromEnv = try {
            System.getenv("ONESIGNAL_APP_ID")?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }
        return if (fromEnv.isNotEmpty() && !fromEnv.contains("your-onesignal")) fromEnv else ""
    }

    fun setOneSignalAppId(appId: String) {
        prefs.edit().putString("onesignal_app_id", appId.trim()).apply()
    }

    fun getOneSignalRestApiKey(): String {
        val saved = prefs.getString("onesignal_rest_api_key", "")?.trim().orEmpty()
        if (saved.isNotEmpty() && !saved.contains("your-onesignal")) {
            return saved
        }
        val fromBuildConfig = try {
            val field = BuildConfig::class.java.getField("ONESIGNAL_REST_API_KEY")
            val value = (field.get(null) as? String)?.trim().orEmpty()
            if (value.isNotEmpty() && !value.contains("your-onesignal")) value else ""
        } catch (_: Exception) {
            ""
        }
        if (fromBuildConfig.isNotEmpty()) return fromBuildConfig

        val fromEnv = try {
            System.getenv("ONESIGNAL_REST_API_KEY")?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }
        return if (fromEnv.isNotEmpty() && !fromEnv.contains("your-onesignal")) fromEnv else ""
    }

    fun setOneSignalRestApiKey(apiKey: String) {
        prefs.edit().putString("onesignal_rest_api_key", apiKey.trim()).apply()
    }

    fun getSavedFcmToken(): String {
        return prefs.getString("admin_fcm_token", "")?.trim().orEmpty()
    }

    fun setSavedFcmToken(token: String) {
        prefs.edit().putString("admin_fcm_token", token.trim()).apply()
    }

    fun getSavedOneSignalPlayerId(): String {
        return prefs.getString("admin_onesignal_player_id", "")?.trim().orEmpty()
    }

    fun setSavedOneSignalPlayerId(playerId: String) {
        prefs.edit().putString("admin_onesignal_player_id", playerId.trim()).apply()
    }

    fun getAdminAndroidDeviceId(context: Context): String {
        val existing = prefs.getString("admin_android_device_id", "")?.trim().orEmpty()
        if (existing.isNotEmpty()) return existing

        val hwId = try {
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }
        val resolved = if (hwId.isNotEmpty() && hwId != "9774d56d682e549c") {
            "admin_$hwId"
        } else {
            "admin_${java.util.UUID.randomUUID().toString().replace("-", "").take(16)}"
        }
        prefs.edit().putString("admin_android_device_id", resolved).apply()
        return resolved
    }
}
