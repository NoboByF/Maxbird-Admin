package com.example.data.model

import kotlin.math.roundToLong

/**
 * Exact 17-field model for `portal_settings/main` inside the named Firestore database:
 * `ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0`
 * on Firebase project `empirical-envelope-b0z9n`.
 *
 * Every create/update strictly maintains these exact 17 keys (`hasAll` and `hasOnly`):
 * 1. appName (string, 1..100)
 * 2. apkDownloadUrl (string, 0..1000)
 * 3. r2BucketPublicDomain (string, 0..500)
 * 4. r2ObjectKey (string, 0..300)
 * 5. appVersion (string, 1..60)
 * 6. fileSize (string, 1..40)
 * 7. telegramHandle (string, 1..80)
 * 8. telegramUrl (string, 1..300)
 * 9. noticeText (string, 1..500)
 * 10. releaseNotes (string, 0..1000)
 * 11. downloadCount (integer number, 0..100000000)
 * 12. minAndroidVersion (string, 1..50)
 * 13. lastUpdated (string, 1..60)
 * 14. sha256Short (string, 1..150)
 * 15. ownerId (string - preserved on update, set to current UID on initial create)
 * 16. createdAt (timestamp - preserved on update, NEVER overwritten on update)
 * 17. updatedAt (timestamp - serverTimestamp on every create & update)
 */
