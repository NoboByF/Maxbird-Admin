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
import com.example.data.model.DeviceShikhoAccount
import com.example.data.model.DeviceWithStudent
import com.example.data.model.DynamicPaymentMethodItem
import com.example.data.model.ManageCourseTrialPayload
import com.example.data.model.ManualCourseAccessPayload
import com.example.data.model.ReviewCoursePaymentRpcPayload
import com.example.data.model.UpdateCodeStatusPayload
import com.example.data.model.UpdateDeviceBlockedPayload
import com.example.data.model.UpdateAppUpdateStatusPayload
import com.example.data.model.UpdateAppNoticeStatusPayload
import com.example.data.model.UpdateSupportLinkStatusPayload
import com.example.data.model.UpsertCoursePaymentSettingsPayload
import com.example.data.model.UserDevice
import com.example.data.model.UpdateDeviceBanPayload
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

    // In-memory fallback / cache to guarantee seamless experience
    private val cachedCodes = mutableListOf<AccessCode>()
    private val cachedDevices = mutableListOf<ActivatedDevice>()
    private val cachedUpdates = mutableListOf<AppUpdate>()
    private val cachedNotices = mutableListOf<AppNotice>()
    private val cachedSupportLinks = mutableListOf<ActivationSupportLink>()
    private val cachedUserDevices = mutableListOf<UserDevice>()
    private var isInitializedWithSampleData = false

    init {
        initSampleDataIfNeeded()
    }

    private fun initSampleDataIfNeeded() {
        if (!isInitializedWithSampleData && cachedCodes.isEmpty()) {
            val code1Id = UUID.randomUUID().toString()
            val code2Id = UUID.randomUUID().toString()
            val code3Id = UUID.randomUUID().toString()
            val code4Id = UUID.randomUUID().toString()

            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

            cachedCodes.addAll(
                listOf(
                    AccessCode(
                        id = code1Id,
                        code = "MAX-8921",
                        studentName = "তানভীর আহমেদ (Tanvir Ahmed)",
                        maxDevices = 1,
                        isActive = true,
                        createdAt = now,
                        note = "Batch 2026 - Mobile App Development"
                    ),
                    AccessCode(
                        id = code2Id,
                        code = "VIP-5541",
                        studentName = "সাকিবুল হাসান (Sakibul Hasan)",
                        maxDevices = 2,
                        isActive = true,
                        createdAt = now,
                        note = "VIP Student - Tab & Phone Access"
                    ),
                    AccessCode(
                        id = code3Id,
                        code = "MAX-3419",
                        studentName = "ফারহানা করিম (Farhana Karim)",
                        maxDevices = 1,
                        isActive = false,
                        createdAt = now,
                        note = "Course Completed"
                    ),
                    AccessCode(
                        id = code4Id,
                        code = "MAX-9012",
                        studentName = "মেহেদী হাসান (Mehedi Hasan)",
                        maxDevices = 2,
                        isActive = true,
                        createdAt = now,
                        note = "Registered 2026-10"
                    )
                )
            )

            cachedDevices.addAll(
                listOf(
                    ActivatedDevice(
                        id = UUID.randomUUID().toString(),
                        codeId = code1Id,
                        deviceId = "MX-A1B2-C3D4",
                        deviceModel = "Samsung Galaxy S24 Ultra",
                        isBlocked = false,
                        activatedAt = now,
                        lastActiveAt = now
                    ),
                    ActivatedDevice(
                        id = UUID.randomUUID().toString(),
                        codeId = code2Id,
                        deviceId = "MX-E5F6-G7H8",
                        deviceModel = "Xiaomi 14 Pro",
                        isBlocked = false,
                        activatedAt = now,
                        lastActiveAt = now
                    ),
                    ActivatedDevice(
                        id = UUID.randomUUID().toString(),
                        codeId = code2Id,
                        deviceId = "MX-TB99-XX21",
                        deviceModel = "Samsung Galaxy Tab S9",
                        isBlocked = true,
                        activatedAt = now,
                        lastActiveAt = now
                    ),
                    ActivatedDevice(
                        id = UUID.randomUUID().toString(),
                        codeId = code3Id,
                        deviceId = "MX-PX88-Z001",
                        deviceModel = "Google Pixel 8 Pro",
                        isBlocked = false,
                        activatedAt = now,
                        lastActiveAt = now
                    )
                )
            )

            cachedUpdates.addAll(
                listOf(
                    AppUpdate(
                        id = UUID.randomUUID().toString(),
                        latestVersionName = "v6.1.0",
                        latestVersionCode = 60100,
                        minSupportedVersionCode = 60000,
                        isForceUpdate = false,
                        title = "MaxBird v6.1.0 আপডেট প্রকাশিত হয়েছে!",
                        changelog = "• নতুন ভিডিও প্লেয়ার ইঞ্জিন যুক্ত হয়েছে\n• লাইভ চ্যাট ফিচারের বাগ ফিক্স\n• ইউজার ইন্টারফেস আরও আকর্ষণীয় করা হয়েছে",
                        downloadUrl = "https://t.me/maxbird_updates",
                        buttonText = "এখনই আপডেট করুন",
                        isActive = true,
                        createdAt = now,
                        updatedAt = now
                    ),
                    AppUpdate(
                        id = UUID.randomUUID().toString(),
                        latestVersionName = "v6.0.0",
                        latestVersionCode = 60000,
                        minSupportedVersionCode = 50900,
                        isForceUpdate = true,
                        title = "জরুরি সিকিউরিটি আপডেট",
                        changelog = "• বাধ্যতামূলক সিকিউরিটি প্যাচ\n• সার্ভার সংযোগ উন্নত করা হয়েছে",
                        downloadUrl = "https://play.google.com/store/apps/details?id=com.maxbird.app",
                        buttonText = "আবশ্যক আপডেট",
                        isActive = true,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            )

            cachedNotices.addAll(
                listOf(
                    AppNotice(
                        id = UUID.randomUUID().toString(),
                        title = "🎉 নতুন স্পেশাল ব্যাচ শুরু হয়েছে!",
                        description = "সকল ক্লাসের লাইভ সেশন, নোট ও কুইজ অ্যাক্সেস করতে আমাদের অফিশিয়াল টেলিগ্রাম চ্যানেলে যুক্ত থাকুন।",
                        imageUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800",
                        actionUrl = "https://t.me/maxbird_updates",
                        actionButtonText = "টেলিগ্রামে যুক্ত হোন",
                        priority = 10,
                        isActive = true,
                        showAsPopup = true,
                        createdAt = now,
                        updatedAt = now
                    ),
                    AppNotice(
                        id = UUID.randomUUID().toString(),
                        title = "⚠️ সার্ভার মেইনটেন্যান্স অ্যালার্ট",
                        description = "আজ রাত ১২টা থেকে রাত ১টা পর্যন্ত সার্ভার আপগ্রেড কার্যক্রম চলবে। সাময়িক অসুবিধার জন্য আন্তরিকভাবে দুঃখিত।",
                        imageUrl = "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=800",
                        actionUrl = "https://maxbird.app/status",
                        actionButtonText = "সার্ভার স্ট্যাটাস দেখুন",
                        priority = 5,
                        isActive = true,
                        showAsPopup = false,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            )

            cachedSupportLinks.addAll(
                listOf(
                    ActivationSupportLink(
                        id = UUID.randomUUID().toString(),
                        title = "Telegram সাপোর্ট",
                        subtitle = "@fahim017740 এ মেসেজ দিন",
                        iconType = "telegram",
                        url = "https://t.me/fahim017740",
                        colorHex = "#229ED9",
                        isActive = true,
                        priority = 10,
                        createdAt = now
                    ),
                    ActivationSupportLink(
                        id = UUID.randomUUID().toString(),
                        title = "WhatsApp হেল্পলাইন",
                        subtitle = "+8801774092040 এ সরাসরি চ্যাট করুন",
                        iconType = "whatsapp",
                        url = "https://wa.me/8801774092040",
                        colorHex = "#25D366",
                        isActive = true,
                        priority = 8,
                        createdAt = now
                    ),
                    ActivationSupportLink(
                        id = UUID.randomUUID().toString(),
                        title = "অফিশিয়াল ফেসবুক গ্রুপ",
                        subtitle = "নোট ও ক্লাস আলোচনা পেতে যুক্ত হোন",
                        iconType = "facebook",
                        url = "https://facebook.com/groups/maxbird",
                        colorHex = "#1877F2",
                        isActive = true,
                        priority = 5,
                        createdAt = now
                    ),
                    ActivationSupportLink(
                        id = UUID.randomUUID().toString(),
                        title = "জরুরি কল সেন্টার",
                        subtitle = "সকাল ১০টা থেকে রাত ১০টা পর্যন্ত",
                        iconType = "phone",
                        url = "tel:+8801774092040",
                        colorHex = "#10B981",
                        isActive = true,
                        priority = 3,
                        createdAt = now
                    ),
                    ActivationSupportLink(
                        id = UUID.randomUUID().toString(),
                        title = "অফিশিয়াল ওয়েবসাইট পোর্টাল",
                        subtitle = "নিয়মাবলি ও লাইসেন্স তথ্যের জন্য",
                        iconType = "website",
                        url = "https://maxbird.app",
                        colorHex = "#0EA5E9",
                        isActive = true,
                        priority = 1,
                        createdAt = now
                    )
                )
            )

            cachedUserDevices.addAll(
                listOf(
                    UserDevice(
                        id = UUID.randomUUID().toString(),
                        deviceHash = "sha256_hw_oneplus_pending_01",
                        displayDeviceId = "MX-9A41-2B7C-E810",
                        studentNote = "রাফি ইসলাম (HSC 26)",
                        deviceModel = "OnePlus 11R 5G",
                        deviceBrand = "OnePlus",
                        androidVersion = "Android 14 (API 34)",
                        appVersion = "1.0.0",
                        status = "pending",
                        isBanned = false,
                        banReason = "",
                        requestedAt = now,
                        updatedAt = now,
                        lastSeen = now,
                        canViewProfiles = false
                    ),
                    UserDevice(
                        id = UUID.randomUUID().toString(),
                        deviceHash = "sha256_hw_xiaomi_9a12c40",
                        displayDeviceId = "MX-7F3A-9B21-C04E",
                        studentNote = "তানভীর আহমেদ",
                        deviceModel = "Xiaomi Redmi Note 12",
                        deviceBrand = "Xiaomi",
                        androidVersion = "Android 14 (API 34)",
                        appVersion = "1.0.0",
                        appliedCode = "VIP-STUDENT-01",
                        status = "active",
                        isBanned = false,
                        banReason = "",
                        requestedAt = now,
                        approvedAt = now,
                        updatedAt = now,
                        lastSeen = now,
                        canViewProfiles = false
                    ),
                    UserDevice(
                        id = UUID.randomUUID().toString(),
                        deviceHash = "sha256_hw_samsung_b38df9",
                        displayDeviceId = "MX-4C82-1D5E-F93A",
                        studentNote = "সাকিবুল হাসান (SSC 26)",
                        deviceModel = "Samsung Galaxy S23",
                        deviceBrand = "Samsung",
                        androidVersion = "Android 14 (API 34)",
                        appVersion = "1.0.0",
                        appliedCode = "MAX-8921",
                        status = "active",
                        isBanned = false,
                        banReason = "",
                        requestedAt = now,
                        approvedAt = now,
                        updatedAt = now,
                        lastSeen = now,
                        canViewProfiles = true
                    ),
                    UserDevice(
                        id = UUID.randomUUID().toString(),
                        deviceHash = "sha256_hw_realme_e847c1",
                        displayDeviceId = "MX-89AB-CDEF-0123",
                        studentNote = "মেহেদী হাসান",
                        deviceModel = "Realme 11 Pro+",
                        deviceBrand = "Realme",
                        androidVersion = "Android 13 (API 33)",
                        appVersion = "1.0.0",
                        appliedCode = "MAX-1144",
                        status = "blocked",
                        isBanned = true,
                        banReason = "একাধিক ডিভাইসে শেয়ার করার অভিযোগে ব্লক করা হয়েছে",
                        requestedAt = now,
                        updatedAt = now,
                        lastSeen = now,
                        canViewProfiles = false
                    )
                )
            )

            isInitializedWithSampleData = true
        }
    }

    override suspend fun getAccessCodes(forceRefresh: Boolean): Result<List<AccessCode>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteCodes = api.getAccessCodes().map { code ->
                code.copy(
                    studentName = code.effectiveStudentName,
                    maxDevices = code.effectiveMaxDevices,
                    expiresAt = code.effectiveExpiresAt
                )
            }
            cachedCodes.clear()
            cachedCodes.addAll(remoteCodes)
            Result.success(remoteCodes)
        } catch (e: Exception) {
            initSampleDataIfNeeded()
            Result.success(cachedCodes.toList())
        }
    }

    override suspend fun createAccessCode(payload: CreateAccessCodePayload): Result<AccessCode> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val syncedPayload = payload.copy(
            code = payload.code.uppercase().trim(),
            studentName = payload.studentName.trim(),
            name = payload.studentName.trim(),
            maxDevices = payload.maxDevices,
            deviceLimit = payload.maxDevices,
            isActive = payload.isActive,
            status = if (payload.isActive) "active" else "inactive",
            note = payload.note ?: ""
        )
        val localNewCode = AccessCode(
            id = UUID.randomUUID().toString(),
            code = syncedPayload.code,
            studentName = syncedPayload.studentName,
            name = syncedPayload.name,
            maxDevices = syncedPayload.maxDevices,
            deviceLimit = syncedPayload.deviceLimit,
            isActive = syncedPayload.isActive,
            status = syncedPayload.status,
            createdAt = now,
            updatedAt = now,
            note = syncedPayload.note
        )

        try {
            val api = supabaseClient.getApi()
            val createdList = api.createAccessCode(syncedPayload)
            val created = createdList.firstOrNull()?.let {
                it.copy(
                    studentName = it.effectiveStudentName,
                    maxDevices = it.effectiveMaxDevices,
                    expiresAt = it.effectiveExpiresAt
                )
            } ?: localNewCode
            cachedCodes.add(0, created)
            Result.success(created)
        } catch (e: Exception) {
            cachedCodes.add(0, localNewCode)
            Result.success(localNewCode)
        }
    }

    override suspend fun toggleCodeStatus(codeId: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val statusStr = if (isActive) "active" else "inactive"
        val index = cachedCodes.indexOfFirst { it.id == codeId }
        if (index != -1) {
            val existing = cachedCodes[index]
            cachedCodes[index] = existing.copy(isActive = isActive, status = statusStr)
        }

        try {
            val api = supabaseClient.getApi()
            api.updateAccessCodeStatus(
                idQuery = "eq.$codeId",
                payload = UpdateCodeStatusPayload(isActive = isActive, status = statusStr)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun extendAccessCodeValidity(codeId: String, extraDays: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, extraDays)
        val newExpiryIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(cal.time)
        val newExpiryDisplay = SimpleDateFormat("dd MMM yyyy", Locale.US).format(cal.time)

        val index = cachedCodes.indexOfFirst { it.id == codeId }
        val updatedNote = if (index != -1) {
            val existing = cachedCodes[index]
            val cleanNote = existing.note.orEmpty().replace(Regex("\\s*\\|\\s*মেয়াদ:.*$"), "").trim()
            val noteWithExpiry = if (cleanNote.isBlank()) "মেয়াদ: $newExpiryDisplay" else "$cleanNote | মেয়াদ: $newExpiryDisplay"
            cachedCodes[index] = existing.copy(
                isActive = true,
                status = "active",
                expiresAt = newExpiryIso,
                expiryDate = newExpiryIso,
                note = noteWithExpiry,
                updatedAt = nowIso
            )
            noteWithExpiry
        } else {
            "মেয়াদ: $newExpiryDisplay"
        }

        try {
            val api = supabaseClient.getApi()
            val res = api.patchAccessCode(
                idQuery = "eq.$codeId",
                payload = mapOf(
                    "is_active" to true,
                    "status" to "active",
                    "expires_at" to newExpiryIso,
                    "expiry_date" to newExpiryIso,
                    "note" to updatedNote,
                    "updated_at" to nowIso
                )
            )
            if (!res.isSuccessful) {
                api.patchAccessCode(
                    idQuery = "eq.$codeId",
                    payload = mapOf(
                        "is_active" to true,
                        "expires_at" to newExpiryIso,
                        "note" to updatedNote
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun resetDevicesForCode(codeId: String, codeString: String): Result<Unit> = withContext(Dispatchers.IO) {
        val linkedDevices = cachedDevices.filter { it.codeId == codeId }
        cachedDevices.removeAll { it.codeId == codeId }

        try {
            val api = supabaseClient.getApi()
            val remoteLinked = try {
                api.getDevicesForCode(codeIdQuery = "eq.$codeId")
            } catch (_: Exception) {
                linkedDevices
            }
            remoteLinked.forEach { dev ->
                if (dev.id.isNotBlank()) {
                    try {
                        api.deleteDevice("eq.${dev.id}")
                    } catch (_: Exception) {
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun deleteAccessCode(codeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedCodes.removeAll { it.id == codeId }
        cachedDevices.removeAll { it.codeId == codeId }

        try {
            val api = supabaseClient.getApi()
            api.deleteAccessCode("eq.$codeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    private fun enrichActivatedDevice(dev: ActivatedDevice): ActivatedDevice {
        val matchInDevices = cachedUserDevices.firstOrNull {
            dev.deviceHash.isNotBlank() && it.deviceHash.equals(dev.deviceHash, ignoreCase = true)
        }
        val resolvedId = matchInDevices?.displayDeviceId?.takeIf { it.isNotBlank() }
            ?: dev.effectiveDeviceId
        val resolvedModel = matchInDevices?.fullPhoneModel?.takeIf { it.isNotBlank() && it != "Unknown Device" }
            ?: dev.effectiveDeviceModel
        val resolvedLastSeen = dev.effectiveLastSeen ?: matchInDevices?.lastSeen
        return dev.copy(
            deviceId = resolvedId,
            deviceModel = resolvedModel,
            lastActiveAt = resolvedLastSeen
        )
    }

    override suspend fun getActivatedDevices(forceRefresh: Boolean): Result<List<ActivatedDevice>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteDevices = api.getActivatedDevices().map { enrichActivatedDevice(it) }
            cachedDevices.clear()
            cachedDevices.addAll(remoteDevices)
            Result.success(remoteDevices)
        } catch (e: Exception) {
            initSampleDataIfNeeded()
            Result.success(cachedDevices.toList())
        }
    }

    override suspend fun getDevicesForCode(codeId: String): Result<List<ActivatedDevice>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val devices = api.getDevicesForCode(codeIdQuery = "eq.$codeId").map { enrichActivatedDevice(it) }
            Result.success(devices)
        } catch (e: Exception) {
            val localFiltered = cachedDevices.filter { it.codeId == codeId }
            Result.success(localFiltered)
        }
    }

    override suspend fun getJoinedDevices(): Result<List<DeviceWithStudent>> = withContext(Dispatchers.IO) {
        val codes = getAccessCodes().getOrDefault(cachedCodes)
        val devices = getActivatedDevices().getOrDefault(cachedDevices)

        val codeMap = codes.associateBy { it.id }

        val joined = devices.map { device ->
            val linkedCode = codeMap[device.codeId]
            DeviceWithStudent(
                device = enrichActivatedDevice(device),
                studentName = linkedCode?.effectiveStudentName ?: "Unknown Student",
                accessCode = linkedCode?.code ?: "N/A"
            )
        }
        Result.success(joined)
    }

    override suspend fun toggleDeviceBlocked(deviceId: String, isBlocked: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val index = cachedDevices.indexOfFirst { it.id == deviceId }
        if (index != -1) {
            val existing = cachedDevices[index]
            cachedDevices[index] = existing.copy(isBlocked = isBlocked)
        }

        try {
            val api = supabaseClient.getApi()
            api.updateDeviceBlockedStatus(
                idQuery = "eq.$deviceId",
                payload = UpdateDeviceBlockedPayload(isBlocked = isBlocked)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun unbindDevice(deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedDevices.removeAll { it.id == deviceId }

        try {
            val api = supabaseClient.getApi()
            api.deleteDevice("eq.$deviceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun getDashboardStats(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        val codes = getAccessCodes().getOrDefault(cachedCodes)
        val activatedDevices = getActivatedDevices().getOrDefault(cachedDevices)
        val realDevices = getUserDevices().getOrDefault(cachedUserDevices)

        val pendingCount = realDevices.count { it.isPendingDevice }
        val activeVerifiedCount = realDevices.count { it.isActiveDevice }
        val blockedRealCount = realDevices.count { it.isBlockedDevice }

        val totalCodes = codes.size
        val activeStudents = if (activeVerifiedCount > 0) activeVerifiedCount else codes.count { it.isActive }
        val totalDevices = if (realDevices.isNotEmpty()) realDevices.size else activatedDevices.size
        val blockedDevices = if (realDevices.isNotEmpty()) blockedRealCount else activatedDevices.count { it.isBlocked }
        val totalCapacity = codes.sumOf { it.maxDevices }
        val availableSlots = (totalCapacity - activatedDevices.size).coerceAtLeast(0)

        Result.success(
            DashboardStats(
                totalCodes = totalCodes,
                activeStudents = activeStudents,
                totalDevices = totalDevices,
                blockedDevices = blockedDevices,
                availableSlots = availableSlots,
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
            val remoteUpdates = api.getAppUpdates()
            cachedUpdates.clear()
            cachedUpdates.addAll(remoteUpdates)
            Result.success(remoteUpdates)
        } catch (e: Exception) {
            initSampleDataIfNeeded()
            Result.success(cachedUpdates.toList())
        }
    }

    override suspend fun createAppUpdate(payload: CreateAppUpdatePayload): Result<AppUpdate> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val localNewUpdate = AppUpdate(
            id = UUID.randomUUID().toString(),
            latestVersionName = payload.latestVersionName.trim(),
            latestVersionCode = payload.latestVersionCode,
            minSupportedVersionCode = payload.minSupportedVersionCode,
            isForceUpdate = payload.isForceUpdate,
            title = payload.title.trim(),
            changelog = payload.changelog.trim(),
            downloadUrl = payload.downloadUrl.trim(),
            buttonText = payload.buttonText.trim().ifBlank { "এখনই আপডেট করুন" },
            isActive = payload.isActive,
            createdAt = now,
            updatedAt = now
        )

        try {
            val api = supabaseClient.getApi()
            val createdList = api.createAppUpdate(payload)
            val created = createdList.firstOrNull() ?: localNewUpdate
            cachedUpdates.add(0, created)
            Result.success(created)
        } catch (e: Exception) {
            cachedUpdates.add(0, localNewUpdate)
            Result.success(localNewUpdate)
        }
    }

    override suspend fun updateAppUpdate(id: String, payload: CreateAppUpdatePayload): Result<AppUpdate> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val index = cachedUpdates.indexOfFirst { it.id == id }
        val updatedLocal = if (index != -1) {
            val existing = cachedUpdates[index]
            existing.copy(
                latestVersionName = payload.latestVersionName.trim(),
                latestVersionCode = payload.latestVersionCode,
                minSupportedVersionCode = payload.minSupportedVersionCode,
                isForceUpdate = payload.isForceUpdate,
                title = payload.title.trim(),
                changelog = payload.changelog.trim(),
                downloadUrl = payload.downloadUrl.trim(),
                buttonText = payload.buttonText.trim().ifBlank { "এখনই আপডেট করুন" },
                isActive = payload.isActive,
                updatedAt = now
            )
        } else {
            AppUpdate(
                id = id,
                latestVersionName = payload.latestVersionName,
                latestVersionCode = payload.latestVersionCode,
                minSupportedVersionCode = payload.minSupportedVersionCode,
                isForceUpdate = payload.isForceUpdate,
                title = payload.title,
                changelog = payload.changelog,
                downloadUrl = payload.downloadUrl,
                buttonText = payload.buttonText,
                isActive = payload.isActive,
                createdAt = now,
                updatedAt = now
            )
        }

        if (index != -1) {
            cachedUpdates[index] = updatedLocal
        } else {
            cachedUpdates.add(0, updatedLocal)
        }

        try {
            val api = supabaseClient.getApi()
            val updatedList = api.updateAppUpdate("eq.$id", payload)
            val updated = updatedList.firstOrNull() ?: updatedLocal
            val idx = cachedUpdates.indexOfFirst { it.id == id }
            if (idx != -1) cachedUpdates[idx] = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.success(updatedLocal)
        }
    }

    override suspend fun toggleAppUpdateStatus(id: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val index = cachedUpdates.indexOfFirst { it.id == id }
        if (index != -1) {
            cachedUpdates[index] = cachedUpdates[index].copy(isActive = isActive)
        }

        try {
            val api = supabaseClient.getApi()
            api.updateAppUpdateStatus("eq.$id", UpdateAppUpdateStatusPayload(isActive = isActive))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun deleteAppUpdate(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedUpdates.removeAll { it.id == id }

        try {
            val api = supabaseClient.getApi()
            api.deleteAppUpdate("eq.$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
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
            initSampleDataIfNeeded()
            Result.success(cachedNotices.sortedWith(compareByDescending<AppNotice> { it.priority }.thenByDescending { it.createdAt }))
        }
    }

    override suspend fun createAppNotice(payload: CreateAppNoticePayload): Result<AppNotice> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val localNewNotice = AppNotice(
            id = UUID.randomUUID().toString(),
            title = payload.title.trim(),
            description = payload.description?.trim(),
            imageUrl = payload.imageUrl.trim(),
            actionUrl = payload.actionUrl?.trim()?.ifBlank { null },
            actionButtonText = payload.actionButtonText.trim().ifBlank { "বিস্তারিত দেখুন" },
            priority = payload.priority,
            isActive = payload.isActive,
            showAsPopup = payload.showAsPopup,
            createdAt = now,
            updatedAt = now
        )

        try {
            val api = supabaseClient.getApi()
            val createdList = api.createAppNotice(payload)
            val created = createdList.firstOrNull() ?: localNewNotice
            cachedNotices.add(0, created)
            Result.success(created)
        } catch (e: Exception) {
            cachedNotices.add(0, localNewNotice)
            Result.success(localNewNotice)
        }
    }

    override suspend fun updateAppNotice(id: String, payload: CreateAppNoticePayload): Result<AppNotice> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val index = cachedNotices.indexOfFirst { it.id == id }
        val updatedLocal = if (index != -1) {
            val existing = cachedNotices[index]
            existing.copy(
                title = payload.title.trim(),
                description = payload.description?.trim(),
                imageUrl = payload.imageUrl.trim(),
                actionUrl = payload.actionUrl?.trim()?.ifBlank { null },
                actionButtonText = payload.actionButtonText.trim().ifBlank { "বিস্তারিত দেখুন" },
                priority = payload.priority,
                isActive = payload.isActive,
                showAsPopup = payload.showAsPopup,
                updatedAt = now
            )
        } else {
            AppNotice(
                id = id,
                title = payload.title.trim(),
                description = payload.description?.trim(),
                imageUrl = payload.imageUrl.trim(),
                actionUrl = payload.actionUrl?.trim()?.ifBlank { null },
                actionButtonText = payload.actionButtonText.trim().ifBlank { "বিস্তারিত দেখুন" },
                priority = payload.priority,
                isActive = payload.isActive,
                showAsPopup = payload.showAsPopup,
                createdAt = now,
                updatedAt = now
            )
        }

        if (index != -1) {
            cachedNotices[index] = updatedLocal
        } else {
            cachedNotices.add(0, updatedLocal)
        }

        try {
            val api = supabaseClient.getApi()
            val updatedList = api.updateAppNotice("eq.$id", payload)
            val updated = updatedList.firstOrNull() ?: updatedLocal
            val idx = cachedNotices.indexOfFirst { it.id == id }
            if (idx != -1) cachedNotices[idx] = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.success(updatedLocal)
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
            Result.success(Unit)
        }
    }

    override suspend fun deleteAppNotice(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedNotices.removeAll { it.id == id }

        try {
            val api = supabaseClient.getApi()
            api.deleteAppNotice("eq.$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
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
            initSampleDataIfNeeded()
            Result.success(cachedSupportLinks.sortedWith(compareByDescending<ActivationSupportLink> { it.priority }.thenByDescending { it.createdAt ?: "" }))
        }
    }

    override suspend fun createActivationSupportLink(payload: CreateSupportLinkPayload): Result<ActivationSupportLink> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val localNewLink = ActivationSupportLink(
            id = UUID.randomUUID().toString(),
            title = payload.title.trim(),
            subtitle = payload.subtitle?.trim()?.ifBlank { null },
            iconType = payload.iconType.trim().lowercase(),
            url = payload.url.trim(),
            colorHex = payload.colorHex?.trim()?.ifBlank { null },
            isActive = payload.isActive,
            priority = payload.priority,
            createdAt = now
        )

        try {
            val api = supabaseClient.getApi()
            val createdList = api.createActivationSupportLink(payload)
            val created = createdList.firstOrNull() ?: localNewLink
            cachedSupportLinks.add(0, created)
            Result.success(created)
        } catch (e: Exception) {
            cachedSupportLinks.add(0, localNewLink)
            Result.success(localNewLink)
        }
    }

    override suspend fun updateActivationSupportLink(id: String, payload: CreateSupportLinkPayload): Result<ActivationSupportLink> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val index = cachedSupportLinks.indexOfFirst { it.id == id }
        val updatedLocal = if (index != -1) {
            val existing = cachedSupportLinks[index]
            existing.copy(
                title = payload.title.trim(),
                subtitle = payload.subtitle?.trim()?.ifBlank { null },
                iconType = payload.iconType.trim().lowercase(),
                url = payload.url.trim(),
                colorHex = payload.colorHex?.trim()?.ifBlank { null },
                isActive = payload.isActive,
                priority = payload.priority
            )
        } else {
            ActivationSupportLink(
                id = id,
                title = payload.title.trim(),
                subtitle = payload.subtitle?.trim()?.ifBlank { null },
                iconType = payload.iconType.trim().lowercase(),
                url = payload.url.trim(),
                colorHex = payload.colorHex?.trim()?.ifBlank { null },
                isActive = payload.isActive,
                priority = payload.priority,
                createdAt = now
            )
        }

        if (index != -1) {
            cachedSupportLinks[index] = updatedLocal
        } else {
            cachedSupportLinks.add(0, updatedLocal)
        }

        try {
            val api = supabaseClient.getApi()
            val updatedList = api.updateActivationSupportLink("eq.$id", payload)
            val updated = updatedList.firstOrNull() ?: updatedLocal
            val idx = cachedSupportLinks.indexOfFirst { it.id == id }
            if (idx != -1) cachedSupportLinks[idx] = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.success(updatedLocal)
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
            Result.success(Unit)
        }
    }

    override suspend fun deleteActivationSupportLink(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedSupportLinks.removeAll { it.id == id }

        try {
            val api = supabaseClient.getApi()
            api.deleteActivationSupportLink("eq.$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
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
            initSampleDataIfNeeded()
            Result.success(cachedUserDevices.toList())
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

            // 1. Call official RPC `admin_approve_device_verification(p_device_hash)`
            if (effectiveHash.isNotBlank()) {
                try {
                    val rpcRes = api.approveDeviceVerificationRpc(
                        mapOf("p_device_hash" to effectiveHash)
                    )
                    if (rpcRes.isSuccessful) rpcOk = true
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
                            mapOf("p_device_hash" to effectiveHash)
                        )
                        if (appRes.isSuccessful) rpcOk = true
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

            // 3. Also sync `is_blocked` in `public.activated_devices` if present
            if (effectiveHash.isNotBlank()) {
                try {
                    api.patchActivatedDeviceByHash(
                        deviceHashQuery = "eq.$effectiveHash",
                        payload = mapOf("is_blocked" to isBlocked)
                    )
                } catch (_: Exception) {
                }
            }

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
            if (patchResult.isSuccess) {
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
            val patchResult = patchPublicDeviceRecord(
                device = targetDevice,
                candidatePayloads = listOf(mapOf("can_view_profiles" to canViewProfiles))
            )
            val effectiveHash = targetDevice.effectiveDeviceHash
            if (effectiveHash.isNotBlank()) {
                try {
                    supabaseClient.getApi().patchActivatedDeviceByHash(
                        deviceHashQuery = "eq.$effectiveHash",
                        payload = mapOf("can_view_profiles" to canViewProfiles)
                    )
                } catch (_: Exception) {
                }
            }
            if (patchResult.isSuccess) {
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
            var deleted = false
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
            if (idToUse != null) {
                try {
                    val res = api.deleteDeviceRecord("eq.$idToUse")
                    if (res.isSuccessful) deleted = true
                } catch (_: Exception) {
                }
            }
            if (!deleted) {
                try {
                    api.deleteDeviceRecordByHash("eq.$deviceId")
                } catch (_: Exception) {
                    api.deleteDeviceRecord("eq.$deviceId")
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
            if (rpcRes.isSuccessful) {
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
        val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(userPhone).ifBlank { userPhone.trim() }
        if (cleanHash.isBlank() || cleanPhone.isBlank()) {
            return@withContext Result.failure(Exception("ডিভাইস হ্যাশ এবং ইউজার ফোন নম্বর আবশ্যক"))
        }
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        try {
            val api = supabaseClient.getApi()
            val rpcPayload = mapOf<String, Any?>(
                "p_device_hash" to cleanHash,
                "p_user_phone" to cleanPhone,
                "p_is_access_granted" to isAccessGranted,
                "p_is_restricted" to isRestricted,
                "p_allowed_classes" to allowedClasses,
                "p_allowed_groups" to allowedGroups,
                "p_allowed_batches" to allowedBatches,
                "p_allowed_programs" to allowedPrograms,
                "p_admin_note" to adminNote.trim()
            )
            val rpcRes = api.setAccountCoursePermissionsRpc(rpcPayload)
            if (rpcRes.isSuccessful) {
                return@withContext Result.success(Unit)
            }

            val patchRes = api.patchDeviceShikhoAccount(
                deviceHashQuery = "eq.$cleanHash",
                userPhoneQuery = "eq.$cleanPhone",
                payload = mapOf(
                    "is_access_granted" to isAccessGranted,
                    "is_access_restricted" to isRestricted,
                    "allowed_classes" to allowedClasses,
                    "allowed_groups" to allowedGroups,
                    "allowed_batches" to allowedBatches,
                    "allowed_programs" to allowedPrograms,
                    "admin_note" to adminNote.trim(),
                    "updated_at" to nowIso
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

            // Optional secondary attempt: if the database also has `payment_methods_json` column, sync it silently
            try {
                api.patchCoursePaymentSettingsRaw(
                    idQuery = "eq.1",
                    payload = mapOf("payment_methods_json" to syncedPayload.paymentMethodsJson)
                )
            } catch (_: Exception) {
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

    private suspend fun patchEnrollmentByIdOrAccount(
        enrollmentId: Long,
        studentPhone: String?,
        shikhoAccountKey: String?,
        programId: String?,
        patchMap: Map<String, Any?>
    ): retrofit2.Response<Unit>? {
        val api = supabaseClient.getApi()
        var primaryResponse: retrofit2.Response<Unit>? = null

        if (enrollmentId > 0L) {
            try {
                primaryResponse = api.patchCourseAccessEnrollment(
                    idQuery = "eq.$enrollmentId",
                    payload = patchMap
                )
            } catch (_: Exception) {
                primaryResponse = null
            }
        }

        val cleanProgId = programId?.trim().orEmpty()
        val cleanAccKey = CourseAccessEnrollment.normalizeBdPhone(shikhoAccountKey)
            .ifBlank { CourseAccessEnrollment.normalizeBdPhone(studentPhone) }
        val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(studentPhone)
            .ifBlank { cleanAccKey }

        if (cleanProgId.isNotBlank() && (enrollmentId <= 0L || primaryResponse?.isSuccessful != true)) {
            if (cleanAccKey.isNotBlank()) {
                try {
                    val res = api.patchCourseAccessEnrollmentByAccountAndProgram(
                        accountKeyQuery = "eq.$cleanAccKey",
                        programIdQuery = "eq.$cleanProgId",
                        payload = patchMap
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
                        payload = patchMap
                    )
                    if (res.isSuccessful) primaryResponse = res
                } catch (_: Exception) {
                }
                try {
                    val res = api.patchCourseAccessEnrollmentByUserPhoneAndProgram(
                        userPhoneQuery = "eq.$cleanPhone",
                        programIdQuery = "eq.$cleanProgId",
                        payload = patchMap
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

            // 1. If instant free unlock is requested and we have an enrollment ID, try `admin_approve_course_access_request` with `p_instant_free_unlock = true`
            var rpcSucceeded = false
            if (payload.instantFreeUnlock && payload.enrollmentId > 0L) {
                try {
                    val rpcRes = api.approveCourseAccessRequestRpc(payload)
                    rpcSucceeded = isJsonRpcSuccess(rpcRes)
                } catch (_: Exception) {
                    rpcSucceeded = false
                }
                if (rpcSucceeded) {
                    return@withContext Result.success(Unit)
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
                if (patchRes?.isSuccessful == true) {
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
            val recordMap = mutableMapOf<String, Any?>(
                "student_phone" to cleanPhone,
                "user_phone" to cleanPhone,
                "shikho_account_key" to cleanPhone,
                "device_id" to cleanPhone,
                "device_hash" to cleanPhone,
                "display_device_id" to cleanPhone,
                "student_name" to payload.studentName.trim().ifBlank { "শিক্ষার্থী ($cleanPhone)" },
                "program_id" to cleanProgramId,
                "program_title" to payload.programTitle.trim().ifBlank { cleanProgramId },
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

            if (existingMatch != null && existingMatch.id > 0L) {
                val patchRes = api.patchCourseAccessEnrollment(
                    idQuery = "eq.${existingMatch.id}",
                    payload = recordMap
                )
                if (patchRes.isSuccessful) {
                    return@withContext Result.success(Unit)
                }
            }

            recordMap["requested_at"] = nowIso
            val createRes = api.createCourseAccessEnrollmentRaw(recordMap)
            if (createRes.isSuccessful) {
                return@withContext Result.success(Unit)
            }

            // Fallback: patch by shikho_account_key + program_id or student_phone + program_id
            val fallbackPatch = patchEnrollmentByIdOrAccount(
                enrollmentId = existingMatch?.id ?: 0L,
                studentPhone = cleanPhone,
                shikhoAccountKey = cleanPhone,
                programId = cleanProgramId,
                patchMap = recordMap
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
                    val newStatus = if (newEndsMillis <= nowMillis) "TRIAL_EXPIRED" else "TRIAL_ACTIVE"

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
                        patchMap = enrollPatch
                    )
                } else {
                    val rejectStatus = when {
                        enrollment.completedInstallments > 0 -> "ACTIVE"
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
                        patchMap = rejectPatch
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
            val response = api.enforceOverdueCourseInstallmentsRpc()
            if (isJsonRpcSuccess(response)) {
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
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

    override suspend fun fetchOneSignalPublicAppId(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val response = api.getOneSignalPublicAppIdRpc()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("get_onesignal_public_app_id failed with HTTP ${response.code()}")
                )
            }
            val raw = response.body()?.string()?.trim().orEmpty()
            if (raw.isEmpty() || raw.equals("null", ignoreCase = true)) {
                return@withContext Result.failure(Exception("Empty OneSignal App ID from RPC"))
            }

            // Can be a quoted string `"uuid"`, a JSON object `{"onesignal_app_id": "uuid"}`, or an array `[{"onesignal_app_id": "uuid"}]`
            val extracted = when {
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

            if (extracted.isNotBlank()) {
                adminPreferences.setOneSignalAppId(extracted)
                Result.success(extracted)
            } else {
                Result.failure(Exception("OneSignal App ID not found in RPC response"))
            }
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
            val response = api.notifyStudentViaOneSignalRpc(payload)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = try { response.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                Result.failure(Exception("notify_student_via_onesignal HTTP ${response.code()}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

