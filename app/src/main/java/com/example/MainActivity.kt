package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.Screen
import com.example.ui.screens.CodeGeneratorScreen
import com.example.ui.screens.CodeManagementScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceManagementScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UpdatesManagementScreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                // Display toast or snackbar
                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                // Directly open Admin Control Center (No PIN gate / barrier)
                BackHandler(enabled = currentScreen != Screen.Dashboard) {
                    viewModel.navigateTo(Screen.Dashboard)
                }

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        AdminBottomNavigationBar(
                            currentScreen = currentScreen,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    },
                    containerColor = Slate900
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Slate900)
                    ) {
                        when (currentScreen) {
                            Screen.Dashboard -> {
                                val stats by viewModel.stats.collectAsStateWithLifecycle()
                                val codes by viewModel.codes.collectAsStateWithLifecycle()
                                val isLoadingStats by viewModel.isLoadingStats.collectAsStateWithLifecycle()

                                DashboardScreen(
                                    stats = stats,
                                    recentCodes = codes,
                                    isLoading = isLoadingStats,
                                    onRefresh = { viewModel.refreshStats() },
                                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                                    onOpenSettings = { viewModel.navigateTo(Screen.Settings) },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )
                            }

                            Screen.Generator -> {
                                val studentName by viewModel.genStudentName.collectAsStateWithLifecycle()
                                val code by viewModel.genCode.collectAsStateWithLifecycle()
                                val maxDevices by viewModel.genMaxDevices.collectAsStateWithLifecycle()
                                val note by viewModel.genNote.collectAsStateWithLifecycle()
                                val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
                                val generatorError by viewModel.generatorError.collectAsStateWithLifecycle()
                                val successCode by viewModel.generatedSuccessCode.collectAsStateWithLifecycle()

                                CodeGeneratorScreen(
                                    studentName = studentName,
                                    code = code,
                                    maxDevices = maxDevices,
                                    note = note,
                                    isGenerating = isGenerating,
                                    errorMessage = generatorError,
                                    successCode = successCode,
                                    onNameChange = { viewModel.setGenStudentName(it) },
                                    onCodeChange = { viewModel.setGenCode(it) },
                                    onMaxDevicesChange = { viewModel.setGenMaxDevices(it) },
                                    onNoteChange = { viewModel.setGenNote(it) },
                                    onGenerateRandom = { prefix -> viewModel.generateRandomCode(prefix) },
                                    onSubmit = { viewModel.submitCreateCode() },
                                    onDismissSuccess = { viewModel.dismissSuccessDialog() },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )
                            }

                            Screen.Codes -> {
                                val codes by viewModel.codes.collectAsStateWithLifecycle()
                                val searchQuery by viewModel.codeSearchQuery.collectAsStateWithLifecycle()
                                val filter by viewModel.codeFilter.collectAsStateWithLifecycle()
                                val isLoadingCodes by viewModel.isLoadingCodes.collectAsStateWithLifecycle()
                                val selectedCodeForDevices by viewModel.selectedCodeForDevices.collectAsStateWithLifecycle()
                                val linkedDevices by viewModel.linkedDevices.collectAsStateWithLifecycle()
                                val isLoadingLinkedDevices by viewModel.isLoadingLinkedDevices.collectAsStateWithLifecycle()

                                CodeManagementScreen(
                                    codes = codes,
                                    searchQuery = searchQuery,
                                    filter = filter,
                                    isLoading = isLoadingCodes,
                                    selectedCodeForDevices = selectedCodeForDevices,
                                    linkedDevices = linkedDevices,
                                    isLoadingLinkedDevices = isLoadingLinkedDevices,
                                    onSearchChange = { viewModel.setCodeSearchQuery(it) },
                                    onFilterChange = { viewModel.setCodeFilter(it) },
                                    onRefresh = { viewModel.loadCodes() },
                                    onToggleActive = { target -> viewModel.toggleCodeStatus(target) },
                                    onDeleteCode = { target -> viewModel.deleteCode(target) },
                                    onOpenLinkedDevices = { target -> viewModel.openLinkedDevices(target) },
                                    onCloseLinkedDevices = { viewModel.closeLinkedDevices() },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )
                            }

                            Screen.Devices -> {
                                val joinedDevices by viewModel.joinedDevices.collectAsStateWithLifecycle()
                                val searchQuery by viewModel.deviceSearchQuery.collectAsStateWithLifecycle()
                                val filter by viewModel.deviceFilter.collectAsStateWithLifecycle()
                                val isLoadingDevices by viewModel.isLoadingDevices.collectAsStateWithLifecycle()

                                DeviceManagementScreen(
                                    devices = joinedDevices,
                                    searchQuery = searchQuery,
                                    filter = filter,
                                    isLoading = isLoadingDevices,
                                    onSearchChange = { viewModel.setDeviceSearchQuery(it) },
                                    onFilterChange = { viewModel.setDeviceFilter(it) },
                                    onRefresh = { viewModel.loadJoinedDevices() },
                                    onToggleBlocked = { target -> viewModel.toggleDeviceBlocked(target) },
                                    onUnbindDevice = { target -> viewModel.unbindDevice(target) }
                                )
                            }

                            Screen.Updates -> {
                                val updates by viewModel.appUpdates.collectAsStateWithLifecycle()
                                val isLoadingUpdates by viewModel.isLoadingUpdates.collectAsStateWithLifecycle()
                                val isDialogOpen by viewModel.isUpdateDialogOpen.collectAsStateWithLifecycle()
                                val editingId by viewModel.editingUpdateId.collectAsStateWithLifecycle()
                                val versionName by viewModel.updateVersionName.collectAsStateWithLifecycle()
                                val versionCode by viewModel.updateVersionCode.collectAsStateWithLifecycle()
                                val minVersionCode by viewModel.updateMinVersionCode.collectAsStateWithLifecycle()
                                val isForce by viewModel.updateIsForce.collectAsStateWithLifecycle()
                                val title by viewModel.updateTitle.collectAsStateWithLifecycle()
                                val changelog by viewModel.updateChangelog.collectAsStateWithLifecycle()
                                val downloadUrl by viewModel.updateDownloadUrl.collectAsStateWithLifecycle()
                                val buttonText by viewModel.updateButtonText.collectAsStateWithLifecycle()
                                val isActive by viewModel.updateIsActive.collectAsStateWithLifecycle()

                                UpdatesManagementScreen(
                                    updates = updates,
                                    isLoading = isLoadingUpdates,
                                    isDialogOpen = isDialogOpen,
                                    editingId = editingId,
                                    versionName = versionName,
                                    versionCode = versionCode,
                                    minVersionCode = minVersionCode,
                                    isForce = isForce,
                                    title = title,
                                    changelog = changelog,
                                    downloadUrl = downloadUrl,
                                    buttonText = buttonText,
                                    isActive = isActive,
                                    onOpenCreateDialog = { viewModel.openCreateUpdateDialog() },
                                    onOpenEditDialog = { update -> viewModel.openEditUpdateDialog(update) },
                                    onCloseDialog = { viewModel.closeUpdateDialog() },
                                    onVersionNameChange = { viewModel.setUpdateVersionName(it) },
                                    onVersionCodeChange = { viewModel.setUpdateVersionCode(it) },
                                    onMinVersionCodeChange = { viewModel.setUpdateMinVersionCode(it) },
                                    onIsForceChange = { viewModel.setUpdateIsForce(it) },
                                    onTitleChange = { viewModel.setUpdateTitle(it) },
                                    onChangelogChange = { viewModel.setUpdateChangelog(it) },
                                    onDownloadUrlChange = { viewModel.setUpdateDownloadUrl(it) },
                                    onButtonTextChange = { viewModel.setUpdateButtonText(it) },
                                    onIsActiveChange = { viewModel.setUpdateIsActive(it) },
                                    onSaveUpdate = { viewModel.saveAppUpdate() },
                                    onToggleStatus = { update -> viewModel.toggleAppUpdateStatus(update) },
                                    onDeleteUpdate = { update -> viewModel.deleteAppUpdate(update) },
                                    onRefresh = { viewModel.loadAppUpdates() },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )
                            }

                            Screen.Settings -> {
                                val supabaseUrl by viewModel.supabaseUrl.collectAsStateWithLifecycle()
                                val supabaseKey by viewModel.supabaseKey.collectAsStateWithLifecycle()
                                val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()
                                val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
                                val isConnectionSuccess by viewModel.isConnectionSuccess.collectAsStateWithLifecycle()

                                SettingsScreen(
                                    supabaseUrl = supabaseUrl,
                                    supabaseKey = supabaseKey,
                                    isTestingConnection = isTestingConnection,
                                    connectionStatus = connectionStatus,
                                    isConnectionSuccess = isConnectionSuccess,
                                    onUrlChange = { viewModel.updateSupabaseUrl(it) },
                                    onKeyChange = { viewModel.updateSupabaseKey(it) },
                                    onSaveSettings = { viewModel.saveSupabaseSettings() },
                                    onTestConnection = { viewModel.testSupabaseConnection() },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Slate800,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val items = Screen.bottomNavItems.filterNotNull()
        items.forEach { screen ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.titleEn
                    )
                },
                label = {
                    Text(
                        text = screen.titleEn,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = NeonCyan,
                    indicatorColor = ElectricBlue,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                ),
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}
