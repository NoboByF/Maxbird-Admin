package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
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
 * 1. All Shikho accounts logged in under a specific device
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

    // BUG 3 FIX: Check whether ANY account on this device has custom access/restriction enabled
    val hasAnyRestrictedAccountOnDevice = remember(accounts) {
        accounts.any { it.isAccessGranted || it.isRestricted }
    }

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
                                            "✅ শিখো সার্ভার সংযুক্ত আছে ($shikhoConnectedPhone)"
                                        } else {
                                            "⚠️ শিখো অ্যাকাউন্ট লগইন করা নেই"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isShikhoConnected) EmeraldSuccess else AmberWarning,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = if (isShikhoConnected) {
                                            "রিয়েল শ্রেণি, ব্যাচ এবং কোর্স তালিকা লোড করার জন্য প্রস্তুত"
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
                                        text = if (isShikhoConnected) "অ্যাকাউন্ট" else "লগইন করুন",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Section Title & Count (Clean Bengali UI - Bug 5 Fix)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "লগইনকৃত শিখো অ্যাকাউন্টসমূহ (${accounts.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "এই ডিভাইসে লগইন করা শিক্ষার্থীদের শিখো অ্যাকাউন্টের তালিকা",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate400,
                                        fontSize = 11.sp
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
                                        text = "ম্যানুয়াল যোগ",
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
                                        label = { Text("শিখো ইউজার আইডি (Shikho User ID) *", fontSize = 11.sp) },
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
                                            label = { Text("ফোন নাম্বার (017...)", fontSize = 11.sp) },
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
                                                onShowToast("ইউজার আইডি অথবা ফোন নাম্বার লিখুন")
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
                                        text = "⚠️ অ্যাকাউন্টের তালিকা লোড করতে সমস্যা হয়েছে:",
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
                                        text = "ডিভাইসের শিখো অ্যাকাউন্টগুলো লোড হচ্ছে...",
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
                                        text = "স্টুডেন্ট অ্যাপে শিখো অ্যাকাউন্ট লগইন করলে এখানে স্বয়ংক্রিয়ভাবে যুক্ত হবে। অথবা উপরের 'ম্যানুয়াল যোগ' বাটন দিয়ে সরাসরি সেট করতে পারেন।",
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
                                hasAnyRestrictedAccountOnDevice = hasAnyRestrictedAccountOnDevice,
                                onCopyUserId = {
                                    clipboardManager.setText(AnnotatedString(account.shikhoUserId))
                                    onShowToast("ইউজার আইডি কপি করা হয়েছে: ${account.shikhoUserId}")
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
                onShowToast = onShowToast,
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
 * BUG 3 & BUG 5 FIX:
 * Card displaying a single logged-in Shikho account under a device:
 * - If `account.isAccessGranted == true`: Green badge `✅ কাস্টম এক্সেস চালু`
 * - Else if `!account.isAccessGranted && hasAnyRestrictedAccountOnDevice`: Red badge `🔒 ব্লকড (অন্য অ্যাকাউন্ট)`
 * - Else (`!hasAnyRestrictedAccountOnDevice`): Cyan/Blue badge `🌐 ডিফল্ট মোড (লক করা হয়নি)`
 */
@Composable
private fun DeviceShikhoAccountCard(
    account: DeviceShikhoAccount,
    hasAnyRestrictedAccountOnDevice: Boolean,
    onCopyUserId: () -> Unit,
    onConfigureAccessClick: () -> Unit
) {
    val isGranted = account.isAccessGranted
    val isBlockedOtherAccount = !isGranted && hasAnyRestrictedAccountOnDevice

    val badgeText = when {
        isGranted -> "✅ কাস্টম এক্সেস চালু"
        isBlockedOtherAccount -> "🔒 ব্লকড (অন্য অ্যাকাউন্ট)"
        else -> "🌐 ডিফল্ট মোড (লক করা হয়নি)"
    }
    val badgeColor = when {
        isGranted -> EmeraldSuccess
        isBlockedOtherAccount -> CoralDanger
        else -> NeonCyan
    }
    val borderColor = when {
        isGranted -> EmeraldSuccess.copy(alpha = 0.7f)
        isBlockedOtherAccount -> CoralDanger.copy(alpha = 0.5f)
        else -> Slate700
    }

    val syllabusParts = listOfNotNull(
        account.currentClass?.takeIf { it.isNotBlank() },
        account.currentGroup?.takeIf { it.isNotBlank() && it != "None" },
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
                                .border(1.dp, badgeColor, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(badgeColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = badgeColor,
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

                // Status Badge (Bug 3 Fix)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColor,
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
                            text = "🆔 ইউজার আইডি: ${account.shikhoUserId.ifBlank { "N/A" }}",
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
                            text = "অনুমোদিত শ্রেণি: ${if (allowedClasses.isEmpty()) "সব" else allowedClasses.joinToString(", ")}" +
                                    " • বিভাগ: ${if (allowedGroups.isEmpty()) "সব" else allowedGroups.joinToString(", ")}" +
                                    " • ব্যাচ: ${if (allowedBatches.isEmpty()) "সব" else allowedBatches.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = if (!account.isRestricted || allowedPrograms.isEmpty()) {
                                "কোর্স: এই শ্রেণির সকল কোর্স উন্মুক্ত"
                            } else {
                                "কোর্স: ${allowedPrograms.size} টি অনুমোদিত"
                            },
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
 * BUG 2, BUG 4 & BUG 5 FIX:
 * Step-by-Step Real Shikho API Syllabus & Course Selector for a Selected Shikho Account
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountRealCoursePermissionConfigurator(
    device: UserDevice,
    account: DeviceShikhoAccount,
    isShikhoConnected: Boolean,
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
    onShowToast: (String) -> Unit,
    onSaveAccountPermissions: (SetAccountCoursePermissionsRpcPayload) -> Unit
) {
    // BUG 4 FIX: Initialize from existing saved permissions OR fallback to student's current syllabus
    val initialClass = remember(account) {
        account.allowedClasses?.firstOrNull()?.trim()?.uppercase()
            ?: account.currentClass?.trim()?.uppercase()?.takeIf { it in ShikhoApiClient.ALLOWED_CLASS_CODES }
            ?: "C10"
    }
    val initialGroup = remember(account, initialClass) {
        val savedGroup = account.allowedGroups?.firstOrNull()?.takeIf { it.isNotBlank() }
        if (savedGroup != null) {
            ShikhoApiClient.normalizeGroupCode(savedGroup, initialClass)
        } else {
            ShikhoApiClient.normalizeGroupCode(account.currentGroup, initialClass)
        }
    }
    val initialBatch = remember(account) {
        val savedBatch = account.allowedBatches?.firstOrNull()?.let { ShikhoApiClient.extractBatchYear(it) }
        if (!savedBatch.isNullOrBlank()) {
            savedBatch
        } else {
            ShikhoApiClient.extractBatchYear(account.currentBatch).ifBlank { "2026" }
        }
    }

    var selectedClassCode by remember(account) { mutableStateOf(initialClass) }
    var selectedGroupCode by remember(account) { mutableStateOf(initialGroup) }
    var selectedBatchYear by remember(account) { mutableStateOf(initialBatch) }
    var customYearInput by remember { mutableStateOf("") }
    var selectedProgramIds by remember(account) {
        mutableStateOf(account.allowedPrograms.orEmpty().toSet())
    }

    var isAccessGranted by remember(account) { mutableStateOf(true) }
    // If the account previously had isAccessGranted = true and isRestricted = false (all courses open), preserve that
    var allowAllCoursesInClass by remember(account) {
        mutableStateOf(
            account.isAccessGranted && !account.isRestricted && account.allowedPrograms.isNullOrEmpty()
        )
    }
    var revokeOtherAccounts by remember(account) { mutableStateOf(true) }

    val requiresGroup = selectedClassCode in ShikhoApiClient.GROUP_REQUIRED_CLASSES

    // Combine API batch years + guaranteed fallback years (2026, 2027, 2028)
    val displayBatchYears = remember(shikhoBatchYears, selectedClassCode) {
        ShikhoApiClient.mergeWithFallbackBatchYears(shikhoBatchYears, selectedClassCode)
    }

    // Step 1: Load real class list automatically when Shikho token is connected
    LaunchedEffect(isShikhoConnected) {
        if (isShikhoConnected && shikhoClassList.isEmpty()) {
            onLoadRealClasses()
        }
    }

    // Step 3: Load real batch options automatically when class changes
    LaunchedEffect(isShikhoConnected, selectedClassCode) {
        if (selectedClassCode.isNotBlank()) {
            onLoadRealBatches(selectedClassCode)
        }
    }

    // Auto-select first returned batch year if none selected
    LaunchedEffect(displayBatchYears) {
        if (displayBatchYears.isNotEmpty() && selectedBatchYear.isBlank()) {
            selectedBatchYear = displayBatchYears.first().year
        }
    }

    // Step 4: Load real academic programs (ChangeSyllabus -> UpdateExamYear -> GetAcademicProgram)
    LaunchedEffect(isShikhoConnected, selectedClassCode, selectedGroupCode, selectedBatchYear) {
        if (isShikhoConnected && selectedClassCode.isNotBlank() && selectedBatchYear.isNotBlank()) {
            val effectiveGroup = ShikhoApiClient.normalizeGroupCode(selectedGroupCode, selectedClassCode)
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
                        text = "📞 ${account.userPhone ?: "N/A"} • আইডি: ${account.shikhoUserId}",
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
                                text = "শিখোর অফিসিয়াল সার্ভার থেকে ১০০% রিয়েল শ্রেণি, বিভাগ, ব্যাচ এবং কোর্সের তালিকা লোড করতে প্রথমে আপনার শিখো অ্যাকাউন্ট লগইন করুন।",
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
                                Text("শিখো অ্যাকাউন্ট কানেক্ট করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Account Lock & Revoke Other Accounts Toggle (Clean Bengali UI - Bug 5 Fix)
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
                                    text = "অন্যান্য অ্যাকাউন্ট ব্লক রাখুন",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = "স্টুডেন্ট অন্য নাম্বার দিয়ে লগইন করলে কোনো কোর্স দেখবে না",
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
                                text = "এই অ্যাকাউন্টে কাস্টম কোর্স ও সিলেবাস লক সক্রিয় করুন",
                                style = MaterialTheme.typography.labelMedium.copy(color = Slate200),
                                modifier = Modifier.weight(1f)
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
            // STEP 1: Class Selection
            // =========================================================================
            item {
                val classesToRender = remember(shikhoClassList) {
                    if (shikhoClassList.isNotEmpty()) {
                        shikhoClassList
                    } else {
                        ShikhoApiClient.CLASS_BENGALI_NAMES.entries.map { (code, bn) ->
                            ShikhoClassItem(
                                code = code,
                                titleBn = bn,
                                nameEn = code,
                                serial = ShikhoApiClient.DEFAULT_CLASS_SERIAL[code] ?: 99,
                                isActive = true
                            )
                        }
                    }
                }

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
                                    text = "ধাপ ১: শ্রেণি নির্বাচন করুন",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                )
                                Text(
                                    text = "শিক্ষার্থীর জন্য অনুমোদিত শ্রেণি সিলেক্ট করুন",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate400,
                                        fontSize = 11.sp
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

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            classesToRender.forEach { cls ->
                                val isSelected = selectedClassCode.equals(cls.code, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (!selectedClassCode.equals(cls.code, ignoreCase = true)) {
                                            selectedClassCode = cls.code
                                            if (cls.code !in ShikhoApiClient.GROUP_REQUIRED_CLASSES) {
                                                selectedGroupCode = "None"
                                            } else if (selectedGroupCode == "None" || selectedGroupCode.isBlank()) {
                                                selectedGroupCode = "Science"
                                            }
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

            // =========================================================================
            // STEP 2: Study Group Selection (`C9`, `C10`, `C11`, `C12`)
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
                                text = "ধাপ ২: বিভাগ নির্বাচন করুন",
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
                                        text = "ℹ️ ${ShikhoApiClient.CLASS_BENGALI_NAMES[selectedClassCode] ?: selectedClassCode}-এর জন্য আলাদা বিভাগের প্রয়োজন নেই।",
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
            // STEP 3: Batch / Exam Year Selection (Bug 1 Fix: options + 2026, 2027, 2028)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ধাপ ৩: পরীক্ষার সাল / ব্যাচ নির্বাচন করুন",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    )
                                    Text(
                                        text = "ব্যাচ সিলেক্ট করুন অথবা কাস্টম সাল যোগ করুন",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate400,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = { onLoadRealBatches(selectedClassCode) },
                                    enabled = !isLoadingBatches,
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

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                displayBatchYears.forEach { batchItem ->
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

                            // Custom Batch Year Input Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customYearInput,
                                    onValueChange = { customYearInput = it.filter { ch -> ch.isDigit() }.take(4) },
                                    placeholder = { Text("অন্য সাল লিখুন (যেমন: 2029)", fontSize = 12.sp, color = Slate400) },
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
                                Button(
                                    onClick = {
                                        val yr = customYearInput.trim()
                                        if (yr.length == 4) {
                                            selectedBatchYear = yr
                                            customYearInput = ""
                                        } else {
                                            onShowToast("৪ সংখ্যার সঠিক সাল লিখুন (যেমন: 2028)")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("সিলেক্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // STEP 4: Real Course Selection (Bug 2 & Bug 4 Fix)
            // =========================================================================
            if (selectedClassCode.isNotBlank() && selectedBatchYear.isNotBlank()) {
                item {
                    val effectiveGroup = ShikhoApiClient.normalizeGroupCode(selectedGroupCode, selectedClassCode)
                    val classDisplayName = ShikhoApiClient.CLASS_BENGALI_NAMES[selectedClassCode] ?: selectedClassCode

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
                                        text = "ধাপ ৪: কোর্স নির্বাচন করুন (${if (allowAllCoursesInClass) "সব কোর্স উন্মুক্ত" else "${selectedProgramIds.size} টি সিলেক্টেড"})",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    )
                                    Text(
                                        text = "$classDisplayName • ব্যাচ $selectedBatchYear-এর অনুমোদিত কোর্সসমূহ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate400,
                                            fontSize = 11.sp
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

                            // BUG 4 FIX: Toggle to open all courses of this selected class without picking individual ones
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (allowAllCoursesInClass) EmeraldSuccess.copy(alpha = 0.14f) else Slate900,
                                border = BorderStroke(
                                    1.dp,
                                    if (allowAllCoursesInClass) EmeraldSuccess.copy(alpha = 0.7f) else Slate700
                                ),
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
                                            text = "এই শ্রেণির সকল কোর্স উন্মুক্ত রাখুন",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = if (allowAllCoursesInClass) {
                                                "এই শ্রেণির সবগুলো কোর্স স্টুডেন্ট দেখতে পাবে"
                                            } else {
                                                "শুধুমাত্র নিচে টিক দেওয়া নির্দিষ্ট কোর্সগুলো স্টুডেন্ট দেখতে পাবে"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (allowAllCoursesInClass) EmeraldSuccess else Slate400,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    Switch(
                                        checked = allowAllCoursesInClass,
                                        onCheckedChange = { allowAllCoursesInClass = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = EmeraldSuccess
                                        ),
                                        modifier = Modifier.testTag("switch_allow_all_courses_in_class")
                                    )
                                }
                            }

                            if (!allowAllCoursesInClass) {
                                // Select All / Clear All quick buttons
                                if (shikhoPrograms.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "মোট ${shikhoPrograms.size} টি কোর্স পাওয়া গেছে",
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
                                                text = "সিলেবাস পরিবর্তন ও রিয়েল কোর্সের তালিকা লোড হচ্ছে...",
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
                                            text = if (isShikhoConnected) {
                                                "এই সিলেবাসের জন্য কোনো কোর্স পাওয়া যায়নি। ডানপাশের রিফ্রেশ বাটনে চাপুন অথবা উপরের 'এই শ্রেণির সকল কোর্স উন্মুক্ত রাখুন' চালু করুন।"
                                            } else {
                                                "কোর্সের তালিকা দেখতে আগে উপরে আপনার শিখো অ্যাকাউন্ট লগইন করুন।"
                                            },
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
                        // BUG 4 FIX: Validate that at least 1 course is selected unless allowAllCoursesInClass is ON
                        if (isAccessGranted && !allowAllCoursesInClass && selectedProgramIds.isEmpty()) {
                            onShowToast(
                                "অনুগ্রহ করে অন্তত ১টি কোর্স সিলেক্ট করুন অথবা 'এই শ্রেণির সকল কোর্স উন্মুক্ত রাখুন' অপশনটি চালু করুন"
                            )
                            return@Button
                        }

                        val allowedClassesList = if (selectedClassCode.isNotBlank()) {
                            listOf(selectedClassCode)
                        } else {
                            emptyList()
                        }
                        val allowedGroupsList = if (selectedClassCode in ShikhoApiClient.GROUP_REQUIRED_CLASSES &&
                            selectedGroupCode.isNotBlank() &&
                            selectedGroupCode != "None"
                        ) {
                            listOf(ShikhoApiClient.normalizeGroupCode(selectedGroupCode, selectedClassCode))
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
                            isRestricted = !allowAllCoursesInClass,
                            allowedClasses = allowedClassesList,
                            allowedGroups = allowedGroupsList,
                            allowedBatches = allowedBatchesList,
                            allowedPrograms = if (allowAllCoursesInClass) emptyList() else selectedProgramIds.toList(),
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
                        val saveBtnLabel = when {
                            !isAccessGranted -> "এক্সেস বন্ধ করে সেভ করুন"
                            allowAllCoursesInClass -> "পারমিশন সেভ করুন (সকল কোর্স)"
                            else -> "পারমিশন সেভ করুন (${selectedProgramIds.size} টি কোর্স)"
                        }
                        Text(
                            text = saveBtnLabel,
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
                    if (program.classes.isNotEmpty()) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = program.classes.joinToString(", "),
                                color = NeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (program.isFree) {
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ফ্রি কোর্স",
                                color = EmeraldSuccess,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = ElectricBlue.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "একাডেমিক প্রোগ্রাম",
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
