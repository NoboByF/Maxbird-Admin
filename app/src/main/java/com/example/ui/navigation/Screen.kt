package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val titleEn: String,
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        titleEn = "Home",
        titleBn = "হোম",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    data object CourseSales : Screen(
        route = "course_sales",
        titleEn = "Course Sales & Installments",
        titleBn = "কোর্স বিক্রি ও কিস্তি ম্যানেজমেন্ট",
        selectedIcon = Icons.Filled.Payments,
        unselectedIcon = Icons.Outlined.Payments
    )

    data object MaxbirdWebsiteAdmin : Screen(
        route = "maxbird_web_admin",
        titleEn = "Maxbird Admin Control Panel (maxbird.pages.dev)",
        titleBn = "Maxbird ওয়েবসাইট কন্ট্রোল প্যানেল",
        selectedIcon = Icons.Filled.Language,
        unselectedIcon = Icons.Outlined.Language
    )

    data object Notices : Screen(
        route = "notices",
        titleEn = "Notices",
        titleBn = "নোটিশ",
        selectedIcon = Icons.Filled.Campaign,
        unselectedIcon = Icons.Outlined.Campaign
    )

    data object Updates : Screen(
        route = "updates",
        titleEn = "Updates",
        titleBn = "আপডেট",
        selectedIcon = Icons.Filled.SystemUpdate,
        unselectedIcon = Icons.Outlined.SystemUpdate
    )

    data object SupportLinks : Screen(
        route = "support_links",
        titleEn = "Support",
        titleBn = "সাপোর্ট",
        selectedIcon = Icons.Filled.HeadsetMic,
        unselectedIcon = Icons.Outlined.HeadsetMic
    )

    data object Devices : Screen(
        route = "devices",
        titleEn = "One-Click Student Device Verification",
        titleBn = "স্টুডেন্ট ডিভাইস ভেরিফিকেশন ও কন্ট্রোল",
        selectedIcon = Icons.Filled.Devices,
        unselectedIcon = Icons.Outlined.Devices
    )

    data object CloudinaryMedia : Screen(
        route = "cloudinary_media",
        titleEn = "Uploader",
        titleBn = "মিডিয়া আপলোড",
        selectedIcon = Icons.Filled.CloudUpload,
        unselectedIcon = Icons.Outlined.CloudUpload
    )

    data object Settings : Screen(
        route = "settings",
        titleEn = "Settings",
        titleBn = "সেটিংস",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    val navBarLabel: String
        get() = when (this) {
            Dashboard -> "হোম"
            CourseSales -> "কোর্স"
            MaxbirdWebsiteAdmin -> "ওয়েবসাইট"
            CloudinaryMedia -> "মিডিয়া"
            Devices -> "ডিভাইস"
            Settings -> "সেটিংস"
            Notices -> "নোটিশ"
            Updates -> "আপডেট"
            SupportLinks -> "সাপোর্ট"
        }

    companion object {
        // Core 5 primary destinations including the dedicated Maxbird Website Admin tab
        val bottomNavItems: List<Screen>
            get() = listOf(Dashboard, Devices, CourseSales, MaxbirdWebsiteAdmin, Settings)
    }
}



