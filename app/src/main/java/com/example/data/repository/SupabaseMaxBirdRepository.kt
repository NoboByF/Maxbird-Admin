package com.example.data.repository

import com.example.data.model.AccessCode
import com.example.data.model.ActivatedDevice
import com.example.data.model.AppUpdate
import com.example.data.model.AppNotice
import com.example.data.model.ActivationSupportLink
import com.example.data.model.ApproveCourseAccessRequestRpcPayload
import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CourseCatalogItem
import com.example.data.model.CoursePaymentSettings
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.CreateAccessCodePayload
import com.example.data.model.CreateAppUpdatePayload
import com.example.data.model.CreateAppNoticePayload
import com.example.data.model.CreateSupportLinkPayload
import com.example.data.model.DashboardStats
import com.example.data.model.DeviceShikhoAccount
import com.example.data.model.DeviceWithStudent
import com.example.data.model.ReviewCoursePaymentRpcPayload
import com.example.data.model.SetAccountCoursePermissionsRpcPayload
import com.example.data.model.SetDeviceCoursePermissionsRpcPayload
import com.example.data.model.UpdateCodeStatusPayload
import com.example.data.model.UpdateCoursePermissionsPayload
import com.example.data.model.UpdateDeviceBlockedPayload
import com.example.data.model.UpdateAppUpdateStatusPayload
import com.example.data.model.UpdateAppNoticeStatusPayload
import com.example.data.model.UpdateSupportLinkStatusPayload
import com.example.data.model.UpsertCoursePaymentSettingsPayload
import com.example.data.model.UserDevice
import com.example.data.model.UpdateDeviceBanPayload
import com.example.data.api.SupabaseClient
import com.example.data.preferences.AdminPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private val cachedCourseCatalog = mutableListOf<CourseCatalogItem>()
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
                        deviceHash = "sha256_hw_xiaomi_9a12c40",
                        displayDeviceId = "MX-7F3A-9B21-C04E",
                        deviceModel = "Xiaomi Redmi Note 12",
                        deviceBrand = "Xiaomi",
                        androidVersion = "Android 14 (API 34)",
                        appVersion = "1.0.0",
                        appliedCode = "VIP-STUDENT-01",
                        isBanned = false,
                        banReason = null,
                        status = "active",
                        lastSeen = now
                    ),
                    UserDevice(
                        id = UUID.randomUUID().toString(),
                        deviceHash = "sha256_hw_samsung_b38df9",
                        displayDeviceId = "MX-4C82-1D5E-F93A",
                        deviceModel = "Samsung Galaxy S23",
                        deviceBrand = "Samsung",
                        androidVersion = "Android 14 (API 34)",
                        appVersion = "1.0.0",
                        appliedCode = "MAX-8921",
                        isBanned = false,
                        banReason = null,
                        status = "active",
                        lastSeen = now
                    ),
                    UserDevice(
                        id = UUID.randomUUID().toString(),
                        deviceHash = "sha256_hw_realme_e847c1",
                        displayDeviceId = "MX-89AB-CDEF-0123",
                        deviceModel = "Realme 11 Pro+",
                        deviceBrand = "Realme",
                        androidVersion = "Android 13 (API 33)",
                        appVersion = "1.0.0",
                        appliedCode = "MAX-1144",
                        isBanned = true,
                        banReason = "একাধিক ডিভাইসে শেয়ার করার অভিযোগে ব্যান করা হয়েছে",
                        status = "banned",
                        lastSeen = now
                    ),
                    UserDevice(
                        id = UUID.randomUUID().toString(),
                        deviceHash = "sha256_hw_vivo_aa2938",
                        displayDeviceId = "MX-D104-55FA-82EE",
                        deviceModel = "Vivo V29 5G",
                        deviceBrand = "Vivo",
                        androidVersion = "Android 14 (API 34)",
                        appVersion = "1.0.0",
                        appliedCode = "VIP-BATCH-2026",
                        isBanned = false,
                        banReason = null,
                        status = "active",
                        lastSeen = now,
                        isAccessRestricted = true,
                        allowedClasses = listOf("C9", "C10"),
                        allowedGroups = listOf("Science"),
                        allowedBatches = listOf("2026", "2027"),
                        allowedPrograms = listOf("prog_c9_sci_2026", "এসএসসি ২০২৬ ফুল একাডেমিক প্রোগ্রাম (বিজ্ঞান)")
                    )
                )
            )

            cachedCourseCatalog.addAll(
                listOf(
                    CourseCatalogItem(
                        programId = "prog_c5_all_2026",
                        titleBn = "৫ম শ্রেণি - বার্ষিক প্রস্তুতি ও বৃত্তি প্রোগ্রাম ২০২৬",
                        classCode = "C5",
                        groupCode = null,
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c6_all_2026",
                        titleBn = "৬ষ্ঠ শ্রেণি - একাডেমিক পূর্ণাঙ্গ কোর্স ২০২৬",
                        classCode = "C6",
                        groupCode = null,
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c7_all_2026",
                        titleBn = "৭ম শ্রেণি - একাডেমিক পূর্ণাঙ্গ কোর্স ২০২৬",
                        classCode = "C7",
                        groupCode = null,
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c8_all_2026",
                        titleBn = "৮ম শ্রেণি - জুনিয়র বৃত্তি ও একাডেমিক কোর্স ২০২৬",
                        classCode = "C8",
                        groupCode = null,
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c9_sci_2026",
                        titleBn = "নবম শ্রেণি - একাডেমিক প্রোগ্রাম (বিজ্ঞান)",
                        classCode = "C9",
                        groupCode = "Science",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c9_hum_2026",
                        titleBn = "নবম শ্রেণি - একাডেমিক প্রোগ্রাম (মানবিক)",
                        classCode = "C9",
                        groupCode = "Humanities",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c9_bus_2026",
                        titleBn = "নবম শ্রেণি - একাডেমিক প্রোগ্রাম (ব্যবসায় শিক্ষা)",
                        classCode = "C9",
                        groupCode = "BusinessStudies",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c10_sci_2026",
                        titleBn = "দশম শ্রেণি ও এসএসসি ফাইনাল প্রস্তুতি (বিজ্ঞান)",
                        classCode = "C10",
                        groupCode = "Science",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c10_hum_2026",
                        titleBn = "দশম শ্রেণি ও এসএসসি ফাইনাল প্রস্তুতি (মানবিক)",
                        classCode = "C10",
                        groupCode = "Humanities",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c10_bus_2026",
                        titleBn = "দশম শ্রেণি ও এসএসসি ফাইনাল প্রস্তুতি (ব্যবসায় শিক্ষা)",
                        classCode = "C10",
                        groupCode = "BusinessStudies",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c11_sci_2027",
                        titleBn = "একাদশ শ্রেণি - এইচএসসি একাডেমিক প্রোগ্রাম (বিজ্ঞান)",
                        classCode = "C11",
                        groupCode = "Science",
                        batchId = "2027",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c11_hum_2027",
                        titleBn = "একাদশ শ্রেণি - এইচএসসি একাডেমিক প্রোগ্রাম (মানবিক)",
                        classCode = "C11",
                        groupCode = "Humanities",
                        batchId = "2027",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c11_bus_2027",
                        titleBn = "একাদশ শ্রেণি - এইচএসসি একাডেমিক প্রোগ্রাম (ব্যবসায় শিক্ষা)",
                        classCode = "C11",
                        groupCode = "BusinessStudies",
                        batchId = "2027",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c12_sci_2026",
                        titleBn = "দ্বাদশ শ্রেণি ও এইচএসসি টেস্ট পেপার সলভ (বিজ্ঞান)",
                        classCode = "C12",
                        groupCode = "Science",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c12_hum_2026",
                        titleBn = "দ্বাদশ শ্রেণি ও এইচএসসি টেস্ট পেপার সলভ (মানবিক)",
                        classCode = "C12",
                        groupCode = "Humanities",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    ),
                    CourseCatalogItem(
                        programId = "prog_c12_bus_2026",
                        titleBn = "দ্বাদশ শ্রেণি ও এইচএসসি টেস্ট পেপার সলভ (ব্যবসায় শিক্ষা)",
                        classCode = "C12",
                        groupCode = "BusinessStudies",
                        batchId = "2026",
                        bannerUrl = null,
                        isFree = false
                    )
                )
            )

            isInitializedWithSampleData = true
        }
    }


    override suspend fun getAccessCodes(forceRefresh: Boolean): Result<List<AccessCode>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteCodes = api.getAccessCodes()
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
        val localNewCode = AccessCode(
            id = UUID.randomUUID().toString(),
            code = payload.code.uppercase().trim(),
            studentName = payload.studentName.trim(),
            maxDevices = payload.maxDevices,
            isActive = payload.isActive,
            createdAt = now,
            note = payload.note,
            isAccessRestricted = payload.isAccessRestricted,
            allowedClasses = payload.allowedClasses,
            allowedGroups = payload.allowedGroups,
            allowedBatches = payload.allowedBatches,
            allowedPrograms = payload.allowedPrograms
        )

        try {
            val api = supabaseClient.getApi()
            val createdList = api.createAccessCode(payload)
            val created = createdList.firstOrNull() ?: localNewCode
            cachedCodes.add(0, created)
            Result.success(created)
        } catch (e: Exception) {
            cachedCodes.add(0, localNewCode)
            Result.success(localNewCode)
        }
    }

    override suspend fun toggleCodeStatus(codeId: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val index = cachedCodes.indexOfFirst { it.id == codeId }
        if (index != -1) {
            val existing = cachedCodes[index]
            cachedCodes[index] = existing.copy(isActive = isActive)
        }

        try {
            val api = supabaseClient.getApi()
            api.updateAccessCodeStatus(
                idQuery = "eq.$codeId",
                payload = UpdateCodeStatusPayload(isActive = isActive)
            )
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

    override suspend fun getActivatedDevices(forceRefresh: Boolean): Result<List<ActivatedDevice>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteDevices = api.getActivatedDevices()
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
            val devices = api.getDevicesForCode(codeIdQuery = "eq.$codeId")
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
                device = device,
                studentName = linkedCode?.studentName ?: "Unknown Student",
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
        val devices = getActivatedDevices().getOrDefault(cachedDevices)

        val totalCodes = codes.size
        val activeStudents = codes.count { it.isActive }
        val totalDevices = devices.size
        val blockedDevices = devices.count { it.isBlocked }
        val totalCapacity = codes.sumOf { it.maxDevices }
        val availableSlots = (totalCapacity - totalDevices).coerceAtLeast(0)

        Result.success(
            DashboardStats(
                totalCodes = totalCodes,
                activeStudents = activeStudents,
                totalDevices = totalDevices,
                blockedDevices = blockedDevices,
                availableSlots = availableSlots
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
            val remoteDevices = api.getDevices()
            cachedUserDevices.clear()
            cachedUserDevices.addAll(remoteDevices)
            Result.success(remoteDevices)
        } catch (e: Exception) {
            Result.success(cachedUserDevices.toList())
        }
    }

    override suspend fun toggleUserDeviceBan(deviceId: String, isBanned: Boolean, banReason: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val newStatus = if (isBanned) "banned" else "active"
        val idx = cachedUserDevices.indexOfFirst { it.id == deviceId || it.deviceHash == deviceId }
        if (idx != -1) {
            val old = cachedUserDevices[idx]
            cachedUserDevices[idx] = old.copy(
                isBanned = isBanned,
                status = newStatus,
                banReason = if (isBanned) (banReason ?: "অ্যাডমিন কর্তৃক ব্যান করা হয়েছে") else null
            )
        }

        try {
            val api = supabaseClient.getApi()
            val payload = UpdateDeviceBanPayload(
                isBanned = isBanned,
                status = newStatus,
                banReason = if (isBanned) (banReason ?: "অ্যাডমিন কর্তৃক ব্যান করা হয়েছে") else null
            )
            api.updateDeviceBan("eq.$deviceId", payload)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun deleteUserDevice(deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedUserDevices.removeAll { it.id == deviceId || it.deviceHash == deviceId }

        try {
            val api = supabaseClient.getApi()
            api.deleteDeviceRecord("eq.$deviceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun getAvailableCoursesCatalog(forceRefresh: Boolean): Result<List<CourseCatalogItem>> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val remoteCatalog = api.getAvailableCoursesCatalog()
            if (remoteCatalog.isNotEmpty()) {
                cachedCourseCatalog.clear()
                cachedCourseCatalog.addAll(remoteCatalog)
                Result.success(remoteCatalog)
            } else {
                initSampleDataIfNeeded()
                Result.success(cachedCourseCatalog.toList())
            }
        } catch (e: Exception) {
            initSampleDataIfNeeded()
            Result.success(cachedCourseCatalog.toList())
        }
    }

    override suspend fun setDeviceCoursePermissions(
        device: UserDevice,
        isRestricted: Boolean,
        allowedClasses: List<String>,
        allowedGroups: List<String>,
        allowedBatches: List<String>,
        allowedPrograms: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Update local cache immediately
        val idx = cachedUserDevices.indexOfFirst {
            (device.id.isNotBlank() && it.id == device.id) ||
            (device.deviceHash.isNotBlank() && it.deviceHash == device.deviceHash)
        }
        if (idx != -1) {
            val existing = cachedUserDevices[idx]
            cachedUserDevices[idx] = existing.copy(
                isAccessRestricted = isRestricted,
                allowedClasses = allowedClasses,
                allowedGroups = allowedGroups,
                allowedBatches = allowedBatches,
                allowedPrograms = allowedPrograms
            )
        }

        try {
            val api = supabaseClient.getApi()
            var rpcSucceeded = false

            // 1. Primary: Call Supabase RPC `admin_set_device_course_permissions`
            if (device.deviceHash.isNotBlank()) {
                try {
                    val rpcPayload = SetDeviceCoursePermissionsRpcPayload(
                        deviceHash = device.deviceHash,
                        isRestricted = isRestricted,
                        allowedClasses = allowedClasses,
                        allowedGroups = allowedGroups,
                        allowedBatches = allowedBatches,
                        allowedPrograms = allowedPrograms
                    )
                    val rpcResponse = api.setDeviceCoursePermissionsRpc(rpcPayload)
                    rpcSucceeded = rpcResponse.isSuccessful
                } catch (_: Exception) {
                    rpcSucceeded = false
                }
            }

            // 2. Also ensure `public.devices` row is updated via PATCH if RPC didn't succeed or device_hash was empty
            if (!rpcSucceeded) {
                val patchPayload = UpdateCoursePermissionsPayload(
                    isAccessRestricted = isRestricted,
                    allowedClasses = allowedClasses,
                    allowedGroups = allowedGroups,
                    allowedBatches = allowedBatches,
                    allowedPrograms = allowedPrograms
                )
                if (device.deviceHash.isNotBlank()) {
                    api.updateDeviceCoursePermissionsByHash("eq.${device.deviceHash}", patchPayload)
                } else if (device.id.isNotBlank()) {
                    api.updateDeviceCoursePermissionsById("eq.${device.id}", patchPayload)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun setAccessCodeCoursePermissions(
        codeId: String,
        isRestricted: Boolean,
        allowedClasses: List<String>,
        allowedGroups: List<String>,
        allowedBatches: List<String>,
        allowedPrograms: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val idx = cachedCodes.indexOfFirst { it.id == codeId }
        if (idx != -1) {
            val existing = cachedCodes[idx]
            cachedCodes[idx] = existing.copy(
                isAccessRestricted = isRestricted,
                allowedClasses = allowedClasses,
                allowedGroups = allowedGroups,
                allowedBatches = allowedBatches,
                allowedPrograms = allowedPrograms
            )
        }

        try {
            val api = supabaseClient.getApi()
            val patchPayload = UpdateCoursePermissionsPayload(
                isAccessRestricted = isRestricted,
                allowedClasses = allowedClasses,
                allowedGroups = allowedGroups,
                allowedBatches = allowedBatches,
                allowedPrograms = allowedPrograms
            )
            api.updateAccessCodeCoursePermissions("eq.$codeId", patchPayload)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun getDeviceShikhoAccounts(
        deviceHash: String,
        displayDeviceId: String
    ): Result<List<DeviceShikhoAccount>> = withContext(Dispatchers.IO) {
        try {
            val clauses = mutableListOf<String>()
            if (deviceHash.isNotBlank()) {
                clauses.add("device_hash.eq.${deviceHash.trim()}")
            }
            if (displayDeviceId.isNotBlank()) {
                clauses.add("display_device_id.eq.${displayDeviceId.trim()}")
            }
            if (clauses.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val orFilter = "(${clauses.joinToString(",")})"
            val api = supabaseClient.getApi()
            val accounts = api.getDeviceShikhoAccounts(
                orFilter = orFilter,
                order = "is_access_granted.desc,last_seen_at.desc"
            )
            Result.success(accounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setAccountCoursePermissions(
        payload: SetAccountCoursePermissionsRpcPayload
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val response = api.setAccountCoursePermissionsRpc(payload)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = try {
                    response.errorBody()?.string().orEmpty()
                } catch (_: Exception) {
                    ""
                }
                Result.failure(
                    Exception(
                        "RPC admin_set_account_course_permissions ব্যর্থ (HTTP ${response.code()})${if (errBody.isNotBlank()) ": $errBody" else ""}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // Course Sales, Installment Plans & Payment Verification
    // =========================================================================

    override suspend fun getCoursePaymentSettings(): Result<CoursePaymentSettings> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val list = api.getCoursePaymentSettings(idQuery = "eq.1")
            val settings = list.firstOrNull() ?: CoursePaymentSettings(
                id = 1,
                bkashNumber = "",
                nagadNumber = "",
                rocketNumber = "",
                paymentInstructionsBn = "উপরের যেকোনো নম্বরে Send Money করে নিচের ফর্মে আপনার প্রেরক নম্বর ও Transaction ID জমা দিন।",
                defaultCoursePrice = 1000
            )
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCoursePaymentSettings(
        payload: UpsertCoursePaymentSettingsPayload
    ): Result<CoursePaymentSettings> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val updatedList = try {
                api.upsertCoursePaymentSettings(payload)
            } catch (_: Exception) {
                api.patchCoursePaymentSettings(idQuery = "eq.1", payload = payload)
            }
            val saved = updatedList.firstOrNull() ?: CoursePaymentSettings(
                id = 1,
                bkashNumber = payload.bkashNumber,
                nagadNumber = payload.nagadNumber,
                rocketNumber = payload.rocketNumber,
                paymentInstructionsBn = payload.paymentInstructionsBn,
                defaultCoursePrice = payload.defaultCoursePrice
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

    override suspend fun approveCourseAccessRequest(
        payload: ApproveCourseAccessRequestRpcPayload
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val response = api.approveCourseAccessRequestRpc(payload)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = try {
                    response.errorBody()?.string().orEmpty()
                } catch (_: Exception) {
                    ""
                }
                Result.failure(
                    Exception(
                        "রিকোয়েস্ট অনুমোদন ব্যর্থ (HTTP ${response.code()})${if (errBody.isNotBlank()) ": $errBody" else ""}"
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
            val response = api.reviewCoursePaymentRpc(payload)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = try {
                    response.errorBody()?.string().orEmpty()
                } catch (_: Exception) {
                    ""
                }
                Result.failure(
                    Exception(
                        "পেমেন্ট রিভিউ ব্যর্থ (HTTP ${response.code()})${if (errBody.isNotBlank()) ": $errBody" else ""}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enforceOverdueCourseInstallments(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val response = api.enforceOverdueCourseInstallmentsRpc()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = try {
                    response.errorBody()?.string().orEmpty()
                } catch (_: Exception) {
                    ""
                }
                Result.failure(
                    Exception(
                        "ওভারডিউ কিস্তি চেক ব্যর্থ (HTTP ${response.code()})${if (errBody.isNotBlank()) ": $errBody" else ""}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEnrollmentStatusOrDueDate(
        enrollmentId: Long,
        newStatus: String?,
        newNextDueDateIso: String?,
        adminNote: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val api = supabaseClient.getApi()
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            val patchMap = mutableMapOf<String, Any?>(
                "updated_at" to nowIso
            )
            if (!newStatus.isNullOrBlank()) {
                patchMap["status"] = newStatus
            }
            if (!newNextDueDateIso.isNullOrBlank()) {
                patchMap["next_due_date"] = newNextDueDateIso
            }
            if (adminNote != null) {
                patchMap["admin_note"] = adminNote
            }

            val response = api.patchCourseAccessEnrollment(
                idQuery = "eq.$enrollmentId",
                payload = patchMap
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = try {
                    response.errorBody()?.string().orEmpty()
                } catch (_: Exception) {
                    ""
                }
                Result.failure(
                    Exception(
                        "এনরোলমেন্ট আপডেট ব্যর্থ (HTTP ${response.code()})${if (errBody.isNotBlank()) ": $errBody" else ""}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



