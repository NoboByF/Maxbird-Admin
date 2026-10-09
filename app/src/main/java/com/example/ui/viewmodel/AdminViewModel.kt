package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.SupabaseClient
import com.example.data.model.AccessCode
import com.example.data.model.ActivatedDevice
import com.example.data.model.CreateAccessCodePayload
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
