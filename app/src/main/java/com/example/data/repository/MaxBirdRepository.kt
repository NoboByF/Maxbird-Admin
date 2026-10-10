package com.example.data.repository

import com.example.data.model.AccessCode
import com.example.data.model.ActivatedDevice
import com.example.data.model.AppUpdate
import com.example.data.model.AppNotice
import com.example.data.model.ActivationSupportLink
import com.example.data.model.CreateAccessCodePayload
import com.example.data.model.CreateAppUpdatePayload
import com.example.data.model.CreateAppNoticePayload
import com.example.data.model.CreateSupportLinkPayload
import com.example.data.model.DashboardStats
import com.example.data.model.DeviceWithStudent
import com.example.data.model.UserDevice
import kotlinx.coroutines.flow.Flow

/**
 * Universal repository contract for MaxBird Access Control & Device Management.
 * Designed to be 100% portable across Android, Desktop (JVM), Web, and iOS.
 */
interface MaxBirdRepository {

    suspend fun getAccessCodes(forceRefresh: Boolean = false): Result<List<AccessCode>>

    suspend fun createAccessCode(payload: CreateAccessCodePayload): Result<AccessCode>

    suspend fun toggleCodeStatus(codeId: String, isActive: Boolean): Result<Unit>

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

    suspend fun toggleUserDeviceBan(deviceId: String, isBanned: Boolean, banReason: String?): Result<Unit>

    suspend fun deleteUserDevice(deviceId: String): Result<Unit>
}

