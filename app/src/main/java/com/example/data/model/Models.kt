package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Access code representing a student license for MaxBird.
 * Matches Supabase table: `public.access_codes` (all 17 columns).
 */
@JsonClass(generateAdapter = true)
data class AccessCode(
    @Json(name = "id") val id: String = "",
    @Json(name = "code") val code: String = "",
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "max_devices") val maxDevices: Int = 1,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "expires_at") val expiresAt: String? = null,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "name") val name: String? = "",
    @Json(name = "device_limit") val deviceLimit: Int? = 1,
    @Json(name = "status") val status: String? = "active",
    @Json(name = "duration_days") val durationDays: Int? = 30,
    @Json(name = "validity_days") val validityDays: Int? = 30,
    @Json(name = "expiry_date") val expiryDate: String? = null,
    @Json(name = "note") val note: String? = null,
    @Json(name = "batch") val batch: String? = "",
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "can_view_profiles") val canViewProfiles: Boolean = false
) {
    val effectiveStudentName: String
        get() = studentName.trim().ifBlank { name?.trim().orEmpty().ifBlank { "শিক্ষার্থী" } }

    val effectiveMaxDevices: Int
        get() = if (maxDevices > 0) maxDevices else (deviceLimit ?: 1).coerceAtLeast(1)

    val effectiveExpiresAt: String?
        get() = expiresAt?.takeIf { it.isNotBlank() } ?: expiryDate?.takeIf { it.isNotBlank() }
}

/**
 * Device registered with a particular access code.
 * Matches Supabase table: `public.activated_devices`
 * (`id`, `code_id`, `device_hash`, `app_signature`, `session_token`, `is_blocked`, `activated_at`, `last_seen`, `can_view_profiles`)
 */
@JsonClass(generateAdapter = true)
data class ActivatedDevice(
    @Json(name = "id") val id: String = "",
    @Json(name = "code_id") val codeId: String = "",
    @Json(name = "device_hash") val deviceHash: String = "",
    @Json(name = "device_id") val deviceId: String = "",
    @Json(name = "device_model") val deviceModel: String = "Unknown Device",
    @Json(name = "app_signature") val appSignature: String? = null,
    @Json(name = "session_token") val sessionToken: String = "",
    @Json(name = "is_blocked") val isBlocked: Boolean = false,
    @Json(name = "activated_at") val activatedAt: String = "",
    @Json(name = "last_seen") val lastSeen: String? = null,
    @Json(name = "last_active_at") val lastActiveAt: String? = null,
    @Json(name = "can_view_profiles") val canViewProfiles: Boolean = false
) {
    val effectiveDeviceId: String
        get() = deviceId.trim().ifBlank { deviceHash.trim() }

    val effectiveDeviceModel: String
        get() = deviceModel.trim().takeIf { it.isNotBlank() && it != "Unknown Device" }
            ?: if (deviceHash.isNotBlank()) "Device (${deviceHash.take(10)})" else "Unknown Device"

    val effectiveLastSeen: String?
        get() = lastSeen?.takeIf { it.isNotBlank() } ?: lastActiveAt?.takeIf { it.isNotBlank() }
}

/**
 * Request payload to create an access code in `public.access_codes`.
 * Syncs both `student_name`/`name`, `max_devices`/`device_limit`, `is_active`/`status`, and `duration_days`/`validity_days`.
 */
@JsonClass(generateAdapter = true)
data class CreateAccessCodePayload(
    @Json(name = "code") val code: String,
    @Json(name = "student_name") val studentName: String,
    @Json(name = "name") val name: String = studentName,
    @Json(name = "max_devices") val maxDevices: Int = 1,
    @Json(name = "device_limit") val deviceLimit: Int = maxDevices,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "status") val status: String = if (isActive) "active" else "inactive",
    @Json(name = "duration_days") val durationDays: Int = 30,
    @Json(name = "validity_days") val validityDays: Int = 30,
    @Json(name = "note") val note: String? = "",
    @Json(name = "batch") val batch: String = "",
    @Json(name = "can_view_profiles") val canViewProfiles: Boolean = false
)

