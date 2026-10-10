package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Access code representing a student license for MaxBird.
 * Matches Supabase table: `access_codes`
 */
@JsonClass(generateAdapter = true)
data class AccessCode(
    @Json(name = "id") val id: String = "",
    @Json(name = "code") val code: String,
    @Json(name = "student_name") val studentName: String,
    @Json(name = "max_devices") val maxDevices: Int = 1,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "note") val note: String? = null
)

/**
 * Device registered with a particular access code.
 * Matches Supabase table: `activated_devices`
 */
@JsonClass(generateAdapter = true)
data class ActivatedDevice(
    @Json(name = "id") val id: String = "",
    @Json(name = "code_id") val codeId: String,
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "device_model") val deviceModel: String = "Unknown Device",
    @Json(name = "is_blocked") val isBlocked: Boolean = false,
    @Json(name = "activated_at") val activatedAt: String = "",
    @Json(name = "last_active_at") val lastActiveAt: String? = null
)

/**
 * Request payload to create an access code.
 */
@JsonClass(generateAdapter = true)
data class CreateAccessCodePayload(
    @Json(name = "code") val code: String,
    @Json(name = "student_name") val studentName: String,
    @Json(name = "max_devices") val maxDevices: Int = 1,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "note") val note: String? = null
)

/**
 * Request payload to update access code status.
 */
@JsonClass(generateAdapter = true)
data class UpdateCodeStatusPayload(
    @Json(name = "is_active") val isActive: Boolean
)

/**
 * Request payload to block / unblock a device.
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
    val availableSlots: Int = 0
)

/**
 * App Update release record.
 * Matches Supabase table: `app_updates`
 */
@JsonClass(generateAdapter = true)
data class AppUpdate(
    @Json(name = "id") val id: String = "",
    @Json(name = "latest_version_name") val latestVersionName: String,
    @Json(name = "latest_version_code") val latestVersionCode: Int,
    @Json(name = "min_supported_version_code") val minSupportedVersionCode: Int,
    @Json(name = "is_force_update") val isForceUpdate: Boolean = false,
    @Json(name = "title") val title: String,
    @Json(name = "changelog") val changelog: String,
    @Json(name = "download_url") val downloadUrl: String,
    @Json(name = "button_text") val buttonText: String = "এখনই আপডেট করুন",
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = ""
)

/**
 * Request payload to create or update an app update release.
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
 * Notice model mapping to Supabase `app_notices` table.
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
 * Request payload to create or update an app notice.
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
    @Json(name = "icon_type") val iconType: String = "telegram", // 'telegram', 'whatsapp', 'facebook', 'website', 'phone'
    @Json(name = "url") val url: String = "",
    @Json(name = "color_hex") val colorHex: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "priority") val priority: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null
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
 * User Device model representing user telemetry and hardware binding.
 * Matches Supabase table: `public.devices`
 */
@JsonClass(generateAdapter = true)
data class UserDevice(
    @Json(name = "id") val id: String = "",
    @Json(name = "device_hash") val deviceHash: String = "",
    @Json(name = "display_device_id") val displayDeviceId: String = "",
    @Json(name = "device_model") val deviceModel: String = "Unknown Device",
    @Json(name = "device_brand") val deviceBrand: String? = null,
    @Json(name = "android_version") val androidVersion: String? = null,
    @Json(name = "app_version") val appVersion: String? = null,
    @Json(name = "applied_code") val appliedCode: String? = null,
    @Json(name = "is_banned") val isBanned: Boolean = false,
    @Json(name = "ban_reason") val banReason: String? = null,
    @Json(name = "status") val status: String = "active",
    @Json(name = "last_seen") val lastSeen: String? = null
)

/**
 * Request payload to update device ban status.
 */
@JsonClass(generateAdapter = true)
data class UpdateDeviceBanPayload(
    @Json(name = "is_banned") val isBanned: Boolean,
    @Json(name = "status") val status: String,
    @Json(name = "ban_reason") val banReason: String?
)

