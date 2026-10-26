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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AccessCode
import com.example.data.model.DashboardStats
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
 * quick code copy actions, and clean zero-clutter information architecture.
 */
@Composable
fun DashboardScreen(
    stats: DashboardStats,
    recentCodes: List<AccessCode>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

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

                        // Capacity Bar & Slots Info
                        val capacityRatio = if (stats.totalCodes > 0) {
                            (stats.totalDevices.toFloat() / (stats.totalCodes * 1.5f)).coerceIn(0f, 1f)
                        } else 0f

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "লাইসেন্স ব্যবহার হার",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Slate200,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "${stats.totalDevices} ডিভাইস সংযুক্ত",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            LinearProgressIndicator(
                                progress = { capacityRatio },
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
                                    text = "অবশিষ্ট স্লট: ${stats.availableSlots}",
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
                        modifier = Modifier.weight(1f),
                        title = "মোট কোড",
                        subtitle = "Total Access Keys",
                        value = "${stats.totalCodes}",
                        icon = Icons.Default.VpnKey,
                        accentColor = ElectricBlue,
                        gradientColors = listOf(ElectricBlue.copy(alpha = 0.2f), Slate800),
                        testTag = "stat_total_codes"
                    )
                    MetricStatCard(
                        modifier = Modifier.weight(1f),
                        title = "সক্রিয় শিক্ষার্থী",
                        subtitle = "Active Students",
                        value = "${stats.activeStudents}",
                        icon = Icons.Default.Person,
                        accentColor = EmeraldSuccess,
                        gradientColors = listOf(EmeraldSuccess.copy(alpha = 0.2f), Slate800),
                        testTag = "stat_active_students"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        modifier = Modifier.weight(1f),
                        title = "বাইন্ডেড ডিভাইস",
                        subtitle = "Bound Hardware",
                        value = "${stats.totalDevices}",
                        icon = Icons.Default.Devices,
                        accentColor = NeonCyan,
                        gradientColors = listOf(NeonCyan.copy(alpha = 0.2f), Slate800),
                        testTag = "stat_total_devices"
                    )
                    MetricStatCard(
                        modifier = Modifier.weight(1f),
                        title = "ব্লকড ডিভাইস",
                        subtitle = "Kill-Switch Blocked",
                        value = "${stats.blockedDevices}",
                        icon = Icons.Default.Block,
                        accentColor = CoralDanger,
                        gradientColors = listOf(CoralDanger.copy(alpha = 0.2f), Slate800),
                        testTag = "stat_blocked_devices"
                    )
                }
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
                        text = "৯টি মডিউল",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate400,
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
                // Featured Row 0: Course Sales, Custom Pricing, Installments & Payment Verification
                FeaturedModuleCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "কোর্স বিক্রি ও কিস্তি ম্যানেজমেন্ট",
                    subtitle = "কোর্স এক্সেস রিকোয়েস্ট, কাস্টম দাম, কিস্তি প্ল্যান ও বিকাশ/নগদ/রকেট পেমেন্ট ভেরিফিকেশন",
                    icon = Icons.Default.Payments,
                    badge = "কিস্তি ও পেমেন্ট",
                    accentColor = EmeraldSuccess,
                    gradientStart = EmeraldSuccess.copy(alpha = 0.22f),
                    onClick = { onNavigate(Screen.CourseSales) },
                    testTag = "mod_card_course_sales"
                )

                // Row 1: Cloudinary & New Code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "নতুন কোড",
                        subtitle = "স্টুডেন্ট অ্যাক্সেস কোড জেনারেটর",
                        icon = Icons.Default.AddModerator,
                        badge = "Create",
                        accentColor = ElectricBlue,
                        gradientStart = ElectricBlue.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.Generator) },
                        testTag = "mod_card_generator"
                    )
                }

                // Row 2: Notices & App Updates
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

                // Row 3: Support Links & Kill-Switch
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
                        title = "কিল-সুইচ",
                        subtitle = "ডিভাইস লক ও হার্ডওয়্যার ব্যান",
                        icon = Icons.Default.Devices,
                        badge = null,
                        accentColor = CoralDanger,
                        gradientStart = CoralDanger.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.Devices) },
                        testTag = "mod_card_devices"
                    )
                }

                // Row 4: Student List & System Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "শিক্ষার্থী তালিকা",
                        subtitle = "সকল অ্যাক্সেস কোড ডাটাবেজ",
                        icon = Icons.Default.Group,
                        badge = null,
                        accentColor = Sky500,
                        gradientStart = Sky500.copy(alpha = 0.18f),
                        onClick = { onNavigate(Screen.Codes) },
                        testTag = "mod_card_codes"
                    )
                    FeaturedModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "সিস্টেম সেটিংস",
                        subtitle = "সুপাবেস ডাটাবেজ ও রিলিজ APK",
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

        // 5. Recent Codes Preview Header & List
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
                            .background(NeonCyan)
                    )
                    Text(
                        text = "সাম্প্রতিক অ্যাক্সেস কোড",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElectricBlue.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable { onNavigate(Screen.Codes) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "সব কোড দেখুন",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectricBlueLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = ElectricBlueLight,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        if (recentCodes.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "কোনো অ্যাক্সেস কোড এখনো তৈরি করা হয়নি।",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                            Button(
                                onClick = { onNavigate(Screen.Generator) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("নতুন কোড তৈরি করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(recentCodes.take(4)) { code ->
                RecentCodeCard(
                    code = code,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(code.code))
                        onShowToast("কোড ${code.code} কপি করা হয়েছে!")
                    }
                )
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
 * Modern Recent Code Card with Clear Visual Separation and Instant Copy
 */
@Composable
private fun RecentCodeCard(
    code: AccessCode,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (code.isActive) EmeraldSuccess else CoralDanger)
                    )
                    Text(
                        text = code.studentName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = code.code,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = Slate700,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "সর্বোচ্চ ${code.maxDevices} ডিভাইস",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate200,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Slate700)
                    .size(38.dp)
                    .testTag("btn_copy_${code.code}")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = ElectricBlueLight,
                    modifier = Modifier.size(18.dp)
                )
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
