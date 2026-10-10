package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Group
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

    data object Settings : Screen(
        route = "settings",
        titleEn = "Settings",
        titleBn = "সেটিংস",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        // Use custom getter to prevent Kotlin static initialization circular dependency null-reference
        val bottomNavItems: List<Screen>
            get() = listOf(Dashboard, Notices, Updates, Codes, Devices, Settings)
    }
}
