package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CosmicDarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    secondary = NeonIndigo,
    tertiary = PremiumCoral,
    background = Slate900,
    surface = Slate800,
    onPrimary = Slate900,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = Slate700,
    onSurfaceVariant = TextWhite,
    outline = Slate600
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark mode for a premium, unified high-end AI dashboard feel
    dynamicColor: Boolean = false, // Disable dynamic colors to maintain our custom visual branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
