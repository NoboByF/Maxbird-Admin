package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.HeadsetMic
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

    data object Generator : Screen(
        route = "generator",
        titleEn = "New Code",
        titleBn = "নতুন কোড",
        selectedIcon = Icons.Filled.AddModerator,
        unselectedIcon = Icons.Outlined.AddModerator
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

    data object Codes : Screen(
        route = "codes",
        titleEn = "Students",
        titleBn = "কোডসমূহ",
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group
    )

    data object Devices : Screen(
        route = "devices",
        titleEn = "Devices",
        titleBn = "ডিভাইস",
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
            CourseSales -> "কিস্তি ও বিক্রি"
            Codes -> "কোডসমূহ"
            CloudinaryMedia -> "মিডিয়া"
            Devices -> "ডিভাইস"
            Settings -> "সেটিংস"
            Generator -> "নতুন কোড"
            Notices -> "নোটিশ"
            Updates -> "আপডেট"
            SupportLinks -> "সাপোর্ট"
        }

    companion object {
        // Core 5 primary destinations for clean, spacious Material 3 NavigationBar
        val bottomNavItems: List<Screen>
            get() = listOf(Dashboard, CourseSales, Codes, Devices, Settings)
    }
}


