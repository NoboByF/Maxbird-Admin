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
        const val DEFAULT_PIN = "8899"

        // Default project fallback
        const val DEFAULT_FALLBACK_URL = "https://krmhyxovnpxlqfaptyuv.supabase.co/"
        const val DEFAULT_FALLBACK_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.fake_key"
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
        if (!saved.isNullOrBlank()) {
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
        if (trimmed.isBlank() || trimmed.contains("your-project-ref")) {
            return DEFAULT_FALLBACK_URL
        }

        val withScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            if (!trimmed.contains(".")) {
                // If user entered only project ref e.g. "aaxonnktltywbwgjxpot"
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
        val saved = prefs.getString(KEY_SUPABASE_KEY, "")
        if (!saved.isNullOrBlank()) return saved

        // Check SERVICE_ROLE first (highest priority for Admin operations), then SUPABASE_ANON_KEY, then SUPABASE_KEY
        val candidateKeys = listOf("SERVICE_ROLE", "SUPABASE_ANON_KEY", "SUPABASE_KEY")
        for (fieldKey in candidateKeys) {
            try {
                val field = BuildConfig::class.java.getField(fieldKey)
                val value = field.get(null) as? String
                if (!value.isNullOrBlank() && !value.contains("your-") && !value.contains("fake_key")) {
                    return value
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
        return !url.contains("your-project-ref") && !url.contains("krmhyxovnpxlqfaptyuv")
    }
}
