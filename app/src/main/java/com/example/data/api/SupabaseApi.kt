package com.example.data.api

import com.example.data.model.AccessCode
import com.example.data.model.ActivatedDevice
import com.example.data.model.AppUpdate
import com.example.data.model.AppNotice
import com.example.data.model.CreateAccessCodePayload
import com.example.data.model.CreateAppUpdatePayload
import com.example.data.model.CreateAppNoticePayload
import com.example.data.model.UpdateCodeStatusPayload
import com.example.data.model.UpdateDeviceBlockedPayload
import com.example.data.model.UpdateAppUpdateStatusPayload
import com.example.data.model.UpdateAppNoticeStatusPayload
import com.example.data.model.ActivationSupportLink
import com.example.data.model.ApproveCourseAccessRequestRpcPayload
import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CoursePaymentSettings
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.CreateSupportLinkPayload
import com.example.data.model.ReviewCoursePaymentRpcPayload
import com.example.data.model.UpdateSupportLinkStatusPayload
import com.example.data.model.UpsertCoursePaymentSettingsPayload
import com.example.data.model.UserDevice
import com.example.data.model.DeviceShikhoAccount
import com.example.data.model.UpdateDeviceBanPayload
import com.example.data.model.UserGeminiApiKey
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface communicating with Supabase PostgREST endpoints.
 */
interface SupabaseApi {

