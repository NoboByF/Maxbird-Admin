package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Shikho Official API Login Request (`POST https://api.shikho.com/auth/v2/login`)
 */
@JsonClass(generateAdapter = true)
data class ShikhoLoginProfilePayload(
    @Json(name = "device_id") val deviceId: String = "admin-panel-device-001"
)

@JsonClass(generateAdapter = true)
data class ShikhoLoginRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "otp") val otp: String,
    @Json(name = "auth_type") val authType: String = "login",
    @Json(name = "type") val type: String = "student",
    @Json(name = "profile") val profile: ShikhoLoginProfilePayload = ShikhoLoginProfilePayload(),
    @Json(name = "google_ads_id") val googleAdsId: String = "00000000-0000-0000-0000-000000000000"
)

@JsonClass(generateAdapter = true)
data class ShikhoTokens(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "user_id") val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class ShikhoLoginResponse(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "tokens") val tokens: ShikhoTokens? = null
)

/**
 * Parsed result after logging into Shikho API.
 */
data class ShikhoAuthSession(
    val accessToken: String,
    val refreshToken: String = "",
    val userId: String = "",
    val phone: String = ""
)

/**
 * Real class item from `GET https://api.shikho.com/class_list?vendor=BD&type=syllabus`
 */
data class ShikhoClassItem(
    val code: String,
    val titleBn: String,
    val nameEn: String = "",
    val serial: Int = 0,
    val isActive: Boolean = true
)

/**
 * Real exam year option from GraphQL `BatchOptions` query on `https://api.shikho.com/graphql`
 */
data class ShikhoBatchExamYear(
    val year: String,
    val label: String
)

/**
 * Real academic course/program from GraphQL `GetAcademicProgram` (`listAcademicProgramByEnrollment`)
 */
data class ShikhoAcademicProgram(
    val id: String,
    val classes: List<String> = emptyList(),
    val titleBn: String,
    val bannerUrl: String? = null,
    val isFree: Boolean = false,
    val isEnrolledProgram: Boolean = false
)

/**
 * Represents a row in Supabase `public.device_shikho_accounts`.
 */
@JsonClass(generateAdapter = true)
data class DeviceShikhoAccount(
    @Json(name = "id") val id: String = "",
    @Json(name = "device_hash") val deviceHash: String = "",
    @Json(name = "display_device_id") val displayDeviceId: String? = null,
    @Json(name = "shikho_user_id") val shikhoUserId: String = "",
    @Json(name = "user_phone") val userPhone: String? = null,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "user_avatar") val userAvatar: String? = null,
    @Json(name = "current_class") val currentClass: String? = null,
    @Json(name = "current_group") val currentGroup: String? = null,
    @Json(name = "current_batch") val currentBatch: String? = null,
    @Json(name = "is_access_granted") val isAccessGranted: Boolean = false,
    @Json(name = "is_restricted") val isRestricted: Boolean = true,
    @Json(name = "allowed_classes") val allowedClasses: List<String>? = emptyList(),
    @Json(name = "allowed_groups") val allowedGroups: List<String>? = emptyList(),
    @Json(name = "allowed_batches") val allowedBatches: List<String>? = emptyList(),
    @Json(name = "allowed_programs") val allowedPrograms: List<String>? = emptyList(),
    @Json(name = "last_seen_at") val lastSeenAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

/**
 * Supabase RPC payload for `POST /rest/v1/rpc/admin_set_account_course_permissions`
 */
@JsonClass(generateAdapter = true)
data class SetAccountCoursePermissionsRpcPayload(
    @Json(name = "p_device_hash") val deviceHash: String,
    @Json(name = "p_shikho_user_id") val shikhoUserId: String,
    @Json(name = "p_user_phone") val userPhone: String,
    @Json(name = "p_is_access_granted") val isAccessGranted: Boolean,
    @Json(name = "p_is_restricted") val isRestricted: Boolean,
    @Json(name = "p_allowed_classes") val allowedClasses: List<String>,
    @Json(name = "p_allowed_groups") val allowedGroups: List<String>,
    @Json(name = "p_allowed_batches") val allowedBatches: List<String>,
    @Json(name = "p_allowed_programs") val allowedPrograms: List<String>,
    @Json(name = "p_revoke_other_accounts") val revokeOtherAccounts: Boolean
)
