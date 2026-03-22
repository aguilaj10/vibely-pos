package com.vibely.pos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val dimensions = if (maxWidth < COMPACT_WIDTH_THRESHOLD) CompactDimensions else ExpandedDimensions
        CompositionLocalProvider(
            LocalAppColors provides AppColors,
            LocalAppDimensions provides dimensions,
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = AppTypography,
                shapes = AppShapes,
                content = content,
            )
        }
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

    val dimensions: AppDimensions
        @Composable
        get() = LocalAppDimensions.current

    val posTextStyles: PosTextStyles
        @Composable
        get() = PosTextStyles

    val posShapes: PosShapes
        @Composable
        get() = PosShapes
}
