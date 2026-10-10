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
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Admin Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MaxBird Supabase Online",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Text(
                        text = "কন্ট্রোল প্যানেল (Dashboard)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Slate800)
                            .testTag("btn_refresh_dashboard")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ElectricBlueLight,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = NeonCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Slate800)
                            .testTag("btn_settings_dashboard")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Slate400
                        )
                    }
                }
            }
        }

        // Featured Modules Hub (Instant One-Tap Access right at top of Dashboard)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(ElectricBlue, NeonCyan)),
                        RoundedCornerShape(20.dp)
                    )
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "প্রধান এডমিন ফিচারসমূহ (Modules)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricBlue.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue)
                        ) {
                            Text(
                                text = "৭টি মডিউল",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Grid of Module Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeaturedModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "🎧 সাপোর্ট লিংক",
                            subtitle = "টেলিগ্রাম ও হোয়াটসঅ্যাপ",
                            badge = "নতুন",
                            accentColor = NeonCyan,
                            onClick = { onNavigate(Screen.SupportLinks) },
                            testTag = "mod_card_support_links"
                        )
                        FeaturedModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "📢 নোটিশ কন্ট্রোল",
                            subtitle = "পপ-আপ ও ব্যানার",
                            badge = "সক্রিয়",
                            accentColor = ElectricBlue,
                            onClick = { onNavigate(Screen.Notices) },
                            testTag = "mod_card_notices"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeaturedModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "🚀 অ্যাপ আপডেট",
                            subtitle = "রিলিজ ও ফোর্স আপডেট",
                            badge = null,
                            accentColor = EmeraldSuccess,
                            onClick = { onNavigate(Screen.Updates) },
                            testTag = "mod_card_updates"
                        )
                        FeaturedModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "➕ নতুন কোড",
                            subtitle = "কোড জেনারেটর",
                            badge = null,
                            accentColor = ElectricBlueLight,
                            onClick = { onNavigate(Screen.Generator) },
                            testTag = "mod_card_generator"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeaturedModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "👥 শিক্ষার্থী তালিকা",
                            subtitle = "কোড ও ডিভাইস",
                            badge = null,
                            accentColor = ElectricBlueLight,
                            onClick = { onNavigate(Screen.Codes) },
                            testTag = "mod_card_codes"
                        )
                        FeaturedModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "🛡️ কিল-সুইচ",
                            subtitle = "ডিভাইস ব্লক কন্ট্রোল",
                            badge = null,
                            accentColor = CoralDanger,
                            onClick = { onNavigate(Screen.Devices) },
                            testTag = "mod_card_devices"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeaturedModuleCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "⚙️ সেটিংস, এপিকে ও সুপাবেস SQL স্ক্রিপ্ট",
                            subtitle = "সুপাবেস ডাটাবেজ সংযোগ, রিলিজ APK ও সকল টেবিল স্ক্রিপ্ট",
                            badge = null,
                            accentColor = Slate400,
                            onClick = { onNavigate(Screen.Settings) },
                            testTag = "mod_card_settings"
                        )
                    }
                }
            }
        }

        // Stat Cards Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "মোট কোড",
                        subtitle = "Total Access Codes",
                        value = "${stats.totalCodes}",
                        icon = Icons.Default.VpnKey,
                        accentColor = ElectricBlue,
                        testTag = "stat_total_codes"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "সক্রিয় শিক্ষার্থী",
                        subtitle = "Active Students",
                        value = "${stats.activeStudents}",
                        icon = Icons.Default.Person,
                        accentColor = EmeraldSuccess,
                        testTag = "stat_active_students"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "বাইন্ডেড ডিভাইস",
                        subtitle = "Registered Hardware",
                        value = "${stats.totalDevices}",
                        icon = Icons.Default.Devices,
                        accentColor = NeonCyan,
                        testTag = "stat_total_devices"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "ব্লকড (কিল-সুইচ)",
                        subtitle = "Blocked Devices",
                        value = "${stats.blockedDevices}",
                        icon = Icons.Default.Block,
                        accentColor = CoralDanger,
                        testTag = "stat_blocked_devices"
                    )
                }
            }
        }

        // Quick Capacity Overview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ডিভাইস লাইসেন্স ব্যবহার (Capacity)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${stats.totalDevices} ডিভাইস সংযুক্ত",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progress = if (stats.totalCodes > 0) {
                        (stats.totalDevices.toFloat() / (stats.totalCodes * 1.5f)).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ElectricBlue,
                        trackColor = Slate700
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "অবশিষ্ট স্লট: ${stats.availableSlots}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                        Text(
                            text = "Zero-Trust Active",
                            style = MaterialTheme.typography.labelSmall.copy(color = EmeraldSuccess)
                        )
                    }
                }
            }
        }

        // Primary Action Shortcuts
        item {
            Text(
                text = "দ্রুত অ্যাকশন (Quick Actions)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate(Screen.Generator) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_quick_new_code")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddModerator,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নতুন কোড", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onNavigate(Screen.Devices) },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_quick_kill_switch")
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কিল-সুইচ", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate(Screen.Notices) },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_quick_notices")
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নোটিশ কন্ট্রোল", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onNavigate(Screen.Updates) },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_quick_updates")
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = ElectricBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("অ্যাপ আপডেট", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Recent Codes Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সর্বশেষ এক্সেস কোডসমূহ (Recent Codes)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "সব দেখুন >",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ElectricBlueLight,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clickable { onNavigate(Screen.Codes) }
                        .padding(4.dp)
                )
            }
        }

        items(recentCodes.take(4)) { code ->
            RecentCodeCard(
                code = code,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(code.code))
                    onShowToast("কোড ${code.code} কপি করা হয়েছে! (Copied)")
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
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
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate400
                )
            )
        }
    }
}

@Composable
private fun RecentCodeCard(
    code: AccessCode,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
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
                    text = code.studentName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Slate700,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = code.code,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Max: ${code.maxDevices} Device",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                }
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Slate700)
                    .size(36.dp)
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

@Composable
private fun FeaturedModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badge: String?,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .border(1.dp, Slate700, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate400,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "খুলুন >",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

