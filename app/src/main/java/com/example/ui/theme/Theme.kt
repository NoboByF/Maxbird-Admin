package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MaxBirdColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = White,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = Slate100,
    secondary = NeonCyan,
    onSecondary = Slate950,
    secondaryContainer = Slate700,
    onSecondaryContainer = CyanGlow,
    tertiary = PurpleRoyal,
    onTertiary = White,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    error = CoralDanger,
    onError = White,
    errorContainer = CoralDark,
    onErrorContainer = Slate100,
    outline = Slate600,
    outlineVariant = Slate700
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Enforce our custom MaxBird Admin slate theme for branding consistency
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaxBirdColorScheme,
        typography = Typography,
        content = content
    )
}
