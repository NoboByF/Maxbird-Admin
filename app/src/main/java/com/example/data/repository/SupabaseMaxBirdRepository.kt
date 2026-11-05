package com.example.data.repository

import com.example.data.model.AppUpdate
import com.example.data.model.AppNotice
import com.example.data.model.ActivationSupportLink
import com.example.data.model.ApproveCourseAccessRequestRpcPayload
import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CoursePaymentSettings
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.CreateAppUpdatePayload
import com.example.data.model.CreateAppNoticePayload
import com.example.data.model.CreateStudentNotificationPayload
import com.example.data.model.CreateSupportLinkPayload
import com.example.data.model.DashboardStats
import com.example.data.model.DeviceShikhoAccount
import com.example.data.model.DynamicPaymentMethodItem
import com.example.data.model.ManageCourseTrialPayload
import com.example.data.model.ManualCourseAccessPayload
import com.example.data.model.ReviewCoursePaymentRpcPayload
import com.example.data.model.StudentNotification
import com.example.data.model.UpdateAppUpdateStatusPayload
import com.example.data.model.UpdateAppNoticeStatusPayload
import com.example.data.model.UpdateSupportLinkStatusPayload
import com.example.data.model.UpsertCoursePaymentSettingsPayload
import com.example.data.model.UserDevice
import com.example.data.api.SupabaseClient
import com.example.data.preferences.AdminPreferences
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class SupabaseMaxBirdRepository(
    private val supabaseClient: SupabaseClient,
    private val adminPreferences: AdminPreferences
) : MaxBirdRepository {

    // Live runtime caches (100% real Supabase rows — ZERO dummy/sample data)
    private val cachedUpdates = mutableListOf<AppUpdate>()
    private val cachedNotices = mutableListOf<AppNotice>()
    private val cachedSupportLinks = mutableListOf<ActivationSupportLink>()
    private val cachedUserDevices = mutableListOf<UserDevice>()
    private val cachedStudentNotifications = mutableListOf<StudentNotification>()

    private fun initSampleDataIfNeeded() {
        // Strict Zero-Dummy-Data Policy: No fake or sample data is ever added.
    }

    override suspend fun getDashboardStats(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        val realDevices = getUserDevices().getOrDefault(cachedUserDevices)

        val pendingCount = realDevices.count { it.isPendingDevice }
        val activeVerifiedCount = realDevices.count { it.isActiveDevice }
        val blockedRealCount = realDevices.count { it.isBlockedDevice }
        val totalDevices = realDevices.size

        Result.success(
            DashboardStats(
                totalCodes = activeVerifiedCount,
                activeStudents = activeVerifiedCount,
                totalDevices = totalDevices,
                blockedDevices = blockedRealCount,
                availableSlots = pendingCount,
                pendingVerificationDevices = pendingCount,
                activeVerifiedDevices = activeVerifiedCount
            )
        )
    }

    override suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val response = api.testConnection()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Supabase returned HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAppUpdates(forceRefresh: Boolean): Result<List<AppUpdate>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteUpdates = try {
                api.getAppUpdatesLegacyOrder()
            } catch (_: Exception) {
                try {
                    api.getAppUpdates()
                } catch (_: Exception) {
                    api.getAppUpdatesUnordered()
                }
            }
            val sorted = remoteUpdates.sortedWith(
                compareByDescending<AppUpdate> { it.effectiveVersionCode }
                    .thenByDescending { it.updatedAt.ifBlank { it.createdAt } }
            )
            cachedUpdates.clear()
            cachedUpdates.addAll(sorted)
            Result.success(sorted)
        } catch (e: Exception) {
            if (cachedUpdates.isNotEmpty()) {
                Result.success(cachedUpdates.toList())
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun createAppUpdate(payload: CreateAppUpdatePayload): Result<AppUpdate> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val vCode = if (payload.versionCode > 0) payload.versionCode else payload.latestVersionCode
        val vName = payload.versionName.ifBlank { payload.latestVersionName }.trim()
        val apkLink = payload.apkUrl.ifBlank { payload.downloadUrl }.trim()
        val notes = payload.releaseNotes.ifBlank { payload.changelog }.trim()
        val minCode = if (payload.minSupportedVersionCode > 0) payload.minSupportedVersionCode else vCode
        val cleanTitle = payload.title.trim().ifBlank { "নতুন আপডেট উপলব্ধ! ($vName)" }
        val cleanBtn = payload.buttonText.trim().ifBlank { "এখনই আপডেট করুন" }

        // Matches the exact columns in public.app_updates of the live database
        val liveSupabaseSchemaMap = mapOf<String, Any?>(
            "latest_version_name" to vName,
            "latest_version_code" to vCode,
            "min_supported_version_code" to minCode,
            "is_force_update" to payload.isForceUpdate,
            "title" to cleanTitle,
            "changelog" to notes,
            "download_url" to apkLink,
            "button_text" to cleanBtn,
            "is_active" to payload.isActive,
            "file_size_mb" to payload.fileSizeMb,
            "is_kill_switch" to payload.isKillSwitch,
            "is_maintenance" to payload.isMaintenance,
            "kill_switch_title" to payload.killSwitchTitle,
            "kill_switch_message" to payload.killSwitchMessage,
            "estimated_back_time" to payload.estimatedBackTime.ifBlank { null },
            "updated_at" to now
        )

        val fullSchemaMap = mapOf<String, Any?>(
            "version_code" to vCode,
            "version_name" to vName,
            "apk_url" to apkLink,
            "file_size_mb" to payload.fileSizeMb,
            "release_notes" to notes,
            "is_force_update" to payload.isForceUpdate,
            "is_kill_switch" to payload.isKillSwitch,
            "is_maintenance" to payload.isMaintenance,
            "kill_switch_title" to payload.killSwitchTitle,
            "kill_switch_message" to payload.killSwitchMessage,
            "estimated_back_time" to payload.estimatedBackTime.ifBlank { null },
            "latest_version_code" to vCode,
            "latest_version_name" to vName,
            "min_supported_version_code" to minCode,
            "title" to cleanTitle,
            "changelog" to notes,
            "download_url" to apkLink,
            "button_text" to cleanBtn,
            "is_active" to payload.isActive,
            "updated_at" to now
        )

        val legacySchemaMap = mapOf<String, Any?>(
            "latest_version_name" to vName,
            "latest_version_code" to vCode,
            "min_supported_version_code" to minCode,
            "is_force_update" to payload.isForceUpdate,
            "title" to cleanTitle,
            "changelog" to notes,
            "download_url" to apkLink,
            "button_text" to cleanBtn,
            "is_active" to payload.isActive
        )

        try {
            val api = supabaseClient.getApi()
            var lastError = ""
            for (candidate in listOf(liveSupabaseSchemaMap, fullSchemaMap, legacySchemaMap)) {
                try {
                    val res = api.createAppUpdateRaw(candidate)
                    if (res.isSuccessful) {
                        val created = res.body()?.firstOrNull() ?: AppUpdate(
                            id = UUID.randomUUID().toString(),
                            versionCode = vCode,
                            versionName = vName,
                            apkUrl = apkLink,
                            fileSizeMb = payload.fileSizeMb,
                            releaseNotes = notes,
                            isForceUpdate = payload.isForceUpdate,
                            isKillSwitch = payload.isKillSwitch,
                            isMaintenance = payload.isMaintenance,
                            killSwitchTitle = payload.killSwitchTitle,
                            killSwitchMessage = payload.killSwitchMessage,
                            estimatedBackTime = payload.estimatedBackTime,
                            latestVersionName = vName,
                            latestVersionCode = vCode,
                            minSupportedVersionCode = minCode,
                            title = cleanTitle,
                            changelog = notes,
                            downloadUrl = apkLink,
                            buttonText = cleanBtn,
                            isActive = payload.isActive,
                            createdAt = now,
                            updatedAt = now
                        )
                        cachedUpdates.add(0, created)
                        return@withContext Result.success(created)
                    } else {
                        val err = res.errorBody()?.string().orEmpty()
                        if (err.isNotBlank()) lastError = err
                    }
                } catch (e: Exception) {
                    if (lastError.isBlank()) lastError = e.message.orEmpty()
                }
            }
            Result.failure(Exception("আপডেট তৈরি ব্যর্থ: $lastError"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAppUpdate(id: String, payload: CreateAppUpdatePayload): Result<AppUpdate> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val vCode = if (payload.versionCode > 0) payload.versionCode else payload.latestVersionCode
        val vName = payload.versionName.ifBlank { payload.latestVersionName }.trim()
        val apkLink = payload.apkUrl.ifBlank { payload.downloadUrl }.trim()
        val notes = payload.releaseNotes.ifBlank { payload.changelog }.trim()
        val minCode = if (payload.minSupportedVersionCode > 0) payload.minSupportedVersionCode else vCode
        val cleanTitle = payload.title.trim().ifBlank { "নতুন আপডেট উপলব্ধ! ($vName)" }
        val cleanBtn = payload.buttonText.trim().ifBlank { "এখনই আপডেট করুন" }

        // Matches the exact columns in public.app_updates of the live database
        val liveSupabaseSchemaMap = mapOf<String, Any?>(
            "latest_version_name" to vName,
            "latest_version_code" to vCode,
            "min_supported_version_code" to minCode,
            "is_force_update" to payload.isForceUpdate,
            "title" to cleanTitle,
            "changelog" to notes,
            "download_url" to apkLink,
            "button_text" to cleanBtn,
            "is_active" to payload.isActive,
            "file_size_mb" to payload.fileSizeMb,
            "is_kill_switch" to payload.isKillSwitch,
            "is_maintenance" to payload.isMaintenance,
            "kill_switch_title" to payload.killSwitchTitle,
            "kill_switch_message" to payload.killSwitchMessage,
            "estimated_back_time" to payload.estimatedBackTime.ifBlank { null },
            "updated_at" to now
        )

        val fullSchemaMap = mapOf<String, Any?>(
            "version_code" to vCode,
            "version_name" to vName,
            "apk_url" to apkLink,
            "file_size_mb" to payload.fileSizeMb,
            "release_notes" to notes,
            "is_force_update" to payload.isForceUpdate,
            "is_kill_switch" to payload.isKillSwitch,
            "is_maintenance" to payload.isMaintenance,
            "kill_switch_title" to payload.killSwitchTitle,
            "kill_switch_message" to payload.killSwitchMessage,
            "estimated_back_time" to payload.estimatedBackTime.ifBlank { null },
            "latest_version_code" to vCode,
            "latest_version_name" to vName,
            "min_supported_version_code" to minCode,
            "title" to cleanTitle,
            "changelog" to notes,
            "download_url" to apkLink,
            "button_text" to cleanBtn,
            "is_active" to payload.isActive,
            "updated_at" to now
        )

        val legacySchemaMap = mapOf<String, Any?>(
            "latest_version_name" to vName,
            "latest_version_code" to vCode,
            "min_supported_version_code" to minCode,
            "is_force_update" to payload.isForceUpdate,
            "title" to cleanTitle,
            "changelog" to notes,
            "download_url" to apkLink,
            "button_text" to cleanBtn,
            "is_active" to payload.isActive,
            "updated_at" to now
        )

        try {
            val api = supabaseClient.getApi()
            var lastError = ""
            for (candidate in listOf(liveSupabaseSchemaMap, fullSchemaMap, legacySchemaMap)) {
                try {
                    val res = api.patchAppUpdateRaw("eq.$id", candidate)
                    if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                        val updated = res.body()!!.first()
                        val idx = cachedUpdates.indexOfFirst { it.id == id }
                        if (idx != -1) cachedUpdates[idx] = updated
                        return@withContext Result.success(updated)
                    } else if (!res.isSuccessful) {
                        val err = res.errorBody()?.string().orEmpty()
                        if (err.isNotBlank()) lastError = err
                    }
                } catch (e: Exception) {
                    if (lastError.isBlank()) lastError = e.message.orEmpty()
                }
            }
            Result.failure(Exception("আপডেট সংরক্ষণ ব্যর্থ: $lastError"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGlobalKillSwitch(
        isEnabled: Boolean,
        title: String,
        message: String,
        estimatedBackTime: String
    ): Result<AppUpdate> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val cleanTitle = title.trim().ifBlank { "অ্যাপ আপডেটের কাজ চলছে" }
        val cleanMessage = message.trim().ifBlank {
            "অ্যাপটিকে আরও উন্নত, দ্রুত ও নিরাপদ করতে বর্তমানে সার্ভার ও সিস্টেম আপডেটের কাজ চলছে। কাজ শেষ হওয়া মাত্রই অ্যাপটি পুনরায় চালু হবে।"
        }
        val cleanBackTime = estimatedBackTime.trim()

        try {
            val api = supabaseClient.getApi()
            var rpcSucceeded = false

            // 1. Call RPC `admin_set_kill_switch(p_enabled, p_title, p_message, p_estimated_time)`
            try {
                val rpcPayload = mapOf<String, Any?>(
                    "p_enabled" to isEnabled,
                    "p_title" to cleanTitle,
                    "p_message" to cleanMessage,
                    "p_estimated_time" to cleanBackTime.ifBlank { null }
                )
                val rpcRes = api.setKillSwitchRpc(rpcPayload)
                if (rpcRes.isSuccessful) {
                    rpcSucceeded = true
                }
            } catch (_: Exception) {
            }

            // 2. Also ensure the latest row in `public.app_updates` is directly updated
            val currentList = getAppUpdates(forceRefresh = true).getOrNull().orEmpty()
            val latestRow = currentList.firstOrNull()

            val killSwitchPatch = mapOf<String, Any?>(
                "is_kill_switch" to isEnabled,
                "is_maintenance" to isEnabled,
                "kill_switch_title" to cleanTitle,
                "kill_switch_message" to cleanMessage,
                "estimated_back_time" to cleanBackTime.ifBlank { null }
            )

            val killSwitchPatchWithUpdated = killSwitchPatch + ("updated_at" to now)

            if (latestRow != null && latestRow.id.isNotBlank()) {
                var lastErr = ""
                for (candidate in listOf(killSwitchPatchWithUpdated, killSwitchPatch)) {
                    try {
                        val patchRes = api.patchAppUpdateRaw("eq.${latestRow.id}", candidate)
                        if (patchRes.isSuccessful && !patchRes.body().isNullOrEmpty()) {
                            val updatedRow = patchRes.body()!!.first()
                            val idx = cachedUpdates.indexOfFirst { it.id == latestRow.id }
                            if (idx != -1) cachedUpdates[idx] = updatedRow
                            return@withContext Result.success(updatedRow)
                        } else if (!patchRes.isSuccessful) {
                            val err = patchRes.errorBody()?.string().orEmpty()
                            if (err.isNotBlank()) lastErr = err
                        }
                    } catch (e: Exception) {
                        if (lastErr.isBlank()) lastErr = e.message.orEmpty()
                    }
                }
                if (rpcSucceeded) {
                    val refreshed = getAppUpdates(forceRefresh = true).getOrNull()?.firstOrNull()
                        ?: latestRow.copy(
                            isKillSwitch = isEnabled,
                            isMaintenance = isEnabled,
                            killSwitchTitle = cleanTitle,
                            killSwitchMessage = cleanMessage,
                            estimatedBackTime = cleanBackTime
                        )
                    return@withContext Result.success(refreshed)
                }
                return@withContext Result.failure(
                    Exception("Kill Switch আপডেট ব্যর্থ: $lastErr")
                )
            } else {
                // No rows exist in `public.app_updates` yet — insert an initial row
                val createRes = createAppUpdate(
                    CreateAppUpdatePayload(
                        versionCode = 110,
                        versionName = "1.1.0",
                        apkUrl = "https://maxbird.pages.dev",
                        fileSizeMb = 25.0,
                        releaseNotes = "Initial configuration",
                        isForceUpdate = false,
                        isKillSwitch = isEnabled,
                        isMaintenance = isEnabled,
                        killSwitchTitle = cleanTitle,
                        killSwitchMessage = cleanMessage,
                        estimatedBackTime = cleanBackTime,
                        latestVersionCode = 110,
                        latestVersionName = "1.1.0",
                        minSupportedVersionCode = 110,
                        title = cleanTitle,
                        changelog = cleanMessage,
                        downloadUrl = "https://maxbird.pages.dev",
                        buttonText = "এখনই আপডেট করুন",
                        isActive = true
                    )
                )
                return@withContext createRes
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleAppUpdateStatus(id: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val index = cachedUpdates.indexOfFirst { it.id == id }
        if (index != -1) {
            cachedUpdates[index] = cachedUpdates[index].copy(isActive = isActive)
        }

        try {
            val api = supabaseClient.getApi()
            val res = api.updateAppUpdateStatus("eq.$id", UpdateAppUpdateStatusPayload(isActive = isActive))
            if (res.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAppUpdate(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedUpdates.removeAll { it.id == id }

        try {
            val api = supabaseClient.getApi()
            val delRes = try {
                api.deleteAppUpdate("eq.$id")
            } catch (_: Exception) {
                null
            }
            if (delRes?.isSuccessful != true) {
                try {
                    api.adminDeleteAppUpdateRpc(mapOf("p_id" to id, "p_update_id" to id))
                } catch (_: Exception) {
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // Real Server Push Notifications (`public.student_notifications`)
    // =========================================================================

    override suspend fun getStudentNotifications(): Result<List<StudentNotification>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val list = try {
                api.getStudentNotifications()
            } catch (_: Exception) {
                api.getStudentNotificationsUnordered()
            }
            cachedStudentNotifications.clear()
            cachedStudentNotifications.addAll(list)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createStudentNotification(
        payload: CreateStudentNotificationPayload
    ): Result<StudentNotification> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val cleanTargetType = payload.targetType.trim().uppercase()
            val cleanTargetValue = when (cleanTargetType) {
                "ALL" -> null
                "PHONE" -> CourseAccessEnrollment.normalizeBdPhone(payload.targetValue.orEmpty())
                    .ifBlank { payload.targetValue?.trim() }
                else -> payload.targetValue?.trim()?.ifBlank { null }
            }

            val baseMap = mutableMapOf<String, Any?>(
                "title" to payload.title.trim(),
                "message" to payload.message.trim(),
                "target_type" to cleanTargetType,
                "target_value" to cleanTargetValue,
                "notification_type" to payload.notificationType.trim().uppercase().ifBlank { "ADMIN_PUSH" },
                "image_url" to payload.imageUrl?.trim()?.ifBlank { null },
                "deeplink" to payload.deeplink?.trim()?.ifBlank { null },
                "is_active" to payload.isActive
            )

            // Try without explicit `id` first (if identity/serial exists), then fallback to explicit bigint `id`
            val generatedId = System.currentTimeMillis()
            val mapWithId = LinkedHashMap<String, Any?>().apply {
                put("id", generatedId)
                putAll(baseMap)
            }

            var lastErr = ""
            for (candidate in listOf(baseMap, mapWithId)) {
                val res = api.createStudentNotificationRaw(candidate)
                if (res.isSuccessful) {
                    val created = res.body()?.firstOrNull() ?: StudentNotification(
                        id = generatedId.toString(),
                        title = payload.title.trim(),
                        message = payload.message.trim(),
                        targetType = cleanTargetType,
                        targetValue = cleanTargetValue,
                        notificationType = payload.notificationType.trim().uppercase(),
                        imageUrl = payload.imageUrl?.trim()?.ifBlank { null },
                        deeplink = payload.deeplink?.trim()?.ifBlank { null },
                        isActive = payload.isActive
                    )
                    cachedStudentNotifications.add(0, created)
                    return@withContext Result.success(created)
                } else {
                    lastErr = "${res.code()}: ${res.errorBody()?.string().orEmpty()}"
                }
            }
            Result.failure(Exception("নোটিফিকেশন সেভ ব্যর্থ (HTTP $lastErr)"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleStudentNotificationStatus(id: String, isActive: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val api = supabaseClient.getApi()
                val res = api.patchStudentNotificationRaw(
                    idQuery = "eq.$id",
                    payload = mapOf("is_active" to isActive)
                )
                if (res.isSuccessful) {
                    val idx = cachedStudentNotifications.indexOfFirst { it.id == id }
                    if (idx != -1) {
                        cachedStudentNotifications[idx] = cachedStudentNotifications[idx].copy(isActive = isActive)
                    }
                    Result.success(Unit)
                } else {
                    val err = res.errorBody()?.string().orEmpty()
                    Result.failure(Exception("স্ট্যাটাস পরিবর্তন ব্যর্থ: $err"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteStudentNotification(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val res = api.deleteStudentNotification("eq.$id")
            if (res.isSuccessful) {
                cachedStudentNotifications.removeAll { it.id == id }
                Result.success(Unit)
            } else {
                val err = res.errorBody()?.string().orEmpty()
                Result.failure(Exception("মুছে ফেলতে ব্যর্থ: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAppNotices(forceRefresh: Boolean): Result<List<AppNotice>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteNotices = api.getAppNotices()
            cachedNotices.clear()
            cachedNotices.addAll(remoteNotices)
            Result.success(remoteNotices)
        } catch (e: Exception) {
            if (cachedNotices.isNotEmpty()) {
                Result.success(cachedNotices.toList())
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun createAppNotice(payload: CreateAppNoticePayload): Result<AppNotice> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val createdList = api.createAppNotice(payload)
            val created = createdList.firstOrNull()
                ?: return@withContext Result.failure(Exception("নোটিশ ডাটাবেসে সেভ হয়নি"))
            cachedNotices.add(0, created)
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAppNotice(id: String, payload: CreateAppNoticePayload): Result<AppNotice> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val updatedList = api.updateAppNotice("eq.$id", payload)
            val updated = updatedList.firstOrNull()
                ?: return@withContext Result.failure(Exception("নোটিশ আপডেট হয়নি"))
            val idx = cachedNotices.indexOfFirst { it.id == id }
            if (idx != -1) cachedNotices[idx] = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleAppNoticeStatus(id: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val index = cachedNotices.indexOfFirst { it.id == id }
        if (index != -1) {
            cachedNotices[index] = cachedNotices[index].copy(isActive = isActive)
        }

        try {
            val api = supabaseClient.getApi()
            api.updateAppNoticeStatus("eq.$id", UpdateAppNoticeStatusPayload(isActive = isActive))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAppNotice(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedNotices.removeAll { it.id == id }

        try {
            val api = supabaseClient.getApi()
            val delRes = try {
                api.deleteAppNotice("eq.$id")
            } catch (_: Exception) {
                null
            }
            if (delRes?.isSuccessful != true) {
                try {
                    api.adminDeleteAppNoticeRpc(mapOf("p_id" to id, "p_notice_id" to id))
                } catch (_: Exception) {
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActivationSupportLinks(forceRefresh: Boolean): Result<List<ActivationSupportLink>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteLinks = api.getActivationSupportLinks()
            cachedSupportLinks.clear()
            cachedSupportLinks.addAll(remoteLinks)
            Result.success(remoteLinks)
        } catch (e: Exception) {
            if (cachedSupportLinks.isNotEmpty()) {
                Result.success(cachedSupportLinks.toList())
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun createActivationSupportLink(payload: CreateSupportLinkPayload): Result<ActivationSupportLink> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val createdList = api.createActivationSupportLink(payload)
            val created = createdList.firstOrNull()
                ?: return@withContext Result.failure(Exception("সাপোর্ট লিংক সেভ হয়নি"))
            cachedSupportLinks.add(0, created)
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateActivationSupportLink(id: String, payload: CreateSupportLinkPayload): Result<ActivationSupportLink> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val updatedList = api.updateActivationSupportLink("eq.$id", payload)
            val updated = updatedList.firstOrNull()
                ?: return@withContext Result.failure(Exception("সাপোর্ট লিংক আপডেট হয়নি"))
            val idx = cachedSupportLinks.indexOfFirst { it.id == id }
            if (idx != -1) cachedSupportLinks[idx] = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleActivationSupportLinkStatus(id: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val index = cachedSupportLinks.indexOfFirst { it.id == id }
        if (index != -1) {
            cachedSupportLinks[index] = cachedSupportLinks[index].copy(isActive = isActive)
        }

        try {
            val api = supabaseClient.getApi()
            api.updateActivationSupportLinkStatus("eq.$id", UpdateSupportLinkStatusPayload(isActive = isActive))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteActivationSupportLink(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedSupportLinks.removeAll { it.id == id }

        try {
            val api = supabaseClient.getApi()
            var deletedViaRpc = false
            try {
                val rpcRes = api.adminDeleteSupportLinkRpc(mapOf("p_id" to id, "p_link_id" to id))
                if (rpcRes.isSuccessful) {
                    deletedViaRpc = true
                }
            } catch (_: Exception) {
            }
            if (!deletedViaRpc) {
                api.deleteActivationSupportLink("eq.$id")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserDevices(forceRefresh: Boolean): Result<List<UserDevice>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteDevices = try {
                api.getDevices()
            } catch (_: Exception) {
                api.getDevicesUnordered()
            }
            val sorted = remoteDevices.sortedWith(
                compareByDescending<UserDevice> { it.requestedAt ?: it.updatedAt ?: it.lastSeen ?: "" }
            )
            cachedUserDevices.clear()
            cachedUserDevices.addAll(sorted)
            Result.success(sorted)
        } catch (e: Exception) {
            if (cachedUserDevices.isNotEmpty()) {
                Result.success(cachedUserDevices.toList())
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun patchPublicDeviceRecord(
        device: UserDevice,
        candidatePayloads: List<Map<String, Any?>>
    ): Result<UserDevice?> {
        val api = supabaseClient.getApi()
        var lastErrorMsg = ""

        val cleanPayloads = candidatePayloads.map { map ->
            map.filterValues { it != null }
        }.filter { it.isNotEmpty() }.distinct()

        val hashToUse = device.effectiveDeviceHash
        val rawHash = device.deviceHash
        val rawDeviceIdCol = device.deviceIdColumn
        val rowId = device.id
        val displayId = device.displayDeviceId

        for (payload in cleanPayloads) {
            // 1. Try by device_hash
            val hashCandidates = listOf(rawHash, hashToUse).filter { it.isNotBlank() }.distinct()
            for (h in hashCandidates) {
                try {
                    val res = api.patchDeviceByHash("eq.$h", payload)
                    if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                        return Result.success(res.body()?.firstOrNull())
                    } else if (!res.isSuccessful) {
                        val err = res.errorBody()?.string().orEmpty()
                        if (err.isNotBlank()) lastErrorMsg = err
                    }
                } catch (e: Exception) {
                    if (lastErrorMsg.isBlank()) lastErrorMsg = e.message.orEmpty()
                }
            }

            // 2. Try by id (UUID or primary key)
            if (rowId.isNotBlank()) {
                try {
                    val res = api.patchDevice("eq.$rowId", payload)
                    if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                        return Result.success(res.body()?.firstOrNull())
                    } else if (!res.isSuccessful) {
                        val err = res.errorBody()?.string().orEmpty()
                        if (err.isNotBlank()) lastErrorMsg = err
                    }
                } catch (e: Exception) {
                    if (lastErrorMsg.isBlank()) lastErrorMsg = e.message.orEmpty()
                }
            }

            // 3. Try by device_id column (if schema uses device_id)
            val deviceIdCandidates = listOf(rawDeviceIdCol, hashToUse).filter { it.isNotBlank() }.distinct()
            for (dId in deviceIdCandidates) {
                try {
                    val res = api.patchDeviceByDeviceIdColumn("eq.$dId", payload)
                    if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                        return Result.success(res.body()?.firstOrNull())
                    } else if (!res.isSuccessful) {
                        val err = res.errorBody()?.string().orEmpty()
                        if (err.isNotBlank()) lastErrorMsg = err
                    }
                } catch (e: Exception) {
                    if (lastErrorMsg.isBlank()) lastErrorMsg = e.message.orEmpty()
                }
            }

            // 4. Try by display_device_id
            if (displayId.isNotBlank()) {
                try {
                    val res = api.patchDeviceByDisplayId("eq.$displayId", payload)
                    if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                        return Result.success(res.body()?.firstOrNull())
                    } else if (!res.isSuccessful) {
                        val err = res.errorBody()?.string().orEmpty()
                        if (err.isNotBlank()) lastErrorMsg = err
                    }
                } catch (e: Exception) {
                    if (lastErrorMsg.isBlank()) lastErrorMsg = e.message.orEmpty()
                }
            }
        }

        return Result.failure(
            Exception(
                if (lastErrorMsg.isNotBlank()) {
                    "ডাটাবেস আপডেট ত্রুটি: $lastErrorMsg"
                } else {
                    "ডিভাইসটি ডাটাবেসে আপডেট করা যায়নি (0 rows matched)"
                }
            )
        )
    }

    private fun matchesCachedDevice(item: UserDevice, target: UserDevice): Boolean {
        if (target.effectiveKey.isNotBlank() && item.effectiveKey == target.effectiveKey) return true
        if (target.effectiveDeviceHash.isNotBlank() && item.effectiveDeviceHash == target.effectiveDeviceHash) return true
        if (target.id.isNotBlank() && item.id == target.id) return true
        if (target.displayDeviceId.isNotBlank() && item.displayDeviceId == target.displayDeviceId) return true
        return false
    }

    override suspend fun approveUserDeviceVerification(
        device: UserDevice,
        studentNote: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val resolvedNote = studentNote?.trim() ?: device.studentNote

        val idx = cachedUserDevices.indexOfFirst { matchesCachedDevice(it, device) }
        if (idx != -1) {
            val old = cachedUserDevices[idx]
            cachedUserDevices[idx] = old.copy(
                status = "active",
                isBanned = false,
                isBlockedColumn = false,
                banReason = "",
                rejectionReasonColumn = "",
                studentNote = resolvedNote ?: old.studentNote,
                approvedAt = nowIso,
                updatedAt = nowIso
            )
        }

        try {
            val api = supabaseClient.getApi()
            val effectiveHash = device.effectiveDeviceHash
            var rpcOk = false

            // 1. Call official RPC `admin_approve_device_verification(p_device_hash, p_student_note)`
            // Passing both parameters avoids PostgreSQL PGRST203 overload ambiguity between the 1-arg and 2-arg versions
            if (effectiveHash.isNotBlank()) {
                try {
                    val rpcRes = api.approveDeviceVerificationRpc(
                        mapOf(
                            "p_device_hash" to effectiveHash,
                            "p_student_note" to (resolvedNote ?: "")
                        )
                    )
                    if (isJsonRpcSuccess(rpcRes)) {
                        rpcOk = true
                    } else {
                        val fallbackRpcRes = api.approveDeviceVerificationRpc(
                            mapOf("p_device_hash" to effectiveHash)
                        )
                        if (fallbackRpcRes.isSuccessful) rpcOk = true
                    }
                } catch (_: Exception) {
                }
            }

            // 2. Patch `public.devices` across all possible schema variants
            val dualSchemaMap = mutableMapOf<String, Any?>(
                "status" to "active",
                "is_banned" to false,
                "is_blocked" to false,
                "ban_reason" to "",
                "rejection_reason" to "",
                "approved_at" to nowIso,
                "updated_at" to nowIso
            )
            val standardSchemaMap = mutableMapOf<String, Any?>(
                "status" to "active",
                "is_banned" to false,
                "ban_reason" to "",
                "approved_at" to nowIso,
                "updated_at" to nowIso
            )
            val triggerSchemaMap = mutableMapOf<String, Any?>(
                "status" to "approved",
                "is_blocked" to false,
                "rejection_reason" to "",
                "approved_at" to nowIso,
                "updated_at" to nowIso
            )
            val minimalMap = mutableMapOf<String, Any?>(
                "status" to "active",
                "is_banned" to false
            )
            if (studentNote != null) {
                val cleanNote = studentNote.trim()
                dualSchemaMap["student_note"] = cleanNote
                standardSchemaMap["student_note"] = cleanNote
                triggerSchemaMap["student_note"] = cleanNote
                minimalMap["student_note"] = cleanNote
            }

            val patchResult = patchPublicDeviceRecord(
                device = device,
                candidatePayloads = listOf(dualSchemaMap, standardSchemaMap, triggerSchemaMap, minimalMap)
            )
            if (patchResult.isSuccess || rpcOk) {
                Result.success(Unit)
            } else {
                Result.failure(patchResult.exceptionOrNull() ?: Exception("অ্যাপ্রুভ ব্যর্থ হয়েছে"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setUserDeviceBlockStatus(
        device: UserDevice,
        isBlocked: Boolean,
        banReason: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val targetStatus = if (isBlocked) "blocked" else "active"
        val cleanReason = if (isBlocked) {
            banReason?.trim()?.ifBlank { "অ্যাডমিন কর্তৃক ব্লক করা হয়েছে" } ?: "অ্যাডমিন কর্তৃক ব্লক করা হয়েছে"
        } else {
            ""
        }

        val idx = cachedUserDevices.indexOfFirst { matchesCachedDevice(it, device) }
        if (idx != -1) {
            val old = cachedUserDevices[idx]
            cachedUserDevices[idx] = old.copy(
                status = targetStatus,
                isBanned = isBlocked,
                isBlockedColumn = isBlocked,
                banReason = cleanReason,
                rejectionReasonColumn = cleanReason,
                approvedAt = if (!isBlocked) nowIso else old.approvedAt,
                updatedAt = nowIso
            )
        }

        try {
            val api = supabaseClient.getApi()
            val effectiveHash = device.effectiveDeviceHash
            var rpcOk = false

            // 1. Call official RPC `admin_set_device_block_status(p_device_hash, p_is_banned, p_reason)` -> RETURNS void
            if (effectiveHash.isNotBlank()) {
                try {
                    val res = api.setDeviceBlockStatusRpc(
                        mapOf(
                            "p_device_hash" to effectiveHash,
                            "p_is_banned" to isBlocked,
                            "p_reason" to cleanReason
                        )
                    )
                    if (res.isSuccessful) {
                        rpcOk = true
                    } else {
                        val toggleRes = api.toggleDeviceBanRpc(
                            mapOf(
                                "p_device_hash" to effectiveHash,
                                "p_is_banned" to isBlocked,
                                "p_reason" to cleanReason
                            )
                        )
                        if (isJsonRpcSuccess(toggleRes)) rpcOk = true
                    }
                } catch (_: Exception) {
                }

                if (!isBlocked) {
                    try {
                        val appRes = api.approveDeviceVerificationRpc(
                            mapOf(
                                "p_device_hash" to effectiveHash,
                                "p_student_note" to (device.studentNote ?: "")
                            )
                        )
                        if (isJsonRpcSuccess(appRes)) rpcOk = true
                    } catch (_: Exception) {
                    }
                }
            }

            // 2. Patch `public.devices` directly across all schema variations (no null values in Map!)
            val dualSchemaMap = mutableMapOf<String, Any?>(
                "status" to targetStatus,
                "is_banned" to isBlocked,
                "is_blocked" to isBlocked,
                "ban_reason" to cleanReason,
                "rejection_reason" to cleanReason,
                "updated_at" to nowIso
            )
            val standardSchemaMap = mutableMapOf<String, Any?>(
                "status" to targetStatus,
                "is_banned" to isBlocked,
                "ban_reason" to cleanReason,
                "updated_at" to nowIso
            )
            val triggerSchemaMap = mutableMapOf<String, Any?>(
                "status" to if (isBlocked) "blocked" else "approved",
                "is_blocked" to isBlocked,
                "rejection_reason" to cleanReason,
                "updated_at" to nowIso
            )
            val minimalBannedMap = mapOf<String, Any?>(
                "status" to targetStatus,
                "is_banned" to isBlocked,
                "ban_reason" to cleanReason
            )
            val minimalBlockedMap = mapOf<String, Any?>(
                "status" to targetStatus,
                "is_blocked" to isBlocked
            )

            if (isBlocked) {
                dualSchemaMap["banned_at"] = nowIso
                standardSchemaMap["banned_at"] = nowIso
            } else {
                dualSchemaMap["approved_at"] = nowIso
                standardSchemaMap["approved_at"] = nowIso
                triggerSchemaMap["approved_at"] = nowIso
            }

            val patchResult = patchPublicDeviceRecord(
                device = device,
                candidatePayloads = listOf(
                    dualSchemaMap,
                    standardSchemaMap,
                    triggerSchemaMap,
                    minimalBannedMap,
                    minimalBlockedMap
                )
            )

            if (patchResult.isSuccess || rpcOk) {
                Result.success(Unit)
            } else {
                Result.failure(
                    patchResult.exceptionOrNull()
                        ?: Exception("ডিভাইস ব্লক স্ট্যাটাস ডাটাবেসে সেভ হয়নি")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun revokeUserDeviceToPending(device: UserDevice): Result<Unit> = withContext(Dispatchers.IO) {
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val idx = cachedUserDevices.indexOfFirst { matchesCachedDevice(it, device) }
        if (idx != -1) {
            val old = cachedUserDevices[idx]
            cachedUserDevices[idx] = old.copy(
                status = "pending",
                isBanned = false,
                isBlockedColumn = false,
                banReason = "",
                rejectionReasonColumn = "",
                updatedAt = nowIso
            )
        }

        try {
            val api = supabaseClient.getApi()
            val cleanHash = device.effectiveDeviceHash.trim()

            val dualMap = mapOf<String, Any?>(
                "status" to "pending",
                "is_banned" to false,
                "is_blocked" to false,
                "ban_reason" to "",
                "rejection_reason" to "",
                "updated_at" to nowIso
            )
            val standardMap = mapOf<String, Any?>(
                "status" to "pending",
                "is_banned" to false,
                "ban_reason" to "",
                "updated_at" to nowIso
            )
            val triggerMap = mapOf<String, Any?>(
                "status" to "pending",
                "is_blocked" to false,
                "rejection_reason" to "",
                "updated_at" to nowIso
            )
            val minimalMap = mapOf<String, Any?>(
                "status" to "pending",
                "is_banned" to false
            )
            val patchResult = patchPublicDeviceRecord(
                device = device,
                candidatePayloads = listOf(dualMap, standardMap, triggerMap, minimalMap)
            )

            var rpcSucceeded = false
            if (patchResult.isFailure && cleanHash.isNotBlank()) {
                try {
                    val rpcRes = api.adminRevokeDeviceToPendingRpc(mapOf("p_device_hash" to cleanHash))
                    if (rpcRes.isSuccessful) {
                        rpcSucceeded = true
                    }
                } catch (_: Exception) {
                }
            }

            if (patchResult.isSuccess || rpcSucceeded) {
                Result.success(Unit)
            } else {
                Result.failure(patchResult.exceptionOrNull() ?: Exception("পেন্ডিং করতে ব্যর্থ হয়েছে"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserDeviceStudentNote(
        device: UserDevice,
        studentNote: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val trimmedNote = studentNote.trim()
        val idx = cachedUserDevices.indexOfFirst { matchesCachedDevice(it, device) }
        if (idx != -1) {
            cachedUserDevices[idx] = cachedUserDevices[idx].copy(
                studentNote = trimmedNote,
                updatedAt = nowIso
            )
        }

        try {
            val dualMap = mapOf<String, Any?>(
                "student_note" to trimmedNote,
                "student_name" to trimmedNote,
                "updated_at" to nowIso
            )
            val standardMap = mapOf<String, Any?>(
                "student_note" to trimmedNote,
                "updated_at" to nowIso
            )
            val minimalMap = mapOf<String, Any?>(
                "student_note" to trimmedNote
            )
            val patchResult = patchPublicDeviceRecord(
                device = device,
                candidatePayloads = listOf(dualMap, standardMap, minimalMap)
            )
            if (patchResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(patchResult.exceptionOrNull() ?: Exception("নোট সেভ করতে ব্যর্থ হয়েছে"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleUserDeviceBan(deviceId: String, isBanned: Boolean, banReason: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val targetDevice = cachedUserDevices.firstOrNull {
            it.id == deviceId || it.effectiveDeviceHash == deviceId || it.displayDeviceId == deviceId
        } ?: UserDevice(id = deviceId, deviceHash = deviceId, deviceIdColumn = deviceId)
        setUserDeviceBlockStatus(targetDevice, isBlocked = isBanned, banReason = banReason)
    }

    override suspend fun toggleDeviceCanViewProfiles(deviceId: String, canViewProfiles: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val idx = cachedUserDevices.indexOfFirst {
            it.id == deviceId || it.effectiveDeviceHash == deviceId || it.displayDeviceId == deviceId
        }
        val targetDevice = if (idx != -1) {
            val updated = cachedUserDevices[idx].copy(canViewProfiles = canViewProfiles)
            cachedUserDevices[idx] = updated
            updated
        } else {
            UserDevice(id = deviceId, deviceHash = deviceId, deviceIdColumn = deviceId, canViewProfiles = canViewProfiles)
        }

        try {
            val api = supabaseClient.getApi()
            val effectiveHash = targetDevice.effectiveDeviceHash.trim()

            val patchResult = patchPublicDeviceRecord(
                device = targetDevice,
                candidatePayloads = listOf(mapOf("can_view_profiles" to canViewProfiles))
            )

            var rpcSucceeded = false
            if (patchResult.isFailure && effectiveHash.isNotBlank()) {
                try {
                    val rpcRes = api.adminSetDeviceCanViewProfilesRpc(
                        mapOf(
                            "p_device_hash" to effectiveHash,
                            "p_can_view_profiles" to canViewProfiles
                        )
                    )
                    if (rpcRes.isSuccessful) {
                        rpcSucceeded = true
                    }
                } catch (_: Exception) {
                }
            }

            if (patchResult.isSuccess || rpcSucceeded) {
                Result.success(Unit)
            } else {
                Result.failure(patchResult.exceptionOrNull() ?: Exception("পারমিশন আপডেট ব্যর্থ হয়েছে"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUserDevice(deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val target = cachedUserDevices.firstOrNull {
            it.id == deviceId || it.effectiveDeviceHash == deviceId || it.displayDeviceId == deviceId
        }
        cachedUserDevices.removeAll {
            it.id == deviceId || it.effectiveDeviceHash == deviceId || it.displayDeviceId == deviceId
        }

        try {
            val api = supabaseClient.getApi()
            val hashToUse = target?.effectiveDeviceHash?.takeIf { it.isNotBlank() }
            val idToUse = target?.id?.takeIf { it.isNotBlank() }
            val displayIdToUse = target?.displayDeviceId?.takeIf { it.isNotBlank() }
            var deleted = false

            // 1. Primary: Direct REST DELETE on `public.devices`
            if (hashToUse != null) {
                try {
                    val res = api.deleteDeviceRecordByHash("eq.$hashToUse")
                    if (res.isSuccessful) deleted = true
                } catch (_: Exception) {
                }
                if (!deleted) {
                    try {
                        val res = api.deleteDeviceRecordByDeviceIdColumn("eq.$hashToUse")
                        if (res.isSuccessful) deleted = true
                    } catch (_: Exception) {
                    }
                }
            }
            if (!deleted && idToUse != null) {
                try {
                    val res = api.deleteDeviceRecord("eq.$idToUse")
                    if (res.isSuccessful) deleted = true
                } catch (_: Exception) {
                }
            }
            if (!deleted) {
                try {
                    val res = api.deleteDeviceRecordByHash("eq.$deviceId")
                    if (res.isSuccessful) deleted = true
                } catch (_: Exception) {
                    try {
                        val res = api.deleteDeviceRecord("eq.$deviceId")
                        if (res.isSuccessful) deleted = true
                    } catch (_: Exception) {
                    }
                }
            }

            // 2. Fallback: SECURITY DEFINER RPC `admin_delete_device` if configured
            if (!deleted) {
                val candidateHashes = listOfNotNull(hashToUse, displayIdToUse, idToUse, deviceId.takeIf { it.isNotBlank() }).distinct()
                for (candidate in candidateHashes) {
                    try {
                        val rpcRes = api.adminDeleteDeviceRpc(
                            mapOf(
                                "p_device_hash" to candidate,
                                "p_device_id" to (idToUse ?: candidate)
                            )
                        )
                        if (rpcRes.isSuccessful) {
                            deleted = true
                            break
                        }
                    } catch (_: Exception) {
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDeviceShikhoAccounts(deviceHash: String?): Result<List<DeviceShikhoAccount>> =
        withContext(Dispatchers.IO) {
            try {
                val api = supabaseClient.getApi()
                val cleanHash = deviceHash?.trim().orEmpty()
                val list = if (cleanHash.isNotBlank()) {
                    api.getDeviceShikhoAccountsByHash(deviceHashQuery = "eq.$cleanHash")
                } else {
                    api.getDeviceShikhoAccounts()
                }
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun setDeviceCoursePermissions(
        deviceHash: String,
        isRestricted: Boolean,
        allowedClasses: List<String>,
        allowedGroups: List<String>,
        allowedBatches: List<String>,
        allowedPrograms: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanHash = deviceHash.trim()
        if (cleanHash.isBlank()) {
            return@withContext Result.failure(Exception("ডিভাইস হ্যাশ (device_hash) খালি রাখা যাবে না"))
        }
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val idx = cachedUserDevices.indexOfFirst { it.effectiveDeviceHash == cleanHash || it.id == cleanHash }
        if (idx != -1) {
            cachedUserDevices[idx] = cachedUserDevices[idx].copy(
                isAccessRestricted = isRestricted,
                allowedClasses = allowedClasses,
                allowedGroups = allowedGroups,
                allowedBatches = allowedBatches,
                allowedPrograms = allowedPrograms,
                updatedAt = nowIso
            )
        }

        try {
            val api = supabaseClient.getApi()
            val rpcPayload = mapOf<String, Any?>(
                "p_device_hash" to cleanHash,
                "p_is_restricted" to isRestricted,
                "p_allowed_classes" to allowedClasses,
                "p_allowed_groups" to allowedGroups,
                "p_allowed_batches" to allowedBatches,
                "p_allowed_programs" to allowedPrograms
            )
            val rpcRes = api.setDeviceCoursePermissionsRpc(rpcPayload)
            if (isJsonRpcSuccess(rpcRes)) {
                return@withContext Result.success(Unit)
            }

            val patchRes = api.patchDeviceByHash(
                deviceHashQuery = "eq.$cleanHash",
                payload = mapOf(
                    "is_access_restricted" to isRestricted,
                    "allowed_classes" to allowedClasses,
                    "allowed_groups" to allowedGroups,
                    "allowed_batches" to allowedBatches,
                    "allowed_programs" to allowedPrograms,
                    "updated_at" to nowIso
                )
            )
            if (patchRes.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = try { rpcRes.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                Result.failure(Exception("কোর্স পারমিশন সেভ ব্যর্থ: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setAccountCoursePermissions(
        deviceHash: String,
        userPhone: String,
        isAccessGranted: Boolean,
        isRestricted: Boolean,
        allowedClasses: List<String>,
        allowedGroups: List<String>,
        allowedBatches: List<String>,
        allowedPrograms: List<String>,
        adminNote: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanHash = deviceHash.trim()
        val rawInput = userPhone.trim()
        val normalizedInputPhone = CourseAccessEnrollment.normalizeBdPhone(rawInput)
        val cleanPhone = normalizedInputPhone.ifBlank { rawInput }
        if (cleanHash.isBlank() || cleanPhone.isBlank()) {
            return@withContext Result.failure(Exception("ডিভাইস হ্যাশ এবং ইউজার ফোন নম্বর আবশ্যক"))
        }
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        try {
            val api = supabaseClient.getApi()
            val matchedAccount = try {
                val existingAccounts = api.getDeviceShikhoAccountsByHash(deviceHashQuery = "eq.$cleanHash")
                existingAccounts.firstOrNull {
                    it.shikhoUserId?.trim()?.equals(rawInput, ignoreCase = true) == true ||
                        CourseAccessEnrollment.normalizeBdPhone(it.userPhone) == cleanPhone ||
                        it.userPhone.trim() == cleanPhone
                }
            } catch (_: Exception) {
                null
            }
            val resolvedShikhoUserId = matchedAccount?.effectiveShikhoUserId?.ifBlank { cleanPhone } ?: cleanPhone
            val resolvedUserPhone = matchedAccount?.userPhone?.trim()?.takeIf { it.isNotBlank() }?.let {
                CourseAccessEnrollment.normalizeBdPhone(it).ifBlank { it }
            } ?: (if (CourseAccessEnrollment.isValidBdPhone(cleanPhone)) cleanPhone else "")

            val rpcPayload = mapOf<String, Any?>(
                "p_device_hash" to cleanHash,
                "p_shikho_user_id" to resolvedShikhoUserId,
                "p_user_phone" to resolvedUserPhone,
                "p_is_access_granted" to isAccessGranted,
                "p_is_restricted" to isRestricted,
                "p_allowed_classes" to allowedClasses,
                "p_allowed_groups" to allowedGroups,
                "p_allowed_batches" to allowedBatches,
                "p_allowed_programs" to allowedPrograms,
                "p_revoke_other_accounts" to true
            )
            val rpcRes = api.setAccountCoursePermissionsRpc(rpcPayload)
            if (isJsonRpcSuccess(rpcRes)) {
                return@withContext Result.success(Unit)
            }

            val patchRes = api.patchDeviceShikhoAccount(
                deviceHashQuery = "eq.$cleanHash",
                userPhoneQuery = "eq.${resolvedUserPhone.ifBlank { cleanPhone }}",
                payload = mapOf(
                    "is_access_granted" to isAccessGranted,
                    "is_access_restricted" to isRestricted,
                    "allowed_classes" to allowedClasses,
                    "allowed_groups" to allowedGroups,
                    "allowed_batches" to allowedBatches,
                    "allowed_programs" to allowedPrograms,
                    "last_seen_at" to nowIso
                )
            )
            if (patchRes.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = try { rpcRes.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                Result.failure(Exception("অ্যাকাউন্ট পারমিশন সেভ ব্যর্থ: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isJsonRpcSuccess(response: retrofit2.Response<okhttp3.ResponseBody>): Boolean {
        if (!response.isSuccessful) return false
        val bodyStr = try {
            response.body()?.string()?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }
        if (bodyStr.isEmpty() || !bodyStr.startsWith("{")) return true
        return try {
            val json = org.json.JSONObject(bodyStr)
            if (json.has("success")) json.optBoolean("success", false) else true
        } catch (_: Exception) {
            true
        }
    }

    // =========================================================================
    // Course Sales, Installment Plans & Payment Verification
    // =========================================================================

    private fun parseSavedPaymentMethodsFromPrefs(): List<DynamicPaymentMethodItem>? {
        val rawJson = adminPreferences.getSavedPaymentMethodsJson()?.takeIf { it.isNotBlank() } ?: return null
        return try {
            val listType = Types.newParameterizedType(List::class.java, DynamicPaymentMethodItem::class.java)
            val adapter = supabaseClient.moshi.adapter<List<DynamicPaymentMethodItem>>(listType)
            adapter.fromJson(rawJson)
        } catch (_: Exception) {
            null
        }
    }

    private fun savePaymentMethodsToPrefs(methods: List<DynamicPaymentMethodItem>) {
        try {
            val listType = Types.newParameterizedType(List::class.java, DynamicPaymentMethodItem::class.java)
            val adapter = supabaseClient.moshi.adapter<List<DynamicPaymentMethodItem>>(listType)
            adminPreferences.setSavedPaymentMethodsJson(adapter.toJson(methods))
        } catch (_: Exception) {
        }
    }

    /**
     * Reconciles locally saved payment methods (which include disabled toggles or custom methods like Upay/CellFin)
     * with the live `public.course_payment_settings` columns (`bkash_number`, `nagad_number`, `rocket_number`).
     */
    private fun reconcileMethodsWithLiveColumns(
        remoteMethods: List<DynamicPaymentMethodItem>?,
        cleanBkash: String,
        cleanNagad: String,
        cleanRocket: String
    ): List<DynamicPaymentMethodItem>? {
        if (!remoteMethods.isNullOrEmpty()) {
            savePaymentMethodsToPrefs(remoteMethods)
            return remoteMethods
        }

        val savedLocal = parseSavedPaymentMethodsFromPrefs() ?: return null
        val reconciled = savedLocal.map { item ->
            when {
                item.id.equals("bkash", ignoreCase = true) || item.code.equals("bKash", ignoreCase = true) -> {
                    if (cleanBkash.isNotBlank()) item.copy(accountNumber = cleanBkash, isEnabled = true)
                    else item.copy(isEnabled = false)
                }
                item.id.equals("nagad", ignoreCase = true) || item.code.equals("Nagad", ignoreCase = true) -> {
                    if (cleanNagad.isNotBlank()) item.copy(accountNumber = cleanNagad, isEnabled = true)
                    else item.copy(isEnabled = false)
                }
                item.id.equals("rocket", ignoreCase = true) || item.code.equals("Rocket", ignoreCase = true) -> {
                    if (cleanRocket.isNotBlank()) item.copy(accountNumber = cleanRocket, isEnabled = true)
                    else item.copy(isEnabled = false)
                }
                else -> item
            }
        }.toMutableList()

        if (cleanBkash.isNotBlank() && reconciled.none { it.id.equals("bkash", ignoreCase = true) || it.code.equals("bKash", ignoreCase = true) }) {
            reconciled.add(
                0,
                DynamicPaymentMethodItem(
                    id = "bkash",
                    code = "bKash",
                    nameBn = "বিকাশ",
                    accountNumber = cleanBkash,
                    accountType = "Send Money করুন",
                    instructionBn = "বিকাশ পার্সোনাল নম্বরে Send Money করে TrxID দিন।",
                    colorHex = "#E2136E",
                    isEnabled = true
                )
            )
        }
        if (cleanNagad.isNotBlank() && reconciled.none { it.id.equals("nagad", ignoreCase = true) || it.code.equals("Nagad", ignoreCase = true) }) {
            reconciled.add(
                DynamicPaymentMethodItem(
                    id = "nagad",
                    code = "Nagad",
                    nameBn = "নগদ",
                    accountNumber = cleanNagad,
                    accountType = "Send Money করুন",
                    instructionBn = "নগদ পার্সোনাল নম্বরে Send Money করে TrxID দিন।",
                    colorHex = "#F7941D",
                    isEnabled = true
                )
            )
        }
        if (cleanRocket.isNotBlank() && reconciled.none { it.id.equals("rocket", ignoreCase = true) || it.code.equals("Rocket", ignoreCase = true) }) {
            reconciled.add(
                DynamicPaymentMethodItem(
                    id = "rocket",
                    code = "Rocket",
                    nameBn = "রকেট",
                    accountNumber = cleanRocket,
                    accountType = "Send Money করুন",
                    instructionBn = "রকেট নম্বরে Send Money করে TrxID দিন।",
                    colorHex = "#8C3494",
                    isEnabled = true
                )
            )
        }

        return reconciled
    }

    override suspend fun getCoursePaymentSettings(): Result<CoursePaymentSettings> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val list = api.getCoursePaymentSettings(idQuery = "eq.1")
            val raw = list.firstOrNull()
            val settings = if (raw != null) {
                val cleanBkash = raw.bkashNumber?.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) }?.trim().orEmpty()
                val cleanNagad = raw.nagadNumber?.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) }?.trim().orEmpty()
                val cleanRocket = raw.rocketNumber?.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) }?.trim().orEmpty()
                val cleanUpay = raw.upayNumber?.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) }?.trim().orEmpty()

                val reconciledMethods = reconcileMethodsWithLiveColumns(
                    remoteMethods = raw.paymentMethodsJson,
                    cleanBkash = cleanBkash,
                    cleanNagad = cleanNagad,
                    cleanRocket = cleanRocket
                )

                raw.copy(
                    paymentMethodsJson = reconciledMethods,
                    bkashNumber = cleanBkash,
                    bkashEnabled = cleanBkash.isNotBlank(),
                    nagadNumber = cleanNagad,
                    nagadEnabled = cleanNagad.isNotBlank(),
                    rocketNumber = cleanRocket,
                    rocketEnabled = cleanRocket.isNotBlank(),
                    upayNumber = cleanUpay,
                    upayEnabled = cleanUpay.isNotBlank()
                )
            } else {
                CoursePaymentSettings(
                    id = 1,
                    paymentMethodsJson = parseSavedPaymentMethodsFromPrefs() ?: emptyList(),
                    bkashNumber = "",
                    bkashEnabled = false,
                    nagadNumber = "",
                    nagadEnabled = false,
                    rocketNumber = "",
                    rocketEnabled = false,
                    upayNumber = "",
                    upayEnabled = false,
                    paymentInstructionsBn = "প্রদত্ত পেমেন্ট নম্বরে টাকা পাঠিয়ে নিচের বক্সে আপনার প্রেরক মোবাইল নম্বর এবং Transaction ID দিয়ে সাবমিট করুন।",
                    paymentInstructionBn = "প্রদত্ত পেমেন্ট নম্বরে টাকা পাঠিয়ে নিচের বক্সে আপনার প্রেরক মোবাইল নম্বর এবং Transaction ID দিয়ে সাবমিট করুন।",
                    defaultCoursePrice = 1000
                )
            }
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCoursePaymentSettings(
        payload: UpsertCoursePaymentSettingsPayload
    ): Result<CoursePaymentSettings> = withContext(Dispatchers.IO) {
        try {
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            val syncedPayload = payload.copy(
                bkashNumber = payload.bkashNumber.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) } ?: "",
                nagadNumber = payload.nagadNumber.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) } ?: "",
                rocketNumber = payload.rocketNumber.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) } ?: "",
                upayNumber = payload.upayNumber.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) } ?: "",
                paymentInstructionBn = payload.paymentInstructionsBn
            )

            // Save full dynamic payment methods list locally so disabled toggles & custom methods persist
            savePaymentMethodsToPrefs(syncedPayload.paymentMethodsJson)

            // Exact 8 columns of `public.course_payment_settings` in the live Supabase SQL schema:
            // id, bkash_number, nagad_number, rocket_number, payment_instructions_bn, default_course_price, updated_at, payment_instruction_bn
            val exactSqlSchemaMap = mapOf<String, Any?>(
                "id" to 1,
                "bkash_number" to syncedPayload.bkashNumber,
                "nagad_number" to syncedPayload.nagadNumber,
                "rocket_number" to syncedPayload.rocketNumber,
                "payment_instructions_bn" to syncedPayload.paymentInstructionsBn,
                "payment_instruction_bn" to syncedPayload.paymentInstructionBn,
                "default_course_price" to syncedPayload.defaultCoursePrice,
                "updated_at" to nowIso
            )

            val api = supabaseClient.getApi()
            val updatedList = try {
                // Primary: Upsert exact 8 columns matching live `public.course_payment_settings`
                api.upsertCoursePaymentSettingsRaw(exactSqlSchemaMap)
            } catch (_: Exception) {
                try {
                    api.patchCoursePaymentSettingsRaw(idQuery = "eq.1", payload = exactSqlSchemaMap)
                } catch (_: Exception) {
                    val minimalMap = mapOf<String, Any?>(
                        "id" to 1,
                        "bkash_number" to syncedPayload.bkashNumber,
                        "nagad_number" to syncedPayload.nagadNumber,
                        "rocket_number" to syncedPayload.rocketNumber,
                        "payment_instructions_bn" to syncedPayload.paymentInstructionsBn,
                        "default_course_price" to syncedPayload.defaultCoursePrice,
                        "updated_at" to nowIso
                    )
                    api.upsertCoursePaymentSettingsRaw(minimalMap)
                }
            }

            val rawSaved = updatedList.firstOrNull()
            val saved = rawSaved?.copy(
                paymentMethodsJson = syncedPayload.paymentMethodsJson,
                bkashNumber = rawSaved.bkashNumber?.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) } ?: syncedPayload.bkashNumber,
                bkashEnabled = syncedPayload.bkashEnabled,
                nagadNumber = rawSaved.nagadNumber?.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) } ?: syncedPayload.nagadNumber,
                nagadEnabled = syncedPayload.nagadEnabled,
                rocketNumber = rawSaved.rocketNumber?.takeUnless { CoursePaymentSettings.isDummyPhoneNumber(it) } ?: syncedPayload.rocketNumber,
                rocketEnabled = syncedPayload.rocketEnabled,
                upayNumber = syncedPayload.upayNumber,
                upayEnabled = syncedPayload.upayEnabled
            ) ?: CoursePaymentSettings(
                id = 1,
                paymentMethodsJson = syncedPayload.paymentMethodsJson,
                bkashNumber = syncedPayload.bkashNumber,
                bkashEnabled = syncedPayload.bkashEnabled,
                nagadNumber = syncedPayload.nagadNumber,
                nagadEnabled = syncedPayload.nagadEnabled,
                rocketNumber = syncedPayload.rocketNumber,
                rocketEnabled = syncedPayload.rocketEnabled,
                upayNumber = syncedPayload.upayNumber,
                upayEnabled = syncedPayload.upayEnabled,
                paymentInstructionsBn = syncedPayload.paymentInstructionsBn,
                paymentInstructionBn = syncedPayload.paymentInstructionBn,
                defaultCoursePrice = syncedPayload.defaultCoursePrice
            )
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCourseAccessEnrollments(): Result<List<CourseAccessEnrollment>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val enrollments = api.getCourseAccessEnrollments()
            Result.success(enrollments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ensures that Student App identity & push columns (`device_hash`, `display_device_id`, `device_id`,
     * `student_phone`, `user_phone`, `shikho_account_key`, `student_key`, `shikho_user_id`,
     * `onesignal_player_id`, `fcm_token`) are NEVER overwritten with empty strings (`""`) or `null`
     * during any Admin App update, and enriches them from existing enrollment / transaction / device
     * records when available so the Student App's filtered queries always match 100%.
     */
    private fun preserveAndSanitizeEnrollmentIdentity(
        rawPatch: Map<String, Any?>,
        existingEnrollment: CourseAccessEnrollment? = null,
        transaction: CoursePaymentTransaction? = null,
        matchedDevice: UserDevice? = null
    ): Map<String, Any?> {
        val protectedIdentityKeys = setOf(
            "device_hash",
            "display_device_id",
            "device_id",
            "student_phone",
            "user_phone",
            "shikho_account_key",
            "student_key",
            "shikho_user_id",
            "onesignal_player_id",
            "fcm_token"
        )
        val sanitized = mutableMapOf<String, Any?>()
        for ((k, v) in rawPatch) {
            if (k in protectedIdentityKeys) {
                val strVal = (v as? String)?.trim().orEmpty()
                if (strVal.isNotEmpty()) {
                    sanitized[k] = strVal
                }
            } else {
                sanitized[k] = v
            }
        }

        // Preserve existing non-blank device/account/push identifiers so they are never lost or replaced by a phone fallback
        val realDeviceHash = existingEnrollment?.deviceHash?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
            ?: transaction?.deviceHash?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
            ?: matchedDevice?.effectiveDeviceHash?.trim()?.takeIf { it.isNotBlank() }
            ?: existingEnrollment?.deviceHash?.trim()?.takeIf { it.isNotBlank() }
            ?: transaction?.deviceHash?.trim()?.takeIf { it.isNotBlank() }

        val realDisplayDeviceId = existingEnrollment?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
            ?: transaction?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
            ?: matchedDevice?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() }
            ?: existingEnrollment?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() }
            ?: transaction?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() }

        val realDeviceId = existingEnrollment?.deviceId?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
            ?: transaction?.deviceId?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
            ?: matchedDevice?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() }
            ?: existingEnrollment?.deviceId?.trim()?.takeIf { it.isNotBlank() }
            ?: transaction?.deviceId?.trim()?.takeIf { it.isNotBlank() }

        if (!realDeviceHash.isNullOrBlank()) {
            sanitized["device_hash"] = realDeviceHash
        }
        if (!realDisplayDeviceId.isNullOrBlank()) {
            sanitized["display_device_id"] = realDisplayDeviceId
        }
        if (!realDeviceId.isNullOrBlank()) {
            sanitized["device_id"] = realDeviceId
        }

        val realShikhoUserId = existingEnrollment?.shikhoUserId?.trim()?.takeIf { it.isNotBlank() }
        if (!realShikhoUserId.isNullOrBlank()) {
            sanitized["shikho_user_id"] = realShikhoUserId
        }

        val realPlayerId = existingEnrollment?.onesignalPlayerId?.trim()?.takeIf { it.isNotBlank() }
            ?: matchedDevice?.onesignalPlayerId?.trim()?.takeIf { it.isNotBlank() }
        if (!realPlayerId.isNullOrBlank()) {
            sanitized["onesignal_player_id"] = realPlayerId
        }

        val realFcmToken = existingEnrollment?.fcmToken?.trim()?.takeIf { it.isNotBlank() }
            ?: matchedDevice?.fcmToken?.trim()?.takeIf { it.isNotBlank() }
        if (!realFcmToken.isNullOrBlank()) {
            sanitized["fcm_token"] = realFcmToken
        }

        return sanitized
    }

    private suspend fun patchEnrollmentByIdOrAccount(
        enrollmentId: Long,
        studentPhone: String?,
        shikhoAccountKey: String?,
        programId: String?,
        patchMap: Map<String, Any?>,
        existingEnrollment: CourseAccessEnrollment? = null,
        transaction: CoursePaymentTransaction? = null
    ): retrofit2.Response<Unit>? {
        val api = supabaseClient.getApi()
        val cleanProgId = programId?.trim().orEmpty()
        val cleanAccKey = CourseAccessEnrollment.normalizeBdPhone(shikhoAccountKey)
            .ifBlank { CourseAccessEnrollment.normalizeBdPhone(studentPhone) }
        val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(studentPhone)
            .ifBlank { cleanAccKey }

        val matchedDevice = cachedUserDevices.firstOrNull { dev ->
            (existingEnrollment?.deviceHash?.isNotBlank() == true && dev.effectiveDeviceHash.equals(existingEnrollment.deviceHash, ignoreCase = true)) ||
                (cleanPhone.isNotBlank() && (dev.extractedPhoneFromDevice == cleanPhone || dev.studentNote?.contains(cleanPhone) == true))
        }

        val safePatchMap = preserveAndSanitizeEnrollmentIdentity(
            rawPatch = patchMap,
            existingEnrollment = existingEnrollment,
            transaction = transaction,
            matchedDevice = matchedDevice
        )

        var primaryResponse: retrofit2.Response<Unit>? = null

        if (enrollmentId > 0L) {
            try {
                primaryResponse = api.patchCourseAccessEnrollment(
                    idQuery = "eq.$enrollmentId",
                    payload = safePatchMap
                )
            } catch (_: Exception) {
                primaryResponse = null
            }
        }

        if (cleanProgId.isNotBlank() && (enrollmentId <= 0L || primaryResponse?.isSuccessful != true)) {
            if (cleanAccKey.isNotBlank()) {
                try {
                    val res = api.patchCourseAccessEnrollmentByAccountAndProgram(
                        accountKeyQuery = "eq.$cleanAccKey",
                        programIdQuery = "eq.$cleanProgId",
                        payload = safePatchMap
                    )
                    if (res.isSuccessful) primaryResponse = res
                } catch (_: Exception) {
                }
            }
            if (cleanPhone.isNotBlank()) {
                try {
                    val res = api.patchCourseAccessEnrollmentByPhoneAndProgram(
                        studentPhoneQuery = "eq.$cleanPhone",
                        programIdQuery = "eq.$cleanProgId",
                        payload = safePatchMap
                    )
                    if (res.isSuccessful) primaryResponse = res
                } catch (_: Exception) {
                }
                try {
                    val res = api.patchCourseAccessEnrollmentByUserPhoneAndProgram(
                        userPhoneQuery = "eq.$cleanPhone",
                        programIdQuery = "eq.$cleanProgId",
                        payload = safePatchMap
                    )
                    if (res.isSuccessful) primaryResponse = res
                } catch (_: Exception) {
                }
            }
        }

        return primaryResponse
    }

    private fun formatUtcIso(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date(epochMillis))
    }

    override suspend fun approveCourseAccessRequest(
        payload: ApproveCourseAccessRequestRpcPayload
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val nowMillis = System.currentTimeMillis()
            val nowIso = formatUtcIso(nowMillis)
            val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(payload.studentPhone)
            val cleanAccountKey = CourseAccessEnrollment.normalizeBdPhone(payload.shikhoAccountKey)
                .ifBlank { cleanPhone }
            val cleanProgramId = payload.programId?.trim().orEmpty()

            // 1. If instant free unlock is requested and we have an enrollment ID, call `admin_approve_course_access_request` with `p_instant_free_unlock = true` and `p_total_price = 0` so trigger `fn_sync_course_enrollment_columns` keeps `due_amount = 0`
            var rpcSucceeded = false
            if (payload.instantFreeUnlock && payload.enrollmentId > 0L) {
                try {
                    val freePayload = payload.copy(
                        totalPrice = 0,
                        installmentEnabled = false,
                        installmentPlansJson = emptyList(),
                        trialEnabled = false,
                        trialDays = 0,
                        trialHours = 0
                    )
                    val rpcRes = api.approveCourseAccessRequestRpc(freePayload)
                    rpcSucceeded = isJsonRpcSuccess(rpcRes)
                } catch (_: Exception) {
                    rpcSucceeded = false
                }
            }

            if (payload.instantFreeUnlock) {
                val freeUnlockPatch = mutableMapOf<String, Any?>(
                    "status" to "ACTIVE",
                    "access_status" to "ACTIVE",
                    "total_price" to 0,
                    "total_fee" to 0,
                    "due_amount" to 0,
                    "installment_enabled" to false,
                    "allow_installments" to false,
                    "selected_plan_id" to "FULL",
                    "selected_plan_title" to "এককালীন সম্পূর্ণ পেমেন্ট",
                    "active_plan_id" to "FULL",
                    "active_plan_title" to "এককালীন সম্পূর্ণ পেমেন্ট",
                    "total_installments" to 1,
                    "completed_installments" to 1,
                    "next_installment_amount" to 0,
                    "next_due_date" to null,
                    "next_installment_due_at" to null,
                    "installment_plans_json" to emptyList<Any>(),
                    "installment_plans" to emptyList<Any>(),
                    "trial_enabled" to false,
                    "trial_days" to 0,
                    "trial_hours" to 0,
                    "admin_note" to payload.adminNote,
                    "approved_at" to nowIso,
                    "unlocked_at" to nowIso,
                    "updated_at" to nowIso
                )
                if (CourseAccessEnrollment.isValidBdPhone(cleanPhone)) {
                    freeUnlockPatch["student_phone"] = cleanPhone
                    freeUnlockPatch["user_phone"] = cleanPhone
                    freeUnlockPatch["shikho_account_key"] = cleanAccountKey.ifBlank { cleanPhone }
                    freeUnlockPatch["student_key"] = cleanAccountKey.ifBlank { cleanPhone }
                }
                val patchRes = patchEnrollmentByIdOrAccount(
                    enrollmentId = payload.enrollmentId,
                    studentPhone = cleanPhone,
                    shikhoAccountKey = cleanAccountKey,
                    programId = cleanProgramId,
                    patchMap = freeUnlockPatch
                )
                if (patchRes?.isSuccessful == true || rpcSucceeded) {
                    return@withContext Result.success(Unit)
                }
            }

            val safeTrialDays = if (payload.trialEnabled) payload.trialDays.coerceAtLeast(0) else 0
            val safeTrialHours = if (payload.trialEnabled) payload.trialHours.coerceAtLeast(0) else 0
            val totalTrialDurationHours = (safeTrialDays * 24) + safeTrialHours
            val shouldStartTrialNow = payload.trialEnabled && payload.startTrialImmediately && totalTrialDurationHours > 0
            val trialEndsIso = if (shouldStartTrialNow) {
                formatUtcIso(nowMillis + (totalTrialDurationHours * 3600_000L))
            } else {
                null
            }

            // 2. Call `admin_approve_course_access_with_trial` with the EXACT 10 parameters defined in PostgreSQL:
            // (p_student_key, p_program_id, p_total_price, p_allow_installments, p_installment_plans,
            //  p_trial_enabled, p_trial_days, p_trial_hours, p_start_trial_immediately, p_admin_note)
            val studentKeyCandidates = listOf(
                if (payload.enrollmentId > 0L) payload.enrollmentId.toString() else "",
                cleanAccountKey,
                cleanPhone
            ).filter { it.isNotBlank() }.distinct()

            if (cleanProgramId.isNotBlank()) {
                for (candidateKey in studentKeyCandidates) {
                    try {
                        val withTrialRpcMap = mapOf<String, Any?>(
                            "p_student_key" to candidateKey,
                            "p_program_id" to cleanProgramId,
                            "p_total_price" to payload.totalPrice,
                            "p_allow_installments" to payload.installmentEnabled,
                            "p_installment_plans" to payload.installmentPlansJson,
                            "p_trial_enabled" to payload.trialEnabled,
                            "p_trial_days" to safeTrialDays,
                            "p_trial_hours" to safeTrialHours,
                            "p_start_trial_immediately" to shouldStartTrialNow,
                            "p_admin_note" to payload.adminNote
                        )
                        val trialRpcRes = api.approveCourseAccessWithTrialRpcRaw(withTrialRpcMap)
                        if (isJsonRpcSuccess(trialRpcRes)) {
                            rpcSucceeded = true
                            break
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            // 3. Fallback to `admin_approve_course_access_request` (when trial is not enabled or if with_trial failed)
            if (!rpcSucceeded && !payload.trialEnabled && payload.enrollmentId > 0L) {
                try {
                    val rpcRes = api.approveCourseAccessRequestRpc(payload)
                    rpcSucceeded = isJsonRpcSuccess(rpcRes)
                } catch (_: Exception) {
                    rpcSucceeded = false
                }
            }

            // 4. If RPC succeeded and no extra phone normalization is needed, return success;
            // otherwise sync via table PATCH so all Free Trial columns & status are guaranteed 100% synced
            if (rpcSucceeded && !CourseAccessEnrollment.isValidBdPhone(cleanPhone)) {
                return@withContext Result.success(Unit)
            }

            val firstPlan = payload.installmentPlansJson.firstOrNull()
            val resolvedPlanId = firstPlan?.planId?.takeIf { it.isNotBlank() } ?: "FULL"
            val resolvedPlanTitle = firstPlan?.effectiveTitle?.takeIf { it.isNotBlank() } ?: "এককালীন সম্পূর্ণ পেমেন্ট"
            val targetStatus = when {
                payload.instantFreeUnlock -> "ACTIVE"
                shouldStartTrialNow -> "TRIAL_ACTIVE"
                else -> "APPROVED_TO_BUY"
            }

            val patchMap = mutableMapOf<String, Any?>(
                "status" to targetStatus,
                "access_status" to targetStatus,
                "total_price" to payload.totalPrice,
                "total_fee" to payload.totalPrice,
                "due_amount" to payload.totalPrice,
                "installment_enabled" to payload.installmentEnabled,
                "allow_installments" to payload.installmentEnabled,
                "selected_plan_id" to resolvedPlanId,
                "selected_plan_title" to resolvedPlanTitle,
                "active_plan_id" to resolvedPlanId,
                "active_plan_title" to resolvedPlanTitle,
                "total_installments" to (firstPlan?.effectiveInstallmentsCount ?: 1),
                "installment_plans_json" to payload.installmentPlansJson,
                "installment_plans" to payload.installmentPlansJson,
                "next_installment_amount" to (firstPlan?.effectiveAmountPerInstallment ?: payload.totalPrice),
                "installment_interval_days" to (firstPlan?.intervalDays ?: 7),
                "trial_enabled" to payload.trialEnabled,
                "trial_days" to safeTrialDays,
                "trial_days_granted" to safeTrialDays,
                "trial_hours" to safeTrialHours,
                "trial_started_at" to if (shouldStartTrialNow) nowIso else null,
                "trial_ends_at" to trialEndsIso,
                "trial_expires_at" to trialEndsIso,
                "is_trial_used" to shouldStartTrialNow,
                "trial_used" to shouldStartTrialNow,
                "admin_note" to payload.adminNote,
                "rejection_reason" to "",
                "approved_at" to nowIso,
                "updated_at" to nowIso
            )
            if (shouldStartTrialNow || payload.instantFreeUnlock) {
                patchMap["unlocked_at"] = nowIso
            }
            if (CourseAccessEnrollment.isValidBdPhone(cleanPhone)) {
                patchMap["student_phone"] = cleanPhone
                patchMap["user_phone"] = cleanPhone
                patchMap["shikho_account_key"] = cleanAccountKey.ifBlank { cleanPhone }
                patchMap["student_key"] = cleanAccountKey.ifBlank { cleanPhone }
            }

            val patchResponse = patchEnrollmentByIdOrAccount(
                enrollmentId = payload.enrollmentId,
                studentPhone = cleanPhone,
                shikhoAccountKey = cleanAccountKey,
                programId = cleanProgramId,
                patchMap = patchMap
            )

            if (rpcSucceeded || patchResponse?.isSuccessful == true) {
                Result.success(Unit)
            } else {
                val errBody = try {
                    patchResponse?.errorBody()?.string().orEmpty()
                } catch (_: Exception) {
                    ""
                }
                Result.failure(
                    Exception(
                        "রিকোয়েস্ট অনুমোদন ব্যর্থ (HTTP ${patchResponse?.code() ?: 400})${if (errBody.isNotBlank()) ": $errBody" else ""}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun grantManualCourseAccess(
        payload: ManualCourseAccessPayload
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(payload.studentPhone)
            if (!CourseAccessEnrollment.isValidBdPhone(cleanPhone)) {
                return@withContext Result.failure(
                    Exception("সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)")
                )
            }

            val cleanProgramId = payload.programId.trim()
            if (cleanProgramId.isBlank()) {
                return@withContext Result.failure(
                    Exception("কোর্স বা প্রোগ্রামের আইডি (program_id) আবশ্যক")
                )
            }

            val api = supabaseClient.getApi()
            val nowMillis = System.currentTimeMillis()
            val nowIso = formatUtcIso(nowMillis)

            val safeTotalPrice = payload.totalPrice.coerceAtLeast(0)
            val safePaidAmount = payload.paidAmount.coerceIn(0, safeTotalPrice)
            val safeDueAmount = (safeTotalPrice - safePaidAmount).coerceAtLeast(0)
            val isUnlockedActive = payload.status.equals("ACTIVE", ignoreCase = true)
            val isTrialActiveMode = payload.status.equals("TRIAL_ACTIVE", ignoreCase = true) ||
                (payload.trialEnabled && payload.startTrialImmediately)

            val safeTrialDays = if (payload.trialEnabled || isTrialActiveMode) payload.trialDays.coerceAtLeast(0) else 0
            val safeTrialHours = if (payload.trialEnabled || isTrialActiveMode) payload.trialHours.coerceAtLeast(0) else 0
            val totalTrialHours = ((safeTrialDays * 24) + safeTrialHours).let {
                if (isTrialActiveMode && it <= 0) 24 else it
            }
            val trialEndsIso = if (isTrialActiveMode) {
                formatUtcIso(nowMillis + (totalTrialHours * 3600_000L))
            } else {
                null
            }

            val firstPlan = payload.installmentPlansJson.firstOrNull()
            val totalInstallments = firstPlan?.effectiveInstallmentsCount ?: 1
            val completedInstallments = when {
                isUnlockedActive && safeDueAmount == 0 -> totalInstallments
                isUnlockedActive && safePaidAmount > 0 -> 1
                else -> 0
            }
            val intervalDays = payload.nextDueDays?.takeIf { it > 0 }
                ?: firstPlan?.intervalDays?.takeIf { it > 0 }
                ?: 30

            val nextDueDateIso = if (isUnlockedActive && safeDueAmount > 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, intervalDays)
                formatUtcIso(cal.timeInMillis)
            } else {
                null
            }

            val nextInstallmentAmount = if (safeDueAmount > 0) {
                firstPlan?.effectiveAmountPerInstallment?.takeIf { it > 0 }?.coerceAtMost(safeDueAmount)
                    ?: safeDueAmount
            } else {
                0
            }

            val resolvedStatus = when {
                isTrialActiveMode -> "TRIAL_ACTIVE"
                isUnlockedActive -> "ACTIVE"
                else -> "APPROVED_TO_BUY"
            }

            val resolvedPlanId = firstPlan?.planId?.takeIf { it.isNotBlank() } ?: "FULL"
            val resolvedPlanTitle = firstPlan?.effectiveTitle?.takeIf { it.isNotBlank() } ?: "এককালীন সম্পূর্ণ পেমেন্ট"

            // Check if an enrollment row already exists for this student mobile + program_id
            val existingEnrollments = try {
                api.getCourseAccessEnrollments()
            } catch (_: Exception) {
                emptyList()
            }

            val existingMatch = existingEnrollments.firstOrNull { item ->
                item.programId.equals(cleanProgramId, ignoreCase = true) && (
                    CourseAccessEnrollment.normalizeBdPhone(item.effectivePhone) == cleanPhone ||
                        CourseAccessEnrollment.normalizeBdPhone(item.effectiveAccountKey) == cleanPhone ||
                        CourseAccessEnrollment.normalizeBdPhone(item.deviceId) == cleanPhone
                    )
            }

            val matchedDevice = cachedUserDevices.firstOrNull { dev ->
                (existingMatch?.deviceHash?.isNotBlank() == true && dev.effectiveDeviceHash.equals(existingMatch.deviceHash, ignoreCase = true)) ||
                    dev.extractedPhoneFromDevice == cleanPhone ||
                    (dev.studentNote?.contains(cleanPhone) == true)
            }

            val preservedDeviceHash = existingMatch?.deviceHash?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
                ?: matchedDevice?.effectiveDeviceHash?.trim()?.takeIf { it.isNotBlank() }
                ?: existingMatch?.deviceHash?.trim()?.takeIf { it.isNotBlank() }
                ?: cleanPhone
            val preservedDisplayDeviceId = existingMatch?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
                ?: matchedDevice?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() }
                ?: existingMatch?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() }
                ?: cleanPhone
            val preservedDeviceId = existingMatch?.deviceId?.trim()?.takeIf { it.isNotBlank() && !CourseAccessEnrollment.isValidBdPhone(it) }
                ?: matchedDevice?.displayDeviceId?.trim()?.takeIf { it.isNotBlank() }
                ?: existingMatch?.deviceId?.trim()?.takeIf { it.isNotBlank() }
                ?: cleanPhone
            val preservedAccountKey = existingMatch?.shikhoAccountKey?.trim()?.takeIf { it.isNotBlank() }
                ?: cleanPhone

            val recordMap = mutableMapOf<String, Any?>(
                "student_phone" to cleanPhone,
                "user_phone" to cleanPhone,
                "shikho_account_key" to preservedAccountKey,
                "device_id" to preservedDeviceId,
                "device_hash" to preservedDeviceHash,
                "display_device_id" to preservedDisplayDeviceId,
                "student_name" to payload.studentName.trim().ifBlank {
                    existingMatch?.studentName?.takeIf { it.isNotBlank() } ?: "শিক্ষার্থী ($cleanPhone)"
                },
                "program_id" to cleanProgramId,
                "program_title" to payload.programTitle.trim().ifBlank {
                    existingMatch?.programTitle?.takeIf { it.isNotBlank() } ?: cleanProgramId
                },
                "status" to resolvedStatus,
                "total_price" to safeTotalPrice,
                "paid_amount" to safePaidAmount,
                "due_amount" to safeDueAmount,
                "installment_enabled" to payload.installmentEnabled,
                "allow_installments" to payload.installmentEnabled,
                "selected_plan_id" to resolvedPlanId,
                "selected_plan_title" to resolvedPlanTitle,
                "active_plan_id" to resolvedPlanId,
                "active_plan_title" to resolvedPlanTitle,
                "installment_plans_json" to payload.installmentPlansJson,
                "total_installments" to totalInstallments,
                "completed_installments" to completedInstallments,
                "next_installment_amount" to nextInstallmentAmount,
                "installment_interval_days" to intervalDays,
                "next_due_date" to nextDueDateIso,
                "trial_enabled" to (payload.trialEnabled || isTrialActiveMode),
                "trial_days" to if (isTrialActiveMode && safeTrialDays == 0 && safeTrialHours == 0) 1 else safeTrialDays,
                "trial_hours" to safeTrialHours,
                "trial_started_at" to if (isTrialActiveMode) nowIso else null,
                "trial_ends_at" to trialEndsIso,
                "is_trial_used" to isTrialActiveMode,
                "admin_note" to payload.adminNote.trim(),
                "rejection_reason" to "",
                "approved_at" to nowIso,
                "updated_at" to nowIso
            )

            existingMatch?.shikhoUserId?.trim()?.takeIf { it.isNotBlank() }?.let {
                recordMap["shikho_user_id"] = it
            }
            (existingMatch?.onesignalPlayerId?.trim()?.takeIf { it.isNotBlank() }
                ?: matchedDevice?.onesignalPlayerId?.trim()?.takeIf { it.isNotBlank() })?.let {
                recordMap["onesignal_player_id"] = it
            }
            (existingMatch?.fcmToken?.trim()?.takeIf { it.isNotBlank() }
                ?: matchedDevice?.fcmToken?.trim()?.takeIf { it.isNotBlank() })?.let {
                recordMap["fcm_token"] = it
            }

            if (isUnlockedActive || isTrialActiveMode) {
                recordMap["unlocked_at"] = nowIso
            }

            if (payload.classCode.isNotBlank()) {
                recordMap["class_code"] = payload.classCode.trim()
                recordMap["student_class"] = payload.classCode.trim()
            }
            if (payload.groupCode.isNotBlank()) {
                recordMap["group_code"] = payload.groupCode.trim()
                recordMap["student_group"] = payload.groupCode.trim()
            }

            if (existingMatch != null && existingMatch.id > 0L) {
                val safePatch = preserveAndSanitizeEnrollmentIdentity(
                    rawPatch = recordMap,
                    existingEnrollment = existingMatch,
                    matchedDevice = matchedDevice
                )
                val patchRes = api.patchCourseAccessEnrollment(
                    idQuery = "eq.${existingMatch.id}",
                    payload = safePatch
                )
                if (patchRes.isSuccessful) {
                    return@withContext Result.success(Unit)
                }
            }

            recordMap["requested_at"] = existingMatch?.requestedAt?.takeIf { it.isNotBlank() } ?: nowIso
            val createRes = api.createCourseAccessEnrollmentRaw(recordMap)
            if (createRes.isSuccessful) {
                return@withContext Result.success(Unit)
            }

            // Fallback: patch by shikho_account_key + program_id or student_phone + program_id
            val fallbackPatch = patchEnrollmentByIdOrAccount(
                enrollmentId = existingMatch?.id ?: 0L,
                studentPhone = cleanPhone,
                shikhoAccountKey = preservedAccountKey,
                programId = cleanProgramId,
                patchMap = recordMap,
                existingEnrollment = existingMatch
            )
            if (fallbackPatch?.isSuccessful == true) {
                return@withContext Result.success(Unit)
            }

            val errBody = try {
                createRes.errorBody()?.string().orEmpty()
            } catch (_: Exception) {
                ""
            }
            Result.failure(
                Exception(
                    "কোর্স এক্সেস সেভ ব্যর্থ (HTTP ${createRes.code()})${if (errBody.isNotBlank()) ": $errBody" else ""}"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun manageCourseTrial(
        payload: ManageCourseTrialPayload
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val nowMillis = System.currentTimeMillis()
            val nowIso = formatUtcIso(nowMillis)
            val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(payload.studentPhone)
            val cleanAccountKey = CourseAccessEnrollment.normalizeBdPhone(payload.shikhoAccountKey)
                .ifBlank { cleanPhone }
            val cleanProgramId = payload.programId?.trim().orEmpty()
            val actionUpper = payload.action.trim().uppercase(Locale.US)

            val safeDays = payload.days.coerceAtLeast(0)
            val safeHours = payload.hours.coerceAtLeast(0)
            val deltaHours = ((safeDays * 24) + safeHours)

            // 1. Call `admin_manage_course_trial(p_student_key, p_program_id, p_action, p_days, p_hours)`
            var rpcSucceeded = false
            val studentKeyCandidates = listOf(
                if (payload.enrollmentId > 0L) payload.enrollmentId.toString() else "",
                cleanAccountKey,
                cleanPhone
            ).filter { it.isNotBlank() }.distinct()

            if (cleanProgramId.isNotBlank()) {
                for (candidateKey in studentKeyCandidates) {
                    try {
                        val rpcMap = mapOf<String, Any?>(
                            "p_student_key" to candidateKey,
                            "p_program_id" to cleanProgramId,
                            "p_action" to actionUpper,
                            "p_days" to safeDays,
                            "p_hours" to safeHours
                        )
                        val rpcRes = api.manageCourseTrialRpcRaw(rpcMap)
                        if (isJsonRpcSuccess(rpcRes)) {
                            rpcSucceeded = true
                            break
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            if (rpcSucceeded) {
                return@withContext Result.success(Unit)
            }

            // 2. Fallback: Build direct table update map for `public.course_access_enrollments`
            val currentEndsMillis = CourseAccessEnrollment.parseIsoTimestampToMillis(payload.currentTrialEndsAt)
            val patchMap = mutableMapOf<String, Any?>(
                "updated_at" to nowIso
            )

            when (actionUpper) {
                "ADD_HOURS" -> {
                    val effectiveAddHours = deltaHours.coerceAtLeast(1)
                    val baseMillis = if (currentEndsMillis != null && currentEndsMillis > nowMillis) {
                        currentEndsMillis
                    } else {
                        nowMillis
                    }
                    val newEndsMillis = baseMillis + (effectiveAddHours * 3600_000L)
                    val newEndsIso = formatUtcIso(newEndsMillis)
                    val startedIso = payload.currentTrialStartedAt?.takeIf { it.isNotBlank() } ?: nowIso

                    patchMap["status"] = "TRIAL_ACTIVE"
                    patchMap["access_status"] = "TRIAL_ACTIVE"
                    patchMap["trial_enabled"] = true
                    patchMap["is_trial_used"] = true
                    patchMap["trial_used"] = true
                    patchMap["trial_started_at"] = startedIso
                    patchMap["trial_ends_at"] = newEndsIso
                    patchMap["trial_expires_at"] = newEndsIso
                    patchMap["trial_days"] = (payload.currentTrialDays.coerceAtLeast(0) + safeDays)
                    patchMap["trial_days_granted"] = (payload.currentTrialDays.coerceAtLeast(0) + safeDays)
                    patchMap["trial_hours"] = (payload.currentTrialHours.coerceAtLeast(0) + safeHours)
                    patchMap["unlocked_at"] = nowIso
                }

                "REDUCE_HOURS" -> {
                    val effectiveReduceHours = deltaHours.coerceAtLeast(1)
                    val baseMillis = currentEndsMillis ?: nowMillis
                    val newEndsMillis = baseMillis - (effectiveReduceHours * 3600_000L)
                    val newEndsIso = formatUtcIso(newEndsMillis)
                    val newStatus = if (newEndsMillis <= nowMillis) "APPROVED_TO_BUY" else "TRIAL_ACTIVE"

                    patchMap["status"] = newStatus
                    patchMap["access_status"] = newStatus
                    patchMap["trial_enabled"] = true
                    patchMap["is_trial_used"] = true
                    patchMap["trial_used"] = true
                    patchMap["trial_ends_at"] = newEndsIso
                    patchMap["trial_expires_at"] = newEndsIso
                }

                "SET_DURATION" -> {
                    val effectiveTotalHours = deltaHours.coerceAtLeast(1)
                    val newEndsMillis = nowMillis + (effectiveTotalHours * 3600_000L)
                    val newEndsIso = formatUtcIso(newEndsMillis)

                    patchMap["status"] = "TRIAL_ACTIVE"
                    patchMap["access_status"] = "TRIAL_ACTIVE"
                    patchMap["trial_enabled"] = true
                    patchMap["trial_days"] = safeDays
                    patchMap["trial_days_granted"] = safeDays
                    patchMap["trial_hours"] = safeHours
                    patchMap["trial_started_at"] = nowIso
                    patchMap["trial_ends_at"] = newEndsIso
                    patchMap["trial_expires_at"] = newEndsIso
                    patchMap["is_trial_used"] = true
                    patchMap["trial_used"] = true
                    patchMap["unlocked_at"] = nowIso
                }

                "END_TRIAL_NOW" -> {
                    val endedIso = formatUtcIso(nowMillis - 60_000L) // now() - interval '1 minute'
                    patchMap["status"] = "APPROVED_TO_BUY"
                    patchMap["access_status"] = "APPROVED_TO_BUY"
                    patchMap["trial_ends_at"] = endedIso
                    patchMap["trial_expires_at"] = endedIso
                    patchMap["is_trial_used"] = true
                    patchMap["trial_used"] = true
                }

                "RESET_TRIAL" -> {
                    val resetDays = if (safeDays > 0 || safeHours > 0) {
                        safeDays
                    } else if (payload.currentTrialDays > 0 || payload.currentTrialHours > 0) {
                        payload.currentTrialDays
                    } else {
                        1
                    }
                    val resetHours = if (safeDays > 0 || safeHours > 0) {
                        safeHours
                    } else {
                        payload.currentTrialHours.coerceAtLeast(0)
                    }

                    patchMap["status"] = "APPROVED_TO_BUY"
                    patchMap["access_status"] = "APPROVED_TO_BUY"
                    patchMap["trial_enabled"] = true
                    patchMap["is_trial_used"] = false
                    patchMap["trial_used"] = false
                    patchMap["trial_started_at"] = null
                    patchMap["trial_ends_at"] = null
                    patchMap["trial_expires_at"] = null
                    patchMap["trial_days"] = resetDays
                    patchMap["trial_days_granted"] = resetDays
                    patchMap["trial_hours"] = resetHours
                }
            }

            if (CourseAccessEnrollment.isValidBdPhone(cleanPhone)) {
                patchMap["student_phone"] = cleanPhone
                patchMap["user_phone"] = cleanPhone
                patchMap["shikho_account_key"] = cleanAccountKey.ifBlank { cleanPhone }
                patchMap["student_key"] = cleanAccountKey.ifBlank { cleanPhone }
            }

            val patchRes = patchEnrollmentByIdOrAccount(
                enrollmentId = payload.enrollmentId,
                studentPhone = cleanPhone,
                shikhoAccountKey = cleanAccountKey,
                programId = cleanProgramId,
                patchMap = patchMap
            )

            if (patchRes?.isSuccessful == true) {
                Result.success(Unit)
            } else {
                val errBody = try {
                    patchRes?.errorBody()?.string().orEmpty()
                } catch (_: Exception) {
                    ""
                }
                Result.failure(
                    Exception(
                        "ট্রায়াল আপডেট ব্যর্থ (HTTP ${patchRes?.code() ?: 400})${if (errBody.isNotBlank()) ": $errBody" else ""}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCoursePaymentTransactions(): Result<List<CoursePaymentTransaction>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val transactions = api.getCoursePaymentTransactions()
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reviewCoursePayment(
        payload: ReviewCoursePaymentRpcPayload
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

            // 1. Primary: Call official RPC `admin_review_course_payment(p_transaction_id, p_approve, p_rejection_reason, p_custom_next_due_days)`
            var rpcSucceeded = false
            try {
                val primaryResponse = api.reviewCoursePaymentRpc(payload)
                rpcSucceeded = isJsonRpcSuccess(primaryResponse)
            } catch (_: Exception) {
                rpcSucceeded = false
            }

            // 2. Secondary fallback: raw map with exact 4 parameters
            if (!rpcSucceeded) {
                try {
                    val altMap = mutableMapOf<String, Any?>(
                        "p_transaction_id" to payload.transactionId,
                        "p_approve" to payload.approve,
                        "p_rejection_reason" to payload.rejectionReason,
                        "p_custom_next_due_days" to payload.customNextDueDays
                    )
                    val altResponse = api.reviewCoursePaymentRpcRaw(altMap)
                    rpcSucceeded = isJsonRpcSuccess(altResponse)
                } catch (_: Exception) {
                    rpcSucceeded = false
                }
            }

            // When `admin_review_course_payment` succeeds, the database function has already updated
            // both `course_payment_transactions` and `course_access_enrollments` (including installment math and next_due_date).
            if (rpcSucceeded) {
                return@withContext Result.success(Unit)
            }

            // 3. Fallback direct table sync ONLY if the RPC failed
            val allTxs = try {
                api.getCoursePaymentTransactions()
            } catch (_: Exception) {
                emptyList()
            }
            val targetTx = allTxs.firstOrNull { it.id == payload.transactionId }

            api.patchCoursePaymentTransaction(
                idQuery = "eq.${payload.transactionId}",
                payload = mapOf(
                    "status" to if (payload.approve) "APPROVED" else "REJECTED",
                    "rejection_reason" to if (payload.approve) "" else payload.rejectionReason,
                    "reviewed_at" to nowIso
                )
            )

            val enrollmentId = targetTx?.enrollmentId?.takeIf { it > 0L }
                ?: payload.enrollmentId?.takeIf { it > 0L }
                ?: 0L
            val txPhone = CourseAccessEnrollment.normalizeBdPhone(
                targetTx?.effectiveStudentPhone?.ifBlank { payload.studentPhone }
            )
            val txAccountKey = CourseAccessEnrollment.normalizeBdPhone(
                targetTx?.effectiveAccountKey?.ifBlank { payload.shikhoAccountKey }
            ).ifBlank { txPhone }
            val txProgramId = targetTx?.programId?.takeIf { it.isNotBlank() }
                ?: payload.programId.orEmpty()

            val enrollments = try {
                api.getCourseAccessEnrollments()
            } catch (_: Exception) {
                emptyList()
            }

            val enrollment = enrollments.firstOrNull { it.id == enrollmentId && enrollmentId > 0L }
                ?: enrollments.firstOrNull { item ->
                    txProgramId.isNotBlank() &&
                        item.programId.equals(txProgramId, ignoreCase = true) && (
                        (txPhone.isNotBlank() && CourseAccessEnrollment.normalizeBdPhone(item.effectivePhone) == txPhone) ||
                            (txAccountKey.isNotBlank() && CourseAccessEnrollment.normalizeBdPhone(item.effectiveAccountKey) == txAccountKey)
                        )
                }

            if (enrollment != null) {
                if (payload.approve) {
                    val newPaid = enrollment.paidAmount + (targetTx?.amount ?: 0)
                    val newDue = (enrollment.totalPrice - newPaid).coerceAtLeast(0)
                    val newCompleted = enrollment.completedInstallments + 1
                    val totalInst = targetTx?.totalInstallments?.takeIf { it > 0 } ?: enrollment.totalInstallments.coerceAtLeast(1)
                    val intervalDays = payload.customNextDueDays
                        ?: targetTx?.intervalDays?.takeIf { it > 0 }
                        ?: enrollment.installmentIntervalDays.takeIf { it > 0 }
                        ?: 7

                    val nextDueIso = if (newDue > 0) {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, intervalDays)
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(cal.time)
                    } else {
                        null
                    }

                    val resolvedPlanId = targetTx?.planId?.takeIf { it.isNotBlank() }
                        ?: targetTx?.selectedPlanId?.takeIf { it.isNotBlank() }
                        ?: enrollment.selectedPlanId?.takeIf { it.isNotBlank() }
                        ?: "FULL"
                    val resolvedPlanTitle = targetTx?.effectivePlanTitle?.takeIf { it.isNotBlank() }
                        ?: enrollment.selectedPlanTitle?.takeIf { it.isNotBlank() }
                        ?: "এককালীন সম্পূর্ণ পেমেন্ট"

                    val remainingInstallments = (totalInst - newCompleted).coerceAtLeast(1)
                    val nextInstallmentAmount = if (newDue > 0) {
                        kotlin.math.ceil(newDue.toDouble() / remainingInstallments.toDouble()).toInt()
                    } else {
                        0
                    }

                    val enrollPatch = mutableMapOf<String, Any?>(
                        "status" to "ACTIVE",
                        "access_status" to "ACTIVE",
                        "paid_amount" to newPaid,
                        "due_amount" to newDue,
                        "completed_installments" to newCompleted,
                        "total_installments" to totalInst,
                        "selected_plan_id" to resolvedPlanId,
                        "selected_plan_title" to resolvedPlanTitle,
                        "active_plan_id" to resolvedPlanId,
                        "active_plan_title" to resolvedPlanTitle,
                        "next_installment_amount" to nextInstallmentAmount,
                        "next_due_date" to nextDueIso,
                        "next_installment_due_at" to nextDueIso,
                        "pending_installment_tx_id" to "",
                        "pending_installment_amount" to 0,
                        "rejection_reason" to "",
                        "unlocked_at" to (enrollment.unlockedAt?.takeIf { it.isNotBlank() } ?: nowIso),
                        "updated_at" to nowIso
                    )
                    if (targetTx != null) {
                        enrollPatch["last_transaction_id"] = targetTx.transactionId ?: ""
                        enrollPatch["last_payment_amount"] = targetTx.amount
                        enrollPatch["last_sender_mobile"] = targetTx.senderMobile ?: ""
                        enrollPatch["last_payment_method"] = targetTx.paymentMethod ?: "bKash"
                    }
                    val validPhone = CourseAccessEnrollment.normalizeBdPhone(enrollment.effectivePhone)
                        .takeIf { CourseAccessEnrollment.isValidBdPhone(it) }
                        ?: txPhone.takeIf { CourseAccessEnrollment.isValidBdPhone(it) }
                    if (!validPhone.isNullOrBlank()) {
                        enrollPatch["student_phone"] = validPhone
                        enrollPatch["user_phone"] = validPhone
                        enrollPatch["shikho_account_key"] = validPhone
                        enrollPatch["student_key"] = validPhone
                    }

                    patchEnrollmentByIdOrAccount(
                        enrollmentId = enrollment.id,
                        studentPhone = enrollment.effectivePhone.ifBlank { txPhone },
                        shikhoAccountKey = enrollment.effectiveAccountKey.ifBlank { txAccountKey },
                        programId = enrollment.programId ?: txProgramId,
                        patchMap = enrollPatch,
                        existingEnrollment = enrollment,
                        transaction = targetTx
                    )
                } else {
                    val nowMillis = System.currentTimeMillis()
                    val dueIso = enrollment.nextDueDate?.takeIf { it.isNotBlank() } ?: enrollment.nextInstallmentDueAt
                    val dueMillis = CourseAccessEnrollment.parseIsoTimestampToMillis(dueIso)
                    val rejectStatus = when {
                        enrollment.paidAmount > 0 && (dueMillis == null || dueMillis > nowMillis) -> "ACTIVE"
                        enrollment.paidAmount > 0 && dueMillis != null && dueMillis <= nowMillis -> "OVERDUE"
                        enrollment.isTrialActiveStatus && enrollment.remainingTrialMillis() > 0L -> "TRIAL_ACTIVE"
                        else -> "PAYMENT_REJECTED"
                    }
                    val rejectPatch = mapOf<String, Any?>(
                        "status" to rejectStatus,
                        "access_status" to rejectStatus,
                        "rejection_reason" to payload.rejectionReason,
                        "pending_installment_tx_id" to "",
                        "pending_installment_amount" to 0,
                        "updated_at" to nowIso
                    )
                    patchEnrollmentByIdOrAccount(
                        enrollmentId = enrollment.id,
                        studentPhone = enrollment.effectivePhone.ifBlank { txPhone },
                        shikhoAccountKey = enrollment.effectiveAccountKey.ifBlank { txAccountKey },
                        programId = enrollment.programId ?: txProgramId,
                        patchMap = rejectPatch,
                        existingEnrollment = enrollment,
                        transaction = targetTx
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enforceOverdueCourseInstallments(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val rpcSuccess = try {
                val response = api.enforceOverdueCourseInstallmentsRpc()
                isJsonRpcSuccess(response)
            } catch (_: Exception) {
                false
            }
            if (!rpcSuccess) {
                val nowMillis = System.currentTimeMillis()
                val nowIso = formatUtcIso(nowMillis)
                val allEnrollments = try {
                    api.getCourseAccessEnrollments()
                } catch (_: Exception) {
                    emptyList()
                }
                for (enrollment in allEnrollments) {
                    val statusUpper = enrollment.effectiveStatus.uppercase(Locale.US)
                    val trialEndMillis = enrollment.parseTrialEndsAtMillis()
                    val dueIso = enrollment.nextDueDate?.takeIf { it.isNotBlank() } ?: enrollment.nextInstallmentDueAt
                    val dueMillis = CourseAccessEnrollment.parseIsoTimestampToMillis(dueIso)
                    if (statusUpper in setOf("TRIAL_ACTIVE", "FREE_TRIAL") && trialEndMillis != null && trialEndMillis <= nowMillis) {
                        val expiredStatus = if (enrollment.paidAmount > 0) "ACTIVE" else "APPROVED_TO_BUY"
                        patchEnrollmentByIdOrAccount(
                            enrollmentId = enrollment.id,
                            studentPhone = enrollment.studentPhone,
                            shikhoAccountKey = enrollment.shikhoAccountKey,
                            programId = enrollment.programId,
                            patchMap = mapOf(
                                "status" to expiredStatus,
                                "access_status" to expiredStatus,
                                "is_trial_used" to true,
                                "trial_used" to true,
                                "updated_at" to nowIso
                            )
                        )
                    } else if (
                        statusUpper in setOf("ACTIVE", "APPROVED", "INSTALLMENT_ACTIVE", "PARTIAL_PAID") &&
                        enrollment.dueAmount > 0 &&
                        dueMillis != null &&
                        dueMillis <= nowMillis
                    ) {
                        patchEnrollmentByIdOrAccount(
                            enrollmentId = enrollment.id,
                            studentPhone = enrollment.studentPhone,
                            shikhoAccountKey = enrollment.shikhoAccountKey,
                            programId = enrollment.programId,
                            patchMap = mapOf(
                                "status" to "OVERDUE",
                                "access_status" to "OVERDUE",
                                "updated_at" to nowIso
                            )
                        )
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEnrollmentStatusOrDueDate(
        enrollmentId: Long,
        studentPhone: String?,
        shikhoAccountKey: String?,
        programId: String?,
        newStatus: String?,
        newNextDueDateIso: String?,
        adminNote: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            val patchMap = mutableMapOf<String, Any?>(
                "updated_at" to nowIso
            )
            if (!newStatus.isNullOrBlank()) {
                patchMap["status"] = newStatus
                patchMap["access_status"] = newStatus
                if (newStatus.equals("ACTIVE", ignoreCase = true)) {
                    patchMap["unlocked_at"] = nowIso
                    patchMap["approved_at"] = nowIso
                } else if (newStatus.equals("REQUESTED", ignoreCase = true)) {
                    patchMap["approved_at"] = null
                    patchMap["unlocked_at"] = null
                    patchMap["total_price"] = 0
                    patchMap["total_fee"] = 0
                    patchMap["due_amount"] = 0
                    patchMap["next_installment_amount"] = 0
                    patchMap["allow_installments"] = false
                    patchMap["installment_enabled"] = false
                    patchMap["installment_plans_json"] = emptyList<Any>()
                    patchMap["installment_plans"] = emptyList<Any>()
                    patchMap["trial_enabled"] = false
                    patchMap["trial_started_at"] = null
                    patchMap["trial_ends_at"] = null
                } else if (newStatus.equals("REJECTED", ignoreCase = true) || newStatus.equals("LOCKED", ignoreCase = true)) {
                    patchMap["unlocked_at"] = null
                    patchMap["trial_enabled"] = false
                    patchMap["trial_Ends_at".lowercase()] = null
                }
            }
            if (!newNextDueDateIso.isNullOrBlank()) {
                patchMap["next_due_date"] = newNextDueDateIso
                patchMap["next_installment_due_at"] = newNextDueDateIso
            }
            if (adminNote != null) {
                patchMap["admin_note"] = adminNote
            }

            val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(studentPhone)
            if (CourseAccessEnrollment.isValidBdPhone(cleanPhone)) {
                patchMap["student_phone"] = cleanPhone
                patchMap["user_phone"] = cleanPhone
                patchMap["shikho_account_key"] = CourseAccessEnrollment.normalizeBdPhone(shikhoAccountKey).ifBlank { cleanPhone }
                patchMap["student_key"] = CourseAccessEnrollment.normalizeBdPhone(shikhoAccountKey).ifBlank { cleanPhone }
            }

            val response = patchEnrollmentByIdOrAccount(
                enrollmentId = enrollmentId,
                studentPhone = studentPhone,
                shikhoAccountKey = shikhoAccountKey,
                programId = programId,
                patchMap = patchMap
            )
            if (response?.isSuccessful == true) {
                Result.success(Unit)
            } else {
                val errBody = try {
                    response?.errorBody()?.string().orEmpty()
                } catch (_: Exception) {
                    ""
                }
                Result.failure(
                    Exception(
                        "এনরোলমেন্ট আপডেট ব্যর্থ (HTTP ${response?.code() ?: 400})${if (errBody.isNotBlank()) ": $errBody" else ""}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCourseAccessEnrollment(
        enrollmentId: Long,
        studentPhone: String?,
        programId: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            var deleted = false
            // 1. Direct REST DELETE first (`course_access_enrollments` has RLS ALL = true)
            if (enrollmentId > 0L) {
                try {
                    val delRes = api.deleteCourseAccessEnrollment("eq.$enrollmentId")
                    if (delRes.isSuccessful) deleted = true
                } catch (_: Exception) {
                }
            }
            // 2. Fallback to optional helper RPC if installed
            if (!deleted) {
                try {
                    val rpcRes = api.adminDeleteCourseEnrollmentRpc(
                        mapOf(
                            "p_enrollment_id" to enrollmentId,
                            "p_student_phone" to CourseAccessEnrollment.normalizeBdPhone(studentPhone),
                            "p_program_id" to programId?.trim().orEmpty()
                        )
                    )
                    if (rpcRes.isSuccessful) {
                        deleted = true
                    }
                } catch (_: Exception) {
                }
            }
            if (deleted) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("কোর্স এনরোলমেন্ট ডিলিট করা যায়নি"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchOneSignalPublicAppId(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            var extracted = ""

            // 1. Primary: Call `get_onesignal_public_app_id()` RPC (returns JSONB `{"onesignal_app_id": "..."}`)
            try {
                val response = api.getOneSignalPublicAppIdRpc()
                if (response.isSuccessful) {
                    val raw = response.body()?.string()?.trim().orEmpty()
                    if (raw.isNotEmpty() && !raw.equals("null", ignoreCase = true)) {
                        extracted = when {
                            raw.startsWith("\"") && raw.endsWith("\"") -> raw.removeSurrounding("\"").trim()
                            raw.startsWith("{") -> {
                                val obj = org.json.JSONObject(raw)
                                obj.optString("onesignal_app_id")
                                    .ifBlank { obj.optString("app_id") }
                                    .ifBlank { obj.optString("value") }
                                    .trim()
                            }
                            raw.startsWith("[") -> {
                                val arr = org.json.JSONArray(raw)
                                if (arr.length() > 0) {
                                    val first = arr.opt(0)
                                    when (first) {
                                        is org.json.JSONObject -> first.optString("onesignal_app_id")
                                            .ifBlank { first.optString("app_id") }
                                            .ifBlank { first.optString("value") }
                                            .trim()
                                        is String -> first.trim()
                                        else -> ""
                                    }
                                } else {
                                    ""
                                }
                            }
                            else -> raw.removeSurrounding("\"").trim()
                        }
                    }
                }
            } catch (_: Exception) {
            }

            // 2. Fallback: Query `rest/v1/onesignal_config` directly (live DB has RLS `ALL = true`)
            if (extracted.isBlank()) {
                try {
                    val tableRes = api.getOneSignalConfigRaw()
                    if (tableRes.isSuccessful) {
                        val rawJson = tableRes.body()?.string()?.trim().orEmpty()
                        if (rawJson.startsWith("[")) {
                            val arr = org.json.JSONArray(rawJson)
                            if (arr.length() > 0) {
                                val firstObj = arr.optJSONObject(0)
                                val fromTable = firstObj?.optString("onesignal_app_id")?.trim().orEmpty()
                                if (fromTable.isNotBlank()) {
                                    extracted = fromTable
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }

            if (extracted.isNotBlank()) {
                adminPreferences.setOneSignalAppId(extracted)
                Result.success(extracted)
            } else {
                Result.failure(Exception("OneSignal App ID not found in Supabase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOneSignalConfigSecure(
        appId: String,
        restApiKey: String,
        isEnabled: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val cleanAppId = appId.trim()
            val cleanRestKey = restApiKey.trim()
            val nowIso = formatUtcIso(System.currentTimeMillis())

            var syncedToTable = false
            var lastError = ""

            // 1. Primary: Call `public.update_onesignal_config_secure(p_app_id, p_rest_api_key)` (SECURITY DEFINER)
            // Required because `public.onesignal_config` has RLS blocking direct anon access (`USING (false)`).
            try {
                val primaryRpcPayload = mapOf<String, Any?>(
                    "p_app_id" to cleanAppId,
                    "p_rest_api_key" to cleanRestKey
                )
                val rpcRes = api.updateOneSignalConfigSecureRpc(primaryRpcPayload)
                if (rpcRes.isSuccessful) {
                    syncedToTable = true
                } else {
                    lastError = try { rpcRes.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                }
            } catch (e: Exception) {
                lastError = e.message.orEmpty()
            }

            // 2. Secondary fallback: alternate RPC parameter names if a custom RPC signature was used
            if (!syncedToTable) {
                try {
                    val altRpcPayload = mapOf<String, Any?>(
                        "p_onesignal_app_id" to cleanAppId,
                        "p_onesignal_rest_api_key" to cleanRestKey,
                        "p_is_enabled" to isEnabled
                    )
                    val altRes = api.updateOneSignalConfigSecureRpc(altRpcPayload)
                    if (altRes.isSuccessful) {
                        syncedToTable = true
                    }
                } catch (_: Exception) {
                }
            }

            // 3. Tertiary fallback: Direct upsert/patch on `public.onesignal_config` (`id`, `onesignal_app_id`, `onesignal_rest_api_key`, `updated_at`)
            if (!syncedToTable) {
                val tablePayload = mutableMapOf<String, Any?>(
                    "id" to 1,
                    "onesignal_app_id" to cleanAppId,
                    "updated_at" to nowIso
                )
                if (cleanRestKey.isNotBlank()) {
                    tablePayload["onesignal_rest_api_key"] = cleanRestKey
                }

                try {
                    val patchRes = api.patchOneSignalConfigRaw(idQuery = "eq.1", payload = tablePayload)
                    if (patchRes.isSuccessful) {
                        syncedToTable = true
                    }
                } catch (_: Exception) {
                }

                if (!syncedToTable) {
                    try {
                        val upsertPayload = mapOf<String, Any?>(
                            "id" to 1,
                            "onesignal_app_id" to cleanAppId,
                            "onesignal_rest_api_key" to cleanRestKey,
                            "updated_at" to nowIso
                        )
                        val upsertRes = api.upsertOneSignalConfigRaw(payload = upsertPayload)
                        if (upsertRes.isSuccessful) {
                            syncedToTable = true
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            if (!syncedToTable) {
                return@withContext Result.failure(
                    Exception("OneSignal config sync failed: ${lastError.ifBlank { "RPC update_onesignal_config_secure failed" }}")
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun upsertAdminPushDevice(
        adminDeviceId: String,
        oneSignalPlayerId: String,
        fcmToken: String,
        adminLabel: String,
        isActive: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val payload = mutableMapOf<String, Any?>(
                "admin_device_id" to adminDeviceId.trim(),
                "onesignal_player_id" to oneSignalPlayerId.trim(),
                "fcm_token" to fcmToken.trim(),
                "admin_label" to adminLabel.trim().ifBlank { "Admin Android Device" },
                "is_active" to isActive
            )
            val response = api.upsertAdminPushDevice(
                onConflict = "admin_device_id",
                payload = payload,
                prefer = "resolution=merge-duplicates,return=representation"
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = try { response.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                Result.failure(Exception("HTTP ${response.code()}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun notifyStudentViaOneSignal(
        oneSignalPlayerId: String,
        deviceHash: String,
        studentPhone: String,
        title: String,
        message: String,
        eventType: String,
        extraData: Map<String, Any?>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val payload = mapOf<String, Any?>(
                "p_onesignal_player_id" to oneSignalPlayerId.trim(),
                "p_device_hash" to deviceHash.trim(),
                "p_student_phone" to CourseAccessEnrollment.normalizeBdPhone(studentPhone).ifBlank { studentPhone.trim() },
                "p_title" to title.trim(),
                "p_message" to message.trim(),
                "p_event_type" to eventType.trim(),
                "p_extra_data" to extraData
            )
            // 1. Call `notify_student_via_onesignal` first (exists in live DB `2_ALL_FUNCTIONS_AND_FULL_LOGIC`)
            try {
                val response = api.notifyStudentViaOneSignalRpc(payload)
                if (response.isSuccessful) {
                    return@withContext Result.success(Unit)
                }
            } catch (_: Exception) {
            }

            // 2. Fallback to `send_student_push_via_onesignal` if configured
            val sendRes = api.sendStudentPushViaOneSignalRpc(payload)
            if (sendRes.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = try { sendRes.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                Result.failure(Exception("notify_student_via_onesignal HTTP ${sendRes.code()}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun notifyAdminViaOneSignal(
        title: String,
        message: String,
        eventType: String,
        extraData: Map<String, Any?>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val payload = mapOf<String, Any?>(
                "p_title" to title.trim(),
                "p_message" to message.trim(),
                "p_event_type" to eventType.trim(),
                "p_extra_data" to extraData
            )
            val response = api.notifyAdminViaOneSignalRpc(payload)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = try { response.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                Result.failure(Exception("notify_admin_via_onesignal HTTP ${response.code()}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

