package com.vibely.pos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Local composition for accessing custom theme properties
 */
val LocalAppColors = staticCompositionLocalOf { AppColors }

/**
 * Main theme composable for Vibely POS
 * Wraps Material3 theme with custom colors, typography, and shapes
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param content The content to be themed
 */
@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    CompositionLocalProvider(
        LocalAppColors provides AppColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/**
 * Access custom colors from any composable
 * Usage: val colors = appColors()
 */
@Composable
fun appColors() = LocalAppColors.current

/**
 * Extension properties for easier access to custom text styles
 */
object AppTheme {
    val typography
        @Composable
        get() = AppTypography

    val shapes
        @Composable
        get() = AppShapes

    val colors: AppColors
        @Composable
        get() = appColors()

    val posTextStyles: PosTextStyles
        @Composable
        get() = PosTextStyles

    val posShapes: PosShapes
        @Composable
        get() = PosShapes
}
