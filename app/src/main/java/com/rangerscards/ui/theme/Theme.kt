package com.rangerscards.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object CustomTheme {
    val typography: CustomTypography
        @ReadOnlyComposable
        @Composable
        get() = LocalCustomTypography.current
    val colors: CustomColors
        @ReadOnlyComposable
        @Composable
        get() = LocalCustomColors.current
    val shapes: CustomShape
        @ReadOnlyComposable
        @Composable
        get() = LocalCustomShapes.current
}
@Composable
fun RangersCardsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.apply {
                statusBarColor = colorScheme.l30.toArgb()
                navigationBarColor = colorScheme.l30.toArgb()
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(
        LocalCustomColors provides colorScheme,
        LocalCustomTypography provides CustomTheme.typography,
        LocalCustomShapes provides CustomTheme.shapes,
        content = content
    )
}