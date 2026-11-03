package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardStats
import com.example.notifications.OneSignalPushManager
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderGradientEnd
import com.example.ui.theme.CardBorderGradientStart
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueDark
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Indigo600
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleRoyal
import com.example.ui.theme.Rose500
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.Violet500

/**
 * Modern, Ultra-Polished Executive Dashboard Screen for MaxBird Admin.
 * Features glowing gradient hero metrics, balanced modular admin grid,
 * and clean zero-clutter information architecture.
 */
@Composable
fun DashboardScreen(
    stats: DashboardStats,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onShowToast: (String) -> Unit,
    onOpenPaymentMethodsManager: () -> Unit = {},
    isLiveNotificationEnabled: Boolean = true,
    pendingCourseRequestsCount: Int = 0,
    pendingPaymentsCount: Int = 0,
    onToggleLiveNotification: (Boolean) -> Unit = {},
    onTestNotification: () -> Unit = {},
    oneSignalAppId: String = "",
    oneSignalRestApiKey: String = "",
    oneSignalStatusSummary: String = "",
    isSendingOneSignalTest: Boolean = false,
    onSaveOneSignalConfig: (String, String) -> Unit = { _, _ -> }
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showOneSignalDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // 1. Executive Master Hero Card (Gradient Glassmorphism Style)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(ElectricBlue.copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.25f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ElectricBlue.copy(alpha = 0.12f),
                                    Slate850.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(ElectricBlue, NeonCyan)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "সিকিউরিটি ও ডিভাইস ক্লাউড",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                letterSpacing = 0.3.sp
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldSuccess.copy(alpha = pulseAlpha))
                                        )
                                    }
                                    Text(
                                        text = "MaxBird Supabase • Zero-Trust Binding",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Active Status Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    EmeraldSuccess.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "অনলাইন",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmeraldSuccess,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Device Verification Progress Bar & Live Status
                        val verificationRatio = if (stats.totalDevices > 0) {
                            (stats.activeVerifiedDevices.toFloat() / stats.totalDevices.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ডিভাইস ভেরিফিকেশন অনুপাত",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Slate200,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "${stats.activeVerifiedDevices} সক্রিয় / ${stats.totalDevices} মোট ডিভাইস",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            LinearProgressIndicator(
                                progress = { verificationRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ElectricBlue,
                                trackColor = Slate700
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "অপেক্ষমান রিকোয়েস্ট: ${stats.pendingVerificationDevices} টি",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = "ক্লাউড অটো-সিঙ্ক সক্রিয়",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldSuccess,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. High-Impact Metrics Grid (2x2 Balanced Cards with glowing tint)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(Screen.Devices) },
                        title = "অপেক্ষমান আবেদন",
                        subtitle = "Pending Verification",
                        value = "${stats.pendingVerificationDevices}",
                        icon = Icons.Default.Security,
                        accentColor = AmberWarning,
                        gradientColors = listOf(AmberWarning.copy(alpha = 0.22f), Slate800),
                        testTag = "stat_total_codes"
                    )
                    MetricStatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(Screen.Devices) },
                        title = "ভেরিফাইড ডিভাইস",
                        subtitle = "Active Devices",
                        value = "${stats.activeVerifiedDevices}",
                        icon = Icons.Default.CheckCircle,
                        accentColor = EmeraldSuccess,
                        gradientColors = listOf(EmeraldSuccess.copy(alpha = 0.22f), Slate800),
                        testTag = "stat_active_students"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(Screen.Devices) },
                        title = "মোট ডিভাইস",
                        subtitle = "Total Devices",
                        value = "${stats.totalDevices}",
                        icon = Icons.Default.Devices,
                        accentColor = NeonCyan,
                        gradientColors = listOf(NeonCyan.copy(alpha = 0.2f), Slate800),
                        testTag = "stat_total_devices"
                    )
                    MetricStatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(Screen.Devices) },
                        title = "ব্লকড ডিভাইস",
                        subtitle = "Blocked Devices",
                        value = "${stats.blockedDevices}",
                        icon = Icons.Default.Block,
                        accentColor = CoralDanger,
                        gradientColors = listOf(CoralDanger.copy(alpha = 0.2f), Slate800),
                        testTag = "stat_blocked_devices"
                    )
                }
            }
        }

        // 2B. Real-Time Push Notification Monitor Card (Device Requests, Course Requests & Payments)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isLiveNotificationEnabled) EmeraldSuccess.copy(alpha = 0.45f) else Slate700,
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isLiveNotificationEnabled)
                                            EmeraldSuccess.copy(alpha = 0.2f)
                                        else
                                            Slate700
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (isLiveNotificationEnabled) EmeraldSuccess else Slate400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "২৪/৭ লাইভ পুশ নোটিফিকেশন গার্ড",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "ডিভাইস রিকোয়েস্ট, কোর্স আবেদন বা টাকা পাঠালে সাথে সাথে অ্যালার্ট",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = isLiveNotificationEnabled,
                            onCheckedChange = onToggleLiveNotification,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldSuccess
                            ),
                            modifier = Modifier.testTag("switch_live_notification_guard")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (stats.pendingVerificationDevices > 0) AmberWarning.copy(alpha = 0.5f) else Slate700
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.Devices) }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${stats.pendingVerificationDevices}",
                                    color = if (stats.pendingVerificationDevices > 0) AmberWarning else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "ডিভাইস পেন্ডিং",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (pendingCourseRequestsCount > 0) NeonCyan.copy(alpha = 0.5f) else Slate700
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.CourseSales) }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$pendingCourseRequestsCount",
                                    color = if (pendingCourseRequestsCount > 0) NeonCyan else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "কোর্স রিকোয়েস্ট",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (pendingPaymentsCount > 0) EmeraldSuccess.copy(alpha = 0.5f) else Slate700
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.CourseSales) }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$pendingPaymentsCount",
                                    color = if (pendingPaymentsCount > 0) EmeraldSuccess else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "পেমেন্ট পেন্ডিং",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    if (oneSignalStatusSummary.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (oneSignalAppId.isNotBlank()) EmeraldSuccess.copy(alpha = 0.45f) else AmberWarning.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = oneSignalStatusSummary,
                                color = if (oneSignalAppId.isNotBlank()) EmeraldSuccess else AmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestNotification,
                            enabled = !isSendingOneSignalTest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_test_push_notification")
                        ) {
                            if (isSendingOneSignalTest) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "টেস্ট পুশ",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { showOneSignalDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (oneSignalAppId.isNotBlank()) EmeraldSuccess else ElectricBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_onesignal_setup")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = if (oneSignalAppId.isNotBlank()) Slate950 else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (oneSignalAppId.isNotBlank()) "OneSignal সক্রিয়" else "OneSignal সেটআপ",
                                color = if (oneSignalAppId.isNotBlank()) Slate950 else Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            if (showOneSignalDialog) {
                var appIdInput by remember(oneSignalAppId) { mutableStateOf(oneSignalAppId) }
                var restKeyInput by remember(oneSignalRestApiKey) { mutableStateOf(oneSignalRestApiKey) }

                AlertDialog(
                    onDismissRequest = { showOneSignalDialog = false },
                    containerColor = Slate850,
                    titleContentColor = Color.White,
                    textContentColor = Slate200,
                    title = {
                        Text(
                            text = "🔔 OneSignal ক্লাউড পুশ নোটিফিকেশন সেটআপ",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "অ্যাপ সম্পূর্ণ বন্ধ (Closed/Killed) থাকলেও স্টুডেন্ট ডিভাইস ভেরিফিকেশন, কোর্স রিকোয়েস্ট বা টাকা পাঠালে সাথে সাথে নোটিফিকেশন পেতে আপনার OneSignal App ID ও REST API Key দিন:",
                                color = Slate200,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )

                            OutlinedTextField(
                                value = appIdInput,
                                onValueChange = { appIdInput = it },
                                label = { Text("OneSignal App ID", fontSize = 12.sp) },
                                placeholder = { Text("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Slate700,
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate900,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = restKeyInput,
                                onValueChange = { restKeyInput = it },
                                label = { Text("OneSignal REST API Key", fontSize = 12.sp) },
                                placeholder = { Text("os_v2_app_...", fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Slate700,
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate900,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedButton(
                                onClick = {
                                    val sql = OneSignalPushManager.buildSupabaseOneSignalTriggerSql(
                                        appId = appIdInput.trim(),
                                        restApiKey = restKeyInput.trim()
                                    )
                                    clipboardManager.setText(AnnotatedString(sql))
                                    onShowToast("✅ অটো-পুশ SQL Trigger কপি হয়েছে! Supabase SQL Editor-এ Run করুন।")
                                },
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Supabase Auto-Push SQL ট্রিগার কপি",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onSaveOneSignalConfig(appIdInput.trim(), restKeyInput.trim())
                                showOneSignalDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Text("সেভ ও চালু করুন", color = Slate950, fontWeight = FontWeight.ExtraBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showOneSignalDialog = false }) {
                            Text("বন্ধ করুন", color = Slate400)
                        }
                    }
                )
            }
        }

        // 3. Admin Modules Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue)
                    )
                    Text(
                        text = "এডমিন ফিচার হাব (Modules)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate800,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Text(
                        text = "১-ক্লিক কন্ট্রোল",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // 4. Admin Modules 2-Column Balanced Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Featured Row: Maxbird Website Admin Control Panel (https://maxbird.pages.dev)
                FeaturedModuleCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Maxbird Website কন্ট্রোল প্যানেল (maxbird.pages.dev)",
                    subtitle = "ওয়েবসাইটের APK লিংক (R2), ডাউনলোড কাউন্টার, ভার্সন, ফাইল সাইজ ও টপ নোটিশ লাইভ কন্ট্রোল",
                    icon = Icons.Default.Language,
                    badge = "🌐 ওয়েবসাইট লাইভ",
                    accentColor = ElectricBlueLight,
                    gradientStart = ElectricBlue.copy(alpha = 0.28f),
                    onClick = { onNavigate(Screen.MaxbirdWebsiteAdmin) },
                    testTag = "mod_card_maxbird_website_admin"
                )

                // Featured Row 00: 1-Click Student Device Verification & Control
                FeaturedModuleCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "স্টুডেন্ট ডিভাইস ভেরিফিকেশন ও কন্ট্রোল",
                    subtitle = "১-ক্লিকে নতুন ডিভাইস অ্যাপ্রুভ (Pending), স্টুডেন্টের নাম/নোট এডিট এবং ব্লক/আনব্লক কন্ট্রোল",
                    icon = Icons.Default.Devices,
                    badge = if (stats.pendingVerificationDevices > 0) {
                        "⏳ ${stats.pendingVerificationDevices}টি পেন্ডিং"
                    } else {
                        "১-ক্লিকে ভেরিফাই"
                    },
                    accentColor = if (stats.pendingVerificationDevices > 0) AmberWarning else EmeraldSuccess,
                    gradientStart = (if (stats.pendingVerificationDevices > 0) AmberWarning else EmeraldSuccess).copy(alpha = 0.24f),
                    onClick = { onNavigate(Screen.Devices) },
                    testTag = "mod_card_devices"
                )

                // Featured Row 0A: Dynamic Payment Methods Manager (Add/Edit/Delete/ON-OFF bKash, Nagad, Rocket, Upay, Bank)
                FeaturedModuleCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "পেমেন্ট মেথড ম্যানেজার (বিকাশ/নগদ/রকেট)",
                    subtitle = "যেকোনো পেমেন্ট নম্বর যোগ (Add), এডিট, ডিলিট এবং ON / OFF সুইচ কন্ট্রোল করুন",
                    icon = Icons.Default.Payments,
                    badge = "+ মেথড যোগ করুন",
                    accentColor = NeonCyan,
                    gradientStart = NeonCyan.copy(alpha = 0.24f),
                    onClick = onOpenPaymentMethodsManager,
                    testTag = "mod_card_payment_methods_manager"
                )

                // Featured Row 0B: Course Sales, Custom Pricing, Installments & Payment Verification
                FeaturedModuleCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "কোর্স বিক্রি ও কিস্তি ম্যানেজমেন্ট",
                    subtitle = "কোর্স এক্সেস রিকোয়েস্ট, কাস্টম দাম, কিস্তি প্ল্যান ও পেমেন্ট ভেরিফিকেশন",
                    icon = Icons.Default.Payments,
                    badge = "কিস্তি ও পেমেন্ট",
                    accentColor = EmeraldSuccess,
                    gradientStart = EmeraldSuccess.copy(alpha = 0.22f),
                    onClick = { onNavigate(Screen.CourseSales) },
                    testTag = "mod_card_course_sales"
                )

                // Row 1: Notices & App Updates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "নোটিশ কন্ট্রোল",
                        subtitle = "পপ-আপ ও ব্যানার নোটিশ ম্যানেজ",
                        icon = Icons.Default.Campaign,
                        badge = null,
                        accentColor = AmberWarning,
                        gradientStart = AmberWarning.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.Notices) },
                        testTag = "mod_card_notices"
                    )
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "অ্যাপ আপডেট",
                        subtitle = "নতুন রিলিজ ও ফোর্স আপডেট",
                        icon = Icons.Default.SystemUpdate,
                        badge = null,
                        accentColor = EmeraldSuccess,
                        gradientStart = EmeraldSuccess.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.Updates) },
                        testTag = "mod_card_updates"
                    )
                }

                // Row 2: Support Links & Cloudinary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "সাপোর্ট লিংক",
                        subtitle = "টেলিগ্রাম ও হোয়াটসঅ্যাপ সাপোর্ট",
                        icon = Icons.Default.HeadsetMic,
                        badge = null,
                        accentColor = PurpleRoyal,
                        gradientStart = PurpleRoyal.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.SupportLinks) },
                        testTag = "mod_card_support_links"
                    )
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "Shikho ক্লাউডনারি",
                        subtitle = "সরাসরি মিডিয়া আপলোড ও পার্মানেন্ট লিংক",
                        icon = Icons.Default.CloudUpload,
                        badge = "Direct URL",
                        accentColor = NeonCyan,
                        gradientStart = NeonCyan.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.CloudinaryMedia) },
                        testTag = "mod_card_cloudinary_uploader"
                    )
                }

                // Row 3: Direct Device Control & System Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "ডিভাইস ও অ্যাকাউন্ট পারমিশন",
                        subtitle = "কোর্স অ্যাক্সেস ও হার্ডওয়্যার কিল-সুইচ",
                        icon = Icons.Default.Devices,
                        badge = "${stats.activeVerifiedDevices} সক্রিয়",
                        accentColor = Sky500,
                        gradientStart = Sky500.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.Devices) },
                        testTag = "mod_card_devices_permissions"
                    )
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "সিস্টেম সেটিংস",
                        subtitle = "সুপাবেস ডাটাবেজ ও পেমেন্ট সেটিংস",
                        icon = Icons.Default.Settings,
                        badge = null,
                        accentColor = Slate400,
                        gradientStart = Slate400.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.Settings) },
                        testTag = "mod_card_settings"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Metric Card with Glowing Gradient Accent
 */
@Composable
private fun MetricStatCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    gradientColors: List<Color>,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .border(
                1.dp,
                accentColor.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            )
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}



/**
 * Modern Feature Module Card with Subtle Glow and Elevated Feel
 */
@Composable
private fun FeaturedModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String?,
    accentColor: Color,
    gradientStart: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(
                1.dp,
                accentColor.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(gradientStart, Slate800)
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = badge,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate400,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        ),
                        maxLines = 2
                    )
                }
            }
        }
    }
}
