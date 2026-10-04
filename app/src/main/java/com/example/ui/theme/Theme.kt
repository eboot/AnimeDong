package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DongHiveDarkColorScheme = darkColorScheme(
    primary = DongHiveGold,
    onPrimary = DongHiveBg,
    primaryContainer = DongHiveGoldDark,
    onPrimaryContainer = DongHiveTextPrimary,
    secondary = DongHiveOrange,
    onSecondary = DongHiveBg,
    secondaryContainer = DongHiveCard,
    onSecondaryContainer = DongHiveTextPrimary,
    tertiary = DongHiveCyan,
    onTertiary = DongHiveBg,
    background = DongHiveBg,
    onBackground = DongHiveTextPrimary,
    surface = DongHiveSurface,
    onSurface = DongHiveTextPrimary,
    surfaceVariant = DongHiveCard,
    onSurfaceVariant = DongHiveTextSecondary,
    outline = DongHiveCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // DongHive is primarily a dark cinema theme
    content: @Composable () -> Unit
) {
    val colorScheme = DongHiveDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = DongHiveBg.toArgb()
                it.navigationBarColor = DongHiveBg.toArgb()
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
