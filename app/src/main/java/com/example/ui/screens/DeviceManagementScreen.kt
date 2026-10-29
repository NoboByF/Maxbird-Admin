package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserDevice
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * স্টুডেন্ট ডিভাইস ভেরিফিকেশন ও কন্ট্রোল (One-Click Student Verification & Device Control)
 * Connected to Supabase `public.devices` table and RPCs:
 * - `admin_approve_device_verification(p_device_hash)`
 * - `admin_set_device_block_status(p_device_hash, p_is_banned, p_reason)`
 */
@Composable
fun DeviceManagementScreen(
    devices: List<UserDevice>,
    searchQuery: String,
    filter: String,
    isLoading: Boolean,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onApproveDevice: (UserDevice, String?) -> Unit = { _, _ -> },
    onBlockDevice: (UserDevice, String?) -> Unit = { _, _ -> },
    onUnblockAndApproveDevice: (UserDevice) -> Unit = {},
    onRevokeDeviceToPending: (UserDevice) -> Unit = {},
    onUpdateStudentNote: (UserDevice, String) -> Unit = { _, _ -> },
    onToggleBan: (UserDevice, String?) -> Unit = { _, _ -> },
    onToggleCanViewProfiles: (UserDevice) -> Unit = {},
    onDeleteDevice: (UserDevice) -> Unit = {},
    onShowToast: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    // Normalize active tab: PENDING, ACTIVE, or BLOCKED
    val activeTab = remember(filter) {
        when (filter.uppercase()) {
            "PENDING", "ALL" -> "PENDING"
            "ACTIVE" -> "ACTIVE"
            "BLOCKED", "BANNED" -> "BLOCKED"
            else -> "PENDING"
        }
    }

    // Dialog state for blocking a device with custom reason
    var deviceToBlock by remember { mutableStateOf<UserDevice?>(null) }
    var blockReasonInput by remember { mutableStateOf("অ্যাডমিন কর্তৃক ব্লক করা হয়েছে") }

    // Dialog state for editing student_note (শিক্ষার্থীর নাম / নোট)
    var deviceToEditNote by remember { mutableStateOf<UserDevice?>(null) }
    var studentNoteInput by remember { mutableStateOf("") }

    // Dialog state for deleting a device record
    var deviceToDelete by remember { mutableStateOf<UserDevice?>(null) }

    // Realtime counts for the 3 tabs
    val pendingDevicesList = remember(devices) {
        devices
            .filter { it.isPendingDevice }
            .sortedByDescending { it.requestedAt ?: it.updatedAt ?: it.lastSeen ?: "" }
    }
    val activeDevicesList = remember(devices) {
        devices
            .filter { it.isActiveDevice }
            .sortedByDescending { it.approvedAt ?: it.updatedAt ?: it.lastSeen ?: "" }
    }
    val blockedDevicesList = remember(devices) {
        devices
            .filter { it.isBlockedDevice }
            .sortedByDescending { it.updatedAt ?: it.requestedAt ?: it.lastSeen ?: "" }
    }

    val pendingCount = pendingDevicesList.size
    val activeCount = activeDevicesList.size
    val blockedCount = blockedDevicesList.size

    // Filter by selected tab and search query (student_note, device_model, display_device_id)
    val baseTabList = when (activeTab) {
        "PENDING" -> pendingDevicesList
        "ACTIVE" -> activeDevicesList
        "BLOCKED" -> blockedDevicesList
        else -> pendingDevicesList
    }

    val filteredDevices = remember(baseTabList, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            baseTabList
        } else {
            baseTabList.filter { dev ->
                (dev.studentNote?.contains(q, ignoreCase = true) == true) ||
                    dev.displayStudentName.contains(q, ignoreCase = true) ||
                    dev.deviceModel.contains(q, ignoreCase = true) ||
                    (dev.deviceBrand?.contains(q, ignoreCase = true) == true) ||
                    dev.fullPhoneModel.contains(q, ignoreCase = true) ||
                    dev.displayDeviceId.contains(q, ignoreCase = true) ||
                    dev.deviceHash.contains(q, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricBlue, NeonCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "স্টুডেন্ট ডিভাইস ভেরিফিকেশন ও কন্ট্রোল",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "১-ক্লিকে স্টুডেন্ট ভেরিফিকেশন • কোড ছাড়াই সরাসরি অ্যাক্সেস",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                        .border(1.dp, Slate700, CircleShape)
                        .testTag("btn_refresh_devices")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Devices",
                            tint = NeonCyan
                        )
                    }
                }
            }
        }

        // 2. Realtime Counter Badges Banner (Pending Requests, Active Devices, Blocked Devices)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(ElectricBlue.copy(alpha = 0.45f), NeonCyan.copy(alpha = 0.25f))
                        ),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(if (pendingCount > 0) AmberWarning else EmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "রিয়েলটাইম ডিভাইস কাউন্টার (public.devices)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, EmeraldSuccess.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "মোট ${devices.size}টি ডিভাইস",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VerificationCounterBadge(
                            modifier = Modifier.weight(1f),
                            titleBn = "অপেক্ষমান আবেদন",
                            subtitleEn = "Pending Requests",
                            count = pendingCount,
                            accentColor = AmberWarning,
                            icon = Icons.Default.HourglassTop,
                            isSelected = activeTab == "PENDING",
                            onClick = { onFilterChange("PENDING") },
                            testTag = "badge_pending_requests"
                        )
                        VerificationCounterBadge(
                            modifier = Modifier.weight(1f),
                            titleBn = "ভেরিফাইড ডিভাইস",
                            subtitleEn = "Active Devices",
                            count = activeCount,
                            accentColor = EmeraldSuccess,
                            icon = Icons.Default.CheckCircle,
                            isSelected = activeTab == "ACTIVE",
                            onClick = { onFilterChange("ACTIVE") },
                            testTag = "badge_active_devices"
                        )
                        VerificationCounterBadge(
                            modifier = Modifier.weight(1f),
                            titleBn = "ব্লকড ডিভাইস",
                            subtitleEn = "Blocked Devices",
                            count = blockedCount,
                            accentColor = CoralDanger,
                            icon = Icons.Default.Block,
                            isSelected = activeTab == "BLOCKED",
                            onClick = { onFilterChange("BLOCKED") },
                            testTag = "badge_blocked_devices"
                        )
                    }
                }
            }
        }

        // 3. Search Bar & 3 Clear Tabs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Search Input by student_note, device_model, or display_device_id
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_devices"),
                    placeholder = {
                        Text(
                            text = "শিক্ষার্থীর নাম (student_note), ফোনের মডেল বা ডিভাইস আইডি দিয়ে খুঁজুন...",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonCyan
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Slate400
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // 3 Distinct Navigation Tabs:
                // 1) অপেক্ষমান আবেদন (Pending)
                // 2) ভেরিফাইড ডিভাইস (Active)
                // 3) ব্লকড ডিভাইস (Blocked)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate950)
                        .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DeviceVerificationTabItem(
                        modifier = Modifier.weight(1f),
                        label = "অপেক্ষমান",
                        count = pendingCount,
                        selected = activeTab == "PENDING",
                        accentColor = AmberWarning,
                        onClick = { onFilterChange("PENDING") },
                        testTag = "tab_pending_devices"
                    )
                    DeviceVerificationTabItem(
                        modifier = Modifier.weight(1f),
                        label = "ভেরিফাইড",
                        count = activeCount,
                        selected = activeTab == "ACTIVE",
                        accentColor = EmeraldSuccess,
                        onClick = { onFilterChange("ACTIVE") },
                        testTag = "tab_active_devices"
                    )
                    DeviceVerificationTabItem(
                        modifier = Modifier.weight(1f),
                        label = "ব্লকড",
                        count = blockedCount,
                        selected = activeTab == "BLOCKED",
                        accentColor = CoralDanger,
                        onClick = { onFilterChange("BLOCKED") },
                        testTag = "tab_blocked_devices"
                    )
                }
            }
        }

        // 4. Active Tab Context Banner
        item {
            val (bannerTitle, bannerSubtitle, bannerColor) = when (activeTab) {
                "PENDING" -> Triple(
                    "⏳ অপেক্ষমান ভেরিফিকেশন আবেদন ($pendingCount)",
                    "শিক্ষার্থী অ্যাপ থেকে 'স্টুডেন্ট ভেরিফিকেশন করুন' বাটনে ক্লিক করলে এখানে সবার উপরে দেখাবে। ১ ক্লিকে অ্যাপ্রুভ করুন!",
                    AmberWarning
                )
                "ACTIVE" -> Triple(
                    "✅ ভেরিফাইড ও অনুমোদিত ডিভাইস ($activeCount)",
                    "এই ডিভাইসগুলো সরাসরি অ্যাপে প্রবেশ করতে পারবে। চাইলে শিক্ষার্থীর নাম/নোট এডিট করে চিনতে সুবিধা করুন।",
                    EmeraldSuccess
                )
                else -> Triple(
                    "🚫 ব্লক করা ডিভাইস তালিকা ($blockedCount)",
                    "ব্লক করা ডিভাইসগুলো অ্যাপে প্রবেশ করতে পারবে না। চাইলে ১ ক্লিকে আনব্লক ও পুনরায় অ্যাপ্রুভ করতে পারবেন।",
                    CoralDanger
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bannerColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = bannerTitle,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = bannerColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = bannerSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate200,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }

        // 5. Device Cards List or Empty State
        if (filteredDevices.isEmpty()) {
            item {
                EmptyDeviceTabState(
                    activeTab = activeTab,
                    hasSearchQuery = searchQuery.isNotBlank()
                )
            }
        } else {
            items(
                items = filteredDevices,
                key = { it.effectiveKey.ifBlank { it.hashCode().toString() } }
            ) { device ->
                StudentVerificationDeviceCard(
                    device = device,
                    activeTab = activeTab,
                    onCopyId = {
                        val toCopy = device.displayDeviceId.ifBlank { device.deviceHash }
                        clipboardManager.setText(AnnotatedString(toCopy))
                        onShowToast("ডিভাইস আইডি কপি করা হয়েছে: $toCopy")
                    },
                    onApproveClick = {
                        onApproveDevice(device, device.studentNote)
                    },
                    onBlockClick = {
                        blockReasonInput = device.banReason?.takeIf { it.isNotBlank() }
                            ?: "অ্যাডমিন কর্তৃক ব্লক করা হয়েছে"
                        deviceToBlock = device
                    },
                    onUnblockAndApproveClick = {
                        onUnblockAndApproveDevice(device)
                    },
                    onRevokeToPendingClick = {
                        onRevokeDeviceToPending(device)
                    },
                    onEditNoteClick = {
                        studentNoteInput = device.studentNote.orEmpty()
                        deviceToEditNote = device
                    },
                    onToggleCanViewProfiles = {
                        onToggleCanViewProfiles(device)
                    },
                    onDeleteClick = {
                        deviceToDelete = device
                    }
                )
            }
        }
    }

    // =========================================================================
    // Modal 1: Edit Student Name / Note (`student_note`)
    // =========================================================================
    deviceToEditNote?.let { dev ->
        AlertDialog(
            onDismissRequest = { deviceToEditNote = null },
            containerColor = Slate850Color,
            titleContentColor = Color.White,
            textContentColor = Slate400,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "শিক্ষার্থীর নাম / নোট এডিট করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate950,
                        border = BorderStroke(1.dp, Slate700)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📱 ডিভাইস: ${dev.fullPhoneModel}",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🆔 আইডি: ${dev.displayDeviceId.ifBlank { dev.deviceHash.take(16) }}",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "শিক্ষার্থীর নাম বা পরিচয় নোট (student_note):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                        OutlinedTextField(
                            value = studentNoteInput,
                            onValueChange = { studentNoteInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_student_note"),
                            placeholder = {
                                Text(
                                    text = "যেমন: রাফি ইসলাম - HSC 26 (017XXXXXXXX)",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            },
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStudentNote(dev, studentNoteInput.trim())
                        deviceToEditNote = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_save_student_note")
                ) {
                    Text("সেভ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deviceToEditNote = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate400)
                ) {
                    Text("বাতিল")
                }
            }
        )
    }

    // =========================================================================
    // Modal 2: Block Device Confirmation (`admin_set_device_block_status`)
    // =========================================================================
    deviceToBlock?.let { dev ->
        AlertDialog(
            onDismissRequest = { deviceToBlock = null },
            containerColor = Slate850Color,
            titleContentColor = Color.White,
            textContentColor = Slate400,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CoralDanger.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = CoralDanger,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "ডিভাইস ব্লক নিশ্চিতকরণ (Block Device)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "আপনি কি নিশ্চিত যে এই ডিভাইসটি ব্লক করতে চান? ব্লক করলে শিক্ষার্থী অ্যাপে প্রবেশ করতে পারবে না।",
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate950,
                        border = BorderStroke(1.dp, Slate700)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "👤 শিক্ষার্থী: ${dev.displayStudentName}",
                                fontSize = 12.sp,
                                color = AmberWarning,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "📱 মডেল: ${dev.fullPhoneModel}",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🆔 ডিসপ্লে আইডি: ${dev.displayDeviceId.ifBlank { dev.deviceHash.take(16) }}",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ব্লক করার কারণ (ban_reason):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                        OutlinedTextField(
                            value = blockReasonInput,
                            onValueChange = { blockReasonInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_ban_reason"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800,
                                focusedBorderColor = CoralDanger,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            placeholder = {
                                Text(
                                    text = "যেমন: অনুমোদনহীন ডিভাইস / শেয়ার করার অভিযোগে ব্লক",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = blockReasonInput.trim().ifBlank { "অ্যাডমিন কর্তৃক ব্লক করা হয়েছে" }
                        onBlockDevice(dev, reason)
                        deviceToBlock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirm_ban")
                ) {
                    Text("🚫 হ্যাঁ, ব্লক করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deviceToBlock = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate400)
                ) {
                    Text("বাতিল")
                }
            }
        )
    }

    // =========================================================================
    // Modal 3: Delete Device Record Confirmation
    // =========================================================================
    deviceToDelete?.let { dev ->
        AlertDialog(
            onDismissRequest = { deviceToDelete = null },
            containerColor = Slate850Color,
            titleContentColor = Color.White,
            textContentColor = Slate400,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = CoralDanger,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("ডিভাইস রেকর্ড মুছে ফেলবেন?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিত যে '${dev.displayStudentName}' (${dev.displayDeviceId.ifBlank { dev.fullPhoneModel }}) এর ডিভাইস রেকর্ড ডাটাবেজ থেকে স্থায়ীভাবে মুছে ফেলতে চান?",
                    fontSize = 13.sp,
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDevice(dev)
                        deviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirm_delete_device")
                ) {
                    Text("মুছে ফেলুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deviceToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate400)
                ) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * Interactive Counter Badge at the top for Pending Requests, Active Devices, and Blocked Devices.
 */
@Composable
private fun VerificationCounterBadge(
    modifier: Modifier = Modifier,
    titleBn: String,
    subtitleEn: String,
    count: Int,
    accentColor: Color,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.18f) else Slate950,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else Slate700
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                )
            }
            Text(
                text = titleBn,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) accentColor else Slate200,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitleEn,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate400,
                    fontSize = 9.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Segmented Tab Item for Pending, Active, and Blocked tabs.
 */
@Composable
private fun DeviceVerificationTabItem(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) accentColor else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (selected) Slate950 else Slate200,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(5.dp))
            Surface(
                shape = CircleShape,
                color = if (selected) Slate950.copy(alpha = 0.22f) else accentColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (selected) Slate950 else accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

/**
 * Empty state card tailored to each of the 3 tabs.
 */
@Composable
private fun EmptyDeviceTabState(
    activeTab: String,
    hasSearchQuery: Boolean
) {
    val (title, subtitle, accent) = when {
        hasSearchQuery -> Triple(
            "কোনো ডিভাইস খুঁজে পাওয়া যায়নি",
            "অনুগ্রহ করে অন্য নাম (student_note), ফোনের মডেল বা ডিভাইস আইডি দিয়ে অনুসন্ধান করুন।",
            NeonCyan
        )
        activeTab == "PENDING" -> Triple(
            "কোনো অপেক্ষমান ভেরিফিকেশন আবেদন নেই",
            "নতুন কোনো শিক্ষার্থী অ্যাপ ওপেন করে 'স্টুডেন্ট ভেরিফিকেশন করুন' বাটনে ক্লিক করলে সাথে সাথে এখানে দেখা যাবে।",
            AmberWarning
        )
        activeTab == "ACTIVE" -> Triple(
            "কোনো ভেরিফাইড ডিভাইস এখনো নেই",
            "অপেক্ষমান তালিকা থেকে কোনো ডিভাইস '✅ অ্যাপ্রুভ করুন' বাটনে ক্লিক করলেই সেটি এখানে যুক্ত হবে।",
            EmeraldSuccess
        )
        else -> Triple(
            "কোনো ব্লকড ডিভাইস নেই",
            "আপনি কোনো ডিভাইস ব্লক করলে সেটি এই তালিকায় থাকবে।",
            CoralDanger
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate400,
                    lineHeight = 17.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Card representing a student device in Pending, Active, or Blocked state.
 */
@Composable
private fun StudentVerificationDeviceCard(
    device: UserDevice,
    activeTab: String,
    onCopyId: () -> Unit,
    onApproveClick: () -> Unit,
    onBlockClick: () -> Unit,
    onUnblockAndApproveClick: () -> Unit,
    onRevokeToPendingClick: () -> Unit,
    onEditNoteClick: () -> Unit,
    onToggleCanViewProfiles: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isBlocked = device.isBlockedDevice
    val isPending = device.isPendingDevice

    val statusColor = when {
        isBlocked -> CoralDanger
        isPending -> AmberWarning
        else -> EmeraldSuccess
    }

    val statusLabel = when {
        isBlocked -> "🚫 ব্লকড (Blocked)"
        isPending -> "⏳ অপেক্ষমান (Pending)"
        else -> "✅ ভেরিফাইড (Active)"
    }

    val statusIcon = when {
        isBlocked -> Icons.Default.Block
        isPending -> Icons.Default.HourglassTop
        else -> Icons.Default.CheckCircle
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isPending) 1.5.dp else 1.2.dp,
                color = statusColor.copy(alpha = if (isPending) 0.7f else 0.45f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("card_device_${device.displayDeviceId.ifBlank { device.effectiveKey }}")
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Row: Student Name (`student_note`) + Edit Note Button & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = device.displayStudentName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (device.studentNote.isNullOrBlank()) AmberWarning else Color.White,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate950,
                                border = BorderStroke(0.8.dp, NeonCyan.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable { onEditNoteClick() }
                                    .testTag("btn_edit_note_${device.effectiveKey}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Student Note",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (device.studentNote.isNullOrBlank()) "নাম যোগ করুন" else "এডিট",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📱 ${device.fullPhoneModel} • ${device.androidVersion ?: "Android"} (v${device.appVersion ?: "1.0"})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate200,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = Slate700, thickness = 0.8.dp)

            // 2. Display Device ID + Timestamp Info Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copyable Display Device ID (`display_device_id`)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate950,
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCopyId() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DISPLAY DEVICE ID",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate400,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = device.displayDeviceId.ifBlank { device.deviceHash.take(16) },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Slate800,
                            border = BorderStroke(0.5.dp, Slate700)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy ID",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "কপি",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 3. Timestamps Row: Requested At / Approved At
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                    val timeLabel = when {
                        isPending -> "আবেদনের সময়: ${formatTimestampWithRelative(device.requestedAt ?: device.updatedAt ?: device.lastSeen)}"
                        !device.approvedAt.isNullOrBlank() -> "অ্যাপ্রুভড: ${formatTimestampWithRelative(device.approvedAt)}"
                        else -> "আপডেট: ${formatTimestampWithRelative(device.updatedAt ?: device.requestedAt ?: device.lastSeen)}"
                    }
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    )
                }

                if (!device.requestedAt.isNullOrBlank() && !isPending) {
                    Text(
                        text = "আবেদন: ${formatRelativeTime(device.requestedAt)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate400,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // 4. If Blocked, show Ban Reason Banner
            if (isBlocked && !device.banReason.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CoralDanger.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CoralDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ব্লক করার কারণ: ${device.banReason}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // 5. Tab-Specific Primary Action Buttons
            when {
                // TAB 1: PENDING (`status = 'pending' AND is_banned = false`)
                isPending || activeTab == "PENDING" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. ✅ অ্যাপ্রুভ করুন (Approve) — 1-Click Verification
                        Button(
                            onClick = onApproveClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("btn_approve_device_${device.effectiveKey}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✅ অ্যাপ্রুভ করুন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // 2. 🚫 ব্লক করুন (Block)
                        Button(
                            onClick = onBlockClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralDanger,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_block_pending_${device.effectiveKey}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🚫 ব্লক করুন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // TAB 3: BLOCKED (`is_banned = true OR status = 'blocked'`)
                isBlocked || activeTab == "BLOCKED" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🔓 আনব্লক ও অ্যাপ্রুভ করুন (Unblock & Approve)
                        Button(
                            onClick = onUnblockAndApproveClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_unblock_approve_${device.effectiveKey}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🔓 আনব্লক ও অ্যাপ্রুভ করুন (Unblock & Approve)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate950)
                                .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                                .testTag("btn_delete_device_${device.effectiveKey}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Record",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // TAB 2: ACTIVE / VERIFIED (`status = 'active' AND is_banned = false`)
                else -> {
                    // Student Note Quick Edit Bar for Active Devices
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate950,
                        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditNoteClick() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "শিক্ষার্থীর নাম / নোট (student_note)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate400,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = device.studentNote?.takeIf { it.isNotBlank() }
                                            ?: "নাম দেওয়া নেই — চিনতে নাম/নোট লিখতে এখানে ট্যাপ করুন",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (device.studentNote.isNullOrBlank()) AmberWarning else Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "এডিট করুন",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Optional Profile View Permission Switch (`can_view_profiles`)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate950,
                        border = BorderStroke(
                            1.dp,
                            if (device.canViewProfiles) EmeraldSuccess.copy(alpha = 0.5f) else Slate700
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (device.canViewProfiles) EmeraldSuccess else Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "প্রোফাইল ভিউ পারমিশন (can_view_profiles)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Switch(
                                checked = device.canViewProfiles,
                                onCheckedChange = { onToggleCanViewProfiles() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldSuccess,
                                    uncheckedThumbColor = Slate400,
                                    uncheckedTrackColor = Slate700
                                ),
                                modifier = Modifier.testTag("switch_can_view_profiles_${device.effectiveKey}")
                            )
                        }
                    }

                    // Active Device Actions: 1-Click Block, Revoke to Pending, Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1-Click Block Button
                        Button(
                            onClick = onBlockClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralDanger,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_ban_${device.effectiveKey}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "🚫 ব্লক করুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Revoke / Move to Pending Button
                        OutlinedButton(
                            onClick = onRevokeToPendingClick,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_revoke_${device.effectiveKey}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "রিমুভ / পেন্ডিং",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Delete Record Button
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate950)
                                .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                                .testTag("btn_delete_device_${device.effectiveKey}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Record",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formats ISO timestamp with both date/time and relative indicator.
 */
private fun formatTimestampWithRelative(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "অজানা"
    val parsed = parseIsoDate(dateString) ?: return dateString.take(16).replace("T", " ")
    val formatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(parsed)
    val rel = formatRelativeTime(dateString)
    return "$formatted ($rel)"
}

private fun parseIsoDate(dateString: String?): Date? {
    if (dateString.isNullOrBlank()) return null
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    )
    for (format in formats) {
        try {
            val parsed = format.parse(dateString)
            if (parsed != null) return parsed
        } catch (_: Exception) {
        }
    }
    return null
}

/**
 * Parses timestamps and computes a relative Bengali human-readable string.
 */
private fun formatRelativeTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "অজানা"
    return try {
        val parsedDate = parseIsoDate(dateString) ?: return dateString.take(10)

        val diffMs = System.currentTimeMillis() - parsedDate.time
        val diffSec = diffMs / 1000
        val diffMin = diffSec / 60
        val diffHours = diffMin / 60
        val diffDays = diffHours / 24

        when {
            diffSec < 60 -> "এইমাত্র"
            diffMin < 60 -> "$diffMin মিনিট আগে"
            diffHours < 24 -> "$diffHours ঘণ্টা আগে"
            diffDays == 1L -> "গতকাল"
            diffDays < 7 -> "$diffDays দিন আগে"
            else -> SimpleDateFormat("dd MMM, yyyy", Locale.US).format(parsedDate)
        }
    } catch (_: Exception) {
        dateString.take(10)
    }
}

private val Slate850Color = Color(0xFF141D2E)
