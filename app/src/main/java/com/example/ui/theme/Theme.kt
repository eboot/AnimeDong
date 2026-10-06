package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AnimeDongDarkColorScheme = darkColorScheme(
    primary = AnimeDongGold,
    onPrimary = AnimeDongBg,
    primaryContainer = AnimeDongGoldDark,
    onPrimaryContainer = AnimeDongTextPrimary,
    secondary = AnimeDongOrange,
    onSecondary = AnimeDongBg,
    secondaryContainer = AnimeDongCard,
    onSecondaryContainer = AnimeDongTextPrimary,
    tertiary = AnimeDongCyan,
    onTertiary = AnimeDongBg,
    background = AnimeDongBg,
    onBackground = AnimeDongTextPrimary,
    surface = AnimeDongSurface,
    onSurface = AnimeDongTextPrimary,
    surfaceVariant = AnimeDongCard,
    onSurfaceVariant = AnimeDongTextSecondary,
    outline = AnimeDongCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = AnimeDongDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = AnimeDongBg.toArgb()
                it.navigationBarColor = AnimeDongBg.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
