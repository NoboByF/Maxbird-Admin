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
