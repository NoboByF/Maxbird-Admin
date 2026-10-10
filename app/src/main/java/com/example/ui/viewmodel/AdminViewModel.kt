package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.SupabaseClient
import com.example.data.model.AccessCode
import com.example.data.model.ActivatedDevice
import com.example.data.model.AppUpdate
import com.example.data.model.AppNotice
import com.example.data.model.CreateAccessCodePayload
import com.example.data.model.CreateAppUpdatePayload
import com.example.data.model.CreateAppNoticePayload
import com.example.data.model.DashboardStats
import com.example.data.model.DeviceWithStudent
import com.example.data.preferences.AdminPreferences
import com.example.data.repository.MaxBirdRepository
import com.example.data.repository.SupabaseMaxBirdRepository
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val adminPreferences = AdminPreferences(application)
    private val supabaseClient = SupabaseClient(adminPreferences)
    private val repository: MaxBirdRepository = SupabaseMaxBirdRepository(supabaseClient, adminPreferences)

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

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

    private val _deviceSearchQuery = MutableStateFlow("")
    val deviceSearchQuery: StateFlow<String> = _deviceSearchQuery.asStateFlow()

    private val _deviceFilter = MutableStateFlow("ALL") // ALL, ACTIVE, BLOCKED
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

    init {
        loadAllData()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun loadAllData() {
        refreshStats()
        loadCodes()
        loadJoinedDevices()
        loadAppUpdates()
        loadAppNotices()
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

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
