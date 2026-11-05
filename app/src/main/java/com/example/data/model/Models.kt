package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Statistics overview for the Admin Dashboard (100% driven by `public.devices`).
 */
data class DashboardStats(
    val totalCodes: Int = 0,
    val activeStudents: Int = 0,
    val totalDevices: Int = 0,
    val blockedDevices: Int = 0,
    val availableSlots: Int = 0,
    val pendingVerificationDevices: Int = 0,
    val activeVerifiedDevices: Int = 0
)

/**
 * App Update & Global Kill Switch / Maintenance Mode record.
 * Matches Supabase table: `public.app_updates` (supports both new & legacy columns).
 */
@JsonClass(generateAdapter = true)
data class AppUpdate(
    @Json(name = "id") val id: String = "",
    @Json(name = "version_code") val versionCode: Int = 0,
    @Json(name = "version_name") val versionName: String = "",
    @Json(name = "apk_url") val apkUrl: String = "",
    @Json(name = "file_size_mb") val fileSizeMb: Double = 0.0,
    @Json(name = "release_notes") val releaseNotes: String = "",
    @Json(name = "is_kill_switch") val isKillSwitch: Boolean = false,
    @Json(name = "is_maintenance") val isMaintenance: Boolean = false,
    @Json(name = "kill_switch_title") val killSwitchTitle: String = "অ্যাপ আপডেটের কাজ চলছে",
    @Json(name = "kill_switch_message") val killSwitchMessage: String = "অ্যাপটিকে আরও উন্নত, দ্রুত ও নিরাপদ করতে বর্তমানে সার্ভার ও সিস্টেম আপডেটের কাজ চলছে। কাজ শেষ হওয়া মাত্রই অ্যাপটি পুনরায় চালু হবে।",
    @Json(name = "estimated_back_time") val estimatedBackTime: String? = "",
    @Json(name = "latest_version_name") val latestVersionName: String = "",
    @Json(name = "latest_version_code") val latestVersionCode: Int = 0,
    @Json(name = "min_supported_version_code") val minSupportedVersionCode: Int = 0,
    @Json(name = "is_force_update") val isForceUpdate: Boolean = false,
    @Json(name = "title") val title: String = "নতুন আপডেট উপলব্ধ!",
    @Json(name = "changelog") val changelog: String = "",
    @Json(name = "download_url") val downloadUrl: String = "",
    @Json(name = "button_text") val buttonText: String = "এখনই আপডেট করুন",
    @Json(name = "apk_checksum_sha256") val apkChecksumSha256: String = "",
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = ""
) {
    val effectiveVersionCode: Int
        get() = if (versionCode > 0) versionCode else latestVersionCode

    val effectiveVersionName: String
        get() = versionName.ifBlank { latestVersionName }

    val effectiveApkUrl: String
        get() = apkUrl.ifBlank { downloadUrl }

    val effectiveReleaseNotes: String
        get() = releaseNotes.ifBlank { changelog }

    val isKillSwitchActive: Boolean
        get() = isKillSwitch || isMaintenance
}

/**
 * Request payload to create or update an app update release in `public.app_updates`.
 */
