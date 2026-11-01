package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.CloudinaryClient
import com.example.data.api.MaxbirdFirestoreClient
import com.example.data.api.SupabaseClient
import com.example.data.model.AccessCode
import com.example.data.model.ActivatedDevice
import com.example.data.model.AppUpdate
import com.example.data.model.AppNotice
import com.example.data.model.ActivationSupportLink
import com.example.data.model.ApproveCourseAccessRequestRpcPayload
import com.example.data.model.CloudinaryHistoryItem
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
import com.example.data.model.ManageCourseTrialPayload
import com.example.data.model.ManualCourseAccessPayload
import com.example.data.model.MaxbirdAdminAuthSession
import com.example.data.model.MaxbirdPortalSettings
import com.example.data.model.ReviewCoursePaymentRpcPayload
import com.example.data.model.UpsertCoursePaymentSettingsPayload
import com.example.data.model.UserDevice
import com.example.data.preferences.AdminPreferences
import com.example.data.repository.MaxBirdRepository
import com.example.data.repository.SupabaseMaxBirdRepository
import com.example.notifications.AdminNotificationHelper
import com.example.notifications.AdminRealtimeMonitorService
import com.example.notifications.OneSignalPushManager
import com.example.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val adminPreferences = AdminPreferences(application)
    private val supabaseClient = SupabaseClient(adminPreferences)
    private val repository: MaxBirdRepository = SupabaseMaxBirdRepository(supabaseClient, adminPreferences)
    private val cloudinaryClient = CloudinaryClient(application)
    private val maxbirdFirestoreClient = MaxbirdFirestoreClient(application)
    private val notificationHelper = AdminNotificationHelper(application)

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Cloudinary Media Uploader State
    private val _selectedMediaUri = MutableStateFlow<android.net.Uri?>(null)
    val selectedMediaUri: StateFlow<android.net.Uri?> = _selectedMediaUri.asStateFlow()

    private val _selectedMediaMeta = MutableStateFlow<CloudinaryClient.FileMeta?>(null)
    val selectedMediaMeta: StateFlow<CloudinaryClient.FileMeta?> = _selectedMediaMeta.asStateFlow()

    private val _isUploadingMedia = MutableStateFlow(false)
    val isUploadingMedia: StateFlow<Boolean> = _isUploadingMedia.asStateFlow()

    private val _lastUploadedSecureUrl = MutableStateFlow<String?>(null)
    val lastUploadedSecureUrl: StateFlow<String?> = _lastUploadedSecureUrl.asStateFlow()

    private val _mediaUploadHistory = MutableStateFlow<List<CloudinaryHistoryItem>>(emptyList())
    val mediaUploadHistory: StateFlow<List<CloudinaryHistoryItem>> = _mediaUploadHistory.asStateFlow()

    // Dashboard State
    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats: StateFlow<Boolean> = _isLoadingStats.asStateFlow()

    // Generator Form State
    private val _genStudentName = MutableStateFlow("")
    val genStudentName: StateFlow<String> = _genStudentName.asStateFlow()

    private val _genCode = MutableStateFlow("MAX-${Random.nextInt(1000, 9999)}")
    val genCode: StateFlow<String> = _genCode.asStateFlow()

    private val _genMaxDevices = MutableStateFlow(1)
    val genMaxDevices: StateFlow<Int> = _genMaxDevices.asStateFlow()

    private val _genNote = MutableStateFlow("")
    val genNote: StateFlow<String> = _genNote.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedSuccessCode = MutableStateFlow<AccessCode?>(null)
    val generatedSuccessCode: StateFlow<AccessCode?> = _generatedSuccessCode.asStateFlow()

    private val _generatorError = MutableStateFlow<String?>(null)
    val generatorError: StateFlow<String?> = _generatorError.asStateFlow()

    // Code Management State
    private val _codes = MutableStateFlow<List<AccessCode>>(emptyList())
    val codes: StateFlow<List<AccessCode>> = _codes.asStateFlow()

    private val _codeSearchQuery = MutableStateFlow("")
    val codeSearchQuery: StateFlow<String> = _codeSearchQuery.asStateFlow()

    private val _codeFilter = MutableStateFlow("ALL") // ALL, ACTIVE, INACTIVE
    val codeFilter: StateFlow<String> = _codeFilter.asStateFlow()

    private val _isLoadingCodes = MutableStateFlow(false)
    val isLoadingCodes: StateFlow<Boolean> = _isLoadingCodes.asStateFlow()

    // Linked Devices for a code dialog
    private val _selectedCodeForDevices = MutableStateFlow<AccessCode?>(null)
    val selectedCodeForDevices: StateFlow<AccessCode?> = _selectedCodeForDevices.asStateFlow()

    private val _linkedDevices = MutableStateFlow<List<ActivatedDevice>>(emptyList())
    val linkedDevices: StateFlow<List<ActivatedDevice>> = _linkedDevices.asStateFlow()

    private val _isLoadingLinkedDevices = MutableStateFlow(false)
    val isLoadingLinkedDevices: StateFlow<Boolean> = _isLoadingLinkedDevices.asStateFlow()

    // Device Management & Kill-Switch State
    private val _joinedDevices = MutableStateFlow<List<DeviceWithStudent>>(emptyList())
    val joinedDevices: StateFlow<List<DeviceWithStudent>> = _joinedDevices.asStateFlow()

    // Real Supabase Devices (public.devices) & Shikho Accounts (public.device_shikho_accounts)
    private val _userDevices = MutableStateFlow<List<UserDevice>>(emptyList())
    val userDevices: StateFlow<List<UserDevice>> = _userDevices.asStateFlow()

    private val _deviceShikhoAccounts = MutableStateFlow<List<DeviceShikhoAccount>>(emptyList())
    val deviceShikhoAccounts: StateFlow<List<DeviceShikhoAccount>> = _deviceShikhoAccounts.asStateFlow()

    private val _deviceSearchQuery = MutableStateFlow("")
    val deviceSearchQuery: StateFlow<String> = _deviceSearchQuery.asStateFlow()

    private val _deviceFilter = MutableStateFlow("PENDING") // PENDING, ACTIVE, BLOCKED
    val deviceFilter: StateFlow<String> = _deviceFilter.asStateFlow()

    private val _isLoadingDevices = MutableStateFlow(false)
    val isLoadingDevices: StateFlow<Boolean> = _isLoadingDevices.asStateFlow()

    // App Updates Management State
    private val _appUpdates = MutableStateFlow<List<AppUpdate>>(emptyList())
    val appUpdates: StateFlow<List<AppUpdate>> = _appUpdates.asStateFlow()

    private val _isLoadingUpdates = MutableStateFlow(false)
    val isLoadingUpdates: StateFlow<Boolean> = _isLoadingUpdates.asStateFlow()

    private val _isUpdateDialogOpen = MutableStateFlow(false)
    val isUpdateDialogOpen: StateFlow<Boolean> = _isUpdateDialogOpen.asStateFlow()

    private val _editingUpdateId = MutableStateFlow<String?>(null)
    val editingUpdateId: StateFlow<String?> = _editingUpdateId.asStateFlow()

    private val _updateVersionName = MutableStateFlow("v6.1.0")
    val updateVersionName: StateFlow<String> = _updateVersionName.asStateFlow()

    private val _updateVersionCode = MutableStateFlow("60100")
    val updateVersionCode: StateFlow<String> = _updateVersionCode.asStateFlow()

    private val _updateMinVersionCode = MutableStateFlow("60000")
    val updateMinVersionCode: StateFlow<String> = _updateMinVersionCode.asStateFlow()

    private val _updateIsForce = MutableStateFlow(false)
    val updateIsForce: StateFlow<Boolean> = _updateIsForce.asStateFlow()

    private val _updateTitle = MutableStateFlow("নতুন আপডেট উপলভ্য!")
    val updateTitle: StateFlow<String> = _updateTitle.asStateFlow()

    private val _updateChangelog = MutableStateFlow("• বাগ ফিক্স এবং পারফরম্যান্স উন্নতি\n• নতুন সিকিউরিটি ফিচার")
    val updateChangelog: StateFlow<String> = _updateChangelog.asStateFlow()

    private val _updateDownloadUrl = MutableStateFlow("")
    val updateDownloadUrl: StateFlow<String> = _updateDownloadUrl.asStateFlow()

    private val _updateButtonText = MutableStateFlow("এখনই আপডেট করুন")
    val updateButtonText: StateFlow<String> = _updateButtonText.asStateFlow()

    private val _updateIsActive = MutableStateFlow(true)
    val updateIsActive: StateFlow<Boolean> = _updateIsActive.asStateFlow()

    // Notice Management State
    private val _appNotices = MutableStateFlow<List<AppNotice>>(emptyList())
    val appNotices: StateFlow<List<AppNotice>> = _appNotices.asStateFlow()

    private val _isLoadingNotices = MutableStateFlow(false)
    val isLoadingNotices: StateFlow<Boolean> = _isLoadingNotices.asStateFlow()

    private val _noticeSearchQuery = MutableStateFlow("")
    val noticeSearchQuery: StateFlow<String> = _noticeSearchQuery.asStateFlow()

    private val _noticeFilter = MutableStateFlow("ALL") // ALL, ACTIVE, INACTIVE, POPUP
    val noticeFilter: StateFlow<String> = _noticeFilter.asStateFlow()

    private val _isNoticeDialogOpen = MutableStateFlow(false)
    val isNoticeDialogOpen: StateFlow<Boolean> = _isNoticeDialogOpen.asStateFlow()

    private val _editingNoticeId = MutableStateFlow<String?>(null)
    val editingNoticeId: StateFlow<String?> = _editingNoticeId.asStateFlow()

    private val _noticeTitle = MutableStateFlow("")
    val noticeTitle: StateFlow<String> = _noticeTitle.asStateFlow()

    private val _noticeDescription = MutableStateFlow("")
    val noticeDescription: StateFlow<String> = _noticeDescription.asStateFlow()

    private val _noticeImageUrl = MutableStateFlow("")
    val noticeImageUrl: StateFlow<String> = _noticeImageUrl.asStateFlow()

    private val _noticeActionUrl = MutableStateFlow("")
    val noticeActionUrl: StateFlow<String> = _noticeActionUrl.asStateFlow()

    private val _noticeActionButtonText = MutableStateFlow("বিস্তারিত দেখুন")
    val noticeActionButtonText: StateFlow<String> = _noticeActionButtonText.asStateFlow()

    private val _noticePriority = MutableStateFlow("0")
    val noticePriority: StateFlow<String> = _noticePriority.asStateFlow()

    private val _noticeIsActive = MutableStateFlow(true)
    val noticeIsActive: StateFlow<Boolean> = _noticeIsActive.asStateFlow()

    private val _noticeShowAsPopup = MutableStateFlow(true)
    val noticeShowAsPopup: StateFlow<Boolean> = _noticeShowAsPopup.asStateFlow()

    private val _previewNotice = MutableStateFlow<AppNotice?>(null)
    val previewNotice: StateFlow<AppNotice?> = _previewNotice.asStateFlow()

    // Activation Support Links State
    private val _supportLinks = MutableStateFlow<List<ActivationSupportLink>>(emptyList())
    val supportLinks: StateFlow<List<ActivationSupportLink>> = _supportLinks.asStateFlow()

    private val _isLoadingSupportLinks = MutableStateFlow(false)
    val isLoadingSupportLinks: StateFlow<Boolean> = _isLoadingSupportLinks.asStateFlow()

    private val _supportLinkSearchQuery = MutableStateFlow("")
    val supportLinkSearchQuery: StateFlow<String> = _supportLinkSearchQuery.asStateFlow()

    private val _supportLinkFilter = MutableStateFlow("ALL")
    val supportLinkFilter: StateFlow<String> = _supportLinkFilter.asStateFlow()

    private val _isSupportLinkDialogOpen = MutableStateFlow(false)
    val isSupportLinkDialogOpen: StateFlow<Boolean> = _isSupportLinkDialogOpen.asStateFlow()

    private val _editingSupportLinkId = MutableStateFlow<String?>(null)
    val editingSupportLinkId: StateFlow<String?> = _editingSupportLinkId.asStateFlow()

    private val _supportLinkTitle = MutableStateFlow("")
    val supportLinkTitle: StateFlow<String> = _supportLinkTitle.asStateFlow()

    private val _supportLinkSubtitle = MutableStateFlow("")
    val supportLinkSubtitle: StateFlow<String> = _supportLinkSubtitle.asStateFlow()

    private val _supportLinkIconType = MutableStateFlow("telegram")
    val supportLinkIconType: StateFlow<String> = _supportLinkIconType.asStateFlow()

    private val _supportLinkUrl = MutableStateFlow("")
    val supportLinkUrl: StateFlow<String> = _supportLinkUrl.asStateFlow()

    private val _supportLinkColorHex = MutableStateFlow("#229ED9")
    val supportLinkColorHex: StateFlow<String> = _supportLinkColorHex.asStateFlow()

    private val _supportLinkPriority = MutableStateFlow("10")
    val supportLinkPriority: StateFlow<String> = _supportLinkPriority.asStateFlow()

    private val _supportLinkIsActive = MutableStateFlow(true)
    val supportLinkIsActive: StateFlow<Boolean> = _supportLinkIsActive.asStateFlow()

    // Settings State
    private val _supabaseUrl = MutableStateFlow(adminPreferences.getSupabaseUrl())
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseKey = MutableStateFlow(adminPreferences.getSupabaseKey())
    val supabaseKey: StateFlow<String> = _supabaseKey.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus.asStateFlow()

    private val _isConnectionSuccess = MutableStateFlow<Boolean?>(null)
    val isConnectionSuccess: StateFlow<Boolean?> = _isConnectionSuccess.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Course Sales, Installment Plans & Payment Verification State
    private val _courseAccessEnrollments = MutableStateFlow<List<CourseAccessEnrollment>>(emptyList())
    val courseAccessEnrollments: StateFlow<List<CourseAccessEnrollment>> = _courseAccessEnrollments.asStateFlow()

    private val _coursePaymentTransactions = MutableStateFlow<List<CoursePaymentTransaction>>(emptyList())
    val coursePaymentTransactions: StateFlow<List<CoursePaymentTransaction>> = _coursePaymentTransactions.asStateFlow()

    private val _coursePaymentSettings = MutableStateFlow(CoursePaymentSettings())
    val coursePaymentSettings: StateFlow<CoursePaymentSettings> = _coursePaymentSettings.asStateFlow()

    private val _isLoadingCourseSales = MutableStateFlow(false)
    val isLoadingCourseSales: StateFlow<Boolean> = _isLoadingCourseSales.asStateFlow()

    private val _isProcessingCourseSalesAction = MutableStateFlow(false)
    val isProcessingCourseSalesAction: StateFlow<Boolean> = _isProcessingCourseSalesAction.asStateFlow()

    private val _courseSalesSelectedSubTab = MutableStateFlow(0)
    val courseSalesSelectedSubTab: StateFlow<Int> = _courseSalesSelectedSubTab.asStateFlow()

    // Maxbird Website Admin (portal_settings/main on named Firestore database) State
    private val _maxbirdPortalSettings = MutableStateFlow(MaxbirdPortalSettings())
    val maxbirdPortalSettings: StateFlow<MaxbirdPortalSettings> = _maxbirdPortalSettings.asStateFlow()

    private val _maxbirdAuthSession = MutableStateFlow(maxbirdFirestoreClient.getSavedAuthSession())
    val maxbirdAuthSession: StateFlow<MaxbirdAdminAuthSession> = _maxbirdAuthSession.asStateFlow()

    private val _isLoadingMaxbirdPortal = MutableStateFlow(false)
    val isLoadingMaxbirdPortal: StateFlow<Boolean> = _isLoadingMaxbirdPortal.asStateFlow()

    private val _isSavingMaxbirdPortal = MutableStateFlow(false)
    val isSavingMaxbirdPortal: StateFlow<Boolean> = _isSavingMaxbirdPortal.asStateFlow()

    private val _maxbirdSyncMessage = MutableStateFlow<String?>(null)
    val maxbirdSyncMessage: StateFlow<String?> = _maxbirdSyncMessage.asStateFlow()

    // Real-Time Push / Background Notification Monitor State
    private val _isLiveNotificationMonitorEnabled = MutableStateFlow(notificationHelper.isLiveMonitorEnabled())
    val isLiveNotificationMonitorEnabled: StateFlow<Boolean> = _isLiveNotificationMonitorEnabled.asStateFlow()

    // OneSignal Push Configuration State
    private val _oneSignalAppId = MutableStateFlow(adminPreferences.getOneSignalAppId())
    val oneSignalAppId: StateFlow<String> = _oneSignalAppId.asStateFlow()

    private val _oneSignalRestApiKey = MutableStateFlow(adminPreferences.getOneSignalRestApiKey())
    val oneSignalRestApiKey: StateFlow<String> = _oneSignalRestApiKey.asStateFlow()

    private val _oneSignalStatusSummary = MutableStateFlow(OneSignalPushManager.getSubscriptionSummary(application))
    val oneSignalStatusSummary: StateFlow<String> = _oneSignalStatusSummary.asStateFlow()

    private val _isSendingOneSignalTest = MutableStateFlow(false)
    val isSendingOneSignalTest: StateFlow<Boolean> = _isSendingOneSignalTest.asStateFlow()

    init {
        val app = getApplication<Application>()
        OneSignalPushManager.initializeIfConfigured(app)
        OneSignalPushManager.syncAdminPushRegistration(app) {
            _oneSignalAppId.value = adminPreferences.getOneSignalAppId()
            _oneSignalStatusSummary.value = OneSignalPushManager.getSubscriptionSummary(app)
        }
        _oneSignalStatusSummary.value = OneSignalPushManager.getSubscriptionSummary(app)
        loadAllData()
        loadMaxbirdPortalSettings()
        startInAppRealtimeMonitorLoop()
        AdminRealtimeMonitorService.startIfEnabled(app)
    }

    private fun startInAppRealtimeMonitorLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(15_000L)
                val app = getApplication<Application>()
                if (_oneSignalAppId.value.isBlank()) {
                    val savedAppId = adminPreferences.getOneSignalAppId()
                    if (savedAppId.isNotBlank()) {
                        _oneSignalAppId.value = savedAppId
                    }
                }
                _oneSignalStatusSummary.value = OneSignalPushManager.getSubscriptionSummary(app)
                if (_isLiveNotificationMonitorEnabled.value) {
                    try {
                        val devicesRes = repository.getUserDevices()
                        if (devicesRes.isSuccess) {
                            _userDevices.value = devicesRes.getOrNull().orEmpty()
                        }
                        val enrollmentsRes = repository.getCourseAccessEnrollments()
                        if (enrollmentsRes.isSuccess) {
                            _courseAccessEnrollments.value = enrollmentsRes.getOrNull().orEmpty()
                        }
                        val transactionsRes = repository.getCoursePaymentTransactions()
                        if (transactionsRes.isSuccess) {
                            _coursePaymentTransactions.value = transactionsRes.getOrNull().orEmpty()
                        }
                        val statsRes = repository.getDashboardStats()
                        if (statsRes.isSuccess) {
                            statsRes.getOrNull()?.let { _stats.value = it }
                        }

                        notificationHelper.checkAndNotifyNewEvents(
                            devices = _userDevices.value,
                            enrollments = _courseAccessEnrollments.value,
                            transactions = _coursePaymentTransactions.value
                        )
                    } catch (_: Throwable) {}
                }
            }
        }
    }

    fun updateOneSignalAppId(appId: String) {
        _oneSignalAppId.value = appId
    }

    fun updateOneSignalRestApiKey(restApiKey: String) {
        _oneSignalRestApiKey.value = restApiKey
    }

    fun saveOneSignalConfig(appId: String = _oneSignalAppId.value, restApiKey: String = _oneSignalRestApiKey.value) {
        val cleanAppId = appId.trim()
        val cleanRestKey = restApiKey.trim()
        _oneSignalAppId.value = cleanAppId
        _oneSignalRestApiKey.value = cleanRestKey
        adminPreferences.setOneSignalAppId(cleanAppId)
        adminPreferences.setOneSignalRestApiKey(cleanRestKey)
        val app = getApplication<Application>()
        val initialized = OneSignalPushManager.initializeIfConfigured(app, cleanAppId)
        OneSignalPushManager.syncAdminPushRegistration(app, explicitAppId = cleanAppId) {
            _oneSignalAppId.value = adminPreferences.getOneSignalAppId()
            _oneSignalStatusSummary.value = OneSignalPushManager.getSubscriptionSummary(app)
        }
        _oneSignalStatusSummary.value = OneSignalPushManager.getSubscriptionSummary(app)
        if (initialized) {
            showToast("✅ OneSignal App ID সংরক্ষিত ও সক্রিয় করা হয়েছে!")
        } else if (cleanAppId.isBlank()) {
            showToast("ℹ️ OneSignal App ID ডাটাবেজ থেকে অটো-সিঙ্ক করা হচ্ছে...")
        } else {
            showToast("✅ OneSignal কনফিগারেশন সেভ হয়েছে!")
        }
    }

    fun toggleLiveNotificationMonitor(enabled: Boolean) {
        notificationHelper.setLiveMonitorEnabled(enabled)
        _isLiveNotificationMonitorEnabled.value = enabled
        val app = getApplication<Application>()
        if (enabled) {
            AdminRealtimeMonitorService.startIfEnabled(app)
            OneSignalPushManager.initializeIfConfigured(app)
            OneSignalPushManager.syncAdminPushRegistration(app) {
                _oneSignalAppId.value = adminPreferences.getOneSignalAppId()
                _oneSignalStatusSummary.value = OneSignalPushManager.getSubscriptionSummary(app)
            }
            _oneSignalStatusSummary.value = OneSignalPushManager.getSubscriptionSummary(app)
            showToast("🔔 24/7 লাইভ ও OneSignal + FCM পুশ নোটিফিকেশন চালু করা হয়েছে!")
        } else {
            AdminRealtimeMonitorService.stop(app)
            showToast("🔕 লাইভ নোটিফিকেশন মনিটর বন্ধ করা হয়েছে।")
        }
    }

    fun sendTestPushNotification() {
        val app = getApplication<Application>()
        if (!notificationHelper.hasNotificationPermission()) {
            showToast("⚠️ অনুগ্রহ করে ফোনের Notification Permission অ্যালাউ করুন!")
            return
        }
        // Always fire immediate local notification
        notificationHelper.sendTestNotification()

        // Also dispatch real OneSignal Cloud REST API push if App ID + REST API Key are configured
        val appId = _oneSignalAppId.value.trim()
        val restKey = _oneSignalRestApiKey.value.trim()
        if (appId.isNotBlank() && restKey.isNotBlank()) {
            viewModelScope.launch {
                try {
                    _isSendingOneSignalTest.value = true
                    val res = OneSignalPushManager.sendCloudPushNotification(
                        context = app,
                        title = "🚀 MaxBird OneSignal ক্লাউড পুশ সচল!",
                        message = "অ্যাপ সম্পূর্ণ বন্ধ থাকলেও নতুন ডিভাইস রিকোয়েস্ট, কোর্স আবেদন ও পেমেন্ট আসলে এভাবেই পুশ নোটিফিকেশন আসবে।",
                        targetScreen = "course_sales",
                        courseSalesTab = 0
                    )
                    _oneSignalStatusSummary.value = OneSignalPushManager.getSubscriptionSummary(app)
                    if (res.isSuccess) {
                        showToast("✅ লোকাল + OneSignal ক্লাউড পুশ পাঠানো হয়েছে!")
                    } else {
                        showToast("✅ লোকাল টেস্ট নোটিফিকেশন পাঠানো হয়েছে! (OneSignal: ${res.exceptionOrNull()?.message})")
                    }
                } finally {
                    _isSendingOneSignalTest.value = false
                }
            }
        } else {
            showToast("✅ টেস্ট নোটিফিকেশন পাঠানো হয়েছে! স্ট্যাটাস বার চেক করুন।")
        }
    }

    fun handleNotificationDeepLink(
        targetScreen: String?,
        courseSalesTab: Int = 0,
        deviceSearchQuery: String? = null
    ) {
        when (targetScreen) {
            "devices" -> {
                if (!deviceSearchQuery.isNullOrBlank()) {
                    navigateToDeviceInspection(deviceSearchQuery, null)
                } else {
                    _deviceFilter.value = "PENDING"
                    navigateTo(Screen.Devices)
                }
            }
            "course_sales" -> {
                _courseSalesSelectedSubTab.value = courseSalesTab.coerceIn(0, 3)
                navigateTo(Screen.CourseSales)
            }
            "maxbird_web_admin" -> {
                navigateTo(Screen.MaxbirdWebsiteAdmin)
            }
        }
    }

    /**
     * Navigates directly from a Course Access Request, Payment Verification, or Installment Card
     * to the Student's Device in `DeviceManagementScreen`, selecting the right tab and search filter
     * so the admin can immediately inspect or block/unblock the device.
     */
    fun navigateToDeviceInspection(query: String, targetDevice: UserDevice? = null) {
        val cleanQuery = query.trim()
        val matched = targetDevice ?: _userDevices.value.firstOrNull { dev ->
            dev.displayDeviceId.equals(cleanQuery, ignoreCase = true) ||
                dev.deviceHash.equals(cleanQuery, ignoreCase = true) ||
                dev.extractedPhoneFromDevice == cleanQuery ||
                (dev.studentNote?.contains(cleanQuery, ignoreCase = true) == true)
        }
        _deviceFilter.value = when {
            matched?.isBlockedDevice == true -> "BLOCKED"
            matched?.isPendingDevice == true -> "PENDING"
            matched?.isActiveDevice == true -> "ACTIVE"
            else -> "ACTIVE"
        }
        _deviceSearchQuery.value = matched?.displayDeviceId?.ifBlank { cleanQuery } ?: cleanQuery
        _currentScreen.value = Screen.Devices
        loadUserDevices(forceRefresh = true)
    }

    /**
     * 1-Click links a student's phone/name/course to a `UserDevice`'s `student_note` in Supabase
     * so the student and device are permanently linked across both screens.
     */
    fun linkStudentToDevice(
        device: UserDevice,
        studentPhone: String,
        studentName: String,
        courseTitle: String = ""
    ) {
        val cleanName = studentName.trim().ifBlank { "শিক্ষার্থী" }
        val cleanPhone = studentPhone.trim()
        val noteParts = mutableListOf<String>()
        noteParts.add(cleanName)
        if (cleanPhone.isNotBlank()) noteParts.add(cleanPhone)
        if (courseTitle.isNotBlank()) noteParts.add(courseTitle.trim())
        val formattedNote = noteParts.joinToString(" • ")
        updateUserDeviceStudentNote(device, formattedNote)
    }

    fun setCourseSalesSubTab(tabIndex: Int) {
        _courseSalesSelectedSubTab.value = tabIndex.coerceIn(0, 3)
    }

    fun openPaymentMethodsManager() {
        _courseSalesSelectedSubTab.value = 3
        _currentScreen.value = Screen.CourseSales
        loadCourseSalesData()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen == Screen.CourseSales || screen == Screen.Settings) {
            loadCourseSalesData()
            loadUserDevices(forceRefresh = false)
        } else if (screen == Screen.Devices) {
            loadUserDevices(forceRefresh = true)
            loadCourseSalesData()
        } else if (screen == Screen.MaxbirdWebsiteAdmin) {
            loadMaxbirdPortalSettings()
        }
    }

    fun loadAllData() {
        refreshStats()
        loadCodes()
        loadJoinedDevices()
        loadUserDevices()
        loadAppUpdates()
        loadAppNotices()
        loadActivationSupportLinks()
        loadCourseSalesData()
    }

    fun refreshStats() {
        viewModelScope.launch {
            try {
                _isLoadingStats.value = true
                val result = repository.getDashboardStats()
                if (result.isSuccess) {
                    _stats.value = result.getOrNull() ?: DashboardStats()
                }
            } catch (e: Throwable) {
                // Ignore and keep current stats
            } finally {
                _isLoadingStats.value = false
            }
        }
    }

    // Generator Functions
    fun setGenStudentName(name: String) {
        _genStudentName.value = name
    }

    fun setGenCode(code: String) {
        _genCode.value = code.uppercase()
    }

    fun setGenMaxDevices(devices: Int) {
        _genMaxDevices.value = devices.coerceIn(1, 5)
    }

    fun setGenNote(note: String) {
        _genNote.value = note
    }

    fun generateRandomCode(prefix: String = "MAX") {
        val randNum = Random.nextInt(1000, 9999)
        _genCode.value = "$prefix-$randNum"
    }

    fun submitCreateCode() {
        val name = _genStudentName.value.trim()
        val code = _genCode.value.trim().uppercase()

        if (name.isBlank()) {
            _generatorError.value = "শিক্ষার্থীর নাম লিখুন (Student name is required)"
            return
        }
        if (code.isBlank() || code.length < 3) {
            _generatorError.value = "সঠিক কোড দিন (Valid code required)"
            return
        }

        viewModelScope.launch {
            try {
                _isGenerating.value = true
                _generatorError.value = null

                val payload = CreateAccessCodePayload(
                    code = code,
                    studentName = name,
                    maxDevices = _genMaxDevices.value,
                    isActive = true,
                    note = _genNote.value.ifBlank { null }
                )

                val result = repository.createAccessCode(payload)
                if (result.isSuccess) {
                    val created = result.getOrNull()
                    _generatedSuccessCode.value = created
                    _genStudentName.value = ""
                    _genNote.value = ""
                    generateRandomCode("MAX")
                    loadCodes()
                    refreshStats()
                    showToast("এক্সেস কোড তৈরি সম্পন্ন হয়েছে! (Code created successfully)")
                } else {
                    _generatorError.value = result.exceptionOrNull()?.message ?: "কোড তৈরি করতে ব্যর্থ হয়েছে।"
                }
            } catch (e: Throwable) {
                _generatorError.value = e.message ?: "ত্রুটি ঘটেছে।"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun dismissSuccessDialog() {
        _generatedSuccessCode.value = null
    }

    // Code Management Functions
    fun setCodeSearchQuery(query: String) {
        _codeSearchQuery.value = query
    }

    fun setCodeFilter(filter: String) {
        _codeFilter.value = filter
    }

    fun loadCodes() {
        viewModelScope.launch {
            try {
                _isLoadingCodes.value = true
                val result = repository.getAccessCodes(forceRefresh = true)
                if (result.isSuccess) {
                    _codes.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Throwable) {
                // Silently fallback
            } finally {
                _isLoadingCodes.value = false
            }
        }
    }

    fun toggleCodeStatus(code: AccessCode) {
        val newStatus = !code.isActive
        viewModelScope.launch {
            try {
                val result = repository.toggleCodeStatus(code.id, newStatus)
                if (result.isSuccess) {
                    loadCodes()
                    refreshStats()
                    val statusText = if (newStatus) "সক্রিয় (Active)" else "নিষ্ক্রিয় (Disabled)"
                    showToast("${code.code} এখন $statusText")
                }
            } catch (e: Throwable) {
                showToast("স্ট্যাটাস আপডেট ব্যর্থ হয়েছে।")
            }
        }
    }

    fun extendCodeValidity(code: AccessCode, extraDays: Int = 30) {
        viewModelScope.launch {
            try {
                val result = repository.extendAccessCodeValidity(code.id, extraDays)
                if (result.isSuccess) {
                    loadCodes()
                    refreshStats()
                    showToast("✅ ${code.code}-এর মেয়াদ +$extraDays দিন বাড়ানো হয়েছে!")
                }
            } catch (e: Throwable) {
                showToast("মেয়াদ বাড়াতে ব্যর্থ হয়েছে।")
            }
        }
    }

    fun resetDevicesForCode(code: AccessCode) {
        viewModelScope.launch {
            try {
                val result = repository.resetDevicesForCode(code.id, code.code)
                if (result.isSuccess) {
                    loadJoinedDevices()
                    refreshStats()
                    if (_selectedCodeForDevices.value?.id == code.id) {
                        openLinkedDevices(code)
                    }
                    showToast("🔄 ${code.code}-এর সকল ডিভাইস রিসেট (Unbind) করা হয়েছে!")
                }
            } catch (e: Throwable) {
                showToast("ডিভাইস রিসেট ব্যর্থ হয়েছে।")
            }
        }
    }

    fun deleteCode(code: AccessCode) {
        viewModelScope.launch {
            try {
                val result = repository.deleteAccessCode(code.id)
                if (result.isSuccess) {
                    loadCodes()
                    loadJoinedDevices()
                    refreshStats()
                    showToast("${code.code} কোডটি মুছে ফেলা হয়েছে। (Code deleted)")
                }
            } catch (e: Throwable) {
                showToast("মুছে ফেলতে ব্যর্থ হয়েছে।")
            }
        }
    }

    fun openLinkedDevices(code: AccessCode) {
        _selectedCodeForDevices.value = code
        viewModelScope.launch {
            try {
                _isLoadingLinkedDevices.value = true
                val result = repository.getDevicesForCode(code.id)
                _linkedDevices.value = result.getOrNull() ?: emptyList()
            } catch (e: Throwable) {
                _linkedDevices.value = emptyList()
            } finally {
                _isLoadingLinkedDevices.value = false
            }
        }
    }

    fun closeLinkedDevices() {
        _selectedCodeForDevices.value = null
        _linkedDevices.value = emptyList()
    }

    // Device Management & Remote Kill-Switch Functions
    fun setDeviceSearchQuery(query: String) {
        _deviceSearchQuery.value = query
    }

    fun setDeviceFilter(filter: String) {
        _deviceFilter.value = filter
    }

    fun loadJoinedDevices() {
        viewModelScope.launch {
            try {
                _isLoadingDevices.value = true
                val result = repository.getJoinedDevices()
                if (result.isSuccess) {
                    _joinedDevices.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Throwable) {
                // Silently fallback
            } finally {
                _isLoadingDevices.value = false
            }
        }
    }

    fun toggleDeviceBlocked(item: DeviceWithStudent) {
        val newBlockedState = !item.device.isBlocked
        viewModelScope.launch {
            try {
                val result = repository.toggleDeviceBlocked(item.device.id, newBlockedState)
                if (result.isSuccess) {
                    loadJoinedDevices()
                    refreshStats()
                    val action = if (newBlockedState) "ব্লক করা হয়েছে! কিল-সুইচ সক্রিয় (Device Blocked!)" else "আনব্লক করা হয়েছে (Device Unblocked)"
                    showToast("${item.device.deviceModel}: $action")
                }
            } catch (e: Throwable) {
                showToast("ডিভাইস স্ট্যাটাস আপডেট ব্যর্থ হয়েছে।")
            }
        }
    }

    fun unbindDevice(item: DeviceWithStudent) {
        viewModelScope.launch {
            try {
                val result = repository.unbindDevice(item.device.id)
                if (result.isSuccess) {
                    loadJoinedDevices()
                    refreshStats()
                    _selectedCodeForDevices.value?.let { openLinkedDevices(it) }
                    showToast("${item.device.deviceModel} আনবাইন্ড সম্পন্ন হয়েছে। নতুন ডিভাইস যুক্ত করা যাবে।")
                }
            } catch (e: Throwable) {
                showToast("আনবাইন্ড ব্যর্থ হয়েছে।")
            }
        }
    }

    // Real Supabase Devices (public.devices) & Shikho Accounts (public.device_shikho_accounts) Operations
    fun loadUserDevices(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoadingDevices.value = true
                val result = repository.getUserDevices(forceRefresh)
                if (result.isSuccess) {
                    _userDevices.value = result.getOrNull() ?: emptyList()
                }
                val accountsResult = repository.getDeviceShikhoAccounts()
                if (accountsResult.isSuccess) {
                    _deviceShikhoAccounts.value = accountsResult.getOrNull() ?: emptyList()
                }
            } catch (e: Throwable) {
                // Silently fallback
            } finally {
                _isLoadingDevices.value = false
            }
        }
    }

    private fun dispatchStudentPushForDevice(
        device: UserDevice,
        title: String,
        message: String,
        eventType: String,
        extraData: Map<String, Any?> = emptyMap()
    ) {
        viewModelScope.launch {
            try {
                val profile = com.example.util.StudentDeviceMatcher.resolveStudentProfileForDevice(
                    device = device,
                    enrollments = _courseAccessEnrollments.value,
                    transactions = _coursePaymentTransactions.value
                )
                val phone = device.extractedPhoneFromDevice.ifBlank { profile?.studentPhone.orEmpty() }
                repository.notifyStudentViaOneSignal(
                    oneSignalPlayerId = device.onesignalPlayerId.orEmpty(),
                    deviceHash = device.deviceHash.ifBlank { device.displayDeviceId.ifBlank { device.id } },
                    studentPhone = phone,
                    title = title,
                    message = message,
                    eventType = eventType,
                    extraData = extraData
                )
            } catch (_: Throwable) {
            }
        }
    }

    private fun dispatchStudentPushForPhoneOrDevice(
        studentPhone: String,
        deviceIdOrHash: String = "",
        title: String,
        message: String,
        eventType: String,
        extraData: Map<String, Any?> = emptyMap()
    ) {
        viewModelScope.launch {
            try {
                val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(studentPhone)
                val matchedEnrollment = _courseAccessEnrollments.value.firstOrNull { enr ->
                    (cleanPhone.isNotBlank() && CourseAccessEnrollment.normalizeBdPhone(enr.effectivePhone) == cleanPhone) ||
                        (deviceIdOrHash.isNotBlank() && (
                            enr.deviceHash.equals(deviceIdOrHash, ignoreCase = true) ||
                                enr.displayDeviceId.equals(deviceIdOrHash, ignoreCase = true) ||
                                enr.deviceId.equals(deviceIdOrHash, ignoreCase = true)
                            ))
                }
                val matchedDevice = _userDevices.value.firstOrNull { dev ->
                    (deviceIdOrHash.isNotBlank() && (
                        dev.deviceHash.equals(deviceIdOrHash, ignoreCase = true) ||
                            dev.displayDeviceId.equals(deviceIdOrHash, ignoreCase = true) ||
                            dev.id.equals(deviceIdOrHash, ignoreCase = true)
                        )) ||
                        (cleanPhone.isNotBlank() && dev.extractedPhoneFromDevice == cleanPhone)
                }
                val playerId = matchedEnrollment?.onesignalPlayerId?.takeIf { it.isNotBlank() }
                    ?: matchedDevice?.onesignalPlayerId.orEmpty()
                val resolvedHash = matchedDevice?.deviceHash?.takeIf { it.isNotBlank() }
                    ?: matchedEnrollment?.deviceHash?.takeIf { it.isNotBlank() }
                    ?: deviceIdOrHash.ifBlank {
                        matchedDevice?.displayDeviceId?.takeIf { it.isNotBlank() }
                            ?: matchedEnrollment?.effectiveDeviceId.orEmpty()
                    }

                repository.notifyStudentViaOneSignal(
                    oneSignalPlayerId = playerId,
                    deviceHash = resolvedHash,
                    studentPhone = cleanPhone.ifBlank { studentPhone.trim() },
                    title = title,
                    message = message,
                    eventType = eventType,
                    extraData = extraData
                )
            } catch (_: Throwable) {
            }
        }
    }

    fun approveUserDeviceVerification(device: UserDevice, studentNote: String? = null) {
        viewModelScope.launch {
            try {
                // Optimistic UI update
                _userDevices.value = _userDevices.value.map { existing ->
                    if (existing.effectiveKey == device.effectiveKey) {
                        existing.copy(
                            status = "active",
                            isBanned = false,
                            isBlockedColumn = false,
                            banReason = "",
                            rejectionReasonColumn = "",
                            studentNote = studentNote?.trim() ?: existing.studentNote
                        )
                    } else {
                        existing
                    }
                }
                val result = repository.approveUserDeviceVerification(device, studentNote)
                if (result.isSuccess) {
                    dispatchStudentPushForDevice(
                        device = device,
                        title = "✅ আপনার ডিভাইস অনুমোদিত হয়েছে!",
                        message = "অভিনন্দন! আপনার ডিভাইস ভেরিফিকেশন সফলভাবে অনুমোদন করা হয়েছে। এখন আপনি অ্যাপ ব্যবহার করতে পারবেন।",
                        eventType = "DEVICE_APPROVED",
                        extraData = mapOf(
                            "status" to "active",
                            "display_device_id" to device.displayDeviceId
                        )
                    )
                    loadUserDevices(forceRefresh = true)
                    refreshStats()
                    showToast("✅ ডিভাইসটি ভেরিফাই ও অ্যাপ্রুভ (Active) করা হয়েছে!")
                } else {
                    loadUserDevices(forceRefresh = true)
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "অ্যাপ্রুভ করতে ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                loadUserDevices(forceRefresh = true)
                showToast("অ্যাপ্রুভ করতে ব্যর্থ হয়েছে: ${e.message}")
            }
        }
    }

    fun setUserDeviceBlockStatus(device: UserDevice, isBlocked: Boolean, banReason: String? = null) {
        viewModelScope.launch {
            try {
                val targetStatus = if (isBlocked) "blocked" else "active"
                val cleanReason = if (isBlocked) {
                    banReason?.trim()?.ifBlank { "অ্যাডমিন কর্তৃক ব্লক করা হয়েছে" } ?: "অ্যাডমিন কর্তৃক ব্লক করা হয়েছে"
                } else {
                    ""
                }
                _userDevices.value = _userDevices.value.map { existing ->
                    if (existing.effectiveKey == device.effectiveKey) {
                        existing.copy(
                            status = targetStatus,
                            isBanned = isBlocked,
                            isBlockedColumn = isBlocked,
                            banReason = cleanReason,
                            rejectionReasonColumn = cleanReason
                        )
                    } else {
                        existing
                    }
                }
                val result = repository.setUserDeviceBlockStatus(device, isBlocked, cleanReason)
                if (result.isSuccess) {
                    if (isBlocked) {
                        dispatchStudentPushForDevice(
                            device = device,
                            title = "🚫 আপনার ডিভাইস ব্লক করা হয়েছে",
                            message = "কারণ: $cleanReason। বিস্তারিত জানতে অ্যাডমিন সাপোর্টে যোগাযোগ করুন।",
                            eventType = "DEVICE_BLOCKED",
                            extraData = mapOf(
                                "status" to "blocked",
                                "ban_reason" to cleanReason,
                                "display_device_id" to device.displayDeviceId
                            )
                        )
                    } else {
                        dispatchStudentPushForDevice(
                            device = device,
                            title = "🔓 আপনার ডিভাইস আনব্লক করা হয়েছে!",
                            message = "আপনার ডিভাইসটি পুনরায় সক্রিয় (Active) করা হয়েছে। এখন আপনি ক্লাস চালিয়ে যেতে পারবেন।",
                            eventType = "DEVICE_UNBLOCKED",
                            extraData = mapOf(
                                "status" to "active",
                                "display_device_id" to device.displayDeviceId
                            )
                        )
                    }
                    loadUserDevices(forceRefresh = true)
                    refreshStats()
                    val msg = if (isBlocked) {
                        "🚫 ডিভাইসটি সফলভাবে ব্লক করা হয়েছে!"
                    } else {
                        "🔓 ডিভাইসটি আনব্লক ও অ্যাপ্রুভ (Active) করা হয়েছে!"
                    }
                    showToast(msg)
                } else {
                    loadUserDevices(forceRefresh = true)
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "ডিভাইস ব্লক স্ট্যাটাস সেভ হয়নি"}")
                }
            } catch (e: Throwable) {
                loadUserDevices(forceRefresh = true)
                showToast("ডিভাইস স্ট্যাটাস আপডেট ব্যর্থ হয়েছে: ${e.message}")
            }
        }
    }

    fun revokeUserDeviceToPending(device: UserDevice) {
        viewModelScope.launch {
            try {
                _userDevices.value = _userDevices.value.map { existing ->
                    if (existing.effectiveKey == device.effectiveKey) {
                        existing.copy(
                            status = "pending",
                            isBanned = false,
                            isBlockedColumn = false,
                            banReason = "",
                            rejectionReasonColumn = ""
                        )
                    } else {
                        existing
                    }
                }
                val result = repository.revokeUserDeviceToPending(device)
                if (result.isSuccess) {
                    dispatchStudentPushForDevice(
                        device = device,
                        title = "⏳ ডিভাইস ভেরিফিকেশন পেন্ডিং করা হয়েছে",
                        message = "আপনার ডিভাইসের এক্সেস সাময়িকভাবে পেন্ডিং (Pending) তালিকায় নেওয়া হয়েছে।",
                        eventType = "DEVICE_REVOKED_TO_PENDING",
                        extraData = mapOf(
                            "status" to "pending",
                            "display_device_id" to device.displayDeviceId
                        )
                    )
                    loadUserDevices(forceRefresh = true)
                    refreshStats()
                    showToast("⏳ ডিভাইসটি আবার পেন্ডিং (Pending) তালিকায় নেওয়া হয়েছে।")
                } else {
                    loadUserDevices(forceRefresh = true)
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "পেন্ডিং করতে ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                loadUserDevices(forceRefresh = true)
                showToast("স্ট্যাটাস পরিবর্তন ব্যর্থ হয়েছে।")
            }
        }
    }

    fun updateUserDeviceStudentNote(device: UserDevice, studentNote: String) {
        viewModelScope.launch {
            try {
                val trimmed = studentNote.trim()
                _userDevices.value = _userDevices.value.map { existing ->
                    if (existing.effectiveKey == device.effectiveKey) {
                        existing.copy(studentNote = trimmed)
                    } else {
                        existing
                    }
                }
                val result = repository.updateUserDeviceStudentNote(device, trimmed)
                if (result.isSuccess) {
                    loadUserDevices(forceRefresh = true)
                    showToast("✅ শিক্ষার্থীর নাম/নোট সেভ করা হয়েছে!")
                } else {
                    loadUserDevices(forceRefresh = true)
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "নোট সেভ করতে ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                loadUserDevices(forceRefresh = true)
                showToast("নোট সেভ করতে ব্যর্থ হয়েছে।")
            }
        }
    }

    fun toggleUserDeviceBan(device: UserDevice, banReason: String? = null) {
        val willBlock = !device.isBlockedDevice
        setUserDeviceBlockStatus(device, isBlocked = willBlock, banReason = banReason)
    }

    fun toggleDeviceCanViewProfiles(device: UserDevice) {
        val nextValue = !device.canViewProfiles
        viewModelScope.launch {
            try {
                _userDevices.value = _userDevices.value.map { existing ->
                    if (existing.effectiveKey == device.effectiveKey) {
                        existing.copy(canViewProfiles = nextValue)
                    } else {
                        existing
                    }
                }
                val result = repository.toggleDeviceCanViewProfiles(device.effectiveKey, nextValue)
                if (result.isSuccess) {
                    loadUserDevices(forceRefresh = true)
                    val msg = if (nextValue) {
                        "✅ এই ডিভাইসে প্রোফাইল দেখা অনুমোদিত (can_view_profiles = ON) করা হয়েছে!"
                    } else {
                        "🔒 এই ডিভাইসে প্রোফাইল দেখা বন্ধ (can_view_profiles = OFF) করা হয়েছে!"
                    }
                    showToast(msg)
                } else {
                    loadUserDevices(forceRefresh = true)
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "পারমিশন আপডেট ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                loadUserDevices(forceRefresh = true)
                showToast("প্রোফাইল পারমিশন আপডেট ব্যর্থ হয়েছে।")
            }
        }
    }

    fun deleteUserDevice(device: UserDevice) {
        viewModelScope.launch {
            try {
                val result = repository.deleteUserDevice(device.effectiveKey)
                if (result.isSuccess) {
                    loadUserDevices(forceRefresh = true)
                    refreshStats()
                    showToast("ডিভাইস রেকর্ড স্থায়ীভাবে মুছে ফেলা হয়েছে।")
                }
            } catch (e: Throwable) {
                showToast("মুছে ফেলতে ব্যর্থ হয়েছে।")
            }
        }
    }

    fun setDeviceCoursePermissions(
        device: UserDevice,
        isAccessRestricted: Boolean,
        allowedClasses: List<String>,
        allowedGroups: List<String>,
        allowedBatches: List<String>,
        allowedPrograms: List<String>
    ) {
        viewModelScope.launch {
            try {
                val result = repository.setDeviceCoursePermissions(
                    deviceHash = device.effectiveDeviceHash.ifBlank { device.effectiveKey },
                    isRestricted = isAccessRestricted,
                    allowedClasses = allowedClasses,
                    allowedGroups = allowedGroups,
                    allowedBatches = allowedBatches,
                    allowedPrograms = allowedPrograms
                )
                if (result.isSuccess) {
                    loadUserDevices(forceRefresh = true)
                    showToast("✅ ডিভাইসের কোর্স ও ক্লাস পারমিশন সফলভাবে সেভ করা হয়েছে!")
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "ডিভাইস পারমিশন সেভ ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            }
        }
    }

    fun setAccountCoursePermissions(
        deviceHash: String,
        userPhoneOrId: String,
        isAccessGranted: Boolean,
        isAccessRestricted: Boolean,
        allowedClasses: List<String>,
        allowedGroups: List<String>,
        allowedBatches: List<String>,
        allowedPrograms: List<String>
    ) {
        viewModelScope.launch {
            try {
                val result = repository.setAccountCoursePermissions(
                    deviceHash = deviceHash,
                    userPhone = userPhoneOrId,
                    isAccessGranted = isAccessGranted,
                    isRestricted = isAccessRestricted,
                    allowedClasses = allowedClasses,
                    allowedGroups = allowedGroups,
                    allowedBatches = allowedBatches,
                    allowedPrograms = allowedPrograms
                )
                if (result.isSuccess) {
                    loadUserDevices(forceRefresh = true)
                    showToast("✅ শিখো অ্যাকাউন্ট পারমিশন সফলভাবে আপডেট করা হয়েছে!")
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "অ্যাকাউন্ট পারমিশন আপডেট ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            }
        }
    }

    // App Updates Management Functions
    fun loadAppUpdates() {
        viewModelScope.launch {
            try {
                _isLoadingUpdates.value = true
                val result = repository.getAppUpdates(forceRefresh = true)
                if (result.isSuccess) {
                    _appUpdates.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Throwable) {
                // Silently fallback
            } finally {
                _isLoadingUpdates.value = false
            }
        }
    }

    fun openCreateUpdateDialog() {
        _editingUpdateId.value = null
        _updateVersionName.value = "v6.1.0"
        _updateVersionCode.value = "60100"
        _updateMinVersionCode.value = "60000"
        _updateIsForce.value = false
        _updateTitle.value = "নতুন আপডেট উপলভ্য!"
        _updateChangelog.value = "• বাগ ফিক্স এবং পারফরম্যান্স উন্নতি\n• নতুন সিকিউরিটি ফিচার"
        _updateDownloadUrl.value = ""
        _updateButtonText.value = "এখনই আপডেট করুন"
        _updateIsActive.value = true
        _isUpdateDialogOpen.value = true
    }

    fun openEditUpdateDialog(update: AppUpdate) {
        _editingUpdateId.value = update.id
        _updateVersionName.value = update.latestVersionName
        _updateVersionCode.value = update.latestVersionCode.toString()
        _updateMinVersionCode.value = update.minSupportedVersionCode.toString()
        _updateIsForce.value = update.isForceUpdate
        _updateTitle.value = update.title
        _updateChangelog.value = update.changelog
        _updateDownloadUrl.value = update.downloadUrl
        _updateButtonText.value = update.buttonText
        _updateIsActive.value = update.isActive
        _isUpdateDialogOpen.value = true
    }

    fun closeUpdateDialog() {
        _isUpdateDialogOpen.value = false
        _editingUpdateId.value = null
    }

    fun setUpdateVersionName(v: String) { _updateVersionName.value = v }
    fun setUpdateVersionCode(v: String) { _updateVersionCode.value = v }
    fun setUpdateMinVersionCode(v: String) { _updateMinVersionCode.value = v }
    fun setUpdateIsForce(v: Boolean) { _updateIsForce.value = v }
    fun setUpdateTitle(v: String) { _updateTitle.value = v }
    fun setUpdateChangelog(v: String) { _updateChangelog.value = v }
    fun setUpdateDownloadUrl(v: String) { _updateDownloadUrl.value = v }
    fun setUpdateButtonText(v: String) { _updateButtonText.value = v }
    fun setUpdateIsActive(v: Boolean) { _updateIsActive.value = v }

    fun saveAppUpdate() {
        val versionName = _updateVersionName.value.trim()
        val versionCodeInt = _updateVersionCode.value.trim().toIntOrNull()
        val minVersionCodeInt = _updateMinVersionCode.value.trim().toIntOrNull()
        val title = _updateTitle.value.trim()
        val changelog = _updateChangelog.value.trim()
        val downloadUrl = _updateDownloadUrl.value.trim()
        val buttonText = _updateButtonText.value.trim().ifBlank { "এখনই আপডেট করুন" }

        if (versionName.isBlank()) {
            showToast("ভার্সন নাম লিখুন (e.g. v6.1.0)")
            return
        }
        if (versionCodeInt == null) {
            showToast("সঠিক ভার্সন কোড (ইনটিজার) দিন")
            return
        }
        if (minVersionCodeInt == null) {
            showToast("সঠিক সর্বনিম্ন সমর্থিত ভার্সন কোড দিন")
            return
        }
        if (downloadUrl.isBlank() || (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://"))) {
            showToast("সঠিক ডাউনলোড URL দিন (http:// বা https://)")
            return
        }

        viewModelScope.launch {
            val payload = CreateAppUpdatePayload(
                latestVersionName = versionName,
                latestVersionCode = versionCodeInt,
                minSupportedVersionCode = minVersionCodeInt,
                isForceUpdate = _updateIsForce.value,
                title = title.ifBlank { "নতুন আপডেট উপলভ্য!" },
                changelog = changelog,
                downloadUrl = downloadUrl,
                buttonText = buttonText,
                isActive = _updateIsActive.value
            )

            val editId = _editingUpdateId.value
            val result = if (editId == null) {
                repository.createAppUpdate(payload)
            } else {
                repository.updateAppUpdate(editId, payload)
            }

            if (result.isSuccess) {
                closeUpdateDialog()
                loadAppUpdates()
                showToast("অ্যাপ আপডেট সফলভাবে সংরক্ষণ করা হয়েছে! (Saved)")
            } else {
                showToast("আপডেট সংরক্ষণ করতে ব্যর্থ হয়েছে।")
            }
        }
    }

    fun toggleAppUpdateStatus(update: AppUpdate) {
        val newStatus = !update.isActive
        viewModelScope.launch {
            val result = repository.toggleAppUpdateStatus(update.id, newStatus)
            if (result.isSuccess) {
                loadAppUpdates()
                val statusText = if (newStatus) "সক্রিয় (Active)" else "নিষ্ক্রিয় (Inactive)"
                showToast("আপডেট ${update.latestVersionName} এখন $statusText")
            } else {
                showToast("স্ট্যাটাস পরিবর্তন ব্যর্থ হয়েছে।")
            }
        }
    }

    fun deleteAppUpdate(update: AppUpdate) {
        viewModelScope.launch {
            val result = repository.deleteAppUpdate(update.id)
            if (result.isSuccess) {
                loadAppUpdates()
                showToast("আপডেট ${update.latestVersionName} মুছে ফেলা হয়েছে। (Deleted)")
            } else {
                showToast("মুছে ফেলতে ব্যর্থ হয়েছে।")
            }
        }
    }

    // Notice Management Functions
    fun loadAppNotices(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoadingNotices.value = true
                val result = repository.getAppNotices(forceRefresh)
                if (result.isSuccess) {
                    _appNotices.value = result.getOrNull() ?: emptyList()
                } else {
                    showToast("নোটিশ লোড করতে সমস্যা হয়েছে: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isLoadingNotices.value = false
            }
        }
    }

    fun setNoticeSearchQuery(query: String) {
        _noticeSearchQuery.value = query
    }

    fun setNoticeFilter(filter: String) {
        _noticeFilter.value = filter
    }

    fun openCreateNoticeDialog() {
        _editingNoticeId.value = null
        _noticeTitle.value = ""
        _noticeDescription.value = ""
        _noticeImageUrl.value = ""
        _noticeActionUrl.value = ""
        _noticeActionButtonText.value = "বিস্তারিত দেখুন"
        _noticePriority.value = "0"
        _noticeIsActive.value = true
        _noticeShowAsPopup.value = true
        _isNoticeDialogOpen.value = true
    }

    fun openEditNoticeDialog(notice: AppNotice) {
        _editingNoticeId.value = notice.id
        _noticeTitle.value = notice.title
        _noticeDescription.value = notice.description ?: ""
        _noticeImageUrl.value = notice.imageUrl
        _noticeActionUrl.value = notice.actionUrl ?: ""
        _noticeActionButtonText.value = notice.actionButtonText.ifBlank { "বিস্তারিত দেখুন" }
        _noticePriority.value = notice.priority.toString()
        _noticeIsActive.value = notice.isActive
        _noticeShowAsPopup.value = notice.showAsPopup
        _isNoticeDialogOpen.value = true
    }

    fun closeNoticeDialog() {
        _isNoticeDialogOpen.value = false
    }

    fun setNoticeTitle(title: String) { _noticeTitle.value = title }
    fun setNoticeDescription(desc: String) { _noticeDescription.value = desc }
    fun setNoticeImageUrl(url: String) { _noticeImageUrl.value = url }
    fun setNoticeActionUrl(url: String) { _noticeActionUrl.value = url }
    fun setNoticeActionButtonText(txt: String) { _noticeActionButtonText.value = txt }
    fun setNoticePriority(priority: String) { _noticePriority.value = priority }
    fun setNoticeIsActive(active: Boolean) { _noticeIsActive.value = active }
    fun setNoticeShowAsPopup(popup: Boolean) { _noticeShowAsPopup.value = popup }

    fun openPreviewModal(notice: AppNotice) {
        _previewNotice.value = notice
    }

    fun closePreviewModal() {
        _previewNotice.value = null
    }

    fun saveAppNotice() {
        val title = _noticeTitle.value.trim()
        val imageUrl = _noticeImageUrl.value.trim()
        val priorityInt = _noticePriority.value.trim().toIntOrNull() ?: 0

        if (title.isBlank()) {
            showToast("দয়া করে নোটিশের শিরোনাম (Title) লিখুন")
            return
        }

        if (imageUrl.isBlank()) {
            showToast("দয়া করে নোটিশের ছবির URL (Image URL) দিন")
            return
        }

        val payload = CreateAppNoticePayload(
            title = title,
            description = _noticeDescription.value.trim().ifBlank { null },
            imageUrl = imageUrl,
            actionUrl = _noticeActionUrl.value.trim().ifBlank { null },
            actionButtonText = _noticeActionButtonText.value.trim().ifBlank { "বিস্তারিত দেখুন" },
            priority = priorityInt,
            isActive = _noticeIsActive.value,
            showAsPopup = _noticeShowAsPopup.value
        )

        viewModelScope.launch {
            val editingId = _editingNoticeId.value
            if (editingId == null) {
                val result = repository.createAppNotice(payload)
                if (result.isSuccess) {
                    closeNoticeDialog()
                    loadAppNotices(forceRefresh = true)
                    showToast("নতুন নোটিশ সফলভাবে তৈরি হয়েছে! (Notice published)")
                } else {
                    showToast("নোটিশ সেভ করতে ব্যর্থ: ${result.exceptionOrNull()?.message}")
                }
            } else {
                val result = repository.updateAppNotice(editingId, payload)
                if (result.isSuccess) {
                    closeNoticeDialog()
                    loadAppNotices(forceRefresh = true)
                    showToast("নোটিশ সফলভাবে আপডেট করা হয়েছে! (Notice updated)")
                } else {
                    showToast("নোটিশ আপডেট ব্যর্থ: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun toggleAppNoticeStatus(notice: AppNotice) {
        viewModelScope.launch {
            val newStatus = !notice.isActive
            val result = repository.toggleAppNoticeStatus(notice.id, newStatus)
            if (result.isSuccess) {
                loadAppNotices()
                val statusText = if (newStatus) "সক্রিয় (Active)" else "নিষ্ক্রিয় (Inactive)"
                showToast("নোটিশ এখন $statusText")
            } else {
                showToast("স্ট্যাটাস পরিবর্তন ব্যর্থ হয়েছে।")
            }
        }
    }

    fun deleteAppNotice(notice: AppNotice) {
        viewModelScope.launch {
            val result = repository.deleteAppNotice(notice.id)
            if (result.isSuccess) {
                loadAppNotices()
                showToast("নোটিশ মুছে ফেলা হয়েছে। (Deleted)")
            } else {
                showToast("মুছে ফেলতে ব্যর্থ হয়েছে।")
            }
        }
    }

    // Activation Support Links Operations
    fun loadActivationSupportLinks(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoadingSupportLinks.value = true
                val result = repository.getActivationSupportLinks(forceRefresh)
                if (result.isSuccess) {
                    _supportLinks.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Throwable) {
                // Keep cached
            } finally {
                _isLoadingSupportLinks.value = false
            }
        }
    }

    fun setSupportLinkSearchQuery(query: String) {
        _supportLinkSearchQuery.value = query
    }

    fun setSupportLinkFilter(filter: String) {
        _supportLinkFilter.value = filter
    }

    fun openCreateSupportLinkDialog() {
        _editingSupportLinkId.value = null
        _supportLinkTitle.value = ""
        _supportLinkSubtitle.value = ""
        _supportLinkIconType.value = "telegram"
        _supportLinkUrl.value = "https://t.me/"
        _supportLinkColorHex.value = "#229ED9"
        _supportLinkPriority.value = "10"
        _supportLinkIsActive.value = true
        _isSupportLinkDialogOpen.value = true
    }

    fun openEditSupportLinkDialog(link: ActivationSupportLink) {
        _editingSupportLinkId.value = link.id
        _supportLinkTitle.value = link.title
        _supportLinkSubtitle.value = link.subtitle ?: ""
        _supportLinkIconType.value = link.iconType
        _supportLinkUrl.value = link.url
        _supportLinkColorHex.value = link.colorHex ?: getDefaultColorForType(link.iconType)
        _supportLinkPriority.value = link.priority.toString()
        _supportLinkIsActive.value = link.isActive
        _isSupportLinkDialogOpen.value = true
    }

    fun closeSupportLinkDialog() {
        _isSupportLinkDialogOpen.value = false
    }

    fun setSupportLinkTitle(title: String) { _supportLinkTitle.value = title }
    fun setSupportLinkSubtitle(sub: String) { _supportLinkSubtitle.value = sub }
    fun setSupportLinkIconType(type: String) {
        _supportLinkIconType.value = type
        _supportLinkColorHex.value = getDefaultColorForType(type)
    }
    fun setSupportLinkUrl(url: String) { _supportLinkUrl.value = url }
    fun setSupportLinkColorHex(hex: String) { _supportLinkColorHex.value = hex }
    fun setSupportLinkPriority(priority: String) { _supportLinkPriority.value = priority }
    fun setSupportLinkIsActive(active: Boolean) { _supportLinkIsActive.value = active }

    private fun getDefaultColorForType(type: String): String {
        return when (type.lowercase()) {
            "telegram" -> "#229ED9"
            "whatsapp" -> "#25D366"
            "facebook" -> "#1877F2"
            "phone" -> "#10B981"
            "website" -> "#0EA5E9"
            else -> "#3B82F6"
        }
    }

    fun saveSupportLink() {
        val title = _supportLinkTitle.value.trim()
        val url = _supportLinkUrl.value.trim()
        val priorityInt = _supportLinkPriority.value.trim().toIntOrNull() ?: 0

        if (title.isBlank()) {
            showToast("দয়া করে সাপোর্ট লিংকের শিরোনাম (Title) লিখুন")
            return
        }

        if (url.isBlank()) {
            showToast("দয়া করে সাপোর্ট লিংকের URL বা ফোন নম্বর দিন")
            return
        }

        val payload = CreateSupportLinkPayload(
            title = title,
            subtitle = _supportLinkSubtitle.value.trim().ifBlank { null },
            iconType = _supportLinkIconType.value.trim().lowercase(),
            url = url,
            colorHex = _supportLinkColorHex.value.trim().ifBlank { null },
            isActive = _supportLinkIsActive.value,
            priority = priorityInt
        )

        viewModelScope.launch {
            val editingId = _editingSupportLinkId.value
            if (editingId == null) {
                val result = repository.createActivationSupportLink(payload)
                if (result.isSuccess) {
                    closeSupportLinkDialog()
                    loadActivationSupportLinks(forceRefresh = true)
                    showToast("নতুন সাপোর্ট লিংক তৈরি হয়েছে! (Support link added)")
                } else {
                    showToast("লিংক সেভ করতে ব্যর্থ: ${result.exceptionOrNull()?.message}")
                }
            } else {
                val result = repository.updateActivationSupportLink(editingId, payload)
                if (result.isSuccess) {
                    closeSupportLinkDialog()
                    loadActivationSupportLinks(forceRefresh = true)
                    showToast("সাপোর্ট লিংক আপডেট করা হয়েছে! (Support link updated)")
                } else {
                    showToast("লিংক আপডেট ব্যর্থ: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun toggleSupportLinkStatus(link: ActivationSupportLink) {
        viewModelScope.launch {
            val newStatus = !link.isActive
            val result = repository.toggleActivationSupportLinkStatus(link.id, newStatus)
            if (result.isSuccess) {
                loadActivationSupportLinks()
                val statusText = if (newStatus) "সক্রিয় (Active)" else "নিষ্ক্রিয় (Inactive)"
                showToast("সাপোর্ট লিংক এখন $statusText")
            } else {
                showToast("স্ট্যাটাস পরিবর্তন ব্যর্থ হয়েছে।")
            }
        }
    }

    fun deleteSupportLink(link: ActivationSupportLink) {
        viewModelScope.launch {
            val result = repository.deleteActivationSupportLink(link.id)
            if (result.isSuccess) {
                loadActivationSupportLinks()
                showToast("সাপোর্ট লিংক মুছে ফেলা হয়েছে। (Deleted)")
            } else {
                showToast("মুছে ফেলতে ব্যর্থ হয়েছে।")
            }
        }
    }

    // Settings Functions
    fun updateSupabaseUrl(url: String) {
        _supabaseUrl.value = url
    }

    fun updateSupabaseKey(key: String) {
        _supabaseKey.value = key
    }

    fun saveSupabaseSettings() {
        adminPreferences.setSupabaseUrl(_supabaseUrl.value)
        adminPreferences.setSupabaseKey(_supabaseKey.value)
        _supabaseUrl.value = adminPreferences.getSupabaseUrl()
        _supabaseKey.value = adminPreferences.getSupabaseKey()
        showToast("সুপাবেস কনফিগারেশন সেভ হয়েছে! (Supabase config saved)")
        testSupabaseConnection()
    }

    fun testSupabaseConnection() {
        viewModelScope.launch {
            try {
                _isTestingConnection.value = true
                _connectionStatus.value = "কানেকশন পরীক্ষা করা হচ্ছে... (Testing connection)"
                _isConnectionSuccess.value = null

                val result = repository.testConnection()
                if (result.isSuccess) {
                    _isConnectionSuccess.value = true
                    _connectionStatus.value = "সফল! সুপাবেস ডাটাবেজের সাথে সংযোগ স্থাপিত হয়েছে। (Connected)"
                    loadAllData()
                } else {
                    _isConnectionSuccess.value = false
                    val err = result.exceptionOrNull()?.message ?: "সংযোগ ব্যর্থ হয়েছে।"
                    _connectionStatus.value = "সংযোগ ব্যর্থ: $err (Connection Failed)"
                }
            } catch (e: Throwable) {
                _isConnectionSuccess.value = false
                _connectionStatus.value = "ত্রুটি: ${e.message}"
            } finally {
                _isTestingConnection.value = false
            }
        }
    }

    // Cloudinary Media Uploader Operations
    fun selectMediaFile(uri: android.net.Uri?) {
        if (uri == null) return
        _selectedMediaUri.value = uri
        try {
            _selectedMediaMeta.value = cloudinaryClient.getFileMeta(uri)
        } catch (e: Exception) {
            _selectedMediaMeta.value = null
        }
    }

    fun clearSelectedMedia() {
        _selectedMediaUri.value = null
        _selectedMediaMeta.value = null
    }

    fun uploadSelectedMedia() {
        val uri = _selectedMediaUri.value ?: return
        viewModelScope.launch {
            try {
                _isUploadingMedia.value = true
                val result = cloudinaryClient.uploadFile(uri)
                if (result.isSuccess) {
                    val resp = result.getOrNull()
                    val secureUrl = resp?.secureUrl ?: resp?.url
                    if (resp != null && !secureUrl.isNullOrBlank()) {
                        _lastUploadedSecureUrl.value = secureUrl
                        val meta = _selectedMediaMeta.value
                        val sizeFormatted = formatFileSize(meta?.size ?: (resp.bytes ?: 0L))
                        val mimeType = meta?.mimeType ?: "image/jpeg"
                        val resourceType = resp.resourceType ?: if (mimeType.startsWith("video")) "video" else if (mimeType.contains("pdf")) "raw" else "image"
                        val timeFormatted = java.text.SimpleDateFormat("hh:mm a, dd MMM", java.util.Locale.getDefault()).format(java.util.Date())

                        val newItem = CloudinaryHistoryItem(
                            fileName = meta?.name ?: (resp.originalFilename ?: "uploaded_media"),
                            fileSizeFormatted = sizeFormatted,
                            mimeType = mimeType,
                            resourceType = resourceType,
                            secureUrl = secureUrl,
                            uploadedAtFormatted = timeFormatted
                        )
                        _mediaUploadHistory.value = listOf(newItem) + _mediaUploadHistory.value
                        showToast("✅ ফাইল সফলভাবে ক্লাউডনারিতে আপলোড হয়েছে!")
                    } else {
                        showToast("আপলোড হয়েছে কিন্তু ডিরেক্ট লিংক পাওয়া যায়নি।")
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "আপলোড ব্যর্থ হয়েছে"
                    showToast("ত্রুটি: $errorMsg")
                }
            } catch (e: Throwable) {
                showToast("আপলোড ব্যর্থ: ${e.message}")
            } finally {
                _isUploadingMedia.value = false
            }
        }
    }

    fun removeMediaHistoryItem(item: CloudinaryHistoryItem) {
        _mediaUploadHistory.value = _mediaUploadHistory.value.filter { it.id != item.id }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(java.util.Locale.US, "%.2f MB", mb)
        } else if (kb >= 1.0) {
            String.format(java.util.Locale.US, "%.1f KB", kb)
        } else {
            "$bytes B"
        }
    }

    // =========================================================================
    // Course Sales, Installment Plans & Payment Verification Operations
    // =========================================================================

    fun loadCourseSalesData() {
        viewModelScope.launch {
            try {
                _isLoadingCourseSales.value = true
                try {
                    repository.enforceOverdueCourseInstallments()
                } catch (_: Throwable) {}

                val enrollmentsRes = repository.getCourseAccessEnrollments()
                if (enrollmentsRes.isSuccess) {
                    _courseAccessEnrollments.value = enrollmentsRes.getOrNull().orEmpty()
                }

                val transactionsRes = repository.getCoursePaymentTransactions()
                if (transactionsRes.isSuccess) {
                    _coursePaymentTransactions.value = transactionsRes.getOrNull().orEmpty()
                }

                val settingsRes = repository.getCoursePaymentSettings()
                if (settingsRes.isSuccess) {
                    settingsRes.getOrNull()?.let { _coursePaymentSettings.value = it }
                }

                if (_isLiveNotificationMonitorEnabled.value) {
                    notificationHelper.checkAndNotifyNewEvents(
                        devices = _userDevices.value,
                        enrollments = _courseAccessEnrollments.value,
                        transactions = _coursePaymentTransactions.value
                    )
                }
            } catch (_: Throwable) {
                // Keep existing state on transient error
            } finally {
                _isLoadingCourseSales.value = false
            }
        }
    }

    fun approveCourseAccessRequest(
        payload: ApproveCourseAccessRequestRpcPayload,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isProcessingCourseSalesAction.value = true
                val reqPhone = payload.studentPhone.orEmpty()
                val existingEnrollment = _courseAccessEnrollments.value.firstOrNull {
                    it.id == payload.enrollmentId ||
                        (reqPhone.isNotBlank() && it.effectivePhone == CourseAccessEnrollment.normalizeBdPhone(reqPhone) &&
                            it.programId == payload.programId)
                }
                val result = repository.approveCourseAccessRequest(payload)
                if (result.isSuccess) {
                    val targetPhone = reqPhone.ifBlank {
                        payload.shikhoAccountKey.orEmpty().ifBlank { existingEnrollment?.effectivePhone.orEmpty() }
                    }
                    val targetDevice = existingEnrollment?.deviceId.orEmpty()
                        .ifBlank { existingEnrollment?.displayDeviceId.orEmpty() }
                    val courseTitle = existingEnrollment?.programTitle?.takeIf { it.isNotBlank() } ?: "কোর্স"

                    val (pushTitle, pushBody, eventType) = when {
                        payload.instantFreeUnlock -> Triple(
                            "🎉 আপনার কোর্স ফ্রি আনলক হয়েছে!",
                            "'$courseTitle' কোর্সটি অ্যাডমিন সম্পূর্ণ ফ্রি আনলক (ACTIVE) করে দিয়েছেন। এখনই ক্লাস শুরু করুন!",
                            "COURSE_FREE_UNLOCKED"
                        )
                        payload.trialEnabled && payload.startTrialImmediately -> Triple(
                            "🎁 আপনার ফ্রি ট্রায়াল চালু হয়েছে!",
                            "'$courseTitle' কোর্সে আপনার জন্য ${payload.trialDays} দিন ${payload.trialHours} ঘণ্টার ফ্রি ট্রায়াল চালু করা হয়েছে!",
                            "COURSE_TRIAL_STARTED"
                        )
                        payload.trialEnabled -> Triple(
                            "✅ কোর্স অনুমোদন ও ফ্রি ট্রায়াল অফার!",
                            "'$courseTitle' কোর্সের দাম ৳${payload.totalPrice} এবং ${payload.trialDays} দিন ${payload.trialHours} ঘণ্টার ফ্রি ট্রায়াল অনুমোদন দেওয়া হয়েছে।",
                            "COURSE_ACCESS_APPROVED"
                        )
                        else -> Triple(
                            "✅ আপনার কোর্স রিকোয়েস্ট অনুমোদিত হয়েছে!",
                            "'$courseTitle' কোর্সের দাম (৳${payload.totalPrice}) ও কিস্তি প্ল্যান নির্ধারণ করা হয়েছে। এখন পেমেন্ট করে কোর্স আনলক করুন।",
                            "COURSE_ACCESS_APPROVED"
                        )
                    }

                    dispatchStudentPushForPhoneOrDevice(
                        studentPhone = targetPhone,
                        deviceIdOrHash = targetDevice,
                        title = pushTitle,
                        message = pushBody,
                        eventType = eventType,
                        extraData = mapOf(
                            "enrollment_id" to payload.enrollmentId,
                            "program_id" to payload.programId,
                            "total_price" to payload.totalPrice,
                            "trial_enabled" to payload.trialEnabled,
                            "trial_days" to payload.trialDays,
                            "trial_hours" to payload.trialHours
                        )
                    )

                    loadCourseSalesData()
                    val msg = when {
                        payload.instantFreeUnlock -> {
                            "✅ কোর্সটি সফলভাবে ফ্রি আনলক (ACTIVE) করে দেওয়া হয়েছে!"
                        }
                        payload.trialEnabled && payload.startTrialImmediately -> {
                            "🎁 ফ্রি ট্রায়াল (${payload.trialDays} দিন ${payload.trialHours} ঘণ্টা) সাথে সাথে চালু (TRIAL_ACTIVE) করে অনুমোদন দেওয়া হয়েছে!"
                        }
                        payload.trialEnabled -> {
                            "🎁 ফ্রি ট্রায়াল (${payload.trialDays} দিন ${payload.trialHours} ঘণ্টা) সুবিধা ও কোর্সের দাম (৳${payload.totalPrice}) সেট করে অনুমোদন দেওয়া হয়েছে!"
                        }
                        else -> {
                            "✅ কোর্সের দাম (৳${payload.totalPrice}) ও কিস্তি প্ল্যান সেট করে অনুমোদন দেওয়া হয়েছে!"
                        }
                    }
                    showToast(msg)
                    onSuccess()
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "অনুমোদন ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isProcessingCourseSalesAction.value = false
            }
        }
    }

    fun grantManualCourseAccess(
        payload: ManualCourseAccessPayload,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isProcessingCourseSalesAction.value = true
                val result = repository.grantManualCourseAccess(payload)
                if (result.isSuccess) {
                    val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(payload.studentPhone)
                    val courseTitle = payload.programTitle.ifBlank { "কোর্স" }
                    val (pushTitle, pushBody, eventType) = when {
                        payload.status.equals("TRIAL_ACTIVE", ignoreCase = true) ||
                            (payload.trialEnabled && payload.startTrialImmediately) -> Triple(
                            "🎁 ফ্রি ট্রায়াল চালু হয়েছে!",
                            "'$courseTitle' কোর্সে আপনার জন্য ${payload.trialDays} দিন ${payload.trialHours} ঘণ্টার ফ্রি ট্রায়াল সরাসরি চালু করা হয়েছে!",
                            "COURSE_TRIAL_STARTED"
                        )
                        payload.status.equals("ACTIVE", ignoreCase = true) -> Triple(
                            "🎉 আপনার কোর্স আনলক হয়েছে!",
                            "'$courseTitle' কোর্সটি আপনার নম্বরে ($cleanPhone) সরাসরি আনলক (ACTIVE) করে দেওয়া হয়েছে!",
                            "COURSE_MANUAL_UNLOCKED"
                        )
                        else -> Triple(
                            "✅ কোর্স ও কিস্তি প্ল্যান যুক্ত হয়েছে!",
                            "'$courseTitle' কোর্সের দাম (৳${payload.totalPrice}) ও কিস্তি প্ল্যান আপনার নম্বরে সেট করা হয়েছে।",
                            "COURSE_MANUAL_ASSIGNED"
                        )
                    }
                    dispatchStudentPushForPhoneOrDevice(
                        studentPhone = cleanPhone,
                        title = pushTitle,
                        message = pushBody,
                        eventType = eventType,
                        extraData = mapOf(
                            "program_id" to payload.programId,
                            "program_title" to courseTitle,
                            "status" to payload.status
                        )
                    )

                    loadCourseSalesData()
                    val msg = when {
                        payload.status.equals("TRIAL_ACTIVE", ignoreCase = true) ||
                            (payload.trialEnabled && payload.startTrialImmediately) -> {
                            "🎁 $cleanPhone নম্বরে ফ্রি ট্রায়াল (TRIAL_ACTIVE) সরাসরি চালু করা হয়েছে!"
                        }
                        payload.status.equals("ACTIVE", ignoreCase = true) -> {
                            "✅ $cleanPhone নম্বরে কোর্সটি সফলভাবে আনলক (ACTIVE) করা হয়েছে!"
                        }
                        else -> {
                            "✅ $cleanPhone নম্বরে কোর্সের দাম ও কিস্তি প্ল্যান সেট করা হয়েছে!"
                        }
                    }
                    showToast(msg)
                    onSuccess()
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "কোর্স অ্যাসাইন ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isProcessingCourseSalesAction.value = false
            }
        }
    }

    fun manageCourseTrial(
        payload: ManageCourseTrialPayload,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isProcessingCourseSalesAction.value = true
                val existingEnrollment = _courseAccessEnrollments.value.firstOrNull { it.id == payload.enrollmentId }
                val result = repository.manageCourseTrial(payload)
                if (result.isSuccess) {
                    val courseTitle = existingEnrollment?.programTitle?.takeIf { it.isNotBlank() } ?: "কোর্স"
                    val targetPhone = payload.studentPhone.orEmpty().ifBlank {
                        payload.shikhoAccountKey.orEmpty().ifBlank { existingEnrollment?.effectivePhone.orEmpty() }
                    }
                    val targetDevice = existingEnrollment?.deviceId.orEmpty()
                        .ifBlank { existingEnrollment?.displayDeviceId.orEmpty() }

                    val (pushTitle, pushBody) = when (payload.action.uppercase()) {
                        "ADD_HOURS" -> "🎁 ফ্রি ট্রায়ালের সময় বাড়ানো হয়েছে!" to "'$courseTitle' কোর্সে আপনার ফ্রি ট্রায়ালের মেয়াদ বাড়ানো হয়েছে এবং কোর্সটি আনলক করা হয়েছে।"
                        "REDUCE_HOURS" -> "⏱️ ফ্রি ট্রায়ালের সময় আপডেট হয়েছে" to "'$courseTitle' কোর্সে আপনার ফ্রি ট্রায়ালের সময় সমন্বয় করা হয়েছে।"
                        "SET_DURATION" -> "🎁 ফ্রি ট্রায়ালের নতুন মেয়াদ চালু হয়েছে!" to "'$courseTitle' কোর্সে ${payload.days} দিন ${payload.hours} ঘণ্টার ফ্রি ট্রায়াল চালু করা হয়েছে।"
                        "END_TRIAL_NOW" -> "⏰ ফ্রি ট্রায়াল সমাপ্ত হয়েছে" to "'$courseTitle' কোর্সের ফ্রি ট্রায়াল শেষ হয়েছে। ক্লাস চালিয়ে যেতে এখন কোর্সটি কিনুন।"
                        "RESET_TRIAL" -> "🔄 ফ্রি ট্রায়াল রিসেট করা হয়েছে!" to "'$courseTitle' কোর্সে আপনি আবার নতুন করে ফ্রি ট্রায়াল শুরু করতে পারবেন।"
                        else -> "🎁 ফ্রি ট্রায়াল আপডেট" to "'$courseTitle' কোর্সের ফ্রি ট্রায়াল স্ট্যাটাস আপডেট করা হয়েছে।"
                    }

                    dispatchStudentPushForPhoneOrDevice(
                        studentPhone = targetPhone,
                        deviceIdOrHash = targetDevice,
                        title = pushTitle,
                        message = pushBody,
                        eventType = "COURSE_TRIAL_${payload.action.uppercase()}",
                        extraData = mapOf(
                            "enrollment_id" to payload.enrollmentId,
                            "program_id" to payload.programId,
                            "action" to payload.action
                        )
                    )

                    loadCourseSalesData()
                    val msg = when (payload.action.uppercase()) {
                        "ADD_HOURS" -> "➕ ফ্রি ট্রায়ালের মেয়াদ বাড়ানো হয়েছে এবং কোর্স আনলক (TRIAL_ACTIVE) করা হয়েছে!"
                        "REDUCE_HOURS" -> "➖ ফ্রি ট্রায়ালের মেয়াদ কমানো হয়েছে!"
                        "SET_DURATION" -> "⏱️ ফ্রি ট্রায়ালের নতুন মেয়াদ (${payload.days} দিন ${payload.hours} ঘণ্টা) সেট করে চালু করা হয়েছে!"
                        "END_TRIAL_NOW" -> "🛑 ফ্রি ট্রায়াল এখনই শেষ করা হয়েছে এবং কেনার অপশন (APPROVED_TO_BUY) চালু হয়েছে!"
                        "RESET_TRIAL" -> "🔄 ফ্রি ট্রায়াল রিসেট করা হয়েছে! স্টুডেন্ট আবার প্রথম থেকে ট্রায়াল নিতে পারবে।"
                        else -> "✅ ফ্রি ট্রায়াল আপডেট সম্পন্ন হয়েছে!"
                    }
                    showToast(msg)
                    onSuccess()
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "ট্রায়াল আপডেট ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isProcessingCourseSalesAction.value = false
            }
        }
    }

    fun reviewCoursePayment(
        payload: ReviewCoursePaymentRpcPayload,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isProcessingCourseSalesAction.value = true
                val tx = _coursePaymentTransactions.value.firstOrNull { it.id == payload.transactionId }
                val result = repository.reviewCoursePayment(payload)
                if (result.isSuccess) {
                    val targetPhone = tx?.effectiveStudentPhone.orEmpty().ifBlank { payload.studentPhone.orEmpty() }
                    val targetDevice = tx?.deviceId.orEmpty().ifBlank { tx?.displayDeviceId.orEmpty() }
                    val courseTitle = tx?.programTitle?.takeIf { it.isNotBlank() } ?: "কোর্স"
                    val amount = tx?.amount ?: 0
                    val trxId = tx?.transactionId.orEmpty()

                    if (payload.approve) {
                        dispatchStudentPushForPhoneOrDevice(
                            studentPhone = targetPhone,
                            deviceIdOrHash = targetDevice,
                            title = "✅ পেমেন্ট সফলভাবে ভেরিফাই হয়েছে!",
                            message = "'$courseTitle' কোর্সের জন্য আপনার ৳$amount পেমেন্ট (TrxID: $trxId) অনুমোদিত হয়েছে এবং কোর্স আনলক (ACTIVE) করা হয়েছে!",
                            eventType = "PAYMENT_APPROVED",
                            extraData = mapOf(
                                "payment_id" to payload.transactionId,
                                "transaction_id" to trxId,
                                "amount" to amount,
                                "status" to "APPROVED"
                            )
                        )
                    } else {
                        val reason = payload.rejectionReason.ifBlank { "সঠিক TrxID বা তথ্য পাওয়া যায়নি" }
                        dispatchStudentPushForPhoneOrDevice(
                            studentPhone = targetPhone,
                            deviceIdOrHash = targetDevice,
                            title = "🚫 পেমেন্ট ভেরিফিকেশন বাতিল হয়েছে",
                            message = "'$courseTitle' কোর্সের ৳$amount পেমেন্ট (TrxID: $trxId) বাতিল করা হয়েছে। কারণ: $reason",
                            eventType = "PAYMENT_REJECTED",
                            extraData = mapOf(
                                "payment_id" to payload.transactionId,
                                "transaction_id" to trxId,
                                "amount" to amount,
                                "status" to "REJECTED",
                                "rejection_reason" to reason
                            )
                        )
                    }

                    loadCourseSalesData()
                    val msg = if (payload.approve) {
                        "✅ পেমেন্ট অনুমোদিত হয়েছে এবং শিক্ষার্থীর কোর্স আনলক (ACTIVE) করা হয়েছে!"
                    } else {
                        "🚫 পেমেন্ট বাতিল (Rejected) করা হয়েছে এবং কারণটি শিক্ষার্থীকে পাঠানো হয়েছে।"
                    }
                    showToast(msg)
                    onSuccess()
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "পেমেন্ট রিভিউ ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isProcessingCourseSalesAction.value = false
            }
        }
    }

    fun enforceOverdueCourseInstallments() {
        viewModelScope.launch {
            try {
                _isProcessingCourseSalesAction.value = true
                val result = repository.enforceOverdueCourseInstallments()
                if (result.isSuccess) {
                    loadCourseSalesData()
                    showToast("✅ ওভারডিউ কিস্তি চেক ও অটো-লক সফলভাবে সম্পন্ন হয়েছে!")
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "ওভারডিউ চেক ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isProcessingCourseSalesAction.value = false
            }
        }
    }

    fun extendEnrollmentDueDate(enrollment: CourseAccessEnrollment, extraDays: Int) {
        viewModelScope.launch {
            try {
                _isProcessingCourseSalesAction.value = true
                val calendar = java.util.Calendar.getInstance()
                val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                val baseDate = try {
                    val raw = enrollment.nextDueDate
                    if (!raw.isNullOrBlank()) {
                        val clean = raw.take(19)
                        val parsed = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).parse(clean)
                        if (parsed != null && parsed.after(java.util.Date())) parsed else java.util.Date()
                    } else {
                        java.util.Date()
                    }
                } catch (_: Exception) {
                    java.util.Date()
                }
                calendar.time = baseDate
                calendar.add(java.util.Calendar.DAY_OF_YEAR, extraDays.coerceAtLeast(1))
                val newDueIso = isoFormat.format(calendar.time)

                val nextStatus = if (enrollment.isOverdueStatus) "ACTIVE" else null

                val result = repository.updateEnrollmentStatusOrDueDate(
                    enrollmentId = enrollment.id,
                    studentPhone = enrollment.effectivePhone,
                    shikhoAccountKey = enrollment.effectiveAccountKey,
                    programId = enrollment.programId,
                    newStatus = nextStatus,
                    newNextDueDateIso = newDueIso
                )
                if (result.isSuccess) {
                    val courseTitle = enrollment.programTitle?.takeIf { it.isNotBlank() } ?: "কোর্স"
                    dispatchStudentPushForPhoneOrDevice(
                        studentPhone = enrollment.effectivePhone,
                        deviceIdOrHash = enrollment.deviceId.orEmpty().ifBlank { enrollment.displayDeviceId.orEmpty() },
                        title = "📅 কিস্তির সময় বাড়ানো হয়েছে!",
                        message = "'$courseTitle' কোর্সে আপনার পরবর্তী কিস্তি পরিশোধের মেয়াদ +$extraDays দিন বাড়ানো হয়েছে।",
                        eventType = "INSTALLMENT_DUE_EXTENDED",
                        extraData = mapOf(
                            "enrollment_id" to enrollment.id,
                            "extra_days" to extraDays,
                            "next_due_date" to newDueIso
                        )
                    )
                    loadCourseSalesData()
                    showToast("✅ কিস্তির মেয়াদ +$extraDays দিন বাড়ানো হয়েছে!")
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "মেয়াদ বাড়াতে ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isProcessingCourseSalesAction.value = false
            }
        }
    }

    fun updateEnrollmentStatus(enrollment: CourseAccessEnrollment, newStatus: String) {
        viewModelScope.launch {
            try {
                _isProcessingCourseSalesAction.value = true
                val result = repository.updateEnrollmentStatusOrDueDate(
                    enrollmentId = enrollment.id,
                    studentPhone = enrollment.effectivePhone,
                    shikhoAccountKey = enrollment.effectiveAccountKey,
                    programId = enrollment.programId,
                    newStatus = newStatus
                )
                if (result.isSuccess) {
                    val courseTitle = enrollment.programTitle?.takeIf { it.isNotBlank() } ?: "কোর্স"
                    val isUnlocked = newStatus.equals("ACTIVE", ignoreCase = true)
                    dispatchStudentPushForPhoneOrDevice(
                        studentPhone = enrollment.effectivePhone,
                        deviceIdOrHash = enrollment.deviceId.orEmpty().ifBlank { enrollment.displayDeviceId.orEmpty() },
                        title = if (isUnlocked) "✅ আপনার কোর্স আনলক করা হয়েছে!" else "🔒 কোর্স এক্সেস আপডেট",
                        message = if (isUnlocked) {
                            "'$courseTitle' কোর্সটি আবার আনলক (ACTIVE) করা হয়েছে। এখন আপনি ক্লাস করতে পারবেন।"
                        } else {
                            "'$courseTitle' কোর্সের স্ট্যাটাস পরিবর্তন ($newStatus) করা হয়েছে।"
                        },
                        eventType = if (isUnlocked) "COURSE_UNLOCKED" else "COURSE_LOCKED",
                        extraData = mapOf(
                            "enrollment_id" to enrollment.id,
                            "status" to newStatus
                        )
                    )
                    loadCourseSalesData()
                    val msg = if (isUnlocked) {
                        "✅ কোর্সটি আনলক (ACTIVE) করা হয়েছে!"
                    } else {
                        "🔒 কোর্সটি লক ($newStatus) করা হয়েছে!"
                    }
                    showToast(msg)
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "স্ট্যাটাস আপডেট ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isProcessingCourseSalesAction.value = false
            }
        }
    }

    fun saveCoursePaymentSettings(payload: UpsertCoursePaymentSettingsPayload) {
        viewModelScope.launch {
            try {
                _isProcessingCourseSalesAction.value = true
                val result = repository.saveCoursePaymentSettings(payload)
                if (result.isSuccess) {
                    result.getOrNull()?.let { _coursePaymentSettings.value = it }
                    showToast("✅ পেমেন্ট মেথড ও সেটিংস সফলভাবে সেভ হয়েছে!")
                } else {
                    showToast("⚠️ ${result.exceptionOrNull()?.message ?: "সেটিংস সেভ ব্যর্থ হয়েছে"}")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isProcessingCourseSalesAction.value = false
            }
        }
    }

    // =========================================================================
    // Maxbird Website Admin Control Panel (`portal_settings/main`) Operations
    // =========================================================================

    fun updateMaxbirdPortalDraft(updated: MaxbirdPortalSettings) {
        _maxbirdPortalSettings.value = updated
    }

    fun loadMaxbirdPortalSettings() {
        viewModelScope.launch {
            try {
                _isLoadingMaxbirdPortal.value = true
                _maxbirdAuthSession.value = maxbirdFirestoreClient.getSavedAuthSession()
                val res = maxbirdFirestoreClient.fetchPortalSettings()
                if (res.isSuccess) {
                    res.getOrNull()?.let { loaded ->
                        _maxbirdPortalSettings.value = loaded
                    }
                }
            } catch (_: Throwable) {
                // Keep existing state on error
            } finally {
                _isLoadingMaxbirdPortal.value = false
            }
        }
    }

    fun saveMaxbirdPortalSettings(draft: MaxbirdPortalSettings = _maxbirdPortalSettings.value) {
        viewModelScope.launch {
            try {
                _isSavingMaxbirdPortal.value = true
                _maxbirdSyncMessage.value = null
                val res = maxbirdFirestoreClient.savePortalSettings(draft)
                if (res.isSuccess) {
                    val saved = res.getOrNull() ?: draft.copy(documentExists = true)
                    _maxbirdPortalSettings.value = saved
                    val msg = "✅ পরিবর্তনসমূহ সফলভাবে maxbird.pages.dev ওয়েবসাইটে লাইভ আপডেট হয়েছে!"
                    _maxbirdSyncMessage.value = msg
                    showToast(msg)
                } else {
                    val err = res.exceptionOrNull()?.message ?: "সেভ করতে ব্যর্থ হয়েছে"
                    showToast("⚠️ $err")
                }
            } catch (e: Throwable) {
                showToast("ত্রুটি: ${e.message}")
            } finally {
                _isSavingMaxbirdPortal.value = false
            }
        }
    }

    fun signInMaxbirdAdminWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoadingMaxbirdPortal.value = true
                val res = maxbirdFirestoreClient.signInWithEmailPassword(email, password)
                if (res.isSuccess) {
                    val session = res.getOrNull()!!
                    _maxbirdAuthSession.value = session
                    showToast("✅ ${session.email} হিসেবে সফলভাবে লগইন হয়েছে!")
                    loadMaxbirdPortalSettings()
                } else {
                    showToast("⚠️ ${res.exceptionOrNull()?.message ?: "লগইন ব্যর্থ হয়েছে"}")
                }
            } finally {
                _isLoadingMaxbirdPortal.value = false
            }
        }
    }

    fun createMaxbirdAdminWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoadingMaxbirdPortal.value = true
                val res = maxbirdFirestoreClient.createAdminAccountWithEmailPassword(email, password)
                if (res.isSuccess) {
                    val session = res.getOrNull()!!
                    _maxbirdAuthSession.value = session
                    showToast("✅ অ্যাডমিন অ্যাকাউন্ট সেটআপ ও লগইন সম্পন্ন হয়েছে!")
                    loadMaxbirdPortalSettings()
                } else {
                    showToast("⚠️ ${res.exceptionOrNull()?.message ?: "অ্যাকাউন্ট তৈরি ব্যর্থ হয়েছে"}")
                }
            } finally {
                _isLoadingMaxbirdPortal.value = false
            }
        }
    }

    fun sendMaxbirdAdminPasswordReset(email: String) {
        viewModelScope.launch {
            val res = maxbirdFirestoreClient.sendPasswordResetEmail(email)
            if (res.isSuccess) {
                showToast("📧 $email-এ পাসওয়ার্ড রিসেট লিংক পাঠানো হয়েছে! ইনবক্স বা স্প্যাম ফোল্ডার চেক করুন।")
            } else {
                showToast("⚠️ ${res.exceptionOrNull()?.message ?: "রিসেট ইমেইল পাঠানো যায়নি"}")
            }
        }
    }

    fun verifyMaxbirdAdminManualIdToken(idToken: String) {
        viewModelScope.launch {
            val res = maxbirdFirestoreClient.verifyAndSaveFirebaseIdToken(idToken)
            if (res.isSuccess) {
                val session = res.getOrNull()!!
                _maxbirdAuthSession.value = session
                showToast("✅ টোকেন ভেরিফাইড: ${session.email} লগইন সফল!")
                loadMaxbirdPortalSettings()
            } else {
                showToast("⚠️ ${res.exceptionOrNull()?.message ?: "টোকেন ভেরিফিকেশন ব্যর্থ"}")
            }
        }
    }

    fun onMaxbirdWebSdkAuthReceived(
        uid: String,
        email: String,
        displayName: String,
        photoUrl: String,
        idToken: String,
        refreshToken: String
    ) {
        if (idToken.isBlank() || email.isBlank()) return
        val session = MaxbirdAdminAuthSession(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            idToken = idToken,
            refreshToken = refreshToken,
            isSignedIn = true
        )
        maxbirdFirestoreClient.saveAuthSession(session)
        _maxbirdAuthSession.value = session
    }

    fun signOutMaxbirdAdmin() {
        maxbirdFirestoreClient.clearAuthSession()
        _maxbirdAuthSession.value = MaxbirdAdminAuthSession()
        showToast("লগআউট সম্পন্ন হয়েছে।")
    }

    fun inspectLocalApkForMaxbirdWebsite(uri: android.net.Uri) {
        val res = maxbirdFirestoreClient.inspectLocalApkFile(uri)
        if (res.isSuccess) {
            val info = res.getOrNull()!!
            val current = _maxbirdPortalSettings.value
            val autoUrl = MaxbirdPortalSettings.autoBuildR2Url(current.r2BucketPublicDomain, info.fileName)
            val updated = current.copy(
                fileSize = info.fileSizeFormatted,
                r2ObjectKey = info.fileName.take(300),
                apkDownloadUrl = if (autoUrl.isNotEmpty()) autoUrl else current.apkDownloadUrl
            )
            _maxbirdPortalSettings.value = updated
            showToast("📦 APK ফাইল ডিটেক্টেড: ${info.fileName} (${info.fileSizeFormatted})")
        } else {
            showToast("⚠️ ফাইল রিড করা যায়নি: ${res.exceptionOrNull()?.message}")
        }
    }

    // =========================================================================
    // 1-Click Student App & Supabase Database 100% Sync Diagnostic Tool
    // =========================================================================

    data class SyncDiagnosticCheckItem(
        val id: String,
        val titleBn: String,
        val isSuccess: Boolean,
        val detailBn: String
    )

    private val _isRunningSyncDiagnostics = MutableStateFlow(false)
    val isRunningSyncDiagnostics: StateFlow<Boolean> = _isRunningSyncDiagnostics.asStateFlow()

    private val _syncDiagnosticResults = MutableStateFlow<List<SyncDiagnosticCheckItem>>(emptyList())
    val syncDiagnosticResults: StateFlow<List<SyncDiagnosticCheckItem>> = _syncDiagnosticResults.asStateFlow()

    fun runFullSystemSyncDiagnostics() {
        viewModelScope.launch {
            _isRunningSyncDiagnostics.value = true
            val checks = mutableListOf<SyncDiagnosticCheckItem>()
            try {
                // 1. Supabase REST API & Connection Check
                val connRes = repository.testConnection()
                checks.add(
                    SyncDiagnosticCheckItem(
                        id = "supabase_conn",
                        titleBn = "১. Supabase সার্ভার ও API কানেকশন",
                        isSuccess = connRes.isSuccess,
                        detailBn = if (connRes.isSuccess) {
                            "✅ সংযুক্ত (${_supabaseUrl.value.take(36)}...)"
                        } else {
                            "❌ কানেকশন ব্যর্থ: ${connRes.exceptionOrNull()?.message ?: "অজানা ত্রুটি"}"
                        }
                    )
                )

                // 2. Student Device Verification Table (`public.devices`) + Live Write/Ban Sync Test
                val devRes = repository.getUserDevices(forceRefresh = true)
                val devList = devRes.getOrNull().orEmpty()
                var deviceWriteTestOk = devRes.isSuccess
                var deviceWriteNote = ""

                if (devRes.isSuccess && devList.isNotEmpty()) {
                    val sample = devList.first()
                    // Perform a safe, non-destructive self-patch of `student_note` to verify triggers don't rollback UPDATEs!
                    val patchTest = repository.updateUserDeviceStudentNote(
                        device = sample,
                        studentNote = sample.studentNote.orEmpty()
                    )
                    deviceWriteTestOk = patchTest.isSuccess
                    deviceWriteNote = if (patchTest.isSuccess) {
                        " • UPDATE ও ব্লক/অ্যাপ্রুভ ১০০% সচল"
                    } else {
                        " • ⚠️ UPDATE আটকে যাচ্ছে (${patchTest.exceptionOrNull()?.message ?: "SQL Trigger Fix প্রয়োজন"})"
                    }
                }

                val pendingDevs = devList.count { it.isPendingDevice }
                val activeDevs = devList.count { it.isActiveDevice }
                val blockedDevs = devList.count { it.isBlockedDevice }
                checks.add(
                    SyncDiagnosticCheckItem(
                        id = "devices_table",
                        titleBn = "২. স্টুডেন্ট ডিভাইস ভেরিফিকেশন ও ব্যান কন্ট্রোল (public.devices)",
                        isSuccess = devRes.isSuccess && deviceWriteTestOk,
                        detailBn = if (devRes.isSuccess) {
                            "মোট ${devList.size}টি ডিভাইস (Active: $activeDevs, Pending: $pendingDevs, Blocked: $blockedDevs)$deviceWriteNote"
                        } else {
                            "❌ ডিভাইস টেবিল রিড ব্যর্থ: ${devRes.exceptionOrNull()?.message}"
                        }
                    )
                )

                // 3. Course Access Enrollments & Free Trial Sync (`public.course_access_enrollments`)
                val enrollRes = repository.getCourseAccessEnrollments()
                val enrollList = enrollRes.getOrNull().orEmpty()
                val reqCount = enrollList.count { it.isAccessRequestedStatus }
                val trialRunningCount = enrollList.count { it.isTrialActiveStatus }
                val unlockedCount = enrollList.count { it.effectiveStatus.equals("ACTIVE", ignoreCase = true) }
                val overdueCount = enrollList.count { it.isOverdueStatus }
                checks.add(
                    SyncDiagnosticCheckItem(
                        id = "course_enrollments",
                        titleBn = "৩. কোর্স এক্সেস, কিস্তি ও ফ্রি ট্রায়াল সিঙ্ক (course_access_enrollments)",
                        isSuccess = enrollRes.isSuccess,
                        detailBn = if (enrollRes.isSuccess) {
                            "✅ সিঙ্কড • মোট ${enrollList.size}টি (রিকোয়েস্ট: $reqCount, ট্রায়াল: $trialRunningCount, আনলকড: $unlockedCount, ওভারডিউ: $overdueCount)"
                        } else {
                            "❌ কোর্স এনরোলমেন্ট টেবিল ত্রুটি: ${enrollRes.exceptionOrNull()?.message}"
                        }
                    )
                )

                // 3b. Device Shikho Accounts & Course Permissions (`public.device_shikho_accounts`)
                val shikhoAccRes = repository.getDeviceShikhoAccounts()
                val shikhoAccList = shikhoAccRes.getOrNull().orEmpty()
                checks.add(
                    SyncDiagnosticCheckItem(
                        id = "device_shikho_accounts",
                        titleBn = "৩(খ). ডিভাইস ও শিখো অ্যাকাউন্ট পারমিশন (device_shikho_accounts)",
                        isSuccess = shikhoAccRes.isSuccess,
                        detailBn = if (shikhoAccRes.isSuccess) {
                            "✅ সিঙ্কড • মোট ${shikhoAccList.size}টি বাউন্ড অ্যাকাউন্ট (admin_set_device/account_course_permissions সচল)"
                        } else {
                            "❌ শিখো অ্যাকাউন্ট টেবিল রিড ত্রুটি: ${shikhoAccRes.exceptionOrNull()?.message}"
                        }
                    )
                )

                // 4. Payment Verification Transactions (`public.course_payment_transactions`)
                val txRes = repository.getCoursePaymentTransactions()
                val txList = txRes.getOrNull().orEmpty()
                val pendingTx = txList.count { it.status.equals("PENDING", ignoreCase = true) }
                val approvedTx = txList.count { it.status.equals("APPROVED", ignoreCase = true) }
                checks.add(
                    SyncDiagnosticCheckItem(
                        id = "course_payments",
                        titleBn = "৪. পেমেন্ট ও কিস্তি TrxID ভেরিফিকেশন (course_payment_transactions)",
                        isSuccess = txRes.isSuccess,
                        detailBn = if (txRes.isSuccess) {
                            "✅ সিঙ্কড • মোট ${txList.size}টি পেমেন্ট (যাচাই অপেক্ষমান: $pendingTx, অনুমোদিত: $approvedTx)"
                        } else {
                            "❌ পেমেন্ট ট্রানজেকশন টেবিল ত্রুটি: ${txRes.exceptionOrNull()?.message}"
                        }
                    )
                )

                // 5. Payment Methods Settings (`public.course_payment_settings`)
                val paySettingsRes = repository.getCoursePaymentSettings()
                val activeMethods = paySettingsRes.getOrNull()?.effectivePaymentMethods()?.count { it.isEnabled } ?: 0
                checks.add(
                    SyncDiagnosticCheckItem(
                        id = "payment_settings",
                        titleBn = "৫. স্টুডেন্ট অ্যাপের পেমেন্ট নম্বর সিঙ্ক (course_payment_settings)",
                        isSuccess = paySettingsRes.isSuccess && activeMethods > 0,
                        detailBn = if (paySettingsRes.isSuccess) {
                            if (activeMethods > 0) {
                                "✅ সক্রিয় পেমেন্ট মেথড: ${activeMethods}টি (স্টুডেন্ট অ্যাপে লাইভ দেখাচ্ছে)"
                            } else {
                                "⚠️ কোনো পেমেন্ট নম্বর চালু (ON) নেই—উপরে পেমেন্ট মেথড ম্যানেজার থেকে নম্বর যোগ করুন"
                            }
                        } else {
                            "❌ পেমেন্ট সেটিংস রিড ব্যর্থ"
                        }
                    )
                )

                // 6. OneSignal + FCM Push Notification Sync
                val hasAppId = OneSignalPushManager.isValidOneSignalAppId(_oneSignalAppId.value)
                val hasRestKey = _oneSignalRestApiKey.value.isNotBlank()
                checks.add(
                    SyncDiagnosticCheckItem(
                        id = "onesignal_fcm",
                        titleBn = "৬. OneSignal + FCM পুশ নোটিফিকেশন ও ট্রিগার সিঙ্ক",
                        isSuccess = hasAppId && hasRestKey,
                        detailBn = if (hasAppId && hasRestKey) {
                            "✅ OneSignal App ID ও REST API Key সক্রিয় (${_oneSignalStatusSummary.value})"
                        } else {
                            "⚠️ OneSignal App ID বা REST API Key সেট করা নেই"
                        }
                    )
                )

                // 7. Maxbird APK Share Website (`portal_settings/main`)
                val webRes = maxbirdFirestoreClient.fetchPortalSettings()
                val webSession = maxbirdFirestoreClient.getSavedAuthSession()
                val webDoc = webRes.getOrNull()
                checks.add(
                    SyncDiagnosticCheckItem(
                        id = "maxbird_website",
                        titleBn = "৭. APK শেয়ার ওয়েবসাইট কন্ট্রোল (maxbird.pages.dev • Firestore)",
                        isSuccess = webRes.isSuccess,
                        detailBn = if (webRes.isSuccess && webDoc != null) {
                            val authTag = if (webSession.isSignedIn) "অ্যাডমিন লগইন: ${webSession.email}" else "লগইন প্রয়োজন (ওয়েবসাইট ট্যাব থেকে)"
                            "✅ কানেক্টেড • ভার্সন: ${webDoc.appVersion} • ডাউনলোড: ${webDoc.downloadCount} • $authTag"
                        } else {
                            "❌ ওয়েবসাইট ডাটাবেস কানেকশন ত্রুটি: ${webRes.exceptionOrNull()?.message}"
                        }
                    )
                )

                _syncDiagnosticResults.value = checks
                val passed = checks.count { it.isSuccess }
                showToast("🔍 সিস্টেম ডায়াগনস্টিক সম্পন্ন: $passed/${checks.size}টি টেস্ট সফল!")
            } catch (e: Throwable) {
                showToast("ডায়াগনস্টিক ত্রুটি: ${e.message}")
            } finally {
                _isRunningSyncDiagnostics.value = false
            }
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