data class MaxbirdPortalSettings(
    val appName: String = "Maxbird",
    val apkDownloadUrl: String = "",
    val r2BucketPublicDomain: String = "",
    val r2ObjectKey: String = "",
    val appVersion: String = "v1.0.0",
    val fileSize: String = "10 MB",
    val telegramHandle: String = "@Fahim_Miya_F",
    val telegramUrl: String = "https://t.me/Fahim_Miya_F",
    val noticeText: String = "Maxbird অফিশিয়াল অ্যাপের নতুন ভার্সন এখন ডাউনলোডের জন্য উন্মুক্ত!",
    val releaseNotes: String = "• উন্নত পারফরম্যান্স এবং নতুন কোর্স সিস্টেম\n• দ্রুত ডাউনলোড ও বাগ ফিক্স",
    val downloadCount: Long = 0L,
    val minAndroidVersion: String = "Android 7.0+",
    val lastUpdated: String = "নভেম্বর ২০২৬",
    val sha256Short: String = "Play Protect Verified",
    val ownerId: String = "",
    val createdAtIso: String? = null,
    val updatedAtIso: String? = null,
    val documentExists: Boolean = false
) {
    /**
     * Validates and sanitizes all 17 fields according to the Firestore security rules.
     * Returns a pair of (SanitizedSettings, ValidationErrorMessageBn?).
     */
    fun validateAndSanitize(currentAuthUid: String): Pair<MaxbirdPortalSettings, String?> {
        val cleanAppName = appName.trim().ifEmpty { "Maxbird" }.take(100)
        val cleanApkUrl = apkDownloadUrl.trim().take(1000)
        val cleanR2Domain = r2BucketPublicDomain.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
            .take(500)
        val cleanR2Key = r2ObjectKey.trim().trimStart('/').take(300)
        val cleanAppVersion = appVersion.trim().ifEmpty { "v1.0.0" }.take(60)
        val cleanFileSize = fileSize.trim().ifEmpty { "10 MB" }.take(40)
        val cleanTelegramHandle = telegramHandle.trim().ifEmpty { "@Fahim_Miya_F" }.take(80)
        val cleanTelegramUrl = telegramUrl.trim().ifEmpty { "https://t.me/Fahim_Miya_F" }.take(300)
        val cleanNoticeText = noticeText.trim().ifEmpty { "Maxbird অফিশিয়াল ওয়েবসাইটে স্বাগতম!" }.take(500)
        val cleanReleaseNotes = releaseNotes.trim().take(1000)
        val safeDownloadCount = downloadCount.toDouble().roundToLong().coerceIn(0L, 100_000_000L)
        val cleanMinAndroid = minAndroidVersion.trim().ifEmpty { "Android 7.0+" }.take(50)
        val cleanLastUpdated = lastUpdated.trim().ifEmpty { "নভেম্বর ২০২৬" }.take(60)
        val cleanSha256 = sha256Short.trim().ifEmpty { "Play Protect Verified" }.take(150)

        // Preserve existing ownerId if document exists and has ownerId; otherwise use currentAuthUid
        val effectiveOwnerId = if (documentExists && ownerId.isNotBlank()) {
            ownerId.trim()
        } else {
            currentAuthUid.trim().ifEmpty { ownerId.trim().ifEmpty { "admin" } }
        }

        if (cleanAppName.isEmpty()) return this to "App Name খালি রাখা যাবে না (১-১০০ অক্ষর)।"
        if (cleanAppVersion.isEmpty()) return this to "App Version খালি রাখা যাবে না (১-৬০ অক্ষর)।"
        if (cleanFileSize.isEmpty()) return this to "File Size খালি রাখা যাবে না (১-৪০ অক্ষর)।"
        if (cleanTelegramHandle.isEmpty()) return this to "Telegram Handle খালি রাখা যাবে না (১-৮০ অক্ষর)।"
        if (cleanTelegramUrl.isEmpty()) return this to "Telegram URL খালি রাখা যাবে না (১-৩০০ অক্ষর)।"
        if (cleanNoticeText.isEmpty()) return this to "Notice Text খালি রাখা যাবে না (১-৫০০ অক্ষর)।"
        if (cleanMinAndroid.isEmpty()) return this to "Min Android Version খালি রাখা যাবে না (১-৫০ অক্ষর)।"
        if (cleanLastUpdated.isEmpty()) return this to "Last Updated খালি রাখা যাবে না (১-৬০ অক্ষর)।"
        if (cleanSha256.isEmpty()) return this to "SHA-256 / Security Badge খালি রাখা যাবে না (১-১৫০ অক্ষর)।"

        val sanitized = copy(
            appName = cleanAppName,
            apkDownloadUrl = cleanApkUrl,
            r2BucketPublicDomain = cleanR2Domain,
            r2ObjectKey = cleanR2Key,
            appVersion = cleanAppVersion,
            fileSize = cleanFileSize,
            telegramHandle = cleanTelegramHandle,
            telegramUrl = cleanTelegramUrl,
            noticeText = cleanNoticeText,
            releaseNotes = cleanReleaseNotes,
            downloadCount = safeDownloadCount,
            minAndroidVersion = cleanMinAndroid,
            lastUpdated = cleanLastUpdated,
            sha256Short = cleanSha256,
            ownerId = effectiveOwnerId
        )
        return sanitized to null
    }

    companion object {
        const val FIREBASE_PROJECT_ID = "empirical-envelope-b0z9n"
        const val FIREBASE_APP_ID = "1:164560756368:web:584b3905654b76c8e1c215"
        const val FIREBASE_API_KEY = "AIzaSyBzktkBCXHoSNaTfCJ39Lu3ibynDhX5Iu8"
        const val FIREBASE_AUTH_DOMAIN = "empirical-envelope-b0z9n.firebaseapp.com"
        const val FIRESTORE_DATABASE_ID = "ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0"
        const val COLLECTION_NAME = "portal_settings"
        const val DOCUMENT_ID = "main"
        const val ADMIN_EMAIL = "maiafahim1234@gmail.com"
        const val PUBLIC_WEBSITE_URL = "https://maxbird.pages.dev"
        const val FIREBASE_AUTH_SETTINGS_URL =
            "https://console.firebase.google.com/project/empirical-envelope-b0z9n/authentication/settings"

        fun autoBuildR2Url(domain: String, objectKey: String): String {
            val cleanDomain = domain.trim()
                .removePrefix("https://")
                .removePrefix("http://")
                .trimEnd('/')
            val cleanKey = objectKey.trim().trimStart('/')
            return if (cleanDomain.isNotEmpty() && cleanKey.isNotEmpty()) {
                "https://$cleanDomain/$cleanKey"
            } else {
                ""
            }
        }
    }
}

/**
 * Represents the authenticated Firebase Admin state for Maxbird Website Control Panel.
 */
data class MaxbirdAdminAuthSession(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val idToken: String = "",
    val refreshToken: String = "",
    val isSignedIn: Boolean = false
) {
    val isAuthorizedAdminEmail: Boolean
        get() = email.trim().equals(MaxbirdPortalSettings.ADMIN_EMAIL, ignoreCase = true)
}