/**
 * Request payload to update access code status in `public.access_codes`.
 */
@JsonClass(generateAdapter = true)
data class UpdateCodeStatusPayload(
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "status") val status: String = if (isActive) "active" else "inactive"
)

/**
 * Request payload to block / unblock a device in `public.activated_devices`.
 */
@JsonClass(generateAdapter = true)
data class UpdateDeviceBlockedPayload(
    @Json(name = "is_blocked") val isBlocked: Boolean
)

/**
 * Domain joined device model with student & code info for the kill-switch view.
 */
data class DeviceWithStudent(
    val device: ActivatedDevice,
    val studentName: String,
    val accessCode: String
)

/**
 * Statistics overview for the Admin Dashboard.
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
 * App Update release record.
 * Matches Supabase table: `public.app_updates`
 */
@JsonClass(generateAdapter = true)
data class AppUpdate(
    @Json(name = "id") val id: String = "",
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
)

/**
 * Request payload to create or update an app update release in `public.app_updates`.
 */
@JsonClass(generateAdapter = true)
data class CreateAppUpdatePayload(
    @Json(name = "latest_version_name") val latestVersionName: String,
    @Json(name = "latest_version_code") val latestVersionCode: Int,
    @Json(name = "min_supported_version_code") val minSupportedVersionCode: Int,
    @Json(name = "is_force_update") val isForceUpdate: Boolean,
    @Json(name = "title") val title: String,
    @Json(name = "changelog") val changelog: String,
    @Json(name = "download_url") val downloadUrl: String,
    @Json(name = "button_text") val buttonText: String,
    @Json(name = "apk_checksum_sha256") val apkChecksumSha256: String = "",
    @Json(name = "is_active") val isActive: Boolean
)

/**
 * Request payload to update app update active status.
 */
@JsonClass(generateAdapter = true)
data class UpdateAppUpdateStatusPayload(
    @Json(name = "is_active") val isActive: Boolean
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
    @Json(name = "ban_reason") val banReason: String? = null,
    @Json(name = "banned_at") val bannedAt: String? = null,
    @Json(name = "session_token") val sessionToken: String? = null,
    @Json(name = "requested_at") val requestedAt: String? = null,
    @Json(name = "approved_at") val approvedAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "last_seen") val lastSeen: String? = null,
    @Json(name = "can_view_profiles") val canViewProfiles: Boolean = false
) {
    val isBlockedDevice: Boolean
        get() = isBanned ||
            status.equals("blocked", ignoreCase = true) ||
            status.equals("banned", ignoreCase = true)

    val isPendingDevice: Boolean
        get() = !isBlockedDevice && status.equals("pending", ignoreCase = true)

    val isActiveDevice: Boolean
        get() = !isBlockedDevice && (status.equals("active", ignoreCase = true) || status.isBlank())

    val displayStudentName: String
        get() = studentNote?.trim()?.takeIf { it.isNotEmpty() } ?: "নতুন শিক্ষার্থী"

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
        get() = deviceHash.ifBlank { id.ifBlank { displayDeviceId } }
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

/**
 * Row model for `public.user_gemini_api_keys`.
 */
@JsonClass(generateAdapter = true)
data class UserGeminiApiKey(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "account_key") val accountKey: String = "",
    @Json(name = "user_id") val userId: String? = "",
    @Json(name = "user_phone") val userPhone: String? = "",
    @Json(name = "access_key") val accessKey: String? = "",
    @Json(name = "student_name") val studentName: String? = "",
    @Json(name = "gemini_api_key") val geminiApiKey: String = "",
    @Json(name = "preferred_model") val preferredModel: String = "gemini-3.8-flash",
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)
