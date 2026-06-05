package com.odak.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Sage,
    onPrimary = Cream,
    primaryContainer = SageContainer,
    onPrimaryContainer = SageDark,
    secondary = Clay,
    onSecondary = Cream,
    secondaryContainer = ClayContainer,
    onSecondaryContainer = Charcoal,
    background = Cream,
    onBackground = Charcoal,
    surface = CreamSurface,
    onSurface = Charcoal,
    surfaceVariant = SageContainer,
    onSurfaceVariant = MutedText,
    outline = SageLight
)

private val DarkColors = darkColorScheme(
    primary = SageNight,
    onPrimary = NightBg,
    primaryContainer = SageDark,
    onPrimaryContainer = SageContainer,
    secondary = Clay,
    onSecondary = NightBg,
    background = NightBg,
    onBackground = NightOnSurface,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightSurface,
    onSurfaceVariant = SageLight,
    outline = SageDark
)

@Composable
fun OdakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
