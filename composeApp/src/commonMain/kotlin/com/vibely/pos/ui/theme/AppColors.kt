package com.vibely.pos.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Custom color palette for Vibely POS
 * Updated to match Figma design - Near-black primary with light theme
 */
object AppColors {

    // Primary Brand Colors - Near-black (Figma design)
    val Primary = Color(0xFF111827) // Near-black - Main brand color
    val PrimaryLight = Color(0xFF1F2937) // Lighter near-black
    val PrimaryDark = Color(0xFF0B0F1A) // Darker near-black
    val OnPrimary = Color.White

    // Secondary Colors - Grayscale for secondary actions (design system)
    val Secondary = Color(0xFF6B7280) // Medium gray - Secondary actions
    val SecondaryLight = Color(0xFF9CA3AF) // Light gray
    val SecondaryDark = Color(0xFF4B5563) // Dark gray
    val OnSecondary = Color.White

    // Tertiary Colors - Warm Accent (kept for warnings/highlights)
    val Tertiary = Color(0xFFB45309) // Dark amber - Energy, attention (darkened for WCAG AA)
    val TertiaryLight = Color(0xFFFBBF24)
    val TertiaryDark = Color(0xFF92400E)
    val OnTertiary = Color.White

    // Neutral Colors - Light Mode
    val NeutralLight100 = Color(0xFFFAFAFA)
    val NeutralLight200 = Color(0xFFF5F5F5)
    val NeutralLight300 = Color(0xFFE5E7EB) // Subtle borders (Figma)
    val NeutralLight400 = Color(0xFFD1D5DB)
    val NeutralLight500 = Color(0xFF9CA3AF)
    val NeutralLight600 = Color(0xFF6B7280)
    val NeutralLight700 = Color(0xFF4B5563)
    val NeutralLight800 = Color(0xFF374151)
    val NeutralLight900 = Color(0xFF1F2937)

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

    // Status Colors - Success Green
    val Success = Color(0xFF15803D) // Darker green - darkened for WCAG AA
    val SuccessLight = Color(0xFF22C55E)
    val SuccessDark = Color(0xFF166534)
    val OnSuccess = Color.White

    // Status Colors - Warning Amber
    val Warning = Color(0xFFB45309) // Dark amber - darkened for WCAG AA
    val WarningLight = Color(0xFFFBBF24)
    val WarningDark = Color(0xFF92400E)
    val OnWarning = Color.White

    // Status Colors - Error Red
    val Error = Color(0xFFDC2626) // Darker red - darkened for WCAG AA
    val ErrorLight = Color(0xFFF87171)
    val ErrorDark = Color(0xFFB91C1C)
    val OnError = Color.White

    // Status Colors - Info Blue
    val Info = Color(0xFF1D4ED8) // Darker blue - darkened for WCAG AA
    val InfoLight = Color(0xFF3B82F6)
    val InfoDark = Color(0xFF1E40AF)
    val OnInfo = Color.White

    // Accent Colors for Icons (Figma)
    val AccentGreen = Color(0xFF15803D) // Darker green - darkened for WCAG AA
    val AccentBlue = Color(0xFF1D4ED8) // Darker blue - darkened for WCAG AA
    val AccentPurple = Color(0xFF7C3AED) // Purple
    val AccentOrange = Color(0xFFB45309) // Dark amber - darkened for WCAG AA

    // Surface Colors - Light Mode (Figma: White with subtle borders)
    val SurfaceLight = Color.White
    val SurfaceDark = Color(0xFF1E1E1E)
    val SurfaceVariantLight = Color(0xFFF9FAFB) // Slightly off-white
    val SurfaceVariantDark = Color(0xFF2A2A2A)

    // Background Colors - Light Mode (Figma: Off-white)
    val BackgroundLight = Color(0xFFF8FAFC) // Off-white #F8FAFC
    val BackgroundDark = Color(0xFF1E1E1E) // Dark gray (proper dark mode)

    // Outline Colors (Figma: Light gray #E5E7EB)
    val OutlineLight = Color(0xFFE5E7EB)
    val OutlineDark = Color(0xFF374151)

    // Text Colors - Light Mode
    val TextPrimaryLight = Color(0xFF111827) // Near-black
    val TextSecondaryLight = Color(0xFF6B7280) // Gray
    val TextDisabledLight = Color(0xFF9CA3AF)

    // Text Colors - Dark Mode
    val TextPrimaryDark = Color(0xFFF9FAFB)
    val TextSecondaryDark = Color(0xFF9CA3AF)
    val TextDisabledDark = Color(0xFF6B7280)
}

/**
 * Light color scheme for Vibely POS (Figma design)
 */
val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.NeutralLight200,
    onPrimaryContainer = AppColors.PrimaryDark,

    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.NeutralLight200,
    onSecondaryContainer = AppColors.SecondaryDark,

    tertiary = AppColors.Tertiary,
    onTertiary = AppColors.OnTertiary,
    tertiaryContainer = AppColors.WarningLight.copy(alpha = 0.15f),
    onTertiaryContainer = AppColors.WarningDark,

    error = AppColors.Error,
    onError = AppColors.OnError,
    errorContainer = AppColors.ErrorLight.copy(alpha = 0.15f),
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
 * Follows same design language as light theme
 */
val DarkColorScheme = darkColorScheme(
    primary = AppColors.NeutralDark900,
    onPrimary = AppColors.PrimaryDark,
    primaryContainer = AppColors.PrimaryDark,
    onPrimaryContainer = AppColors.NeutralDark900,

    secondary = AppColors.SecondaryLight,
    onSecondary = AppColors.SecondaryDark,
    secondaryContainer = AppColors.NeutralDark300,
    onSecondaryContainer = AppColors.SecondaryLight,

    tertiary = AppColors.TertiaryLight,
    onTertiary = AppColors.TertiaryDark,
    tertiaryContainer = AppColors.TertiaryDark,
    onTertiaryContainer = AppColors.TertiaryLight,

    error = AppColors.ErrorLight,
    onError = AppColors.ErrorDark,
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
