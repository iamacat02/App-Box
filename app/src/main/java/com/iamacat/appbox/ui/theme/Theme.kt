package com.iamacat.appbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.iamacat.appbox.data.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = GreenifyColors.Primary,
    onPrimary = GreenifyColors.White,
    primaryContainer = GreenifyColors.LightGreen,
    onPrimaryContainer = Color(0xFF06400C),
    secondary = GreenifyColors.PrimaryDark,
    onSecondary = GreenifyColors.White,
    background = GreenifyColors.Background,
    onBackground = GreenifyColors.TextPrimary,
    surface = GreenifyColors.Background,
    onSurface = GreenifyColors.TextPrimary,
    surfaceVariant = GreenifyColors.SectionHeaderBackground,
    onSurfaceVariant = GreenifyColors.TextSecondary,
    outline = GreenifyColors.Divider,
    error = GreenifyColors.Destructive,
    onError = GreenifyColors.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5BD469),
    onPrimary = Color(0xFF00390A),
    primaryContainer = Color(0xFF0B4D12),
    onPrimaryContainer = Color(0xFFB7F0B8),
    secondary = Color(0xFF8FD695),
    onSecondary = Color(0xFF00390A),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF1B1B1B),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF404040),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF3B0907)
)

@Composable
fun AppBoxTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = themeMode == ThemeMode.SYSTEM && isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
