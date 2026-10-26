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
import com.example.data.model.CourseCatalogItem
import com.example.data.model.CoursePaymentSettings
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.CreateSupportLinkPayload
import com.example.data.model.DeviceShikhoAccount
import com.example.data.model.ReviewCoursePaymentRpcPayload
import com.example.data.model.SetAccountCoursePermissionsRpcPayload
import com.example.data.model.SetDeviceCoursePermissionsRpcPayload
import com.example.data.model.UpdateCoursePermissionsPayload
import com.example.data.model.UpdateSupportLinkStatusPayload
import com.example.data.model.UpsertCoursePaymentSettingsPayload
import com.example.data.model.UserDevice
import com.example.data.model.UpdateDeviceBanPayload
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
    suspend fun updateAccessCodeCoursePermissions(
        @Query("id") idQuery: String, // e.g. "eq.<uuid>"
        @Body payload: UpdateCoursePermissionsPayload
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

    @DELETE("rest/v1/activated_devices")
    suspend fun deleteDevice(
        @Query("id") idQuery: String // e.g. "eq.<uuid>"
    ): Response<Unit>

    @GET("rest/v1/access_codes")
    suspend fun testConnection(
        @Query("select") select: String = "id",
        @Query("limit") limit: Int = 1
    ): Response<List<Any>>

    @GET("rest/v1/app_updates")
    suspend fun getAppUpdates(
        @Query("select") select: String = "*",
        @Query("order") order: String = "latest_version_code.desc"
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
        @Query("order") order: String = "last_seen.desc.nullslast"
    ): List<UserDevice>

    @PATCH("rest/v1/devices")
    suspend fun updateDeviceBan(
        @Query("id") idQuery: String,
        @Body payload: UpdateDeviceBanPayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<UserDevice>

    @DELETE("rest/v1/devices")
    suspend fun deleteDeviceRecord(
        @Query("id") idQuery: String
    ): Response<Unit>

    @GET("rest/v1/available_courses_catalog")
    suspend fun getAvailableCoursesCatalog(
        @Query("select") select: String = "*"
    ): List<CourseCatalogItem>

    @POST("rest/v1/rpc/admin_set_device_course_permissions")
    suspend fun setDeviceCoursePermissionsRpc(
        @Body payload: SetDeviceCoursePermissionsRpcPayload
    ): Response<Unit>

    @PATCH("rest/v1/devices")
    suspend fun updateDeviceCoursePermissionsByHash(
        @Query("device_hash") deviceHashQuery: String,
        @Body payload: UpdateCoursePermissionsPayload
    ): Response<Unit>

    @PATCH("rest/v1/devices")
    suspend fun updateDeviceCoursePermissionsById(
        @Query("id") idQuery: String,
        @Body payload: UpdateCoursePermissionsPayload
    ): Response<Unit>

    @GET("rest/v1/device_shikho_accounts")
    suspend fun getDeviceShikhoAccountsByDeviceId(
        @Query("device_id") deviceIdQuery: String, // e.g. "in.(hash,displayId)"
        @Query("order") order: String = "last_login_at.desc.nullslast",
        @Query("select") select: String = "*"
    ): List<DeviceShikhoAccount>

    @GET("rest/v1/device_shikho_accounts")
    suspend fun getDeviceShikhoAccounts(
        @Query("or") orFilter: String,
        @Query("order") order: String = "is_access_granted.desc,last_seen_at.desc",
        @Query("select") select: String = "*"
    ): List<DeviceShikhoAccount>

    @GET("rest/v1/device_shikho_accounts")
    suspend fun getAllDeviceShikhoAccounts(
        @Query("select") select: String = "*"
    ): List<DeviceShikhoAccount>

    @PATCH("rest/v1/device_shikho_accounts")
    suspend fun patchDeviceShikhoAccount(
        @Query("device_id") deviceIdQuery: String,
        @Query("shikho_user_id") shikhoUserIdQuery: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_set_account_course_permissions")
    suspend fun setAccountCoursePermissionsRpc(
        @Body payload: SetAccountCoursePermissionsRpcPayload
    ): Response<Unit>

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

    @PATCH("rest/v1/course_payment_settings")
    suspend fun patchCoursePaymentSettings(
        @Query("id") idQuery: String = "eq.1",
        @Body payload: UpsertCoursePaymentSettingsPayload,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<CoursePaymentSettings>

    @GET("rest/v1/course_access_enrollments")
    suspend fun getCourseAccessEnrollments(
        @Query("select") select: String = "*",
        @Query("order") order: String = "updated_at.desc.nullslast,requested_at.desc.nullslast"
    ): List<CourseAccessEnrollment>

    @PATCH("rest/v1/course_access_enrollments")
    suspend fun patchCourseAccessEnrollment(
        @Query("id") idQuery: String, // e.g. "eq.12"
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

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
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_review_course_payment")
    suspend fun reviewCoursePaymentRpc(
        @Body payload: ReviewCoursePaymentRpcPayload
    ): Response<Unit>

    @POST("rest/v1/rpc/admin_review_course_payment")
    suspend fun reviewCoursePaymentRpcRaw(
        @Body payload: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @POST("rest/v1/rpc/enforce_overdue_course_installments")
    suspend fun enforceOverdueCourseInstallmentsRpc(
        @Body payload: Map<String, @JvmSuppressWildcards Any?> = emptyMap()
    ): Response<Unit>
}



