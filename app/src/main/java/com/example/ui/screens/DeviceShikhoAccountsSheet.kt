package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.ShikhoApiClient
import com.example.data.model.DeviceShikhoAccount
import com.example.data.model.SetAccountCoursePermissionsRpcPayload
import com.example.data.model.ShikhoAcademicProgram
import com.example.data.model.ShikhoBatchExamYear
import com.example.data.model.ShikhoClassItem
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

private val STUDY_GROUPS = listOf(
    "Science" to "বিজ্ঞান (Science)",
    "Humanities" to "মানবিক (Humanities)",
    "BusinessStudies" to "ব্যবসায় শিক্ষা (BusinessStudies)"
)

/**
 * BottomSheet displaying:
 * 1. All Shikho accounts logged in under a specific device (`public.device_shikho_accounts`)
 * 2. Step-by-step real Shikho API syllabus & course access selector for a selected Shikho account
 * 3. Saving via Supabase RPC `admin_set_account_course_permissions`
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeviceShikhoAccountsBottomSheet(
    device: UserDevice,
    accounts: List<DeviceShikhoAccount>,
    isLoadingAccounts: Boolean,
    accountsError: String?,
    isShikhoConnected: Boolean,
    shikhoConnectedPhone: String,
    shikhoClassList: List<ShikhoClassItem>,
    isLoadingClassList: Boolean,
    classListError: String?,
    shikhoBatchYears: List<ShikhoBatchExamYear>,
    isLoadingBatches: Boolean,
    batchesError: String?,
    shikhoPrograms: List<ShikhoAcademicProgram>,
    isLoadingPrograms: Boolean,
    programsError: String?,
    isSavingPermissions: Boolean,
    onRefreshAccounts: () -> Unit,
    onOpenShikhoLogin: () -> Unit,
    onLoadRealClasses: () -> Unit,
    onLoadRealBatches: (classCode: String) -> Unit,
    onLoadRealPrograms: (classCode: String, groupCode: String, batchYear: String) -> Unit,
    onSaveAccountPermissions: (
        payload: SetAccountCoursePermissionsRpcPayload,
        onSuccess: () -> Unit
    ) -> Unit,
    onOpenLegacyDeviceCourseSheet: () -> Unit,
    onShowToast: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

    // Currently selected Shikho Account for Step-by-Step Real Course Configuration
    var selectedAccount by remember { mutableStateOf<DeviceShikhoAccount?>(null) }

    // Optional manual account entry if admin wants to pre-authorize a Shikho User ID / Phone
    var showManualAccountEntry by remember { mutableStateOf(false) }
    var manualShikhoUserId by remember { mutableStateOf("") }
    var manualUserPhone by remember { mutableStateOf("") }
    var manualStudentName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate900,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Slate700)
            )
        }
    ) {
        val currentTargetAccount = selectedAccount
        if (currentTargetAccount == null) {
            // =========================================================================
            // VIEW 1: List of all Shikho Accounts logged in under this Device
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                // Header
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ডিভাইসের শিখো অ্যাকাউন্ট ও কোর্স এক্সেস",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "📱 ${device.deviceBrand?.let { "$it " } ?: ""}${device.deviceModel} • ${device.displayDeviceId.ifBlank { device.deviceHash.take(12) }}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onRefreshAccounts,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Slate800)
                                .testTag("btn_refresh_device_shikho_accounts")
                        ) {
                            if (isLoadingAccounts) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Accounts",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Slate800)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    // Admin Shikho API Status Banner
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isShikhoConnected) EmeraldSuccess.copy(alpha = 0.1f) else AmberWarning.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp,
                                if (isShikhoConnected) EmeraldSuccess.copy(alpha = 0.6f) else AmberWarning.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenShikhoLogin() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isShikhoConnected) {
                                            "✅ অ্যাডমিন শিখো টোকেন সংযুক্ত ($shikhoConnectedPhone)"
                                        } else {
                                            "⚠️ অ্যাডমিন শিখো অ্যাকাউন্ট লগইন করা নেই"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isShikhoConnected) EmeraldSuccess else AmberWarning,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = if (isShikhoConnected) {
                                            "api.shikho.com থেকে রিয়েল শ্রেণি, ব্যাচ এবং কোর্স লোড করার জন্য প্রস্তুত"
                                        } else {
                                            "রিয়েল শ্রেণি, ব্যাচ ও কোর্সের তালিকা লোড করতে আপনার শিখো অ্যাকাউন্ট লগইন করুন"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate200,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = onOpenShikhoLogin,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isShikhoConnected) Slate800 else ElectricBlue
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isShikhoConnected) "টোকেন" else "লগইন করুন",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Section Title & Count
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "লগইনকৃত শিখো অ্যাকাউন্টসমূহ (${accounts.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "TABLE: public.device_shikho_accounts",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate400,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { showManualAccountEntry = !showManualAccountEntry },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, ElectricBlue),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ম্যানুয়াল অ্যাকাউন্ট",
                                        fontSize = 11.sp,
                                        color = NeonCyan
                                    )
                                }

                                OutlinedButton(
                                    onClick = onOpenLegacyDeviceCourseSheet,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Slate700),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = Slate200,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ডিভাইস রুলস",
                                        fontSize = 11.sp,
                                        color = Slate200
                                    )
                                }
                            }
                        }
                    }

                    // Optional manual account form
                    if (showManualAccountEntry) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate850),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ElectricBlue, RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "নতুন শিখো অ্যাকাউন্টে সরাসরি কোর্স এক্সেস সেট করুন",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    OutlinedTextField(
                                        value = manualShikhoUserId,
                                        onValueChange = { manualShikhoUserId = it },
                                        label = { Text("Shikho User ID *", fontSize = 11.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Slate900,
                                            unfocusedContainerColor = Slate900,
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = Slate700,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = manualUserPhone,
                                            onValueChange = { manualUserPhone = it },
                                            label = { Text("ফোন নাম্বার (8801...)", fontSize = 11.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Slate900,
                                                unfocusedContainerColor = Slate900,
                                                focusedBorderColor = NeonCyan,
                                                unfocusedBorderColor = Slate700,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = manualStudentName,
                                            onValueChange = { manualStudentName = it },
                                            label = { Text("শিক্ষার্থীর নাম", fontSize = 11.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Slate900,
                                                unfocusedContainerColor = Slate900,
                                                focusedBorderColor = NeonCyan,
                                                unfocusedBorderColor = Slate700,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            val uid = manualShikhoUserId.trim()
                                            val phone = ShikhoApiClient.formatPhoneNumber(manualUserPhone)
                                            if (uid.isBlank() && phone.isBlank()) {
                                                onShowToast("Shikho User ID অথবা ফোন নাম্বার লিখুন")
                                                return@Button
                                            }
                                            selectedAccount = DeviceShikhoAccount(
                                                deviceHash = device.deviceHash,
                                                displayDeviceId = device.displayDeviceId,
                                                shikhoUserId = uid,
                                                userPhone = phone,
                                                studentName = manualStudentName.trim().ifBlank { "শিক্ষার্থী" },
                                                isAccessGranted = true,
                                                isRestricted = true
                                            )
                                            showManualAccountEntry = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("এই অ্যাকাউন্টে কোর্স এক্সেস সেট করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Error Message if query failed
                    if (!accountsError.isNullOrBlank()) {
                        item {
                            Surface(
                                color = CoralDanger.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CoralDanger),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "⚠️ ডাটাবেজ থেকে অ্যাকাউন্ট তালিকা লোড করতে সমস্যা হয়েছে:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = CoralDanger,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = accountsError,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                    )
                                }
                            }
                        }
                    }

                    // Loading Indicator
                    if (isLoadingAccounts) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(color = NeonCyan)
                                    Text(
                                        text = "Supabase `public.device_shikho_accounts` থেকে লগইনকৃত অ্যাকাউন্ট লোড হচ্ছে...",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                                    )
                                }
                            }
                        }
                    } else if (accounts.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate800),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Text(
                                        text = "এই ডিভাইসে এখনো কোনো শিখো অ্যাকাউন্ট পাওয়া যায়নি",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "স্টুডেন্ট অ্যাপে শিখো অ্যাকাউন্ট লগইন করলে `public.device_shikho_accounts` টেবিলে স্বয়ংক্রিয়ভাবে যুক্ত হবে। অথবা উপরের 'ম্যানুয়াল অ্যাকাউন্ট' বাটন দিয়ে সরাসরি সেট করতে পারেন।",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = accounts,
                            key = { it.id.ifBlank { "${it.shikhoUserId}_${it.userPhone}" } }
                        ) { account ->
                            DeviceShikhoAccountCard(
                                account = account,
                                onCopyUserId = {
                                    clipboardManager.setText(AnnotatedString(account.shikhoUserId))
                                    onShowToast("Shikho User ID কপি করা হয়েছে: ${account.shikhoUserId}")
                                },
                                onConfigureAccessClick = {
                                    selectedAccount = account
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // =========================================================================
            // VIEW 2: Real Class -> Group -> Batch -> Course Configurator for Selected Account
            // =========================================================================
            AccountRealCoursePermissionConfigurator(
                device = device,
                account = currentTargetAccount,
                isShikhoConnected = isShikhoConnected,
                shikhoConnectedPhone = shikhoConnectedPhone,
                shikhoClassList = shikhoClassList,
                isLoadingClassList = isLoadingClassList,
                classListError = classListError,
                shikhoBatchYears = shikhoBatchYears,
                isLoadingBatches = isLoadingBatches,
                batchesError = batchesError,
                shikhoPrograms = shikhoPrograms,
                isLoadingPrograms = isLoadingPrograms,
                programsError = programsError,
                isSavingPermissions = isSavingPermissions,
                onBackToAccountsList = { selectedAccount = null },
                onOpenShikhoLogin = onOpenShikhoLogin,
                onLoadRealClasses = onLoadRealClasses,
                onLoadRealBatches = onLoadRealBatches,
                onLoadRealPrograms = onLoadRealPrograms,
                onSaveAccountPermissions = { payload ->
                    onSaveAccountPermissions(payload) {
                        selectedAccount = null
                    }
                }
            )
        }
    }
}

/**
 * Part 2: Card displaying a single logged-in Shikho account under a device (`public.device_shikho_accounts`).
 */
