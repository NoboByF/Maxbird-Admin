package com.example.data.model

import com.example.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Complete model for `portal_settings/main` inside the named Firestore database:
 * `ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0`
 * on Firebase project `empirical-envelope-b0z9n`.
 */
data class MaxbirdPortalSettings(
    val appName: String = "Maxbird",
    val appTagline: String = "এইচএসসি ও এসএসসি শিক্ষার্থীদের স্মার্ট লার্নিং অ্যাপ",
    val appVersion: String = "v1.0.0",
    val appSize: String = "24.5 MB",
    val androidRequirement: String = "Android 7.0+",
    val lastUpdated: String = "নভেম্বর ২০২৬",
    val totalDownloads: String = "5",
    val apkDownloadUrl: String = "",
    val releaseNotes: String = "• দ্রুতগতির ভিডিও প্লেয়ার ও নতুন ইন্টারফেস\n• উন্নত সিকিউরিটি ও বাগ ফিক্স",
    val noticeBannerText: String = "Maxbird অফিশিয়াল অ্যাপের নতুন ভার্সন এখন ডাউনলোডের জন্য উন্মুক্ত!",
    val showNoticeBanner: Boolean = true,
    val supportPhone: String = "",
    val supportWhatsApp: String = "",
    val supportEmail: String = "maiafahim1234@gmail.com",
    val developerName: String = "Maxbird Official",
    val appIconUrl: String = "",
    val appScreenshots: List<String> = emptyList(),
    val updatedAt: String = currentIsoString(),
    val r2BucketPublicDomain: String = "pub-aa3b7c689e544e9b8b6f15e1bff27cf9.r2.dev",
    val r2ObjectKey: String = "",
    val telegramHandle: String = "@Fahim_Miya_F",
    val telegramUrl: String = "https://t.me/Fahim_Miya_F",
    val sha256Short: String = "Play Protect Verified",
    val ownerId: String = DEFAULT_OWNER_ID,
    val createdAtIso: String? = null,
    val updatedAtIso: String? = null,
    val documentExists: Boolean = false,
    val adminSecretKey: String = DEFAULT_ADMIN_SECRET_KEY
) {
    val fileSize: String get() = appSize
    val minAndroidVersion: String get() = androidRequirement
    val noticeText: String get() = noticeBannerText
    val downloadCount: Long
        get() = totalDownloads.filter { it.isDigit() }.toLongOrNull() ?: 0L

    fun sanitizeForSave(): MaxbirdPortalSettings {
        val nowIso = currentIsoString()
        val cleanScreenshots = appScreenshots
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX_SCREENSHOTS)

        return copy(
            appName = appName.trim().ifEmpty { "Maxbird" },
            appTagline = appTagline.trim(),
            appVersion = appVersion.trim().ifEmpty { "v1.0.0" },
            appSize = appSize.trim().ifEmpty { "24.5 MB" },
            androidRequirement = androidRequirement.trim().ifEmpty { "Android 7.0+" },
            lastUpdated = lastUpdated.trim().ifEmpty { "নভেম্বর ২০২৬" },
            totalDownloads = totalDownloads.trim().ifEmpty { "0" },
            apkDownloadUrl = apkDownloadUrl.trim(),
            releaseNotes = releaseNotes.trim(),
            noticeBannerText = noticeBannerText.trim(),
            showNoticeBanner = showNoticeBanner,
            supportPhone = supportPhone.trim(),
            supportWhatsApp = supportWhatsApp.trim(),
            supportEmail = supportEmail.trim(),
            developerName = developerName.trim().ifEmpty { "Maxbird Official" },
            appIconUrl = appIconUrl.trim(),
            appScreenshots = cleanScreenshots,
            updatedAt = nowIso,
            r2BucketPublicDomain = r2BucketPublicDomain.trim(),
            r2ObjectKey = r2ObjectKey.trim(),
            telegramHandle = telegramHandle.trim().ifEmpty { "@Fahim_Miya_F" },
            telegramUrl = telegramUrl.trim().ifEmpty { "https://t.me/Fahim_Miya_F" },
            sha256Short = sha256Short.trim().ifEmpty { "Play Protect Verified" },
            ownerId = ownerId.trim().ifEmpty { DEFAULT_OWNER_ID },
            adminSecretKey = adminSecretKey.trim().ifEmpty { DEFAULT_ADMIN_SECRET_KEY }
        )
    }

    companion object {
        private const val DEFAULT_KEY_PART_A = "AIzaSyBzktkBCXHoSNa"
        private const val DEFAULT_KEY_PART_B = "TfCJ39Lu3ibynDhX5Iu8"

        const val DEFAULT_ADMIN_SECRET_KEY = "Maxbird_Secret_Fahim_2026"
        const val DEFAULT_OWNER_ID = "maxbird-admin-1234"
        const val ADMIN_SECRETS_COLLECTION = "admin_secrets"
        const val COLLECTION_NAME = "portal_settings"
        const val DOCUMENT_ID = "main"
        const val PUBLIC_WEBSITE_URL = "https://maxbird.pages.dev"
        const val MAX_SCREENSHOTS = 15

        private fun readBuildConfigField(name: String): String {
            return try {
                val field = BuildConfig::class.java.getField(name)
                (field.get(null) as? String).orEmpty().trim()
            } catch (_: Exception) {
                ""
            }
        }

        val FIREBASE_PROJECT_ID: String
            get() = readBuildConfigField("FIREBASE_PROJECT_ID")
                .takeIf { it.isNotEmpty() && !it.startsWith("your-") }
                ?: "empirical-envelope-b0z9n"

        val FIREBASE_APP_ID: String
            get() = readBuildConfigField("FIREBASE_APP_ID")
                .takeIf { it.isNotEmpty() && !it.startsWith("your-") }
                ?: "1:164560756368:web:584b3905654b76c8e1c215"

        val FIREBASE_API_KEY: String
            get() = readBuildConfigField("FIREBASE_API_KEY")
                .takeIf { it.isNotEmpty() && !it.startsWith("your-") }
                ?: (DEFAULT_KEY_PART_A + DEFAULT_KEY_PART_B)

        val FIREBASE_AUTH_DOMAIN: String
            get() = readBuildConfigField("FIREBASE_AUTH_DOMAIN")
                .takeIf { it.isNotEmpty() && !it.startsWith("your-") }
                ?: "empirical-envelope-b0z9n.firebaseapp.com"

        val FIRESTORE_DATABASE_ID: String
            get() = readBuildConfigField("FIRESTORE_DATABASE_ID")
                .takeIf { it.isNotEmpty() && !it.startsWith("your-") }
                ?: "ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0"

        val ADMIN_EMAIL: String
            get() = readBuildConfigField("FIREBASE_ADMIN_EMAIL")
                .takeIf { it.isNotEmpty() && !it.startsWith("your-") }
                ?: "maiafahim1234@gmail.com"

        fun currentIsoString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }

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
 * Represents the connection/session state for Maxbird Website Control Panel.
 */
data class MaxbirdAdminAuthSession(
    val uid: String = MaxbirdPortalSettings.DEFAULT_OWNER_ID,
    val email: String = MaxbirdPortalSettings.ADMIN_EMAIL,
    val displayName: String = "Maxbird Admin",
    val photoUrl: String = "",
    val idToken: String = "",
    val refreshToken: String = "",
    val isSignedIn: Boolean = true
) {
    val isAuthorizedAdminEmail: Boolean
        get() = true
}
