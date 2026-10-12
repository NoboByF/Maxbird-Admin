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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUpdate
import com.example.ui.theme.CardBorderGradientEnd
import com.example.ui.theme.CardBorderGradientStart
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.Violet500

@Composable
fun UpdatesManagementScreen(
    updates: List<AppUpdate>,
    isLoading: Boolean,
    isDialogOpen: Boolean,
    editingId: String?,
    versionName: String,
    versionCode: String,
    minVersionCode: String,
    isForce: Boolean,
    title: String,
    changelog: String,
    downloadUrl: String,
    buttonText: String,
    isActive: Boolean,
    onOpenCreateDialog: () -> Unit,
    onOpenEditDialog: (AppUpdate) -> Unit,
    onCloseDialog: () -> Unit,
    onVersionNameChange: (String) -> Unit,
    onVersionCodeChange: (String) -> Unit,
    onMinVersionCodeChange: (String) -> Unit,
    onIsForceChange: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit,
    onChangelogChange: (String) -> Unit,
    onDownloadUrlChange: (String) -> Unit,
    onButtonTextChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSaveUpdate: () -> Unit,
    onToggleStatus: (AppUpdate) -> Unit,
    onDeleteUpdate: (AppUpdate) -> Unit,
    onRefresh: () -> Unit,
    onShowToast: (String) -> Unit
) {
    Scaffold(
        containerColor = Slate900,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenCreateDialog,
                containerColor = ElectricBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_update")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Update")
                    Text(text = "নতুন রিলিজ প্রকাশ", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate900),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Header Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(ElectricBlue.copy(alpha = 0.5f), CardBorderGradientEnd)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricBlue.copy(alpha = 0.12f), Color.Transparent)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = ElectricBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "OTA UPDATE DISPATCH",
                                        color = ElectricBlueLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "অ্যাপ আপডেট ম্যানেজমেন্ট",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Supabase app_updates টেবিল সিঙ্ক ও ইনস্ট্যান্ট ফোর্স আপডেট কন্ট্রোল",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate200.copy(alpha = 0.8f)
                                    )
                                )
                            }

                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate800)
                                    .testTag("btn_refresh_updates")
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
                                        contentDescription = "Refresh Updates",
                                        tint = NeonCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Updates List or Empty State
            if (isLoading && updates.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonCyan, strokeWidth = 2.dp)
                    }
                }
            } else if (updates.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate850),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Slate800),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = NeonCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "কোনো আপডেট রিলিজ পাওয়া যায়নি",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "নতুন ভার্সন প্রকাশ করতে নিচের বাটনে চাপ দিন।",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenCreateDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "প্রথম আপডেট প্রকাশ করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(updates, key = { it.id }) { update ->
                    AppUpdateCard(
                        update = update,
                        onEdit = { onOpenEditDialog(update) },
                        onToggle = { onToggleStatus(update) },
                        onDelete = { onDeleteUpdate(update) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Publish / Edit Update Dialog Modal
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = onCloseDialog,
            containerColor = Slate850,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (editingId == null) Icons.Default.NewReleases else Icons.Default.Edit,
                            contentDescription = null,
                            tint = ElectricBlueLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (editingId == null) "নতুন আপডেট প্রকাশ করুন" else "আপডেট রিলিজ সম্পাদনা",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Version Name
                    Column {
                        Text(
                            text = "ভার্সন নাম (Version Name) *",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = versionName,
                            onValueChange = onVersionNameChange,
                            placeholder = { Text("যেমন: v6.1.0", color = Slate400, fontSize = 13.sp) },
                            singleLine = true,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Version Code & Min Version Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ভার্সন কোড *",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = versionCode,
                                onValueChange = onVersionCodeChange,
                                placeholder = { Text("যেমন: 12", color = Slate400, fontSize = 13.sp) },
                                singleLine = true,
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "মিনিমাম কোড *",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = minVersionCode,
                                onValueChange = onMinVersionCodeChange,
                                placeholder = { Text("যেমন: 10", color = Slate400, fontSize = 13.sp) },
                                singleLine = true,
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Title
                    Column {
                        Text(
                            text = "আপডেট টাইটেল *",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = onTitleChange,
                            placeholder = { Text("যেমন: নতুন দ্রুততম প্লেয়ার ও সিকিউরিটি প্যাচ", color = Slate400, fontSize = 13.sp) },
                            singleLine = true,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Changelog
                    Column {
                        Text(
                            text = "পরিবর্তন ও বিবরণ (Changelog) *",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = changelog,
                            onValueChange = onChangelogChange,
                            placeholder = { Text("• ফাস্ট লোডিং\n• বাগ ফিক্স\n• অটো লগইন সাপোর্ট", color = Slate400, fontSize = 13.sp) },
                            maxLines = 4,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Download URL
                    Column {
                        Text(
                            text = "ডাউনলোড / ড্রাইভ / ক্লাউডনারি লিংক *",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = downloadUrl,
                            onValueChange = onDownloadUrlChange,
                            placeholder = { Text("https://...", color = Slate400, fontSize = 13.sp) },
                            singleLine = true,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Button Text
                    Column {
                        Text(
                            text = "বাটন টেক্সট",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = buttonText,
                            onValueChange = onButtonTextChange,
                            placeholder = { Text("এখনই আপডেট করুন", color = Slate400, fontSize = 13.sp) },
                            singleLine = true,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Toggles: Force Update & Active Status
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("বাধ্যতামূলক আপডেট (Force Update)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("ইউজার আপডেট না করে অ্যাপ চালাতে পারবে না", color = Slate400, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = isForce,
                                    onCheckedChange = onIsForceChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = CoralDanger,
                                        uncheckedThumbColor = Slate400,
                                        uncheckedTrackColor = Slate800
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("সক্রিয় রাখুন (Is Active)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("এই রিলিজটি সক্রিয় হিসেবে গণ্য হবে", color = Slate400, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = isActive,
                                    onCheckedChange = onIsActiveChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmeraldSuccess,
                                        uncheckedThumbColor = Slate400,
                                        uncheckedTrackColor = Slate800
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onSaveUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (editingId == null) "প্রকাশ করুন" else "আপডেট সেভ করুন",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = onCloseDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("বাতিল", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun AppUpdateCard(
    update: AppUpdate,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (update.isActive) {
                    Brush.linearGradient(listOf(CardBorderGradientStart, CardBorderGradientEnd))
                } else {
                    Brush.linearGradient(listOf(Slate700, Color.Transparent))
                },
                RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Badges & Action Buttons
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
                        color = if (update.isActive) ElectricBlue.copy(alpha = 0.15f) else Slate800,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (update.isActive) ElectricBlue else Slate700
                        )
                    ) {
                        Text(
                            text = update.latestVersionName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (update.isActive) NeonCyan else Slate400,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (update.isForceUpdate) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoralDanger.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CoralDanger)
                        ) {
                            Text(
                                text = "ফোর্স আপডেট",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CoralDanger
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (update.isActive) EmeraldSuccess.copy(alpha = 0.15f) else Slate800
                    ) {
                        Text(
                            text = if (update.isActive) "সক্রিয়" else "নিষ্ক্রিয়",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (update.isActive) EmeraldSuccess else Slate400
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = NeonCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                    ) {
                        Icon(
                            imageVector = if (update.isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Toggle Active",
                            tint = if (update.isActive) EmeraldSuccess else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CoralDanger, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Text(
                text = update.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            if (update.changelog.isNotBlank()) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = update.changelog,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate200,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "বিল্ড কোড: ${update.latestVersionCode} • মিনিমাম: ${update.minSupportedVersionCode}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontFamily = FontFamily.Monospace)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = update.buttonText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlueLight
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Slate700,
    focusedContainerColor = Slate900,
    unfocusedContainerColor = Slate900,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Slate400,
    cursorColor = NeonCyan,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