@JsonClass(generateAdapter = true)
data class CreateAppUpdatePayload(
    @Json(name = "version_code") val versionCode: Int = 0,
    @Json(name = "version_name") val versionName: String = "",
    @Json(name = "apk_url") val apkUrl: String = "",
    @Json(name = "file_size_mb") val fileSizeMb: Double = 0.0,
    @Json(name = "release_notes") val releaseNotes: String = "",
    @Json(name = "is_kill_switch") val isKillSwitch: Boolean = false,
    @Json(name = "is_maintenance") val isMaintenance: Boolean = false,
    @Json(name = "kill_switch_title") val killSwitchTitle: String = "অ্যাপ আপডেটের কাজ চলছে",
    @Json(name = "kill_switch_message") val killSwitchMessage: String = "অ্যাপটিকে আরও উন্নত, দ্রুত ও নিরাপদ করতে বর্তমানে সার্ভার ও সিস্টেম আপডেটের কাজ চলছে। কাজ শেষ হওয়া মাত্রই অ্যাপটি পুনরায় চালু হবে।",
    @Json(name = "estimated_back_time") val estimatedBackTime: String = "",
    @Json(name = "latest_version_name") val latestVersionName: String = "",
    @Json(name = "latest_version_code") val latestVersionCode: Int = 0,
    @Json(name = "min_supported_version_code") val minSupportedVersionCode: Int = 0,
    @Json(name = "is_force_update") val isForceUpdate: Boolean = false,
    @Json(name = "title") val title: String = "নতুন আপডেট উপলব্ধ!",
    @Json(name = "changelog") val changelog: String = "",
    @Json(name = "download_url") val downloadUrl: String = "",
    @Json(name = "button_text") val buttonText: String = "এখনই আপডেট করুন",
    @Json(name = "apk_checksum_sha256") val apkChecksumSha256: String = "",
    @Json(name = "is_active") val isActive: Boolean = true
)

/**
 * Request payload to update app update active status.
 */
@JsonClass(generateAdapter = true)
data class UpdateAppUpdateStatusPayload(
    @Json(name = "is_active") val isActive: Boolean
)

/**
 * Real Server Push Notification model mapping to Supabase `public.student_notifications` table.
 */
@JsonClass(generateAdapter = true)
data class StudentNotification(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "message") val message: String = "",
    @Json(name = "target_type") val targetType: String = "ALL", // ALL, PHONE, PROGRAM
    @Json(name = "target_value") val targetValue: String? = null,
    @Json(name = "notification_type") val notificationType: String = "ADMIN_PUSH", // ADMIN_PUSH, LIVE_CLASS, EXAM
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "deeplink") val deeplink: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "created_at") val createdAt: String? = null
)

/**
 * Payload for creating a row in `public.student_notifications`.
 */
data class CreateStudentNotificationPayload(
    val title: String,
    val message: String,
    val targetType: String = "ALL", // ALL, PHONE, PROGRAM
    val targetValue: String? = null,
    val notificationType: String = "ADMIN_PUSH", // ADMIN_PUSH, LIVE_CLASS, EXAM
    val imageUrl: String? = null,
    val deeplink: String? = null,
    val isActive: Boolean = true
)

/**
 * Notice model mapping to Supabase `public.app_notices` table.
 */
@JsonClass(generateAdapter = true)
data class AppNotice(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "image_url") val imageUrl: String = "",
    @Json(name = "action_url") val actionUrl: String? = null,
    @Json(name = "action_button_text") val actionButtonText: String = "বিস্তারিত দেখুন",
    @Json(name = "priority") val priority: Int = 0,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "show_as_popup") val showAsPopup: Boolean = true,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = ""
)

/**
 * Request payload to create or update an app notice in `public.app_notices`.
 */
@JsonClass(generateAdapter = true)
data class CreateAppNoticePayload(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "action_url") val actionUrl: String?,
    @Json(name = "action_button_text") val actionButtonText: String = "বিস্তারিত দেখুন",
    @Json(name = "priority") val priority: Int = 0,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "show_as_popup") val showAsPopup: Boolean = true
)

/**
 * Request payload to update notice active status.
 */
@JsonClass(generateAdapter = true)
data class UpdateAppNoticeStatusPayload(
    @Json(name = "is_active") val isActive: Boolean
)

/**
 * Support Link model mapping to Supabase `public.activation_support_links` table.
 * Used for controlling contact & help links shown on the user app's Device Activation screen.
 */
@JsonClass(generateAdapter = true)
data class ActivationSupportLink(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "icon_type") val iconType: String = "telegram",
    @Json(name = "url") val url: String = "",
    @Json(name = "color_hex") val colorHex: String? = "#229ED9",
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "priority") val priority: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

/**
 * Request payload to create or update an activation support link.
 */
