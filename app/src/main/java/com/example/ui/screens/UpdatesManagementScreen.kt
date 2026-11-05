package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUpdate
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderGradientEnd
import com.example.ui.theme.CardBorderGradientStart
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

@Composable
fun UpdatesManagementScreen(
    updates: List<AppUpdate>,
    isLoading: Boolean,
    isDialogOpen: Boolean,
    editingId: String?,
    versionName: String,
    versionCode: String,
    minVersionCode: String,
    fileSizeMb: String = "28.5",
    isForce: Boolean,
    title: String,
    changelog: String,
    downloadUrl: String,
    buttonText: String,
    isActive: Boolean,
    // Global Kill Switch / Maintenance Mode State
    isKillSwitchEnabled: Boolean = false,
    killSwitchTitle: String = "অ্যাপ আপডেটের কাজ চলছে",
    killSwitchMessage: String = "অ্যাপটিকে আরও উন্নত, দ্রুত ও নিরাপদ করতে বর্তমানে সার্ভার ও সিস্টেম আপডেটের কাজ চলছে। কাজ শেষ হওয়া মাত্রই অ্যাপটি পুনরায় চালু হবে।",
    estimatedBackTime: String = "",
    isSavingKillSwitch: Boolean = false,
    onToggleKillSwitchInstant: (Boolean) -> Unit = {},
    onKillSwitchTitleChange: (String) -> Unit = {},
    onKillSwitchMessageChange: (String) -> Unit = {},
    onEstimatedBackTimeChange: (String) -> Unit = {},
    onSaveKillSwitchSettings: () -> Unit = {},
    // APK Update Dialog Callbacks
    onOpenCreateDialog: () -> Unit,
    onOpenEditDialog: (AppUpdate) -> Unit,
    onCloseDialog: () -> Unit,
    onVersionNameChange: (String) -> Unit,
    onVersionCodeChange: (String) -> Unit,
    onMinVersionCodeChange: (String) -> Unit,
    onFileSizeMbChange: (String) -> Unit = {},
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
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

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
                    Text(text = "নতুন APK আপডেট প্রকাশ", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                        text = "KILL SWITCH & IN-APP APK UPDATE",
                                        color = ElectricBlueLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "কিল সুইচ ও APK আপডেট ম্যানেজার",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "public.app_updates টেবিলের সাথে ১০০% রিয়েল-টাইম কানেক্টেড (কোনো ডামি ডাটা নেই)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate200.copy(alpha = 0.85f)
                                    )
                                )
                            }

                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier
                                    .size(44.dp)
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

            // =========================================================================
            // 1. GLOBAL APP KILL SWITCH / MAINTENANCE MODE CONTROL PANEL
            // =========================================================================
            item {
                val activeColor = if (isKillSwitchEnabled) CoralDanger else EmeraldSuccess
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.5.dp,
                            activeColor.copy(alpha = 0.7f),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        activeColor.copy(alpha = 0.14f),
                                        Slate850
                                    )
                                )
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Top Row: Big Clear ON/OFF Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(activeColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isKillSwitchEnabled) Icons.Default.PowerSettingsNew else Icons.Default.Build,
                                        contentDescription = null,
                                        tint = activeColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Global App Kill Switch",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = activeColor.copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, activeColor)
                                        ) {
                                            Text(
                                                text = if (isKillSwitchEnabled) "ON (লকড)" else "OFF (সচল)",
                                                color = activeColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Maintenance Mode • is_kill_switch ও is_maintenance একসাথে আপডেট হয়",
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isKillSwitchEnabled,
                                onCheckedChange = { checked -> onToggleKillSwitchInstant(checked) },
                                enabled = !isSavingKillSwitch,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CoralDanger,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = EmeraldSuccess.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier.testTag("switch_global_kill_switch")
                            )
                        }

                        // Live Status Banner
                        Surface(
                            color = Slate900,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                activeColor.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isKillSwitchEnabled) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = activeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isKillSwitchEnabled) {
                                        "🛑 বর্তমানে কিল সুইচ চালু (ON) আছে! সকল স্টুডেন্টের অ্যাপে মেইনটেন্যান্স স্ক্রিন দেখাচ্ছে।"
                                    } else {
                                        "✅ বর্তমানে কিল সুইচ বন্ধ (OFF) আছে। স্টুডেন্ট অ্যাপ স্বাভাবিকভাবে চলছে।"
                                    },
                                    color = if (isKillSwitchEnabled) CoralDanger else EmeraldSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 1. kill_switch_title
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "১. কিল সুইচ টাইটেল (kill_switch_title)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            OutlinedTextField(
                                value = killSwitchTitle,
                                onValueChange = onKillSwitchTitleChange,
                                placeholder = { Text("অ্যাপ আপডেটের কাজ চলছে", color = Slate400, fontSize = 13.sp) },
                                singleLine = true,
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_kill_switch_title")
                            )
                        }

                        // 2. kill_switch_message
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "২. কিল সুইচ মেসেজ (kill_switch_message)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            OutlinedTextField(
                                value = killSwitchMessage,
                                onValueChange = onKillSwitchMessageChange,
                                placeholder = {
                                    Text(
                                        "অ্যাপটিকে আরও উন্নত, দ্রুত ও নিরাপদ করতে বর্তমানে সার্ভার ও সিস্টেম আপডেটের কাজ চলছে। কাজ শেষ হওয়া মাত্রই অ্যাপটি পুনরায় চালু হবে।",
                                        color = Slate400,
                                        fontSize = 12.sp
                                    )
                                },
                                minLines = 2,
                                maxLines = 4,
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_kill_switch_message")
                            )
                        }

                        // 3. estimated_back_time
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "৩. পুনরায় চালুর সম্ভাব্য সময় (estimated_back_time - ঐচ্ছিক)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            OutlinedTextField(
                                value = estimatedBackTime,
                                onValueChange = onEstimatedBackTimeChange,
                                placeholder = { Text("যেমন: রাত ১০:০০ টায় চালু হবে (অথবা খালি রাখুন)", color = Slate400, fontSize = 13.sp) },
                                singleLine = true,
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_estimated_back_time")
                            )
                        }

                        // Save Kill Switch Config Button
                        Button(
                            onClick = onSaveKillSwitchSettings,
                            enabled = !isSavingKillSwitch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isKillSwitchEnabled) CoralDanger else ElectricBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_save_kill_switch")
                        ) {
                            if (isSavingKillSwitch) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isKillSwitchEnabled) {
                                    "কিল সুইচ মেসেজ ও ON স্ট্যাটাস সেভ করুন"
                                } else {
                                    "কিল সুইচ তথ্য ও OFF স্ট্যাটাস সেভ করুন"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 2. IN-APP DIRECT APK UPDATE MANAGER SECTION HEADER
            // =========================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "২. ইন-অ্যাপ ডিরেক্ট APK আপডেট রিলিজসমূহ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "ORDER BY version_code DESC, id DESC • সর্বশেষ রো স্টুডেন্ট অ্যাপে চেক হয়",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                    }

                    Button(
                        onClick = onOpenCreateDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন ভার্সন যোগ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Updates List or Empty State
            if (isLoading && updates.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
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
                            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Slate800),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    tint = NeonCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "ডাটাবেজে কোনো APK আপডেট রো নেই",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "public.app_updates টেবিলে নতুন APK ভার্সন যুক্ত করতে নিচের বাটনে ট্যাপ করুন।",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onOpenCreateDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "প্রথম APK আপডেট প্রকাশ করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(updates, key = { it.id.ifBlank { it.effectiveVersionCode.toString() } }) { update ->
                    val isLatestRow = updates.firstOrNull()?.id == update.id
                    AppUpdateCard(
                        update = update,
                        isLatestRow = isLatestRow,
                        onEdit = { onOpenEditDialog(update) },
                        onToggle = { onToggleStatus(update) },
                        onDelete = { onDeleteUpdate(update) }
                    )
                }
            }

            // Optional 1-Click SQL Helper Card (in case schema needs columns added)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🛠️ ডাটাবেজ কলাম ও RPC সিঙ্ক হেল্পার (ঐচ্ছিক)",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "আপনার Supabase ডাটাবেজে যদি এখনো app_updates টেবিলের নতুন কলাম (version_code, apk_url, file_size_mb, is_kill_switch) বা student_notifications টেবিল তৈরি করা না থাকে, তবে নিচের বাটনে ক্লিক করে SQL কপি করে রান করুন:",
                            color = Slate400,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(buildNewFeaturesMigrationSql()))
                                onShowToast("✅ SQL স্ক্রিপ্ট কপি হয়েছে! প্রয়োজনে Supabase SQL Editor-এ Run করুন।")
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kill Switch + APK Update + Push Notification SQL কপি",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // =========================================================================
    // Publish / Edit Direct APK Update Dialog Modal
    // =========================================================================
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
                        text = if (editingId == null) "নতুন ডিরেক্ট APK আপডেট প্রকাশ" else "APK আপডেট সম্পাদনা",
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
                    // 1. version_code & 2. version_name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "version_code (Int) *",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = versionCode,
                                onValueChange = onVersionCodeChange,
                                placeholder = { Text("যেমন: 110, 111", color = Slate400, fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_update_version_code")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "version_name (Text) *",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = versionName,
                                onValueChange = onVersionNameChange,
                                placeholder = { Text("যেমন: 1.1.0", color = Slate400, fontSize = 12.sp) },
                                singleLine = true,
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_update_version_name")
                            )
                        }
                    }

                    // 3. apk_url (Direct APK / Google Drive / GitHub Release)
                    Column {
                        Text(
                            text = "ডিরেক্ট APK লিংক (apk_url) *",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ডিরেক্ট ডাউনলোড, Google Drive শেয়ার লিংক, অথবা GitHub Release APK লিংক",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = downloadUrl,
                            onValueChange = onDownloadUrlChange,
                            placeholder = { Text("https://.../app-release.apk", color = Slate400, fontSize = 12.sp) },
                            singleLine = true,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_update_apk_url")
                        )
                    }

                    // 4. file_size_mb & min_supported_version_code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "APK সাইজ MB (file_size_mb)",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = fileSizeMb,
                                onValueChange = onFileSizeMbChange,
                                placeholder = { Text("যেমন: 28.5", color = Slate400, fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_update_file_size_mb")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "মিনিমাম কোড (ঐচ্ছিক)",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = minVersionCode,
                                onValueChange = onMinVersionCodeChange,
                                placeholder = { Text("যেমন: 100", color = Slate400, fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 5. release_notes (Multiline Text)
                    Column {
                        Text(
                            text = "রিলিজ নোটস / কী কী নতুন থাকছে (release_notes) *",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = changelog,
                            onValueChange = onChangelogChange,
                            placeholder = { Text("• ইন-অ্যাপ ডাউনলোড প্রোগ্রেস বার\n• ভিডিও প্লেয়ার ও বাগ ফিক্স", color = Slate400, fontSize = 12.sp) },
                            minLines = 3,
                            maxLines = 5,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_update_release_notes")
                        )
                    }

                    // Title & Button Text (Optional customization)
                    Column {
                        Text(
                            text = "আপডেট পপ-আপ টাইটেল",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = onTitleChange,
                            placeholder = { Text("নতুন আপডেট উপলব্ধ!", color = Slate400, fontSize = 12.sp) },
                            singleLine = true,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 6. Toggles: is_force_update & is_active
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ফোর্স আপডেট (is_force_update)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isForce) {
                                            "চালু: স্টুডেন্ট আপডেট না করে অ্যাপ চালাতে পারবে না"
                                        } else {
                                            "বন্ধ: স্টুডেন্ট 'পরে করব' চাপ দিয়ে অ্যাপে ঢুকতে পারবে"
                                        },
                                        color = if (isForce) CoralDanger else Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = isForce,
                                    onCheckedChange = onIsForceChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = CoralDanger,
                                        uncheckedThumbColor = Slate400,
                                        uncheckedTrackColor = Slate800
                                    ),
                                    modifier = Modifier.testTag("switch_update_is_force")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "সক্রিয় রাখুন (is_active)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "এই APK রিলিজটি স্টুডেন্ট অ্যাপে সক্রিয় থাকবে",
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_confirm_save_update")
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
    isLatestRow: Boolean,
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
                if (isLatestRow) {
                    Brush.linearGradient(listOf(NeonCyan, ElectricBlue))
                } else if (update.isActive) {
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
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
                            text = "v${update.effectiveVersionName.removePrefix("v")} (Code: ${update.effectiveVersionCode})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (update.isActive) NeonCyan else Slate400,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    if (isLatestRow) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = "LATEST",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldSuccess
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (update.isForceUpdate) CoralDanger.copy(alpha = 0.18f) else AmberWarning.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (update.isForceUpdate) CoralDanger else AmberWarning
                        )
                    ) {
                        Text(
                            text = if (update.isForceUpdate) "ফোর্স আপডেট" else "ঐচ্ছিক আপডেট",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (update.isForceUpdate) CoralDanger else AmberWarning
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
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
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
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
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CoralDanger,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = update.title.ifBlank { "অ্যাপ আপডেট v${update.effectiveVersionName}" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            val notes = update.effectiveReleaseNotes
            if (notes.isNotBlank()) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate200,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // APK URL & File Size Info
            val apkLink = update.effectiveApkUrl
            if (apkLink.isNotBlank()) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = apkLink,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            maxLines = 1,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeLabel = if (update.fileSizeMb > 0.0) "${update.fileSizeMb} MB" else "N/A"
                Text(
                    text = "সাইজ: $sizeLabel • Kill Switch: ${if (update.isKillSwitchActive) "ON" else "OFF"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (update.isKillSwitchActive) CoralDanger else Slate400,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricBlue.copy(alpha = 0.15f)
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
        }
    }
}

private fun buildNewFeaturesMigrationSql(): String = """
-- ============================================================================
-- MAXBIRD 3 NEW REAL FEATURES SQL MIGRATION (ZERO DUMMY DATA)
-- 1. Global Kill Switch / Maintenance Mode columns on public.app_updates
-- 2. In-App Direct APK Update columns on public.app_updates
-- 3. Real Server Push Notification table public.student_notifications
-- ============================================================================

-- Allow optional updates (is_force_update = false) when admin chooses
DROP TRIGGER IF EXISTS trg_enforce_mandatory_app_update ON public.app_updates;

ALTER TABLE public.app_updates
    ADD COLUMN IF NOT EXISTS version_code INTEGER DEFAULT 100,
    ADD COLUMN IF NOT EXISTS version_name TEXT DEFAULT '1.0.0',
    ADD COLUMN IF NOT EXISTS apk_url TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS file_size_mb DOUBLE PRECISION DEFAULT 28.5,
    ADD COLUMN IF NOT EXISTS release_notes TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS is_kill_switch BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS is_maintenance BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS kill_switch_title TEXT DEFAULT 'অ্যাপ আপডেটের কাজ চলছে',
    ADD COLUMN IF NOT EXISTS kill_switch_message TEXT DEFAULT 'অ্যাপটিকে আরও উন্নত, দ্রুত ও নিরাপদ করতে বর্তমানে সার্ভার ও সিস্টেম আপডেটের কাজ চলছে। কাজ শেষ হওয়া মাত্রই অ্যাপটি পুনরায় চালু হবে।',
    ADD COLUMN IF NOT EXISTS estimated_back_time TEXT DEFAULT '';

-- Sync existing rows
UPDATE public.app_updates
SET
    version_code = COALESCE(NULLIF(version_code, 0), latest_version_code, 100),
    version_name = COALESCE(NULLIF(version_name, ''), latest_version_name, '1.0.0'),
    apk_url = COALESCE(NULLIF(apk_url, ''), download_url, ''),
    release_notes = COALESCE(NULLIF(release_notes, ''), changelog, '');

-- RPC for Global Kill Switch
CREATE OR REPLACE FUNCTION public.admin_set_kill_switch(
    p_is_kill_switch BOOLEAN,
    p_is_maintenance BOOLEAN DEFAULT NULL,
    p_kill_switch_title TEXT DEFAULT 'অ্যাপ আপডেটের কাজ চলছে',
    p_kill_switch_message TEXT DEFAULT 'অ্যাপটিকে আরও উন্নত, দ্রুত ও নিরাপদ করতে বর্তমানে সার্ভার ও সিস্টেম আপডেটের কাজ চলছে। কাজ শেষ হওয়া মাত্রই অ্যাপটি পুনরায় চালু হবে।',
    p_estimated_back_time TEXT DEFAULT ''
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_target_id UUID;
    v_maint BOOLEAN := COALESCE(p_is_maintenance, p_is_kill_switch);
BEGIN
    SELECT id INTO v_target_id
    FROM public.app_updates
    ORDER BY version_code DESC NULLS LAST, latest_version_code DESC NULLS LAST, created_at DESC NULLS LAST
    LIMIT 1;

    IF v_target_id IS NOT NULL THEN
        UPDATE public.app_updates
        SET
            is_kill_switch = p_is_kill_switch,
            is_maintenance = v_maint,
            kill_switch_title = COALESCE(NULLIF(TRIM(p_kill_switch_title), ''), 'অ্যাপ আপডেটের কাজ চলছে'),
            kill_switch_message = COALESCE(NULLIF(TRIM(p_kill_switch_message), ''), 'অ্যাপটিকে আরও উন্নত, দ্রুত ও নিরাপদ করতে বর্তমানে সার্ভার ও সিস্টেম আপডেটের কাজ চলছে। কাজ শেষ হওয়া মাত্রই অ্যাপটি পুনরায় চালু হবে।'),
            estimated_back_time = COALESCE(p_estimated_back_time, ''),
            updated_at = NOW()
        WHERE id = v_target_id;
    END IF;
END;
$$;

REVOKE ALL ON FUNCTION public.admin_set_kill_switch(BOOLEAN, BOOLEAN, TEXT, TEXT, TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.admin_set_kill_switch(BOOLEAN, BOOLEAN, TEXT, TEXT, TEXT) TO service_role;

-- 3. Create public.student_notifications table
CREATE TABLE IF NOT EXISTS public.student_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    target_type TEXT NOT NULL DEFAULT 'ALL', -- ALL, PHONE, PROGRAM
    target_value TEXT,
    notification_type TEXT NOT NULL DEFAULT 'ADMIN_PUSH', -- ADMIN_PUSH, LIVE_CLASS, EXAM
    image_url TEXT,
    deeplink TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.student_notifications ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow public read active student_notifications" ON public.student_notifications;
CREATE POLICY "Allow public read active student_notifications"
ON public.student_notifications
FOR SELECT
TO anon, authenticated
USING (is_active = true);

REVOKE INSERT, UPDATE, DELETE, TRUNCATE ON public.student_notifications FROM anon, authenticated;
GRANT SELECT ON public.student_notifications TO anon, authenticated;
GRANT ALL ON public.student_notifications TO service_role;
""".trimIndent()

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
