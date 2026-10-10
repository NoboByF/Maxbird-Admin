package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserDevice
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CoralDark
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Clean & modern Material 3 screen for managing real user devices
 * connected to Supabase table `public.devices`.
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
    onToggleBan: (UserDevice, String?) -> Unit,
    onDeleteDevice: (UserDevice) -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    // Dialog state for banning a device with custom reason
    var deviceToBan by remember { mutableStateOf<UserDevice?>(null) }
    var banReasonInput by remember { mutableStateOf("অ্যাডমিন কর্তৃক ব্যান করা হয়েছে") }

    // Dialog state for deleting a device record
    var deviceToDelete by remember { mutableStateOf<UserDevice?>(null) }

    // Filter and search logic
    val filteredDevices = remember(devices, searchQuery, filter) {
        devices.filter { dev ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                dev.displayDeviceId.contains(searchQuery, ignoreCase = true) ||
                        dev.deviceModel.contains(searchQuery, ignoreCase = true) ||
                        (dev.deviceBrand?.contains(searchQuery, ignoreCase = true) == true) ||
                        (dev.appliedCode?.contains(searchQuery, ignoreCase = true) == true) ||
                        dev.deviceHash.contains(searchQuery, ignoreCase = true)
            }

            val matchesFilter = when (filter) {
                "ACTIVE" -> !dev.isBanned
                "BANNED" -> dev.isBanned
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val totalCount = devices.size
    val activeCount = devices.count { !it.isBanned }
    val bannedCount = devices.count { it.isBanned }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডিভাইস নিয়ন্ত্রণ ও হার্ডওয়্যার সুরক্ষা",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = "Supabase `public.devices` টেলিমেট্রি ও ব্যান কন্ট্রোল",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 40.dp)
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Slate800)
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

        // Live Telemetry & Quick Metrics Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate700, RoundedCornerShape(16.dp))
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
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Supabase লাইভ ডিভাইস টেলিমেট্রি",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Slate950,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Slate700)
                        ) {
                            Text(
                                text = "TABLE: public.devices",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate400,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // 3 Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DeviceMetricPill(
                            modifier = Modifier.weight(1f),
                            label = "মোট ডিভাইস",
                            count = totalCount.toString(),
                            accentColor = ElectricBlueLight
                        )
                        DeviceMetricPill(
                            modifier = Modifier.weight(1f),
                            label = "সক্রিয়",
                            count = activeCount.toString(),
                            accentColor = EmeraldSuccess
                        )
                        DeviceMetricPill(
                            modifier = Modifier.weight(1f),
                            label = "ব্যান করা",
                            count = bannedCount.toString(),
                            accentColor = CoralDanger
                        )
                    }
                }
            }
        }

        // Search Bar & Filter Tabs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_devices"),
                    placeholder = {
                        Text(
                            text = "ডিভাইস আইডি, মডেল বা কোড দিয়ে খুঁজুন...",
                            color = Slate400,
                            fontSize = 13.sp
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

                // Filter Tabs: All, Active, Banned
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter == "ALL",
                        onClick = { onFilterChange("ALL") },
                        label = { Text("সকল ডিভাইস ($totalCount)") },
                        modifier = Modifier.testTag("filter_all_devices"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Slate800,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filter == "ALL",
                            borderColor = Slate700,
                            selectedBorderColor = ElectricBlue
                        )
                    )

                    FilterChip(
                        selected = filter == "ACTIVE",
                        onClick = { onFilterChange("ACTIVE") },
                        label = { Text("সক্রিয় ($activeCount)") },
                        modifier = Modifier.testTag("filter_active_devices"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess,
                            selectedLabelColor = Color.White,
                            containerColor = Slate800,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filter == "ACTIVE",
                            borderColor = Slate700,
                            selectedBorderColor = EmeraldSuccess
                        )
                    )

                    FilterChip(
                        selected = filter == "BANNED",
                        onClick = { onFilterChange("BANNED") },
                        label = { Text("🚫 ব্যান করা ($bannedCount)") },
                        modifier = Modifier.testTag("filter_banned_devices"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralDanger,
                            selectedLabelColor = Color.White,
                            containerColor = Slate800,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filter == "BANNED",
                            borderColor = Slate700,
                            selectedBorderColor = CoralDanger
                        )
                    )
                }
            }
        }

        // Device List Section
        if (filteredDevices.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Slate700),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotEmpty()) "কোনো ডিভাইস খুঁজে পাওয়া যায়নি" else "কোনো ডিভাইস ডাটাবেজে নেই",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "অনুগ্রহ করে অন্য শব্দ দিয়ে অনুসন্ধান করুন।" else "শিক্ষার্থী যখন অ্যাপ ইনস্টল করে অ্যাক্টিভেট করবে, এখানে তার হার্ডওয়্যার তথ্য যুক্ত হবে।",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate400
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredDevices, key = { it.id.ifBlank { it.deviceHash } }) { device ->
                RealDeviceCard(
                    device = device,
                    onCopyId = {
                        val toCopy = device.displayDeviceId.ifBlank { device.deviceHash }
                        clipboardManager.setText(AnnotatedString(toCopy))
                        onShowToast("ডিভাইস আইডি কপি করা হয়েছে: $toCopy")
                    },
                    onBanClick = {
                        banReasonInput = "অ্যাডমিন কর্তৃক ব্যান করা হয়েছে"
                        deviceToBan = device
                    },
                    onUnbanClick = {
                        onToggleBan(device, null)
                    },
                    onDeleteClick = {
                        deviceToDelete = device
                    }
                )
            }
        }
    }

    // Ban Confirmation Modal with Custom Reason Input
    deviceToBan?.let { dev ->
        AlertDialog(
            onDismissRequest = { deviceToBan = null },
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
                    text = "ডিভাইস ব্যান নিশ্চিতকরণ (Ban Device)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "আপনি কি নিশ্চিত যে নিচের ডিভাইসটিকে ব্যান করতে চান? ব্যান করলে এই ডিভাইস থেকে ম্যাক্সবার্ড অ্যাপ অবিলম্বে লক হয়ে যাবে।",
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    // Target summary box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate950,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "📱 মডেল: ${dev.deviceBrand ?: ""} ${dev.deviceModel}",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🆔 ডিসপ্লে আইডি: ${dev.displayDeviceId}",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                            if (!dev.appliedCode.isNullOrBlank()) {
                                Text(
                                    text = "🔑 ব্যবহৃত কোড: ${dev.appliedCode}",
                                    fontSize = 11.sp,
                                    color = EmeraldSuccess
                                )
                            }
                        }
                    }

                    // Ban Reason Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ব্যানের কারণ লিখুন (ব্যবহারকারী দেখতে পাবে):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                        OutlinedTextField(
                            value = banReasonInput,
                            onValueChange = { banReasonInput = it },
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
                            placeholder = { Text("যেমন: একাধিক ডিভাইসে শেয়ার করা হয়েছে", fontSize = 12.sp, color = Slate400) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = banReasonInput.trim().ifBlank { "অ্যাডমিন কর্তৃক ব্যান করা হয়েছে" }
                        onToggleBan(dev, reason)
                        deviceToBan = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirm_ban")
                ) {
                    Text("হ্যাঁ, ব্যান করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deviceToBan = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate400)
                ) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Delete Record Confirmation Modal
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
                    text = "আপনি কি নিশ্চিত যে '${dev.displayDeviceId}' (${dev.deviceModel}) এর টেলিমেট্রি রেকর্ড ডাটাবেজ থেকে স্থায়ীভাবে মুছে ফেলতে চান?",
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
 * Clean & modern Material 3 device card displaying real user device hardware & telemetry.
 */
@Composable
private fun RealDeviceCard(
    device: UserDevice,
    onCopyId: () -> Unit,
    onBanClick: () -> Unit,
    onUnbanClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isBanned = device.isBanned || device.status.equals("banned", ignoreCase = true)
    val cardBorder = if (isBanned) CoralDanger.copy(alpha = 0.6f) else Slate700

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, cardBorder, RoundedCornerShape(16.dp))
            .testTag("card_device_${device.displayDeviceId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Phone Model & Status Indicator
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBanned) CoralDanger.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (isBanned) CoralDanger else ElectricBlueLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${device.deviceBrand?.let { "$it " } ?: ""}${device.deviceModel}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${device.androidVersion ?: "Android"} • v${device.appVersion ?: "1.0.0"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400
                            )
                        )
                    }
                }

                // Status Badge: Green chip for Active, Red chip for Banned
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isBanned) CoralDanger.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBanned) CoralDanger else EmeraldSuccess
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isBanned) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isBanned) CoralDanger else EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBanned) "🚫 ব্যান করা (Banned)" else "সক্রিয় (Active)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isBanned) CoralDanger else EmeraldSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = Slate700, thickness = 0.8.dp)

            // Display Device ID Box with 1-Click Copy
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Slate950,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopyId() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
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
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate800,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Slate700)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = NeonCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "কপি",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Applied Code & Last Active Timestamp Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Applied Access Code badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricBlue.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, ElectricBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "কোড: ${device.appliedCode ?: "নেই"}",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Last active relative timestamp
                Text(
                    text = "সর্বশেষ সক্রিয়: ${formatRelativeTime(device.lastSeen)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontSize = 11.sp
                    )
                )
            }

            // If device is Banned, show Ban Reason Banner
            if (isBanned && !device.banReason.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CoralDanger.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, CoralDanger)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CoralDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ব্যানের কারণ: ${device.banReason}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Action Buttons: Ban / Unban & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ban / Unban Button
                if (isBanned) {
                    Button(
                        onClick = onUnbanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_unban_${device.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আন-ব্যান করুন (Unban Device)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onBanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_ban_${device.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ব্যান করুন (Ban Device)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Delete Record Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                        .testTag("btn_delete_device_${device.id}")
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

/**
 * Metric summary pill.
 */
@Composable
private fun DeviceMetricPill(
    modifier: Modifier = Modifier,
    label: String,
    count: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Slate950,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate400,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Parses timestamps and computes a relative Bengali human-readable string.
 */
private fun formatRelativeTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "অজানা"
    return try {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        )

        var parsedDate: Date? = null
        for (format in formats) {
            try {
                parsedDate = format.parse(dateString)
                if (parsedDate != null) break
            } catch (_: Exception) {}
        }

        if (parsedDate == null) return dateString.take(10)

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
