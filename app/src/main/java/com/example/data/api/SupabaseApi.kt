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
}
