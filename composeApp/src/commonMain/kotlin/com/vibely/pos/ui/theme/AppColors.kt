package com.vibely.pos.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Custom color palette for Vibely POS
 * Designed for professional retail/restaurant environments
 */
object AppColors {

    // Primary Brand Colors - Professional Blue/Purple
    val Primary = Color(0xFF6366F1) // Indigo - Trust, professionalism
    val PrimaryLight = Color(0xFF818CF8)
    val PrimaryDark = Color(0xFF4F46E5)
    val OnPrimary = Color.White

    // Secondary Colors - Complementary Green
    val Secondary = Color(0xFF10B981) // Emerald - Success, growth
    val SecondaryLight = Color(0xFF34D399)
    val SecondaryDark = Color(0xFF059669)
    val OnSecondary = Color.White

    // Tertiary Colors - Warm Accent
    val Tertiary = Color(0xFFF59E0B) // Amber - Energy, attention
    val TertiaryLight = Color(0xFFFBBF24)
    val TertiaryDark = Color(0xFFD97706)
    val OnTertiary = Color.White

    // Neutral Colors - Light Mode
    val NeutralLight100 = Color(0xFFFAFAFA)
    val NeutralLight200 = Color(0xFFF5F5F5)
    val NeutralLight300 = Color(0xFFE5E5E5)
    val NeutralLight400 = Color(0xFFD4D4D4)
    val NeutralLight500 = Color(0xFFA3A3A3)
    val NeutralLight600 = Color(0xFF737373)
    val NeutralLight700 = Color(0xFF525252)
    val NeutralLight800 = Color(0xFF404040)
    val NeutralLight900 = Color(0xFF262626)

    // Neutral Colors - Dark Mode
    val NeutralDark100 = Color(0xFF1E1E1E)
    val NeutralDark200 = Color(0xFF2A2A2A)
    val NeutralDark300 = Color(0xFF3A3A3A)
    val NeutralDark400 = Color(0xFF4A4A4A)
    val NeutralDark500 = Color(0xFF6A6A6A)
    val NeutralDark600 = Color(0xFF8A8A8A)
    val NeutralDark700 = Color(0xFFAAAAAA)
    val NeutralDark800 = Color(0xFFCACACA)
    val NeutralDark900 = Color(0xFFEAEAEA)

    // Status Colors
    val Success = Color(0xFF10B981) // Green
    val SuccessLight = Color(0xFF34D399)
    val SuccessDark = Color(0xFF059669)
    val OnSuccess = Color.White

    val Warning = Color(0xFFF59E0B) // Amber
    val WarningLight = Color(0xFFFBBF24)
    val WarningDark = Color(0xFFD97706)
    val OnWarning = Color.White

    val Error = Color(0xFFEF4444) // Red
    val ErrorLight = Color(0xFFF87171)
    val ErrorDark = Color(0xFFDC2626)
    val OnError = Color.White

    val Info = Color(0xFF3B82F6) // Blue
    val InfoLight = Color(0xFF60A5FA)
    val InfoDark = Color(0xFF2563EB)
    val OnInfo = Color.White

    // Surface Colors
    val SurfaceLight = Color.White
    val SurfaceDark = Color(0xFF1E1E1E)
    val SurfaceVariantLight = Color(0xFFF5F5F5)
    val SurfaceVariantDark = Color(0xFF2A2A2A)

    // Background Colors
    val BackgroundLight = Color(0xFFFAFAFA)
    val BackgroundDark = Color(0xFF121212)

    // Outline Colors
    val OutlineLight = Color(0xFFE5E5E5)
    val OutlineDark = Color(0xFF3A3A3A)

    // Text Colors
    val TextPrimaryLight = Color(0xFF262626)
    val TextSecondaryLight = Color(0xFF737373)
    val TextDisabledLight = Color(0xFFA3A3A3)

    val TextPrimaryDark = Color(0xFFEAEAEA)
    val TextSecondaryDark = Color(0xFF8A8A8A)
    val TextDisabledDark = Color(0xFF6A6A6A)
}

/**
 * Light color scheme for Vibely POS
 */
val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.PrimaryLight,
    onPrimaryContainer = AppColors.PrimaryDark,

    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.SecondaryLight,
    onSecondaryContainer = AppColors.SecondaryDark,

    tertiary = AppColors.Tertiary,
    onTertiary = AppColors.OnTertiary,
    tertiaryContainer = AppColors.TertiaryLight,
    onTertiaryContainer = AppColors.TertiaryDark,

    error = AppColors.Error,
    onError = AppColors.OnError,
    errorContainer = AppColors.ErrorLight,
    onErrorContainer = AppColors.ErrorDark,

    background = AppColors.BackgroundLight,
    onBackground = AppColors.TextPrimaryLight,

    surface = AppColors.SurfaceLight,
    onSurface = AppColors.TextPrimaryLight,
    surfaceVariant = AppColors.SurfaceVariantLight,
    onSurfaceVariant = AppColors.TextSecondaryLight,

    outline = AppColors.OutlineLight,
    outlineVariant = AppColors.NeutralLight300,

    scrim = Color.Black.copy(alpha = 0.32f),
)

/**
 * Dark color scheme for Vibely POS
 */
val DarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryLight,
    onPrimary = AppColors.NeutralDark900,
    primaryContainer = AppColors.PrimaryDark,
    onPrimaryContainer = AppColors.PrimaryLight,

    secondary = AppColors.SecondaryLight,
    onSecondary = AppColors.NeutralDark900,
    secondaryContainer = AppColors.SecondaryDark,
    onSecondaryContainer = AppColors.SecondaryLight,

    tertiary = AppColors.TertiaryLight,
    onTertiary = AppColors.NeutralDark900,
    tertiaryContainer = AppColors.TertiaryDark,
    onTertiaryContainer = AppColors.TertiaryLight,

    error = AppColors.ErrorLight,
    onError = AppColors.NeutralDark900,
    errorContainer = AppColors.ErrorDark,
    onErrorContainer = AppColors.ErrorLight,

    background = AppColors.BackgroundDark,
    onBackground = AppColors.TextPrimaryDark,

    surface = AppColors.SurfaceDark,
    onSurface = AppColors.TextPrimaryDark,
    surfaceVariant = AppColors.SurfaceVariantDark,
    onSurfaceVariant = AppColors.TextSecondaryDark,

    outline = AppColors.OutlineDark,
    outlineVariant = AppColors.NeutralDark400,

    scrim = Color.Black.copy(alpha = 0.5f),
)