@JsonClass(generateAdapter = true)
data class CreateSupportLinkPayload(
    @Json(name = "title") val title: String,
    @Json(name = "subtitle") val subtitle: String?,
    @Json(name = "icon_type") val iconType: String,
    @Json(name = "url") val url: String,
    @Json(name = "color_hex") val colorHex: String?,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "priority") val priority: Int = 0
)

/**
 * Request payload to update support link active status.
 */
@JsonClass(generateAdapter = true)
data class UpdateSupportLinkStatusPayload(
    @Json(name = "is_active") val isActive: Boolean
)

/**
 * User Device model representing One-Click Student Verification & Hardware Control.
 * Matches Supabase table: `public.devices` (all 22 columns).
 */
@JsonClass(generateAdapter = true)
data class UserDevice(
    @Json(name = "id") val id: String = "",
    @Json(name = "device_hash") val deviceHash: String = "",
    @Json(name = "device_id") val deviceIdColumn: String = "",
    @Json(name = "display_device_id") val displayDeviceId: String = "",
    @Json(name = "student_note") val studentNote: String? = null,
    @Json(name = "device_model") val deviceModel: String = "Unknown Device",
    @Json(name = "device_brand") val deviceBrand: String? = null,
    @Json(name = "device_manufacturer") val deviceManufacturer: String? = null,
    @Json(name = "android_version") val androidVersion: String? = null,
    @Json(name = "sdk_version") val sdkVersion: Int? = null,
    @Json(name = "app_version") val appVersion: String? = null,
    @Json(name = "applied_code") val appliedCode: String? = null,
    @Json(name = "status") val status: String = "pending",
    @Json(name = "is_banned") val isBanned: Boolean = false,
    @Json(name = "is_blocked") val isBlockedColumn: Boolean = false,
    @Json(name = "ban_reason") val banReason: String? = null,
    @Json(name = "rejection_reason") val rejectionReasonColumn: String? = null,
    @Json(name = "banned_at") val bannedAt: String? = null,
    @Json(name = "session_token") val sessionToken: String? = null,
    @Json(name = "requested_at") val requestedAt: String? = null,
    @Json(name = "approved_at") val approvedAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "last_seen") val lastSeen: String? = null,
    @Json(name = "last_active_at") val lastActiveAt: String? = null,
    @Json(name = "student_phone") val studentPhone: String? = null,
    @Json(name = "user_phone") val userPhone: String? = null,
    @Json(name = "shikho_account_key") val shikhoAccountKey: String? = null,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "onesignal_player_id") val onesignalPlayerId: String? = null,
    @Json(name = "fcm_token") val fcmToken: String? = null,
    @Json(name = "can_view_profiles") val canViewProfiles: Boolean = false,
    @Json(name = "bound_shikho_user_id") val boundShikhoUserId: String? = null,
    @Json(name = "bound_user_phone") val boundUserPhone: String? = null,
    @Json(name = "is_access_restricted") val isAccessRestricted: Boolean = false,
    @Json(name = "allowed_classes") val allowedClasses: List<String>? = emptyList(),
    @Json(name = "allowed_groups") val allowedGroups: List<String>? = emptyList(),
    @Json(name = "allowed_batches") val allowedBatches: List<String>? = emptyList(),
    @Json(name = "allowed_programs") val allowedPrograms: List<String>? = emptyList()
) {
    val effectiveDeviceHash: String
        get() = deviceHash.ifBlank { deviceIdColumn }

    val effectiveBanReason: String?
        get() = banReason?.takeIf { it.isNotBlank() }
            ?: rejectionReasonColumn?.takeIf { it.isNotBlank() }

    val isBlockedDevice: Boolean
        get() = isBanned ||
            isBlockedColumn ||
            status.equals("blocked", ignoreCase = true) ||
            status.equals("banned", ignoreCase = true) ||
            status.equals("rejected", ignoreCase = true)

    val isPendingDevice: Boolean
        get() = !isBlockedDevice && status.equals("pending", ignoreCase = true)

    val isActiveDevice: Boolean
        get() = !isBlockedDevice && (
            status.equals("active", ignoreCase = true) ||
                status.equals("approved", ignoreCase = true) ||
                status.isBlank()
            )

    val displayStudentName: String
        get() = studentNote?.trim()?.takeIf { it.isNotEmpty() }
            ?: studentName?.trim()?.takeIf { it.isNotEmpty() }
            ?: "নতুন শিক্ষার্থী"

    val extractedPhoneFromDevice: String
        get() {
            val direct = studentPhone?.trim()?.takeIf { it.isNotEmpty() }
                ?: userPhone?.trim()?.takeIf { it.isNotEmpty() }
                ?: boundUserPhone?.trim()?.takeIf { it.isNotEmpty() }
                ?: shikhoAccountKey?.trim()?.takeIf { it.isNotEmpty() }
            if (!direct.isNullOrBlank()) return CourseAccessEnrollment.normalizeBdPhone(direct)
            val noteText = studentNote.orEmpty()
            val match = Regex("01[3-9]\\d{8}").find(noteText)
            return match?.value ?: ""
        }

    val fullPhoneModel: String
        get() {
            val brand = deviceBrand?.trim()?.takeIf { it.isNotEmpty() }
                ?: deviceManufacturer?.trim().orEmpty()
            val model = deviceModel.trim().takeUnless { it.equals("Unknown Device", ignoreCase = true) }.orEmpty()
            return when {
                brand.isNotEmpty() && model.isNotEmpty() && !model.startsWith(brand, ignoreCase = true) -> "$brand $model"
                model.isNotEmpty() -> model
                brand.isNotEmpty() -> brand
                else -> "Unknown Device"
            }
        }

    val effectiveKey: String
        get() = effectiveDeviceHash.ifBlank { id.ifBlank { displayDeviceId } }
}

