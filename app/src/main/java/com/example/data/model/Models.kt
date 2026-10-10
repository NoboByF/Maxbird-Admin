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
