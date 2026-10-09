package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceWithStudent
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

@Composable
fun DeviceManagementScreen(
    devices: List<DeviceWithStudent>,
    searchQuery: String,
    filter: String,
    isLoading: Boolean,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleBlocked: (DeviceWithStudent) -> Unit,
    onUnbindDevice: (DeviceWithStudent) -> Unit
) {
    var deviceToUnbind by remember { mutableStateOf<DeviceWithStudent?>(null) }

    val filteredDevices = devices.filter { item ->
        val matchesSearch = item.studentName.contains(searchQuery, ignoreCase = true) ||
                item.accessCode.contains(searchQuery, ignoreCase = true) ||
                item.device.deviceModel.contains(searchQuery, ignoreCase = true) ||
                item.device.deviceId.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (filter) {
            "ACTIVE" -> !item.device.isBlocked
            "BLOCKED" -> item.device.isBlocked
            else -> true
        }
        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CoralDanger,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "রিমোট কিল-সুইচ ও ডিভাইস",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = "Hardware Lockout & Remote Device Control",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Slate800)
                        .testTag("btn_refresh_devices")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ElectricBlueLight,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NeonCyan)
                    }
                }
            }
        }

        // Info Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = CoralDanger,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "কোনো ডিভাইস ব্লক করলে MaxBird অ্যাপে পরবর্তী হার্টবিট বা ওপেন হওয়ার সাথে সাথেই অ্যাপ লক হয়ে যাবে।",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("শিক্ষার্থী, ডিভাইস মডেল অথবা HW-ID দিয়ে খুঁজুন...", color = Slate400) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate400)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralDanger,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_devices_input")
            )
        }

        // Filter Chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "সব ডিভাইস (${devices.size})",
                    "ACTIVE" to "সক্রিয় (${devices.count { !it.device.isBlocked }})",
                    "BLOCKED" to "ব্লকড (${devices.count { it.device.isBlocked }})"
                ).forEach { (key, label) ->
                    val isSelected = filter == key
                    val chipColor = if (key == "BLOCKED") CoralDanger else ElectricBlue
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(key) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor,
                            selectedLabelColor = Color.White,
                            containerColor = Slate800,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) chipColor else Slate700,
                            selectedBorderColor = chipColor,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো সক্রিয় ডিভাইস রেকর্ড পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "শিক্ষার্থী কোড দিয়ে লগইন করলে ডিভাইস তালিকা প্রদর্শিত হবে।",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }
                }
            }
        } else {
            items(filteredDevices, key = { it.device.id }) { item ->
                DeviceCard(
                    item = item,
                    onToggleBlocked = { onToggleBlocked(item) },
                    onUnbindClick = { deviceToUnbind = item }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Unbind Confirmation Dialog
    deviceToUnbind?.let { target ->
        AlertDialog(
            onDismissRequest = { deviceToUnbind = null },
            containerColor = Slate800,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "ডিভাইস আনবাইন্ড (Unbind) করবেন?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${target.studentName}-এর ${target.device.deviceModel} ডিভাইসটি সিস্টেম থেকে অপসারণ করা হবে। এর ফলে শিক্ষার্থী প্রয়োজনে নতুন ফোনে আবার লগইন করতে পারবে।",
                    color = Slate400
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUnbindDevice(target)
                        deviceToUnbind = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("আনবাইন্ড করুন (Unbind)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { deviceToUnbind = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("বাতিল", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun DeviceCard(
    item: DeviceWithStudent,
    onToggleBlocked: () -> Unit,
    onUnbindClick: () -> Unit
) {
    val isBlocked = item.device.isBlocked

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isBlocked) CoralDanger else Slate700,
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Model & Kill-Switch Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isBlocked) CoralDanger.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (isBlocked) CoralDanger else EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.device.deviceModel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Surface(
                            color = if (isBlocked) CoralDanger.copy(alpha = 0.25f) else EmeraldSuccess.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isBlocked) "⛔ কিল-সুইচ সক্রিয় (LOCKED OUT)" else "✓ সক্রিয় (ACTIVE)",
                                color = if (isBlocked) CoralDanger else EmeraldSuccess,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Instant Remote Kill-Switch Switch
                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = isBlocked,
                        onCheckedChange = { onToggleBlocked() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CoralDanger,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate700
                        ),
                        modifier = Modifier.testTag("kill_switch_${item.device.id}")
                    )
                    Text(
                        text = if (isBlocked) "ব্লকড" else "অনুমোদিত",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isBlocked) CoralDanger else Slate400,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Student & Code Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.studentName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.dp, Slate700, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = item.accessCode,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hardware ID
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hardware ID: ",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                    Text(
                        text = item.device.deviceId,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions: Remote Kill toggle button + Unbind
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onToggleBlocked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBlocked) EmeraldSuccess else CoralDanger
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_block_toggle_${item.device.id}")
                ) {
                    Icon(
                        imageVector = if (isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBlocked) "আনব্লক করুন (Unblock)" else "কিল-সুইচ সক্রিয় (Kill Device)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onUnbindClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_unbind_${item.device.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("আনবাইন্ড", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
