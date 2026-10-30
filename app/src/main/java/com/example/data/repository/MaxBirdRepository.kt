package com.example.data.repository

import com.example.data.model.AccessCode
import com.example.data.model.ActivatedDevice
import com.example.data.model.AppUpdate
import com.example.data.model.AppNotice
import com.example.data.model.ActivationSupportLink
import com.example.data.model.ApproveCourseAccessRequestRpcPayload
import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CoursePaymentSettings
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.CreateAccessCodePayload
import com.example.data.model.CreateAppUpdatePayload
import com.example.data.model.CreateAppNoticePayload
import com.example.data.model.CreateSupportLinkPayload
import com.example.data.model.DashboardStats
import com.example.data.model.DeviceWithStudent
import com.example.data.model.ManageCourseTrialPayload
import com.example.data.model.ManualCourseAccessPayload
import com.example.data.model.ReviewCoursePaymentRpcPayload
import com.example.data.model.UpsertCoursePaymentSettingsPayload
import com.example.data.model.UserDevice

/**
 * Universal repository contract for MaxBird Access Control & Device Management.
 */
interface MaxBirdRepository {

    suspend fun getAccessCodes(forceRefresh: Boolean = false): Result<List<AccessCode>>

    suspend fun createAccessCode(payload: CreateAccessCodePayload): Result<AccessCode>

    suspend fun toggleCodeStatus(codeId: String, isActive: Boolean): Result<Unit>

    suspend fun extendAccessCodeValidity(codeId: String, extraDays: Int): Result<Unit>

    suspend fun resetDevicesForCode(codeId: String, codeString: String): Result<Unit>

    suspend fun deleteAccessCode(codeId: String): Result<Unit>

    suspend fun getActivatedDevices(forceRefresh: Boolean = false): Result<List<ActivatedDevice>>

    suspend fun getDevicesForCode(codeId: String): Result<List<ActivatedDevice>>

    suspend fun getJoinedDevices(): Result<List<DeviceWithStudent>>

    suspend fun toggleDeviceBlocked(deviceId: String, isBlocked: Boolean): Result<Unit>

    suspend fun unbindDevice(deviceId: String): Result<Unit>

    suspend fun getDashboardStats(): Result<DashboardStats>

    suspend fun testConnection(): Result<Boolean>

    suspend fun getAppUpdates(forceRefresh: Boolean = false): Result<List<AppUpdate>>

    suspend fun createAppUpdate(payload: CreateAppUpdatePayload): Result<AppUpdate>

    suspend fun updateAppUpdate(id: String, payload: CreateAppUpdatePayload): Result<AppUpdate>

    suspend fun toggleAppUpdateStatus(id: String, isActive: Boolean): Result<Unit>

    suspend fun deleteAppUpdate(id: String): Result<Unit>

    suspend fun getAppNotices(forceRefresh: Boolean = false): Result<List<AppNotice>>

    suspend fun createAppNotice(payload: CreateAppNoticePayload): Result<AppNotice>

    suspend fun updateAppNotice(id: String, payload: CreateAppNoticePayload): Result<AppNotice>

    suspend fun toggleAppNoticeStatus(id: String, isActive: Boolean): Result<Unit>

    suspend fun deleteAppNotice(id: String): Result<Unit>

    suspend fun getActivationSupportLinks(forceRefresh: Boolean = false): Result<List<ActivationSupportLink>>

    suspend fun createActivationSupportLink(payload: CreateSupportLinkPayload): Result<ActivationSupportLink>

    suspend fun updateActivationSupportLink(id: String, payload: CreateSupportLinkPayload): Result<ActivationSupportLink>

    suspend fun toggleActivationSupportLinkStatus(id: String, isActive: Boolean): Result<Unit>

    suspend fun deleteActivationSupportLink(id: String): Result<Unit>

    suspend fun getUserDevices(forceRefresh: Boolean = false): Result<List<UserDevice>>

    suspend fun approveUserDeviceVerification(device: UserDevice, studentNote: String? = null): Result<Unit>

    suspend fun setUserDeviceBlockStatus(device: UserDevice, isBlocked: Boolean, banReason: String? = null): Result<Unit>

    suspend fun revokeUserDeviceToPending(device: UserDevice): Result<Unit>

    suspend fun updateUserDeviceStudentNote(device: UserDevice, studentNote: String): Result<Unit>

    suspend fun toggleUserDeviceBan(deviceId: String, isBanned: Boolean, banReason: String?): Result<Unit>

    suspend fun toggleDeviceCanViewProfiles(deviceId: String, canViewProfiles: Boolean): Result<Unit>

    suspend fun deleteUserDevice(deviceId: String): Result<Unit>


    // Course Sales, Installment Plans & Payment Verification
    suspend fun getCoursePaymentSettings(): Result<CoursePaymentSettings>

    suspend fun saveCoursePaymentSettings(
        payload: UpsertCoursePaymentSettingsPayload
    ): Result<CoursePaymentSettings>

    suspend fun getCourseAccessEnrollments(): Result<List<CourseAccessEnrollment>>

    suspend fun approveCourseAccessRequest(
        payload: ApproveCourseAccessRequestRpcPayload
    ): Result<Unit>

    suspend fun grantManualCourseAccess(
        payload: ManualCourseAccessPayload
    ): Result<Unit>

    suspend fun manageCourseTrial(
        payload: ManageCourseTrialPayload
    ): Result<Unit>

    suspend fun getCoursePaymentTransactions(): Result<List<CoursePaymentTransaction>>

    suspend fun reviewCoursePayment(
        payload: ReviewCoursePaymentRpcPayload
    ): Result<Unit>

    suspend fun enforceOverdueCourseInstallments(): Result<Unit>

    suspend fun updateEnrollmentStatusOrDueDate(
        enrollmentId: Long,
        studentPhone: String? = null,
        shikhoAccountKey: String? = null,
        programId: String? = null,
        newStatus: String? = null,
        newNextDueDateIso: String? = null,
        adminNote: String? = null
    ): Result<Unit>
}


