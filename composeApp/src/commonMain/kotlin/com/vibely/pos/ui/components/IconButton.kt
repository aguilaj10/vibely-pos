package com.vibely.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors

enum class AppIconButtonVariant {
    Default,
    Primary,
    Success,
    Warning,
    Error,
    Ghost,
}

enum class AppIconButtonShape {
    Circle,
    Square,
}

enum class AppIconButtonSize {
    Small,
    Medium,
    Large,
}

@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppIconButtonVariant = AppIconButtonVariant.Default,
    shape: AppIconButtonShape = AppIconButtonShape.Circle,
    size: AppIconButtonSize = AppIconButtonSize.Medium,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

    val (containerColor, iconColor) = getButtonColors(variant, enabled, isPressed.value)
    val iconSize = getIconSize(size)
    val buttonSize = getButtonSize(size)
    val cornerRadius = when (shape) {
        AppIconButtonShape.Circle -> 100.dp
        AppIconButtonShape.Square -> 8.dp
    }

    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .then(
                if (variant == AppIconButtonVariant.Default && !enabled) {
                    Modifier.border(1.dp, AppColors.OutlineLight, RoundedCornerShape(cornerRadius))
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) iconColor else iconColor.copy(alpha = 0.5f),
        )
    }
}

@Composable
fun AppIconButton(
    text: String,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppIconButtonVariant = AppIconButtonVariant.Default,
    shape: AppIconButtonShape = AppIconButtonShape.Square,
    size: AppIconButtonSize = AppIconButtonSize.Medium,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

    val (containerColor, contentColor) = getButtonColors(variant, enabled, isPressed.value)
    val buttonSize = getButtonSize(size)
    val cornerRadius = when (shape) {
        AppIconButtonShape.Circle -> 100.dp
        AppIconButtonShape.Square -> 8.dp
    }

    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun getButtonColors(variant: AppIconButtonVariant, enabled: Boolean, isPressed: Boolean): Pair<Color, Color> {
    val pressedAlpha = 0.1f
    val disabledAlpha = 0.5f

    return when (variant) {
        AppIconButtonVariant.Default -> {
            val bg = if (!enabled) {
                AppColors.NeutralLight100
            } else if (isPressed) {
                AppColors.NeutralLight300.copy(alpha = pressedAlpha)
            } else {
                AppColors.SurfaceLight
            }
            val icon = if (!enabled) {
                AppColors.NeutralLight500
            } else {
                AppColors.TextPrimaryLight
            }
            bg to icon
        }
        AppIconButtonVariant.Primary -> {
            val bg = if (!enabled) {
                AppColors.Primary.copy(alpha = disabledAlpha)
            } else if (isPressed) {
                AppColors.Primary.copy(alpha = pressedAlpha)
            } else {
                AppColors.Primary.copy(alpha = 0.1f)
            }
            val icon = if (!enabled) AppColors.Primary.copy(alpha = disabledAlpha) else AppColors.Primary
            bg to icon
        }
        AppIconButtonVariant.Success -> {
            val bg = if (!enabled) {
                AppColors.Success.copy(alpha = disabledAlpha)
            } else if (isPressed) {
                AppColors.Success.copy(alpha = pressedAlpha)
            } else {
                AppColors.Success.copy(alpha = 0.1f)
            }
            val icon = if (!enabled) AppColors.Success.copy(alpha = disabledAlpha) else AppColors.Success
            bg to icon
        }
        AppIconButtonVariant.Warning -> {
            val bg = if (!enabled) {
                AppColors.Warning.copy(alpha = disabledAlpha)
            } else if (isPressed) {
                AppColors.Warning.copy(alpha = pressedAlpha)
            } else {
                AppColors.Warning.copy(alpha = 0.1f)
            }
            val icon = if (!enabled) AppColors.Warning.copy(alpha = disabledAlpha) else AppColors.Warning
            bg to icon
        }
        AppIconButtonVariant.Error -> {
            val bg = if (!enabled) {
                AppColors.Error.copy(alpha = disabledAlpha)
            } else if (isPressed) {
                AppColors.Error.copy(alpha = pressedAlpha)
            } else {
                AppColors.Error.copy(alpha = 0.1f)
            }
            val icon = if (!enabled) AppColors.Error.copy(alpha = disabledAlpha) else AppColors.Error
            bg to icon
        }
        AppIconButtonVariant.Ghost -> {
            Color.Transparent to AppColors.TextPrimaryLight
        }
    }
}

private fun getIconSize(size: AppIconButtonSize): Dp = when (size) {
    AppIconButtonSize.Small -> 16.dp
    AppIconButtonSize.Medium -> 20.dp
    AppIconButtonSize.Large -> 24.dp
}

private fun getButtonSize(size: AppIconButtonSize): Dp = when (size) {
    AppIconButtonSize.Small -> 28.dp
    AppIconButtonSize.Medium -> 36.dp
    AppIconButtonSize.Large -> 44.dp
}
