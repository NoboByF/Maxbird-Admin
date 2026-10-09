package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Admin Authentication, Security Master PIN,
 * and Supabase configuration settings.
 */
class AdminPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "maxbird_admin_prefs"
        private const val KEY_ADMIN_PIN = "admin_pin"
        private const val KEY_SUPABASE_URL = "supabase_url"
        private const val KEY_SUPABASE_KEY = "supabase_key"
        private const val KEY_DEMO_MODE = "demo_mode"
        const val DEFAULT_PIN = "8899"

        // Default project placeholder
        const val DEFAULT_FALLBACK_URL = "https://krmhyxovnpxlqfaptyuv.supabase.co"
        const val DEFAULT_FALLBACK_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.fake_key"
    }

    private val _isSessionUnlocked = MutableStateFlow(false)
    val isSessionUnlocked: StateFlow<Boolean> = _isSessionUnlocked.asStateFlow()

    fun getAdminPin(): String {
        return prefs.getString(KEY_ADMIN_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setAdminPin(pin: String) {
        prefs.edit().putString(KEY_ADMIN_PIN, pin).apply()
    }

    fun verifyPin(enteredPin: String): Boolean {
        val currentPin = getAdminPin()
        val isValid = (enteredPin == currentPin) || (enteredPin == "998877") // Master recovery override
        if (isValid) {
            _isSessionUnlocked.value = true
        }
        return isValid
    }

    fun lockSession() {
        _isSessionUnlocked.value = false
    }

    fun unlockSession() {
        _isSessionUnlocked.value = true
    }

    fun getSupabaseUrl(): String {
        val saved = prefs.getString(KEY_SUPABASE_URL, "")
        if (!saved.isNullOrBlank()) return saved

        // Check BuildConfig if provided via .env
        return try {
            val buildConfigField = BuildConfig::class.java.getField("SUPABASE_URL")
            val value = buildConfigField.get(null) as? String
            if (!value.isNullOrBlank() && !value.contains("your-project-ref")) {
                value
            } else {
                DEFAULT_FALLBACK_URL
            }
        } catch (_: Exception) {
            DEFAULT_FALLBACK_URL
        }
    }

    fun setSupabaseUrl(url: String) {
        prefs.edit().putString(KEY_SUPABASE_URL, url.trim().trimEnd('/')).apply()
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
