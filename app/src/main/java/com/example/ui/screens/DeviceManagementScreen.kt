package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.UserDevice
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.StudentDeviceMatcher
import com.example.util.TimestampUtils
import kotlinx.coroutines.delay

/**
 * স্টুডেন্ট ডিভাইস ভেরিফিকেশন ও কন্ট্রোল (One-Click Student Verification & Device Control)
 * Connected to Supabase `public.devices` table and RPCs:
 * - `admin_approve_device_verification(p_device_hash)`
 * - `admin_set_device_block_status(p_device_hash, p_is_banned, p_reason)`
 *
 * Features:
 * - Clean, zero-duplicate header & 3-tab selector (Pending, Active, Blocked)
 * - Accurate real-time "Last Online / Active" status (`last_seen`, `updated_at`, `approved_at`, `requested_at`)
 * - Linked Student Phone & Course Profile (`StudentDeviceMatcher`) + 1-click student note linking
 */
@Composable
fun DeviceManagementScreen(
    devices: List<UserDevice>,
    enrollments: List<CourseAccessEnrollment> = emptyList(),
    transactions: List<CoursePaymentTransaction> = emptyList(),
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

    // Live ticking clock so "সর্বশেষ অনলাইন" (Last Active / Online) updates in real time
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

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

    // Dialog state for editing student_note (শিক্ষার্থীর নাম / মোবাইল নম্বর / নোট)
    var deviceToEditNote by remember { mutableStateOf<UserDevice?>(null) }
    var studentNoteInput by remember { mutableStateOf("") }

    // Dialog state for deleting a device record
    var deviceToDelete by remember { mutableStateOf<UserDevice?>(null) }

    // Sort devices by real parsed timestamp (most recently active / requested first)
    val pendingDevicesList = remember(devices) {
        devices
            .filter { it.isPendingDevice }
            .sortedByDescending {
                TimestampUtils.parseIsoToMillis(it.requestedAt)
                    ?: TimestampUtils.parseIsoToMillis(it.lastSeen)
                    ?: TimestampUtils.parseIsoToMillis(it.updatedAt)
                    ?: 0L
            }
    }
    val activeDevicesList = remember(devices) {
        devices
            .filter { it.isActiveDevice }
            .sortedByDescending {
                TimestampUtils.parseIsoToMillis(it.lastSeen)
                    ?: TimestampUtils.parseIsoToMillis(it.lastActiveAt)
                    ?: TimestampUtils.parseIsoToMillis(it.updatedAt)
                    ?: TimestampUtils.parseIsoToMillis(it.approvedAt)
                    ?: 0L
            }
    }
    val blockedDevicesList = remember(devices) {
        devices
            .filter { it.isBlockedDevice }
            .sortedByDescending {
                TimestampUtils.parseIsoToMillis(it.updatedAt)
                    ?: TimestampUtils.parseIsoToMillis(it.lastSeen)
                    ?: TimestampUtils.parseIsoToMillis(it.requestedAt)
                    ?: 0L
            }
    }

    val pendingCount = pendingDevicesList.size
    val activeCount = activeDevicesList.size
    val blockedCount = blockedDevicesList.size

    val onlineRightNowCount = remember(devices, nowMillis) {
        devices.count { dev ->
            TimestampUtils.resolveDeviceOnlineInfo(
                lastSeen = dev.lastSeen ?: dev.lastActiveAt,
                updatedAt = dev.updatedAt,
                approvedAt = dev.approvedAt,
                requestedAt = dev.requestedAt,
                nowMillis = nowMillis
            ).isOnlineNow
        }
    }

    // If a search query is active and doesn't match the current tab, also check all devices
    val baseTabList = when (activeTab) {
        "PENDING" -> pendingDevicesList
        "ACTIVE" -> activeDevicesList
        "BLOCKED" -> blockedDevicesList
        else -> pendingDevicesList
    }

    val filteredDevices = remember(baseTabList, devices, searchQuery, enrollments, transactions) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            baseTabList
        } else {
            val matchingInTab = baseTabList.filter { dev ->
                matchesDeviceQuery(dev, q, enrollments, transactions)
            }
            if (matchingInTab.isNotEmpty()) {
                matchingInTab
            } else {
                // Search across all devices if not in current tab so 1-click jump from Course/Payment always finds it
                devices.filter { dev ->
                    matchesDeviceQuery(dev, q, enrollments, transactions)
                }
            }
        }
    }

    // Recent known student profiles for 1-click linking in Edit Note modal
    val knownStudentSuggestions = remember(enrollments, transactions) {
        val list = mutableListOf<Pair<String, String>>() // Phone to Label
        val seenPhones = mutableSetOf<String>()
        enrollments.forEach { e ->
            val phone = e.effectivePhone.ifBlank { e.effectiveAccountKey }
            if (phone.isNotBlank() && seenPhones.add(phone)) {
                val name = e.studentName?.takeIf { it.isNotBlank() } ?: "শিক্ষার্থী"
                val course = e.programTitle?.takeIf { it.isNotBlank() } ?: e.programId.orEmpty()
                val label = if (course.isNotBlank()) "$name • $phone • $course" else "$name • $phone"
                list.add(phone to label)
            }
        }
        transactions.forEach { tx ->
            val phone = tx.effectiveStudentPhone
            if (phone.isNotBlank() && seenPhones.add(phone)) {
                val name = tx.studentName?.takeIf { it.isNotBlank() } ?: "শিক্ষার্থী"
                val course = tx.programTitle?.takeIf { it.isNotBlank() } ?: tx.programId.orEmpty()
                val label = if (course.isNotBlank()) "$name • $phone • $course" else "$name • $phone"
                list.add(phone to label)
            }
        }
        list.take(12)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Unified Top Status Strip + 3 Interactive Tab Cards (No duplicate bars!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Live Online Counter + Refresh Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess)
                                    )
                                    Text(
                                        text = "এখন অনলাইন: $onlineRightNowCount",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmeraldSuccess,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate900,
                                border = BorderStroke(1.dp, Slate700)
                            ) {
                                Text(
                                    text = "মোট ${devices.size}টি ডিভাইস",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate200,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate800,
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { onRefresh() }
                                .testTag("btn_refresh_devices")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = NeonCyan,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Devices",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = "রিফ্রেশ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Single Clean 3-Tab Selector Row (Replaces the duplicate 2 tab bars)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VerificationCounterBadge(
                            modifier = Modifier.weight(1f),
                            titleBn = "অপেক্ষমান",
                            count = pendingCount,
                            accentColor = AmberWarning,
                            icon = Icons.Default.HourglassTop,
                            isSelected = activeTab == "PENDING",
                            onClick = { onFilterChange("PENDING") },
                            testTag = "tab_pending_devices"
                        )
                        VerificationCounterBadge(
                            modifier = Modifier.weight(1f),
                            titleBn = "ভেরিফাইড",
                            count = activeCount,
                            accentColor = EmeraldSuccess,
                            icon = Icons.Default.CheckCircle,
                            isSelected = activeTab == "ACTIVE",
                            onClick = { onFilterChange("ACTIVE") },
                            testTag = "tab_active_devices"
                        )
                        VerificationCounterBadge(
                            modifier = Modifier.weight(1f),
                            titleBn = "ব্লকড",
                            count = blockedCount,
                            accentColor = CoralDanger,
                            icon = Icons.Default.Block,
                            isSelected = activeTab == "BLOCKED",
                            onClick = { onFilterChange("BLOCKED") },
                            testTag = "tab_blocked_devices"
                        )
                    }

                    // Full-Width Single-Line Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_devices"),
                        placeholder = {
                            Text(
                                text = "নাম, মোবাইল নম্বর, ফোনের মডেল বা ডিভাইস আইডি খুঁজুন...",
                                color = Slate400,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        }

        // 2. Active Filter / Deep-Link Banner when navigated from Course Request or Payment Verification
        if (searchQuery.isNotBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricBlue.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎯 ফিল্টারকৃত ডিভাইস / স্টুডেন্ট: \"$searchQuery\"",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = "পাওয়া গেছে: ${filteredDevices.size}টি ডিভাইস • নিচে ব্লক/আনব্লক বা কন্ট্রোল করুন",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate200,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate900,
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.clickable { onSearchChange("") }
                        ) {
                            Text(
                                text = "সব দেখুন ✕",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Device Cards List or Empty State
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
                val studentProfile = remember(device, enrollments, transactions) {
                    StudentDeviceMatcher.resolveStudentProfileForDevice(
                        device = device,
                        enrollments = enrollments,
                        transactions = transactions
                    )
                }
                StudentVerificationDeviceCard(
                    device = device,
                    studentProfile = studentProfile,
                    nowMillis = nowMillis,
                    activeTab = activeTab,
                    onCopyId = {
                        val toCopy = device.displayDeviceId.ifBlank { device.deviceHash }
                        clipboardManager.setText(AnnotatedString(toCopy))
                        onShowToast("ডিভাইস আইডি কপি করা হয়েছে: $toCopy")
                    },
                    onCopyPhone = { phone ->
                        clipboardManager.setText(AnnotatedString(phone))
                        onShowToast("মোবাইল নম্বর কপি করা হয়েছে: $phone")
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

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    // =========================================================================
    // Modal 1: Edit Student Name / Phone / Note (`student_note`) with 1-Click Student Picker
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
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "শিক্ষার্থীর পরিচয় ও মোবাইল নম্বর লিংক করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            text = "শিক্ষার্থীর নাম ও মোবাইল নম্বর (student_note):",
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
                                    text = "যেমন: রাফি ইসলাম • 017XXXXXXXX • SSC 27",
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
                        Text(
                            text = "💡 নোটের ভেতর স্টুডেন্টের ১১ ডিজিট মোবাইল নম্বর (01XXXXXXXXX) রাখলে কোর্স ও পেমেন্ট রিকোয়েস্টের সাথে এই ডিভাইসটি অটোমেটিক লিংক হয়ে যাবে।",
                            fontSize = 10.sp,
                            color = EmeraldSuccess,
                            lineHeight = 14.sp
                        )
                    }

                    if (knownStudentSuggestions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "⚡ সাম্প্রতিক কোর্স রিকোয়েস্ট থেকে ১-ক্লিকে সিলেক্ট করুন:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                knownStudentSuggestions.forEach { (_, suggestionLabel) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Slate900,
                                        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                                        modifier = Modifier.clickable {
                                            studentNoteInput = suggestionLabel
                                        }
                                    ) {
                                        Text(
                                            text = suggestionLabel,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
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
                    text = "ডিভাইস ব্লক নিশ্চিতকরণ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "আপনি কি নিশ্চিত যে এই ডিভাইসটি ব্লক করতে চান? ব্লক করলে শিক্ষার্থী সাথে সাথে অ্যাপ থেকে লক হয়ে যাবে।",
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
                                    text = "যেমন: ভুয়া পেমেন্ট রিকোয়েস্ট / অ্যাকাউন্ট শেয়ার করার অভিযোগে ব্লক",
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
                    Text("হ্যাঁ, ব্লক করুন", fontWeight = FontWeight.Bold)
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

private fun matchesDeviceQuery(
    dev: UserDevice,
    q: String,
    enrollments: List<CourseAccessEnrollment>,
    transactions: List<CoursePaymentTransaction>
): Boolean {
    if (
        (dev.studentNote?.contains(q, ignoreCase = true) == true) ||
        dev.displayStudentName.contains(q, ignoreCase = true) ||
        dev.deviceModel.contains(q, ignoreCase = true) ||
        (dev.deviceBrand?.contains(q, ignoreCase = true) == true) ||
        dev.fullPhoneModel.contains(q, ignoreCase = true) ||
        dev.displayDeviceId.contains(q, ignoreCase = true) ||
        dev.deviceHash.contains(q, ignoreCase = true) ||
        dev.extractedPhoneFromDevice.contains(q, ignoreCase = true)
    ) {
        return true
    }
    val profile = StudentDeviceMatcher.resolveStudentProfileForDevice(dev, enrollments, transactions)
    return profile != null && (
        profile.studentPhone.contains(q, ignoreCase = true) ||
            profile.studentName.contains(q, ignoreCase = true) ||
            profile.coursesSummary.contains(q, ignoreCase = true)
        )
}

/**
 * Clean Interactive Tab Card for Pending, Active, and Blocked tabs.
 */
@Composable
private fun VerificationCounterBadge(
    modifier: Modifier = Modifier,
    titleBn: String,
    count: Int,
    accentColor: Color,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else Slate950,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else Slate700
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )
            }
            Text(
                text = titleBn,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) accentColor else Slate200,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
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
            "অনুগ্রহ করে অন্য নাম (student_note), মোবাইল নম্বর, ফোনের মডেল বা ডিভাইস আইডি দিয়ে অনুসন্ধান করুন।",
            NeonCyan
        )
        activeTab == "PENDING" -> Triple(
            "কোনো অপেক্ষমান ভেরিফিকেশন আবেদন নেই",
            "নতুন কোনো শিক্ষার্থী অ্যাপ ওপেন করে 'স্টুডেন্ট ভেরিফিকেশন করুন' বাটনে ক্লিক করলে সাথে সাথে এখানে দেখা যাবে।",
            AmberWarning
        )
        activeTab == "ACTIVE" -> Triple(
            "কোনো ভেরিফাইড ডিভাইস এখনো নেই",
            "অপেক্ষমান তালিকা থেকে কোনো ডিভাইস 'অ্যাপ্রুভ করুন' বাটনে ক্লিক করলেই সেটি এখানে যুক্ত হবে।",
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
 * Clean, well-proportioned Student Device Card:
 * - Fixes the squashed vertical blue line bug from Screenshot 1
 * - Shows Real-Time Online / Last Active Time (`last_seen`) prominently alongside Requested & Approved timestamps
 * - Shows linked Student Phone & Course Profile (`StudentDeviceMatcher`)
 */
@Composable
private fun StudentVerificationDeviceCard(
    device: UserDevice,
    studentProfile: StudentDeviceMatcher.DeviceStudentProfile?,
    nowMillis: Long,
    activeTab: String,
    onCopyId: () -> Unit,
    onCopyPhone: (String) -> Unit,
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
        isBlocked -> "ব্লকড (Blocked)"
        isPending -> "অপেক্ষমান (Pending)"
        else -> "ভেরিফাইড (Active)"
    }

    val statusIcon = when {
        isBlocked -> Icons.Default.Block
        isPending -> Icons.Default.HourglassTop
        else -> Icons.Default.CheckCircle
    }

    val onlineInfo = remember(
        device.lastSeen,
        device.lastActiveAt,
        device.updatedAt,
        device.approvedAt,
        device.requestedAt,
        nowMillis
    ) {
        TimestampUtils.resolveDeviceOnlineInfo(
            lastSeen = device.lastSeen ?: device.lastActiveAt,
            updatedAt = device.updatedAt,
            approvedAt = device.approvedAt,
            requestedAt = device.requestedAt,
            nowMillis = nowMillis
        )
    }

    val resolvedStudentName = when {
        !device.studentNote.isNullOrBlank() -> device.displayStudentName
        studentProfile != null && studentProfile.studentName.isNotBlank() ->
            "${studentProfile.studentName} (${studentProfile.studentPhone})"
        else -> "নতুন শিক্ষার্থী (নাম যোগ করুন)"
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
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Clean Top Header Row: Avatar + Student Name (with full weight) + Edit Icon + Status Pill
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
                        Text(
                            text = resolvedStudentName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (device.studentNote.isNullOrBlank() && studentProfile == null) {
                                    AmberWarning
                                } else {
                                    Color.White
                                },
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "📱 ${device.fullPhoneModel} • ${device.androidVersion ?: "Android"} (v${device.appVersion ?: "1.0"})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate200,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Compact Edit Note Icon Button (Never squashed!)
                IconButton(
                    onClick = onEditNoteClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Slate950)
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(9.dp))
                        .testTag("btn_edit_note_${device.effectiveKey}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Student Note",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Clean Single-Icon Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 2. Real-Time Online / Last Active Status Box (Accurate Postgres Timestamp Parsing)
            val onlineAccent = when {
                onlineInfo.isOnlineNow -> EmeraldSuccess
                onlineInfo.isActiveWithinHour -> NeonCyan
                else -> Slate400
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Slate950,
                border = BorderStroke(
                    1.dp,
                    if (onlineInfo.isOnlineNow) EmeraldSuccess.copy(alpha = 0.6f) else Slate700
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(onlineAccent)
                            )
                            Text(
                                text = onlineInfo.statusBadgeTextBn,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (onlineInfo.isOnlineNow) EmeraldSuccess else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Text(
                            text = onlineInfo.lastActiveExactBn,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 আবেদন: ${onlineInfo.requestedAtFormattedBn}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (!device.approvedAt.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✅ অ্যাপ্রুভড: ${onlineInfo.approvedAtFormattedBn}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 3. Linked Student Phone & Course Profile (if matched via StudentDeviceMatcher)
            val phoneToShow = studentProfile?.studentPhone?.takeIf { it.isNotBlank() }
                ?: device.extractedPhoneFromDevice.takeIf { it.isNotBlank() }

            if (phoneToShow != null || studentProfile != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElectricBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = buildString {
                                        append("স্টুডেন্ট মোবাইল: ${phoneToShow ?: "N/A"}")
                                        if (studentProfile != null && studentProfile.studentName.isNotBlank()) {
                                            append(" • ${studentProfile.studentName}")
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                )
                                if (studentProfile != null && studentProfile.coursesSummary.isNotBlank()) {
                                    Text(
                                        text = "কোর্স: ${studentProfile.coursesSummary}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (!phoneToShow.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate950,
                                border = BorderStroke(1.dp, Slate700),
                                modifier = Modifier.clickable { onCopyPhone(phoneToShow) }
                            ) {
                                Text(
                                    text = "নম্বর কপি",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Display Device ID + Student Note Quick Edit Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            .padding(horizontal = 10.dp, vertical = 7.dp),
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

                // Quick Student Note / Link Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate950,
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onEditNoteClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (device.studentNote.isNullOrBlank()) "নাম/নম্বর লিংক" else "নোট এডিট",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Show saved student_note if present
            if (!device.studentNote.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate950,
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditNoteClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📝 নোট: ${device.studentNote}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "এডিট",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // 5. If Blocked, show Ban Reason Banner
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

            // 6. Tab-Specific Primary Action Buttons (Clean single icons, no duplicate emojis!)
            when {
                // TAB 1: PENDING (`status = 'pending' AND is_banned = false`)
                isPending -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onApproveClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 11.dp, horizontal = 14.dp),
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
                                text = "অ্যাপ্রুভ করুন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Button(
                            onClick = onBlockClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralDanger,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 11.dp, horizontal = 12.dp),
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
                                text = "ব্লক করুন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // TAB 3: BLOCKED (`is_banned = true OR status = 'blocked'`)
                isBlocked -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onUnblockAndApproveClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 11.dp, horizontal = 14.dp),
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
                                text = "আনব্লক ও অ্যাপ্রুভ করুন",
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
                    // Profile View Permission Switch (`can_view_profiles`)
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
                                .padding(horizontal = 12.dp, vertical = 4.dp),
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
                                text = "ব্লক করুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

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
                                text = "পেন্ডিং করুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

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

private val Slate850Color = Color(0xFF141D2E)
