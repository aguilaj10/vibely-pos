package com.vibely.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.common_active
import vibely_pos.composeapp.generated.resources.common_inactive

enum class StatusChipVariant {
    Active,
    Inactive,
    Success,
    Warning,
    Error,
    Info,
    StockLow,
    StockMedium,
    StockGood,
}

enum class StatusChipSize {
    Small,
    Medium,
}

@Composable
fun StatusChip(label: String, variant: StatusChipVariant, modifier: Modifier = Modifier, size: StatusChipSize = StatusChipSize.Medium) {
    val (backgroundColor, textColor) = getVariantColors(variant)
    val paddingHorizontal = if (size == StatusChipSize.Small) 8.dp else 12.dp
    val paddingVertical = if (size == StatusChipSize.Small) 2.dp else 4.dp
    val textStyle =
        if (size == StatusChipSize.Small) {
            MaterialTheme.typography.labelSmall
        } else {
            MaterialTheme.typography.labelMedium
        }

    Box(
        modifier =
        modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(100.dp),
            ).padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = textStyle,
            color = textColor,
        )
    }
}

@Composable
private fun getVariantColors(variant: StatusChipVariant): Pair<Color, Color> = when (variant) {
    StatusChipVariant.Active -> AppColors.SuccessLight.copy(alpha = 0.15f) to AppColors.SuccessDark
    StatusChipVariant.Inactive -> AppColors.NeutralLight400.copy(alpha = 0.15f) to AppColors.NeutralLight600
    StatusChipVariant.Success -> AppColors.SuccessLight.copy(alpha = 0.15f) to AppColors.SuccessDark
    StatusChipVariant.Warning -> AppColors.WarningLight.copy(alpha = 0.15f) to AppColors.WarningDark
    StatusChipVariant.Error -> AppColors.ErrorLight.copy(alpha = 0.15f) to AppColors.ErrorDark
    StatusChipVariant.Info -> AppColors.InfoLight.copy(alpha = 0.15f) to AppColors.InfoDark
    StatusChipVariant.StockLow -> AppColors.ErrorLight.copy(alpha = 0.15f) to AppColors.ErrorDark
    StatusChipVariant.StockMedium -> AppColors.WarningLight.copy(alpha = 0.15f) to AppColors.WarningDark
    StatusChipVariant.StockGood -> AppColors.SuccessLight.copy(alpha = 0.15f) to AppColors.SuccessDark
}

private fun getVariantDotColor(variant: StatusChipVariant): Color = when (variant) {
    StatusChipVariant.Active, StatusChipVariant.Success, StatusChipVariant.StockGood -> AppColors.SuccessDark
    StatusChipVariant.Warning, StatusChipVariant.StockMedium -> AppColors.WarningDark
    StatusChipVariant.Error, StatusChipVariant.StockLow -> AppColors.ErrorDark
    StatusChipVariant.Info -> AppColors.InfoDark
    StatusChipVariant.Inactive -> AppColors.NeutralLight600
}

private const val STATUS_DOT_SIZE_DP = 10

/**
 * Compact status indicator using a colored dot instead of a text label.
 *
 * Use in compact (phone) layouts where horizontal space is limited.
 * Semantically equivalent to [StatusChip] but renders as a 10dp filled circle.
 *
 * @param variant Determines the dot color, following the same semantic mapping as [StatusChip].
 * @param contentDescription Accessibility label read by screen readers (e.g., "Completed").
 * @param modifier Optional modifier for customization.
 */
@Composable
fun StatusDot(variant: StatusChipVariant, contentDescription: String, modifier: Modifier = Modifier) {
    val color = getVariantDotColor(variant)
    Box(
        modifier = modifier
            .size(STATUS_DOT_SIZE_DP.dp)
            .background(color = color, shape = CircleShape)
            .semantics { this.contentDescription = contentDescription },
    )
}

@Composable
fun ActiveStatusChip(modifier: Modifier = Modifier) {
    StatusChip(
        label = stringResource(Res.string.common_active),
        variant = StatusChipVariant.Active,
        modifier = modifier,
    )
}

@Composable
fun InactiveStatusChip(modifier: Modifier = Modifier) {
    StatusChip(
        label = stringResource(Res.string.common_inactive),
        variant = StatusChipVariant.Inactive,
        modifier = modifier,
    )
}

@Composable
fun StockStatusChip(stockLevel: StockLevel, modifier: Modifier = Modifier) {
    val (label, variant) =
        when (stockLevel) {
            StockLevel.Low -> "Low Stock" to StatusChipVariant.StockLow
            StockLevel.Medium -> "Medium Stock" to StatusChipVariant.StockMedium
            StockLevel.Good -> "In Stock" to StatusChipVariant.StockGood
        }
    StatusChip(label = label, variant = variant, modifier = modifier)
}

enum class StockLevel {
    Low,
    Medium,
    Good,
}