/**
 * Row model for `public.device_shikho_accounts`.
 * Matches live Supabase schema 100% (`id`, `device_hash`, `display_device_id`, `shikho_user_id`,
 * `user_phone`, `allowed_classes`, `allowed_groups`, `allowed_batches`, `allowed_programs`,
 * `is_access_restricted`, `is_access_granted`, `created_at`, `last_seen_at`).
 */
@JsonClass(generateAdapter = true)
data class DeviceShikhoAccount(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "device_hash") val deviceHash: String = "",
    @Json(name = "display_device_id") val displayDeviceId: String? = "",
    @Json(name = "shikho_user_id") val shikhoUserId: String? = "",
    @Json(name = "user_phone") val userPhone: String = "",
    @Json(name = "student_name") val studentName: String? = "",
    @Json(name = "student_class") val studentClass: String? = "",
    @Json(name = "student_group") val studentGroup: String? = "",
    @Json(name = "is_access_granted") val isAccessGranted: Boolean = true,
    @Json(name = "is_access_restricted") val isAccessRestricted: Boolean = false,
    @Json(name = "allowed_classes") val allowedClasses: List<String>? = emptyList(),
    @Json(name = "allowed_groups") val allowedGroups: List<String>? = emptyList(),
    @Json(name = "allowed_batches") val allowedBatches: List<String>? = emptyList(),
    @Json(name = "allowed_programs") val allowedPrograms: List<String>? = emptyList(),
    @Json(name = "admin_note") val adminNote: String? = "",
    @Json(name = "first_login_at") val firstLoginAt: String? = null,
    @Json(name = "last_seen_at") val lastSeenAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
) {
    val effectiveShikhoUserId: String
        get() = shikhoUserId?.trim()?.takeIf { it.isNotEmpty() }
            ?: userPhone.trim()
}

/**
 * Request payload to update device ban/block status in `public.devices`.
 */
@JsonClass(generateAdapter = true)
data class UpdateDeviceBanPayload(
    @Json(name = "is_banned") val isBanned: Boolean,
    @Json(name = "status") val status: String,
    @Json(name = "ban_reason") val banReason: String?
)
