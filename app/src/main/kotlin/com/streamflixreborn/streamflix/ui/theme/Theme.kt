package com.streamflixreborn.streamflix.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE50914),      // Netflix red
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB20710),
    secondary = Color(0xFF564D4D),
    background = Color(0xFF141414),   // Dark background
    surface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFF2A2A2A),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB3B3B3),
    error = Color(0xFFCF6679),
    outline = Color(0xFF404040),
)

@Composable
fun StreamflixTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
