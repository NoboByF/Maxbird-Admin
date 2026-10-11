package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.Screen
import com.example.ui.screens.ActivationSupportLinksScreen
import com.example.ui.screens.CloudinaryMediaScreen
import com.example.ui.screens.CodeGeneratorScreen
import com.example.ui.screens.CodeManagementScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceManagementScreen
import com.example.ui.screens.NoticeManagementScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UpdatesManagementScreen

import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
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
                    topBar = {
                        AdminTopHeaderBar(
                            currentScreen = currentScreen,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    },
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
                                val userDevices by viewModel.userDevices.collectAsStateWithLifecycle()
                                val searchQuery by viewModel.deviceSearchQuery.collectAsStateWithLifecycle()
                                val filter by viewModel.deviceFilter.collectAsStateWithLifecycle()
                                val isLoadingDevices by viewModel.isLoadingDevices.collectAsStateWithLifecycle()

                                DeviceManagementScreen(
                                    devices = userDevices,
                                    searchQuery = searchQuery,
                                    filter = filter,
                                    isLoading = isLoadingDevices,
                                    onSearchChange = { viewModel.setDeviceSearchQuery(it) },
                                    onFilterChange = { viewModel.setDeviceFilter(it) },
                                    onRefresh = { viewModel.loadUserDevices(forceRefresh = true) },
                                    onToggleBan = { device, reason -> viewModel.toggleUserDeviceBan(device, reason) },
                                    onDeleteDevice = { device -> viewModel.deleteUserDevice(device) },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
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

                            Screen.Notices -> {
                                val notices by viewModel.appNotices.collectAsStateWithLifecycle()
                                val isLoadingNotices by viewModel.isLoadingNotices.collectAsStateWithLifecycle()
                                val searchQuery by viewModel.noticeSearchQuery.collectAsStateWithLifecycle()
                                val filter by viewModel.noticeFilter.collectAsStateWithLifecycle()
                                val isDialogOpen by viewModel.isNoticeDialogOpen.collectAsStateWithLifecycle()
                                val editingId by viewModel.editingNoticeId.collectAsStateWithLifecycle()
                                val title by viewModel.noticeTitle.collectAsStateWithLifecycle()
                                val description by viewModel.noticeDescription.collectAsStateWithLifecycle()
                                val imageUrl by viewModel.noticeImageUrl.collectAsStateWithLifecycle()
                                val actionUrl by viewModel.noticeActionUrl.collectAsStateWithLifecycle()
                                val actionButtonText by viewModel.noticeActionButtonText.collectAsStateWithLifecycle()
                                val priority by viewModel.noticePriority.collectAsStateWithLifecycle()
                                val isActive by viewModel.noticeIsActive.collectAsStateWithLifecycle()
                                val showAsPopup by viewModel.noticeShowAsPopup.collectAsStateWithLifecycle()
                                val previewNotice by viewModel.previewNotice.collectAsStateWithLifecycle()

                                NoticeManagementScreen(
                                    notices = notices,
                                    isLoading = isLoadingNotices,
                                    searchQuery = searchQuery,
                                    filter = filter,
                                    isDialogOpen = isDialogOpen,
                                    editingId = editingId,
                                    title = title,
                                    description = description,
                                    imageUrl = imageUrl,
                                    actionUrl = actionUrl,
                                    actionButtonText = actionButtonText,
                                    priority = priority,
                                    isActive = isActive,
                                    showAsPopup = showAsPopup,
                                    previewNotice = previewNotice,
                                    onSearchChange = { viewModel.setNoticeSearchQuery(it) },
                                    onFilterChange = { viewModel.setNoticeFilter(it) },
                                    onOpenCreateDialog = { viewModel.openCreateNoticeDialog() },
                                    onOpenEditDialog = { notice -> viewModel.openEditNoticeDialog(notice) },
                                    onCloseDialog = { viewModel.closeNoticeDialog() },
                                    onTitleChange = { viewModel.setNoticeTitle(it) },
                                    onDescriptionChange = { viewModel.setNoticeDescription(it) },
                                    onImageUrlChange = { viewModel.setNoticeImageUrl(it) },
                                    onActionUrlChange = { viewModel.setNoticeActionUrl(it) },
                                    onActionButtonTextChange = { viewModel.setNoticeActionButtonText(it) },
                                    onPriorityChange = { viewModel.setNoticePriority(it) },
                                    onIsActiveChange = { viewModel.setNoticeIsActive(it) },
                                    onShowAsPopupChange = { viewModel.setNoticeShowAsPopup(it) },
                                    onOpenPreview = { notice -> viewModel.openPreviewModal(notice) },
                                    onClosePreview = { viewModel.closePreviewModal() },
                                    onSaveNotice = { viewModel.saveAppNotice() },
                                    onToggleStatus = { notice -> viewModel.toggleAppNoticeStatus(notice) },
                                    onDeleteNotice = { notice -> viewModel.deleteAppNotice(notice) },
                                    onRefresh = { viewModel.loadAppNotices() },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )
                            }

                            Screen.SupportLinks -> {
                                val supportLinks by viewModel.supportLinks.collectAsStateWithLifecycle()
                                val isLoadingSupportLinks by viewModel.isLoadingSupportLinks.collectAsStateWithLifecycle()
                                val searchQuery by viewModel.supportLinkSearchQuery.collectAsStateWithLifecycle()
                                val filter by viewModel.supportLinkFilter.collectAsStateWithLifecycle()
                                val isDialogOpen by viewModel.isSupportLinkDialogOpen.collectAsStateWithLifecycle()
                                val editingId by viewModel.editingSupportLinkId.collectAsStateWithLifecycle()
                                val title by viewModel.supportLinkTitle.collectAsStateWithLifecycle()
                                val subtitle by viewModel.supportLinkSubtitle.collectAsStateWithLifecycle()
                                val iconType by viewModel.supportLinkIconType.collectAsStateWithLifecycle()
                                val url by viewModel.supportLinkUrl.collectAsStateWithLifecycle()
                                val colorHex by viewModel.supportLinkColorHex.collectAsStateWithLifecycle()
                                val priority by viewModel.supportLinkPriority.collectAsStateWithLifecycle()
                                val isActive by viewModel.supportLinkIsActive.collectAsStateWithLifecycle()

                                ActivationSupportLinksScreen(
                                    supportLinks = supportLinks,
                                    isLoading = isLoadingSupportLinks,
                                    searchQuery = searchQuery,
                                    filter = filter,
                                    isDialogOpen = isDialogOpen,
                                    editingId = editingId,
                                    title = title,
                                    subtitle = subtitle,
                                    iconType = iconType,
                                    url = url,
                                    colorHex = colorHex,
                                    priority = priority,
                                    isActive = isActive,
                                    onSearchChange = { viewModel.setSupportLinkSearchQuery(it) },
                                    onFilterChange = { viewModel.setSupportLinkFilter(it) },
                                    onOpenCreateDialog = { viewModel.openCreateSupportLinkDialog() },
                                    onOpenEditDialog = { link -> viewModel.openEditSupportLinkDialog(link) },
                                    onCloseDialog = { viewModel.closeSupportLinkDialog() },
                                    onTitleChange = { viewModel.setSupportLinkTitle(it) },
                                    onSubtitleChange = { viewModel.setSupportLinkSubtitle(it) },
                                    onIconTypeChange = { viewModel.setSupportLinkIconType(it) },
                                    onUrlChange = { viewModel.setSupportLinkUrl(it) },
                                    onColorHexChange = { viewModel.setSupportLinkColorHex(it) },
                                    onPriorityChange = { viewModel.setSupportLinkPriority(it) },
                                    onIsActiveChange = { viewModel.setSupportLinkIsActive(it) },
                                    onSaveLink = { viewModel.saveSupportLink() },
                                    onToggleStatus = { link -> viewModel.toggleSupportLinkStatus(link) },
                                    onDeleteLink = { link -> viewModel.deleteSupportLink(link) },
                                    onRefresh = { viewModel.loadActivationSupportLinks(forceRefresh = true) },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )
                            }

                            Screen.CloudinaryMedia -> {
                                val selectedUri by viewModel.selectedMediaUri.collectAsStateWithLifecycle()
                                val fileMeta by viewModel.selectedMediaMeta.collectAsStateWithLifecycle()
                                val isUploading by viewModel.isUploadingMedia.collectAsStateWithLifecycle()
                                val lastUploadedUrl by viewModel.lastUploadedSecureUrl.collectAsStateWithLifecycle()
                                val history by viewModel.mediaUploadHistory.collectAsStateWithLifecycle()

                                CloudinaryMediaScreen(
                                    selectedUri = selectedUri,
                                    fileMeta = fileMeta,
                                    isUploading = isUploading,
                                    lastUploadedUrl = lastUploadedUrl,
                                    uploadHistory = history,
                                    onSelectFile = { uri -> viewModel.selectMediaFile(uri) },
                                    onClearSelectedFile = { viewModel.clearSelectedMedia() },
                                    onUploadClick = { viewModel.uploadSelectedMedia() },
                                    onRemoveHistoryItem = { item -> viewModel.removeMediaHistoryItem(item) },
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
private fun AdminTopHeaderBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate950)
            .padding(top = 4.dp, bottom = 6.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .background(ElectricBlue),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(10.dp))
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = "MAXBIRD ADMIN",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "এখন আছেন: ${currentScreen.titleBn} (${currentScreen.titleEn})",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Direct shortcut to Notice or Update or Home
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
            ) {
                androidx.compose.material3.IconButton(
                    onClick = { onNavigate(Screen.Notices) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (currentScreen == Screen.Notices) ElectricBlue else Slate800)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Campaign,
                        contentDescription = "Notices",
                        tint = if (currentScreen == Screen.Notices) Color.White else NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                androidx.compose.material3.IconButton(
                    onClick = { onNavigate(Screen.SupportLinks) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (currentScreen == Screen.SupportLinks) ElectricBlue else Slate800)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.HeadsetMic,
                        contentDescription = "Support Links",
                        tint = if (currentScreen == Screen.SupportLinks) Color.White else NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                androidx.compose.material3.IconButton(
                    onClick = { onNavigate(Screen.Updates) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (currentScreen == Screen.Updates) ElectricBlue else Slate800)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.SystemUpdate,
                        contentDescription = "Updates",
                        tint = if (currentScreen == Screen.Updates) Color.White else NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Quick Navigation Tabs Row across the top
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            val allScreens = listOf(
                Screen.Dashboard,
                Screen.CloudinaryMedia,
                Screen.SupportLinks,
                Screen.Notices,
                Screen.Updates,
                Screen.Generator,
                Screen.Codes,
                Screen.Devices,
                Screen.Settings
            )
            items(allScreens) { screen ->
                val isSelected = currentScreen == screen
                androidx.compose.material3.FilterChip(
                    selected = isSelected,
                    onClick = { onNavigate(screen) },
                    label = {
                        Text(
                            text = screen.titleBn,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        containerColor = Slate800,
                        labelColor = Slate400,
                        iconColor = Slate400,
                        selectedContainerColor = ElectricBlue,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) ElectricBlue else Slate700,
                        selectedBorderColor = NeonCyan
                    )
                )
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
                        text = screen.titleBn,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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
