package com.animedong.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema AnimeDong mengikuti desain referensi: aksen kuning emas
 * di atas background navy sangat gelap.
 */
private val AnimeDongColors = darkColorScheme(
    primary = Color(0xFFFFC107),
    onPrimary = Color(0xFF201A00),
    primaryContainer = Color(0xFF3A2E00),
    onPrimaryContainer = Color(0xFFFFD54F),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF201A00),
    tertiary = Color(0xFFFF8A00),
    background = Color(0xFF0E1218),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1A2130),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF232C3D),
    onSurfaceVariant = Color(0xFF8A94A6),
    outline = Color(0xFF2A3446),
)

@Composable
fun AnimeDongTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AnimeDongColors,
        content = content
    )
}