    @GET("rest/v1/access_codes")
    suspend fun getAccessCodes(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<AccessCode>

    @POST("rest/v1/access_codes")
    suspend fun createAccessCode(
        @Body payload: CreateAccessCodePayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<AccessCode>

    @PATCH("rest/v1/access_codes")
    suspend fun updateAccessCodeStatus(
        @Query("id") idQuery: String, // e.g. "eq.<uuid>"
        @Body payload: UpdateCodeStatusPayload
    ): Response<Unit>

    @PATCH("rest/v1/access_codes")
    suspend fun patchAccessCode(
        @Query("id") idQuery: String, // e.g. "eq.<uuid>"
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @DELETE("rest/v1/access_codes")
    suspend fun deleteAccessCode(
        @Query("id") idQuery: String // e.g. "eq.<uuid>"
    ): Response<Unit>

    @GET("rest/v1/activated_devices")
    suspend fun getActivatedDevices(
        @Query("select") select: String = "*",
        @Query("order") order: String = "activated_at.desc"
    ): List<ActivatedDevice>

    @GET("rest/v1/activated_devices")
    suspend fun getDevicesForCode(
        @Query("code_id") codeIdQuery: String, // e.g. "eq.<uuid>"
        @Query("select") select: String = "*"
    ): List<ActivatedDevice>

    @PATCH("rest/v1/activated_devices")
    suspend fun updateDeviceBlockedStatus(
        @Query("id") idQuery: String, // e.g. "eq.<uuid>"
        @Body payload: UpdateDeviceBlockedPayload
    ): Response<Unit>

    @PATCH("rest/v1/activated_devices")
    suspend fun patchActivatedDeviceByHash(
        @Query("device_hash") deviceHashQuery: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @DELETE("rest/v1/activated_devices")
    suspend fun deleteDevice(
        @Query("id") idQuery: String // e.g. "eq.<uuid>"
    ): Response<Unit>

    @DELETE("rest/v1/activated_devices")
    suspend fun deleteActivatedDeviceByHash(
        @Query("device_hash") deviceHashQuery: String // e.g. "eq.<hash>"
    ): Response<Unit>

    @GET("rest/v1/access_codes")
    suspend fun testConnection(
        @Query("select") select: String = "id",
        @Query("limit") limit: Int = 1
    ): Response<List<Any>>

    @GET("rest/v1/app_updates")
    suspend fun getAppUpdates(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<AppUpdate>

    @POST("rest/v1/app_updates")
    suspend fun createAppUpdate(
        @Body payload: CreateAppUpdatePayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<AppUpdate>

    @PATCH("rest/v1/app_updates")
    suspend fun updateAppUpdate(
        @Query("id") idQuery: String,
        @Body payload: CreateAppUpdatePayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<AppUpdate>

    @PATCH("rest/v1/app_updates")
    suspend fun updateAppUpdateStatus(
        @Query("id") idQuery: String,
        @Body payload: UpdateAppUpdateStatusPayload
    ): Response<Unit>

    @DELETE("rest/v1/app_updates")
    suspend fun deleteAppUpdate(
        @Query("id") idQuery: String
    ): Response<Unit>

    @GET("rest/v1/app_notices")
    suspend fun getAppNotices(
        @Query("select") select: String = "*",
        @Query("order") order: String = "priority.desc,created_at.desc"
    ): List<AppNotice>

    @POST("rest/v1/app_notices")
    suspend fun createAppNotice(
        @Body payload: CreateAppNoticePayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<AppNotice>

    @PATCH("rest/v1/app_notices")
    suspend fun updateAppNotice(
        @Query("id") idQuery: String,
        @Body payload: CreateAppNoticePayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<AppNotice>

    @PATCH("rest/v1/app_notices")
    suspend fun updateAppNoticeStatus(
        @Query("id") idQuery: String,
        @Body payload: UpdateAppNoticeStatusPayload
    ): Response<Unit>

    @DELETE("rest/v1/app_notices")
    suspend fun deleteAppNotice(
        @Query("id") idQuery: String
    ): Response<Unit>

    @GET("rest/v1/activation_support_links")
    suspend fun getActivationSupportLinks(
        @Query("select") select: String = "*",
        @Query("order") order: String = "priority.desc,created_at.desc"
    ): List<ActivationSupportLink>

    @POST("rest/v1/activation_support_links")
    suspend fun createActivationSupportLink(
        @Body payload: CreateSupportLinkPayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<ActivationSupportLink>

    @PATCH("rest/v1/activation_support_links")
    suspend fun updateActivationSupportLink(
        @Query("id") idQuery: String,
        @Body payload: CreateSupportLinkPayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<ActivationSupportLink>

    @PATCH("rest/v1/activation_support_links")
    suspend fun updateActivationSupportLinkStatus(
        @Query("id") idQuery: String,
        @Body payload: UpdateSupportLinkStatusPayload
    ): Response<Unit>

    @DELETE("rest/v1/activation_support_links")
    suspend fun deleteActivationSupportLink(
        @Query("id") idQuery: String
    ): Response<Unit>

    @GET("rest/v1/devices")
    suspend fun getDevices(
        @Query("select") select: String = "*",
        @Query("order") order: String = "requested_at.desc.nullslast,updated_at.desc.nullslast"
    ): List<UserDevice>

    @GET("rest/v1/devices")
    suspend fun getDevicesUnordered(
        @Query("select") select: String = "*"
    ): List<UserDevice>

    @PATCH("rest/v1/devices")
    suspend fun updateDeviceBan(
        @Query("id") idQuery: String,
        @Body payload: UpdateDeviceBanPayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<UserDevice>

    @PATCH("rest/v1/devices")
    suspend fun patchDevice(
        @Query("id") idQuery: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<UserDevice>>

    @PATCH("rest/v1/devices")
    suspend fun patchDeviceByHash(
        @Query("device_hash") deviceHashQuery: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<UserDevice>>

    @PATCH("rest/v1/devices")
    suspend fun patchDeviceByDeviceIdColumn(
        @Query("device_id") deviceIdQuery: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<UserDevice>>

    @PATCH("rest/v1/devices")
    suspend fun patchDeviceByDisplayId(
        @Query("display_device_id") displayIdQuery: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<UserDevice>>

    @DELETE("rest/v1/devices")
    suspend fun deleteDeviceRecord(
        @Query("id") idQuery: String
    ): Response<Unit>

    @DELETE("rest/v1/devices")
    suspend fun deleteDeviceRecordByHash(
        @Query("device_hash") deviceHashQuery: String
    ): Response<Unit>

    @DELETE("rest/v1/devices")
    suspend fun deleteDeviceRecordByDeviceIdColumn(
        @Query("device_id") deviceIdQuery: String
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_approve_device_verification")
    suspend fun approveDeviceVerificationRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_set_device_block_status")
    suspend fun setDeviceBlockStatusRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_toggle_device_ban")
    suspend fun toggleDeviceBanRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_set_device_course_permissions")
    suspend fun setDeviceCoursePermissionsRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_set_account_course_permissions")
    suspend fun setAccountCoursePermissionsRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @GET("rest/v1/device_shikho_accounts")
    suspend fun getDeviceShikhoAccounts(
        @Query("select") select: String = "*",
        @Query("order") order: String = "last_seen_at.desc.nullslast,created_at.desc.nullslast"
    ): List<DeviceShikhoAccount>

    @GET("rest/v1/device_shikho_accounts")
    suspend fun getDeviceShikhoAccountsByHash(
        @Query("device_hash") deviceHashQuery: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "last_seen_at.desc.nullslast"
    ): List<DeviceShikhoAccount>

    @PATCH("rest/v1/device_shikho_accounts")
    suspend fun patchDeviceShikhoAccount(
        @Query("device_hash") deviceHashQuery: String,
        @Query("user_phone") userPhoneQuery: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<DeviceShikhoAccount>>


    // =========================================================================
    // Course Sales, Installment Plans & Payment Verification Endpoints
    // =========================================================================

    @GET("rest/v1/course_payment_settings")
    suspend fun getCoursePaymentSettings(
        @Query("id") idQuery: String = "eq.1",
        @Query("select") select: String = "*"
    ): List<CoursePaymentSettings>

    @POST("rest/v1/course_payment_settings")
    suspend fun upsertCoursePaymentSettings(
        @Body payload: UpsertCoursePaymentSettingsPayload,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation"
    ): List<CoursePaymentSettings>

    @POST("rest/v1/course_payment_settings")
    suspend fun upsertCoursePaymentSettingsRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation"
    ): List<CoursePaymentSettings>

    @PATCH("rest/v1/course_payment_settings")
    suspend fun patchCoursePaymentSettings(
        @Query("id") idQuery: String = "eq.1",
        @Body payload: UpsertCoursePaymentSettingsPayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<CoursePaymentSettings>

    @PATCH("rest/v1/course_payment_settings")
    suspend fun patchCoursePaymentSettingsRaw(
        @Query("id") idQuery: String = "eq.1",
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<CoursePaymentSettings>

    @GET("rest/v1/course_access_enrollments")
    suspend fun getCourseAccessEnrollments(
        @Query("select") select: String = "*",
        @Query("order") order: String = "updated_at.desc.nullslast,requested_at.desc.nullslast"
    ): List<CourseAccessEnrollment>

    @POST("rest/v1/course_access_enrollments")
    suspend fun createCourseAccessEnrollmentRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation"
    ): Response<List<CourseAccessEnrollment>>

    @PATCH("rest/v1/course_access_enrollments")
    suspend fun patchCourseAccessEnrollment(
        @Query("id") idQuery: String, // e.g. "eq.12"
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @PATCH("rest/v1/course_access_enrollments")
    suspend fun patchCourseAccessEnrollmentByAccountAndProgram(
        @Query("shikho_account_key") accountKeyQuery: String, // e.g. "eq.017XXXXXXXX"
        @Query("program_id") programIdQuery: String, // e.g. "eq.prog_123"
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @PATCH("rest/v1/course_access_enrollments")
    suspend fun patchCourseAccessEnrollmentByPhoneAndProgram(
        @Query("student_phone") studentPhoneQuery: String, // e.g. "eq.017XXXXXXXX"
        @Query("program_id") programIdQuery: String, // e.g. "eq.prog_123"
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @PATCH("rest/v1/course_access_enrollments")
    suspend fun patchCourseAccessEnrollmentByUserPhoneAndProgram(
        @Query("user_phone") userPhoneQuery: String, // e.g. "eq.017XXXXXXXX"
        @Query("program_id") programIdQuery: String, // e.g. "eq.prog_123"
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @GET("rest/v1/user_gemini_api_keys")
    suspend fun getUserGeminiApiKeys(
        @Query("select") select: String = "*",
        @Query("order") order: String = "updated_at.desc.nullslast"
    ): List<UserGeminiApiKey>

    @GET("rest/v1/course_payment_transactions")
    suspend fun getCoursePaymentTransactions(
        @Query("select") select: String = "*",
        @Query("order") order: String = "submitted_at.desc.nullslast"
    ): List<CoursePaymentTransaction>

    @PATCH("rest/v1/course_payment_transactions")
    suspend fun patchCoursePaymentTransaction(
        @Query("id") idQuery: String, // e.g. "eq.15"
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_approve_course_access_request")
    suspend fun approveCourseAccessRequestRpc(
        @Body payload: ApproveCourseAccessRequestRpcPayload
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_approve_course_access_request")
    suspend fun approveCourseAccessRequestRpcRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_approve_course_access_with_trial")
    suspend fun approveCourseAccessWithTrialRpcRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_manage_course_trial")
    suspend fun manageCourseTrialRpcRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_set_course_access_offer")
    suspend fun setCourseAccessOfferRpcRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_review_course_payment")
    suspend fun reviewCoursePaymentRpc(
        @Body payload: ReviewCoursePaymentRpcPayload
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_review_course_payment")
    suspend fun reviewCoursePaymentRpcRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_verify_course_payment")
    suspend fun verifyCoursePaymentRpcRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/enforce_overdue_course_installments")
    suspend fun enforceOverdueCourseInstallmentsRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?> = emptyMap()
    ): Response<okhttp3.ResponseBody>

    // =========================================================================
    // OneSignal + FCM Admin Push & Student Notification Endpoints (SECURITY DEFINER RPCs)
    // =========================================================================

    @POST("rest/v1/rpc/get_onesignal_public_app_id")
    suspend fun getOneSignalPublicAppIdRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?> = emptyMap()
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/update_onesignal_config_secure")
    suspend fun updateOneSignalConfigSecureRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @GET("rest/v1/onesignal_config")
    suspend fun getOneSignalConfigRaw(
        @Query("id") idQuery: String = "eq.1",
        @Query("select") select: String = "id,onesignal_app_id,updated_at"
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/onesignal_config")
    suspend fun upsertOneSignalConfigRaw(
        @Query("on_conflict") onConflict: String = "id",
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=minimal"
    ): Response<Unit>

    @PATCH("rest/v1/onesignal_config")
    suspend fun patchOneSignalConfigRaw(
        @Query("id") idQuery: String = "eq.1",
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @POST("rest/v1/admin_push_devices")
    suspend fun upsertAdminPushDevice(
        @Query("on_conflict") onConflict: String = "admin_device_id",
        @Body payload: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation"
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/send_student_push_via_onesignal")
    suspend fun sendStudentPushViaOneSignalRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/notify_student_via_onesignal")
    suspend fun notifyStudentViaOneSignalRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/notify_admin_via_onesignal")
    suspend fun notifyAdminViaOneSignalRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    // =========================================================================
    // Secure Admin Delete & Permission Management RPCs (SECURITY DEFINER)
    // Used when `REVOKE DELETE ON ALL TABLES FROM anon` is active on Supabase
    // =========================================================================

    @POST("rest/v1/rpc/admin_delete_device")
    suspend fun adminDeleteDeviceRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_delete_activated_device")
    suspend fun adminDeleteActivatedDeviceRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_reset_devices_for_code")
    suspend fun adminResetDevicesForCodeRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_delete_access_code")
    suspend fun adminDeleteAccessCodeRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_delete_app_notice")
    suspend fun adminDeleteAppNoticeRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_delete_app_update")
    suspend fun adminDeleteAppUpdateRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_delete_support_link")
    suspend fun adminDeleteSupportLinkRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_delete_course_enrollment")
    suspend fun adminDeleteCourseEnrollmentRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @DELETE("rest/v1/course_access_enrollments")
    suspend fun deleteCourseAccessEnrollment(
        @Query("id") idQuery: String
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_set_device_can_view_profiles")
    suspend fun adminSetDeviceCanViewProfilesRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>

    @POST("rest/v1/rpc/admin_revoke_device_to_pending")
    suspend fun adminRevokeDeviceToPendingRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<okhttp3.ResponseBody>
}



