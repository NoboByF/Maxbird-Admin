package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Supabase configuration settings and session state.
 */
class AdminPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "maxbird_admin_prefs"
        private const val KEY_ADMIN_PIN = "admin_pin"
        private const val KEY_SUPABASE_URL = "supabase_url"
        private const val KEY_SUPABASE_KEY = "supabase_key"
        private const val KEY_SAVED_PAYMENT_METHODS_JSON = "saved_payment_methods_json"
        const val DEFAULT_PIN = "8899"

        // Default project fallback (public base URL)
        const val DEFAULT_FALLBACK_URL = "https://aaxonnktltywbwgjxpot.supabase.co/"
        const val DEFAULT_FALLBACK_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.fake_key"

        fun isServiceRoleJwt(key: String): Boolean {
            return key.trim().contains("InJvbGUiOiJzZXJ2aWNlX3JvbGUi")
        }
    }

    fun getSavedPaymentMethodsJson(): String? {
        return prefs.getString(KEY_SAVED_PAYMENT_METHODS_JSON, null)
    }

    fun setSavedPaymentMethodsJson(json: String) {
        prefs.edit().putString(KEY_SAVED_PAYMENT_METHODS_JSON, json).apply()
    }

    private val _isSessionUnlocked = MutableStateFlow(true) // Always unlocked (No PIN barrier)
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

        // Check BuildConfig if provided via .env
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

    fun getSupabaseKey(): String {
        val saved = prefs.getString(KEY_SUPABASE_KEY, "")?.trim().orEmpty()
        if (saved.isNotEmpty() && isServiceRoleJwt(saved)) {
            return saved
        }

        // Check BuildConfig SERVICE_ROLE first (injected via Secrets panel)
        try {
            val serviceRoleField = BuildConfig::class.java.getField("SERVICE_ROLE")
            val serviceRoleVal = (serviceRoleField.get(null) as? String)?.trim().orEmpty()
            if (serviceRoleVal.isNotEmpty() && !serviceRoleVal.contains("your-") && !serviceRoleVal.contains("fake_key")) {
                return serviceRoleVal
            }
        } catch (_: Exception) {
            // Ignore
        }

        if (saved.isNotEmpty() && !saved.contains("fake_key")) {
            return saved
        }

        val candidateKeys = listOf("SUPABASE_KEY", "SUPABASE_ANON_KEY")
        for (fieldKey in candidateKeys) {
            try {
                val field = BuildConfig::class.java.getField(fieldKey)
                val value = field.get(null) as? String
                if (!value.isNullOrBlank() && !value.contains("your-") && !value.contains("fake_key")) {
                    return value.trim()
                }
            } catch (_: Exception) {
                // Ignore and try next
            }
        }
        return DEFAULT_FALLBACK_KEY
    }

    fun setSupabaseKey(key: String) {
        prefs.edit().putString(KEY_SUPABASE_KEY, key.trim()).apply()
    }

    fun isCustomConfigured(): Boolean {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        return !url.contains("your-project-ref") && !key.contains("fake_key")
    }

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

