package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccessCode
import com.example.data.model.ActivatedDevice
import com.example.ui.theme.CoralDanger
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
fun CodeManagementScreen(
    codes: List<AccessCode>,
    searchQuery: String,
    filter: String,
    isLoading: Boolean,
    selectedCodeForDevices: AccessCode?,
    linkedDevices: List<ActivatedDevice>,
    isLoadingLinkedDevices: Boolean,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleActive: (AccessCode) -> Unit,
    onDeleteCode: (AccessCode) -> Unit,
    onOpenLinkedDevices: (AccessCode) -> Unit,
    onCloseLinkedDevices: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var codeToDelete by remember { mutableStateOf<AccessCode?>(null) }

    val filteredCodes = codes.filter { item ->
        val matchesSearch = item.studentName.contains(searchQuery, ignoreCase = true) ||
                item.code.contains(searchQuery, ignoreCase = true) ||
                (item.note?.contains(searchQuery, ignoreCase = true) == true)
        val matchesFilter = when (filter) {
            "ACTIVE" -> item.isActive
            "INACTIVE" -> !item.isActive
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
                    Text(
                        text = "শিক্ষার্থী ও এক্সেস কোড",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "মোট ${codes.size} টি কোড • Student Key Management",
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
                        .testTag("btn_refresh_codes")
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

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("শিক্ষার্থীর নাম অথবা কোড দিয়ে খুঁজুন...", color = Slate400) },
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
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_codes_input")
            )
        }

        // Filter Chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "সব কোড (${codes.size})",
                    "ACTIVE" to "সক্রিয় (${codes.count { it.isActive }})",
                    "INACTIVE" to "নিষ্ক্রিয় (${codes.count { !it.isActive }})"
                ).forEach { (key, label) ->
                    val isSelected = filter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(key) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Slate800,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) ElectricBlue else Slate700,
                            selectedBorderColor = ElectricBlue,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        if (filteredCodes.isEmpty()) {
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
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো এক্সেস কোড পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "ভিন্ন নামে খুঁজুন অথবা নতুন কোড তৈরি করুন।",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }
                }
            }
        } else {
            items(filteredCodes, key = { it.id }) { code ->
                StudentCodeCard(
                    code = code,
                    onCopyCode = {
                        clipboardManager.setText(AnnotatedString(code.code))
                        onShowToast("কোড ${code.code} কপি হয়েছে!")
                    },
                    onToggleActive = { onToggleActive(code) },
                    onDeleteClick = { codeToDelete = code },
                    onViewDevices = { onOpenLinkedDevices(code) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Delete Confirmation Dialog
    codeToDelete?.let { targetCode ->
        AlertDialog(
            onDismissRequest = { codeToDelete = null },
            containerColor = Slate800,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "কোড মুছে ফেলতে চান?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${targetCode.studentName}-এর কোড (${targetCode.code}) মুছে ফেললে তার সকল সংযুক্ত ডিভাইস স্বয়ংক্রিয়ভাবে অ্যাক্সেস হারাবে।",
                    color = Slate400
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCode(targetCode)
                        codeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("মুছে ফেলুন (Delete)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { codeToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("বাতিল", color = Color.White)
                }
            }
        )
    }

    // Linked Devices Dialog
    selectedCodeForDevices?.let { code ->
        AlertDialog(
            onDismissRequest = onCloseLinkedDevices,
            containerColor = Slate800,
            shape = RoundedCornerShape(18.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সংযুক্ত ডিভাইসসমূহ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${code.studentName} (${code.code})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "অনুমোদিত সর্বোচ্চ ডিভাইস: ${code.maxDevices}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoadingLinkedDevices) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonCyan, strokeWidth = 2.dp)
                        }
                    } else if (linkedDevices.isEmpty()) {
                        Surface(
                            color = Slate700.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "এই কোডে এখনো কোনো ডিভাইস সক্রিয় করা হয়নি।",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            linkedDevices.forEach { dev ->
                                Surface(
                                    color = Slate900,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (dev.isBlocked) CoralDanger else Slate700,
                                            RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = dev.deviceModel,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                            Surface(
                                                color = if (dev.isBlocked) CoralDanger.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (dev.isBlocked) "ব্লকড (Killed)" else "সক্রিয় (Active)",
                                                    color = if (dev.isBlocked) CoralDanger else EmeraldSuccess,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "HW ID: ${dev.deviceId}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Slate400,
                                                fontFamily = FontFamily.Monospace
                                            )
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
                    onClick = onCloseLinkedDevices,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("বন্ধ করুন", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun StudentCodeCard(
    code: AccessCode,
    onCopyCode: () -> Unit,
    onToggleActive: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewDevices: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (code.isActive) Slate700 else CoralDanger.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Student Name + Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (code.isActive) EmeraldSuccess else CoralDanger)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = code.studentName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Switch(
                    checked = code.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldSuccess,
                        uncheckedThumbColor = Slate400,
                        uncheckedTrackColor = Slate700
                    ),
                    modifier = Modifier.testTag("switch_active_${code.code}")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code Badge & Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { onCopyCode() }
                        .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = code.code,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = ElectricBlueLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Surface(
                    color = Slate700,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Max Devices: ${code.maxDevices}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (!code.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "নোট: ${code.note}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: View Linked Devices + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onViewDevices,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_view_devices_${code.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সংযুক্ত ডিভাইস", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Slate700.copy(alpha = 0.6f))
                        .size(36.dp)
                        .testTag("btn_delete_${code.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Code",
                        tint = CoralDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
