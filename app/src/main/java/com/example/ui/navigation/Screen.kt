package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
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
        titleEn = "Dashboard",
        titleBn = "ড্যাশবোর্ড",
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

    data object Codes : Screen(
        route = "codes",
        titleEn = "Students",
        titleBn = "শিক্ষার্থী তালিকা",
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group
    )

    data object Devices : Screen(
        route = "devices",
        titleEn = "Kill-Switch",
        titleBn = "ডিভাইস কন্ট্রোল",
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
            get() = listOf(Dashboard, Generator, Codes, Devices, Settings)
    }
}