@Composable
private fun DeviceShikhoAccountCard(
    account: DeviceShikhoAccount,
    onCopyUserId: () -> Unit,
    onConfigureAccessClick: () -> Unit
) {
    val isGranted = account.isAccessGranted
    val borderColor = if (isGranted) EmeraldSuccess.copy(alpha = 0.7f) else Slate700

    val syllabusParts = listOfNotNull(
        account.currentClass?.takeIf { it.isNotBlank() },
        account.currentGroup?.takeIf { it.isNotBlank() },
        account.currentBatch?.takeIf { it.isNotBlank() }
    )
    val currentSyllabusText = if (syllabusParts.isNotEmpty()) {
        syllabusParts.joinToString(" • ")
    } else {
        "নির্ধারিত নেই"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag("card_shikho_account_${account.shikhoUserId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Avatar, Student Name, Phone, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (!account.userAvatar.isNullOrBlank()) {
                        AsyncImage(
                            model = account.userAvatar,
                            contentDescription = account.studentName ?: "Student Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.dp, if (isGranted) EmeraldSuccess else Slate700, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isGranted) EmeraldSuccess.copy(alpha = 0.2f)
                                    else ElectricBlue.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isGranted) EmeraldSuccess else NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = account.studentName?.takeIf { it.isNotBlank() } ?: "নামবিহীন শিক্ষার্থী",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "📞 ${account.userPhone?.takeIf { it.isNotBlank() } ?: "ফোন নাম্বার নেই"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isGranted) EmeraldSuccess.copy(alpha = 0.16f) else CoralDanger.copy(alpha = 0.14f),
                    border = BorderStroke(
                        1.dp,
                        if (isGranted) EmeraldSuccess else CoralDanger.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = if (isGranted) "✅ এক্সেস দেওয়া আছে" else "🔒 এক্সেস নেই (অন্য অ্যাকাউন্ট)",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isGranted) EmeraldSuccess else CoralDanger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Shikho User ID & Current Syllabus Info Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Slate950,
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🆔 Shikho User ID: ${account.shikhoUserId.ifBlank { "N/A" }}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate200,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (account.shikhoUserId.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Shikho User ID",
                                tint = NeonCyan,
                                modifier = Modifier
                                    .size(15.dp)
                                    .clickable { onCopyUserId() }
                            )
                        }
                    }

                    Text(
                        text = "📚 বর্তমান সিলেবাস: $currentSyllabusText",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ElectricBlueLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // If access is granted, show summary of permitted classes/groups/batches & courses count
            if (isGranted) {
                val allowedClasses = account.allowedClasses.orEmpty()
                val allowedGroups = account.allowedGroups.orEmpty()
                val allowedBatches = account.allowedBatches.orEmpty()
                val allowedPrograms = account.allowedPrograms.orEmpty()

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldSuccess.copy(alpha = 0.1f),
                    border = BorderStroke(0.8.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "অনুমোদিত পারমিশন সামারি:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "• শ্রেণি: ${if (allowedClasses.isEmpty()) "সব" else allowedClasses.joinToString(", ")}" +
                                    "  • বিভাগ: ${if (allowedGroups.isEmpty()) "সব" else allowedGroups.joinToString(", ")}" +
                                    "  • ব্যাচ: ${if (allowedBatches.isEmpty()) "সব" else allowedBatches.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "• অনুমোদিত কোর্স সংখ্যা: ${allowedPrograms.size} টি কোর্স",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Action Button: "এই অ্যাকাউন্টে কোর্স এক্সেস সেট করুন"
            Button(
                onClick = onConfigureAccessClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) ElectricBlue else Slate700
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_set_account_access_${account.shikhoUserId}")
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "এই অ্যাকাউন্টে কোর্স এক্সেস সেট করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Part 3 & Part 4: Step-by-Step Real Shikho API Syllabus & Course Selector for a Selected Shikho Account
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountRealCoursePermissionConfigurator(
    device: UserDevice,
    account: DeviceShikhoAccount,
    isShikhoConnected: Boolean,
    shikhoConnectedPhone: String,
    shikhoClassList: List<ShikhoClassItem>,
    isLoadingClassList: Boolean,
    classListError: String?,
    shikhoBatchYears: List<ShikhoBatchExamYear>,
    isLoadingBatches: Boolean,
    batchesError: String?,
    shikhoPrograms: List<ShikhoAcademicProgram>,
    isLoadingPrograms: Boolean,
    programsError: String?,
    isSavingPermissions: Boolean,
    onBackToAccountsList: () -> Unit,
    onOpenShikhoLogin: () -> Unit,
    onLoadRealClasses: () -> Unit,
    onLoadRealBatches: (classCode: String) -> Unit,
    onLoadRealPrograms: (classCode: String, groupCode: String, batchYear: String) -> Unit,
    onSaveAccountPermissions: (SetAccountCoursePermissionsRpcPayload) -> Unit
) {
    // Initialize from existing account permissions or account's current syllabus
    val initialClass = remember(account) {
        account.allowedClasses?.firstOrNull()
            ?: account.currentClass?.takeIf { it.uppercase() in ShikhoApiClient.ALLOWED_CLASS_CODES }?.uppercase()
            ?: ""
    }
    val initialGroup = remember(account) {
        account.allowedGroups?.firstOrNull()
            ?: account.currentGroup?.takeIf { it.isNotBlank() && it != "None" }
            ?: "Science"
    }
    val initialBatch = remember(account) {
        account.allowedBatches?.firstOrNull()
            ?: account.currentBatch?.replace(Regex("^(SSC|HSC)\\s*", RegexOption.IGNORE_CASE), "")?.trim()
            ?: ""
    }

    var selectedClassCode by remember(account) { mutableStateOf(initialClass) }
    var selectedGroupCode by remember(account) { mutableStateOf(initialGroup) }
    var selectedBatchYear by remember(account) { mutableStateOf(initialBatch) }
    var selectedProgramIds by remember(account) {
        mutableStateOf(account.allowedPrograms.orEmpty().toSet())
    }

    var isAccessGranted by remember(account) { mutableStateOf(true) }
    var isRestricted by remember(account) { mutableStateOf(true) }
    var revokeOtherAccounts by remember(account) { mutableStateOf(true) }

    val requiresGroup = selectedClassCode in ShikhoApiClient.GROUP_REQUIRED_CLASSES

    // Step 1: Load real class list automatically when Shikho token is connected
    LaunchedEffect(isShikhoConnected) {
        if (isShikhoConnected && shikhoClassList.isEmpty()) {
            onLoadRealClasses()
        }
    }

    // Step 3: Load real batch options automatically when class changes
    LaunchedEffect(isShikhoConnected, selectedClassCode) {
        if (isShikhoConnected && selectedClassCode.isNotBlank()) {
            onLoadRealBatches(selectedClassCode)
        }
    }

    // Auto-select first returned batch year if none selected or current not in list
    LaunchedEffect(shikhoBatchYears) {
        if (shikhoBatchYears.isNotEmpty() && selectedBatchYear.isBlank()) {
            selectedBatchYear = shikhoBatchYears.first().year
        }
    }

    // Step 4: Load real academic programs automatically when class, group, and batch year are ready
    LaunchedEffect(isShikhoConnected, selectedClassCode, selectedGroupCode, selectedBatchYear) {
        if (isShikhoConnected && selectedClassCode.isNotBlank() && selectedBatchYear.isNotBlank()) {
            val effectiveGroup = if (selectedClassCode in ShikhoApiClient.GROUP_REQUIRED_CLASSES) {
                selectedGroupCode.ifBlank { "Science" }
            } else {
                "None"
            }
            onLoadRealPrograms(selectedClassCode, effectiveGroup, selectedBatchYear)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.94f)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header with Back Button to Account List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBackToAccountsList,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800)
                        .testTag("btn_back_to_accounts_list")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = account.studentName?.takeIf { it.isNotBlank() } ?: "শিখো অ্যাকাউন্ট পারমিশন",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "📞 ${account.userPhone ?: "N/A"} • ID: ${account.shikhoUserId}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Slate700)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // If Admin has not logged into Shikho API yet, prompt prominently
            if (!isShikhoConnected) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.14f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberWarning, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🔑 শিখো অফিসিয়াল সার্ভার লগইন প্রয়োজন",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning
                                )
                            )
                            Text(
                                text = "https://api.shikho.com থেকে ১০০% রিয়েল শ্রেণি, বিভাগ, ব্যাচ এবং কোর্সের তালিকা লোড করতে প্রথমে আপনার শিখো অ্যাকাউন্ট লগইন করুন।",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                            Button(
                                onClick = onOpenShikhoLogin,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("শিখো অ্যাকাউন্ট কানেক্ট করুন (Shikho API Login)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Revoke Other Accounts Toggle (Part 4 requirement)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (revokeOtherAccounts) EmeraldSuccess.copy(alpha = 0.7f) else Slate700,
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "অন্যান্য সকল অ্যাকাউন্টে কোর্স এক্সেস বন্ধ রাখুন",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = "p_revoke_other_accounts = $revokeOtherAccounts • স্টুডেন্ট অন্য কোনো শিখো অ্যাকাউন্ট লগইন করলে একটি কোর্সও দেখতে পাবে না",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (revokeOtherAccounts) EmeraldSuccess else Slate400,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Switch(
                                checked = revokeOtherAccounts,
                                onCheckedChange = { revokeOtherAccounts = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldSuccess
                                ),
                                modifier = Modifier.testTag("switch_revoke_other_accounts")
                            )
                        }

                        HorizontalDivider(color = Slate700.copy(alpha = 0.6f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "এই অ্যাকাউন্টে কোর্স এক্সেস সক্রিয় (p_is_access_granted)",
                                style = MaterialTheme.typography.labelMedium.copy(color = Slate200)
                            )
                            Switch(
                                checked = isAccessGranted,
                                onCheckedChange = { isAccessGranted = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ElectricBlue
                                ),
                                modifier = Modifier.testTag("switch_is_access_granted")
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // STEP 1: Real Class List (`GET https://api.shikho.com/class_list?vendor=BD&type=syllabus`)
            // =========================================================================
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ধাপ ১: শ্রেণি নির্বাচন (Real Class List API)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                )
                                Text(
                                    text = "GET /class_list?vendor=BD&type=syllabus",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate400,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = onLoadRealClasses,
                                enabled = isShikhoConnected && !isLoadingClassList,
                                modifier = Modifier.size(32.dp)
                            ) {
                                if (isLoadingClassList) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = NeonCyan,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reload Classes",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (!classListError.isNullOrBlank()) {
                            Text(
                                text = "⚠️ $classListError",
                                style = MaterialTheme.typography.labelSmall.copy(color = CoralDanger)
                            )
                        }

                        if (isLoadingClassList && shikhoClassList.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                                Text("শিখো সার্ভার থেকে রিয়েল শ্রেণির তালিকা লোড হচ্ছে...", fontSize = 12.sp, color = Slate400)
                            }
                        } else if (shikhoClassList.isEmpty()) {
                            Text(
                                text = if (isShikhoConnected) "শ্রেণি লোড করতে ডানপাশের রিফ্রেশ বাটনে চাপুন।" else "শ্রেণির তালিকা লোড করতে আগে শিখো অ্যাকাউন্ট লগইন করুন।",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                shikhoClassList.forEach { cls ->
                                    val isSelected = selectedClassCode.equals(cls.code, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedClassCode = cls.code
                                            if (cls.code !in ShikhoApiClient.GROUP_REQUIRED_CLASSES) {
                                                selectedGroupCode = "None"
                                            } else if (selectedGroupCode == "None" || selectedGroupCode.isBlank()) {
                                                selectedGroupCode = "Science"
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = "${cls.code} (${cls.titleBn})",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricBlue,
                                            selectedLabelColor = Color.White,
                                            containerColor = Slate900,
                                            labelColor = Slate200
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = Slate700,
                                            selectedBorderColor = NeonCyan
                                        ),
                                        modifier = Modifier.testTag("chip_real_class_${cls.code}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // STEP 2: Study Group Selection (`C9`, `C10`, `C11`, `C12` -> Science, Humanities, BusinessStudies)
            // =========================================================================
            if (selectedClassCode.isNotBlank()) {
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "ধাপ ২: বিভাগ নির্বাচন (Study Group)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            )

                            if (requiresGroup) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    STUDY_GROUPS.forEach { (groupCode, groupLabel) ->
                                        val isSelected = selectedGroupCode.equals(groupCode, ignoreCase = true)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedGroupCode = groupCode },
                                            label = {
                                                Text(
                                                    text = groupLabel,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldSuccess,
                                                selectedLabelColor = Color.White,
                                                containerColor = Slate900,
                                                labelColor = Slate200
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = Slate700,
                                                selectedBorderColor = EmeraldSuccess
                                            ),
                                            modifier = Modifier.testTag("chip_real_group_$groupCode")
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    color = Slate900,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Slate700),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ℹ️ $selectedClassCode শ্রেণির জন্য কোনো আলাদা বিভাগ নেই (API-তে স্বয়ংক্রিয়ভাবে group: \"None\" পাঠানো হবে)",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate200),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // STEP 3: Real Batch / Exam Year (`BatchOptions` GraphQL API)
            // =========================================================================
            if (selectedClassCode.isNotBlank()) {
                item {
                    val computedBatchId = ShikhoApiClient.buildBatchId(selectedClassCode, selectedBatchYear)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ধাপ ৩: ব্যাচ / পরীক্ষার বছর নির্বাচন (Real BatchOptions API)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    )
                                    Text(
                                        text = "POST /graphql (operationName: BatchOptions, class: $selectedClassCode)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate400,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = { onLoadRealBatches(selectedClassCode) },
                                    enabled = isShikhoConnected && !isLoadingBatches,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    if (isLoadingBatches) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = NeonCyan,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reload Batches",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (!batchesError.isNullOrBlank()) {
                                Text(
                                    text = "⚠️ $batchesError",
                                    style = MaterialTheme.typography.labelSmall.copy(color = CoralDanger)
                                )
                            }

                            if (isLoadingBatches && shikhoBatchYears.isEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                                    Text("BatchOptions GraphQL থেকে রিয়েল ব্যাচ লোড হচ্ছে...", fontSize = 12.sp, color = Slate400)
                                }
                            } else if (shikhoBatchYears.isEmpty()) {
                                Text(
                                    text = "এই শ্রেণির জন্য কোনো ব্যাচ পাওয়া যায়নি। রিফ্রেশ বাটনে চাপুন।",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    shikhoBatchYears.forEach { batchItem ->
                                        val isSelected = selectedBatchYear == batchItem.year
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedBatchYear = batchItem.year },
                                            label = {
                                                Text(
                                                    text = if (batchItem.label != batchItem.year) {
                                                        "${batchItem.label} (${batchItem.year})"
                                                    } else {
                                                        batchItem.year
                                                    },
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = ElectricBlue,
                                                selectedLabelColor = Color.White,
                                                containerColor = Slate900,
                                                labelColor = Slate200
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = Slate700,
                                                selectedBorderColor = NeonCyan
                                            ),
                                            modifier = Modifier.testTag("chip_real_batch_${batchItem.year}")
                                        )
                                    }
                                }
                            }

                            if (computedBatchId.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate950,
                                    border = BorderStroke(0.8.dp, Slate700)
                                ) {
                                    Text(
                                        text = "⚡ GraphQL Batch ID: \"$computedBatchId\"",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // STEP 4: Real Course Selection (`GetAcademicProgram` GraphQL API)
            // =========================================================================
            if (selectedClassCode.isNotBlank() && selectedBatchYear.isNotBlank()) {
                item {
                    val effectiveGroup = if (requiresGroup) selectedGroupCode.ifBlank { "Science" } else "None"
                    val computedBatchId = ShikhoApiClient.buildBatchId(selectedClassCode, selectedBatchYear)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ধাপ ৪: রিয়েল কোর্স নির্বাচন (${selectedProgramIds.size} টি সিলেক্টেড)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    )
                                    Text(
                                        text = "GetAcademicProgram • batch_id: \"$computedBatchId\" • class: \"$selectedClassCode\" • group: \"$effectiveGroup\"",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate400,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onLoadRealPrograms(selectedClassCode, effectiveGroup, selectedBatchYear)
                                    },
                                    enabled = isShikhoConnected && !isLoadingPrograms,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    if (isLoadingPrograms) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = NeonCyan,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reload Courses",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Select All / Clear All quick buttons
                            if (shikhoPrograms.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "মোট ${shikhoPrograms.size} টি রিয়েল কোর্স পাওয়া গেছে",
                                        style = MaterialTheme.typography.labelSmall.copy(color = EmeraldSuccess)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(
                                            onClick = {
                                                selectedProgramIds = selectedProgramIds + shikhoPrograms.map { it.id }
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("সব সিলেক্ট", fontSize = 11.sp, color = NeonCyan)
                                        }
                                        TextButton(
                                            onClick = { selectedProgramIds = emptySet() },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("সব মুছুন", fontSize = 11.sp, color = CoralDanger)
                                        }
                                    }
                                }
                            }

                            if (!programsError.isNullOrBlank()) {
                                Text(
                                    text = "⚠️ $programsError",
                                    style = MaterialTheme.typography.labelSmall.copy(color = CoralDanger)
                                )
                            }

                            if (isLoadingPrograms) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(28.dp))
                                        Text(
                                            text = "শিখো সার্ভার থেকে রিয়েল কোর্সের তালিকা লোড হচ্ছে...",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                                        )
                                    }
                                }
                            } else if (shikhoPrograms.isEmpty()) {
                                Surface(
                                    color = Slate900,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Slate700),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "এই সিলেবাসের ($selectedClassCode • $effectiveGroup • $computedBatchId) জন্য কোনো কোর্স পাওয়া যায়নি।",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    shikhoPrograms.forEach { program ->
                                        val isChecked = program.id in selectedProgramIds
                                        RealShikhoProgramItemCard(
                                            program = program,
                                            isSelected = isChecked,
                                            onToggle = {
                                                selectedProgramIds = if (isChecked) {
                                                    selectedProgramIds - program.id
                                                } else {
                                                    selectedProgramIds + program.id
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sticky Save Button (`admin_set_account_course_permissions`)
        Surface(
            color = Slate950,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackToAccountsList,
                    enabled = !isSavingPermissions,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("ফিরে যান", color = Slate200)
                }

                Button(
                    onClick = {
                        val allowedClassesList = if (selectedClassCode.isNotBlank()) {
                            listOf(selectedClassCode)
                        } else {
                            emptyList()
                        }
                        val allowedGroupsList = if (selectedClassCode in ShikhoApiClient.GROUP_REQUIRED_CLASSES && selectedGroupCode.isNotBlank() && selectedGroupCode != "None") {
                            listOf(selectedGroupCode)
                        } else {
                            emptyList()
                        }
                        val allowedBatchesList = if (selectedBatchYear.isNotBlank()) {
                            listOf(selectedBatchYear)
                        } else {
                            emptyList()
                        }

                        val rpcPayload = SetAccountCoursePermissionsRpcPayload(
                            deviceHash = device.deviceHash.ifBlank { account.deviceHash },
                            shikhoUserId = account.shikhoUserId,
                            userPhone = account.userPhone.orEmpty(),
                            isAccessGranted = isAccessGranted,
                            isRestricted = isRestricted,
                            allowedClasses = allowedClassesList,
                            allowedGroups = allowedGroupsList,
                            allowedBatches = allowedBatchesList,
                            allowedPrograms = selectedProgramIds.toList(),
                            revokeOtherAccounts = revokeOtherAccounts
                        )
                        onSaveAccountPermissions(rpcPayload)
                    },
                    enabled = !isSavingPermissions,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_save_account_course_permissions")
                ) {
                    if (isSavingPermissions) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সেভ হচ্ছে...", fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "পারমিশন সেভ করুন (${selectedProgramIds.size} কোর্স)",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays a single real course from `GetAcademicProgram` (`enrolled_programs` + `other_programs`)
 * with banner image (`banner_url`), Bengali title (`title_bn`), ID (`id`), and checkbox.
 */
@Composable
private fun RealShikhoProgramItemCard(
    program: ShikhoAcademicProgram,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ElectricBlue.copy(alpha = 0.16f) else Slate900,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) NeonCyan else Slate700
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("card_shikho_program_${program.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonCyan,
                    uncheckedColor = Slate400,
                    checkmarkColor = Slate950
                )
            )

            if (!program.bannerUrl.isNullOrBlank()) {
                AsyncImage(
                    model = program.bannerUrl,
                    contentDescription = program.titleBn,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 64.dp, height = 42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = program.titleBn,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ID: ${program.id}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )

                    if (program.isFree) {
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "FREE",
                                color = EmeraldSuccess,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (program.isEnrolledProgram) {
                        Surface(
                            color = ElectricBlue.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ENROLLED",
                                color = ElectricBlueLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
