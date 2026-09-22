package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GamiIndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = GamiIndigoDark,
    onPrimaryContainer = GamiTextPrimary,
    secondary = GamiCyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E7490),
    onSecondaryContainer = Color.White,
    tertiary = GamiMagentaAccent,
    onTertiary = Color.White,
    background = GamiBackgroundDark,
    onBackground = GamiTextPrimary,
    surface = GamiSurfaceDark,
    onSurface = GamiTextPrimary,
    surfaceVariant = GamiSurfaceVariant,
    onSurfaceVariant = GamiTextSecondary,
    outline = GamiBorderDark,
    error = GamiError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GamiIndigoDark,
    onPrimary = Color.White,
    primaryContainer = GamiIndigoLight,
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = GamiCyanAccent,
    onSecondary = Color.White,
    tertiary = GamiMagentaAccent,
    onTertiary = Color.White,
    background = Color(0xFF0F1221),
    onBackground = GamiTextPrimary,
    surface = Color(0xFF161A30),
    onSurface = GamiTextPrimary,
    surfaceVariant = Color(0xFF222845),
    onSurfaceVariant = GamiTextSecondary,
    outline = Color(0xFF333E68),
    error = GamiError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek live-entertainment dark theme
    dynamicColor: Boolean = false, // Keep brand aesthetic consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

