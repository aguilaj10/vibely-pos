package com.vibely.pos.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.theme.PosShapes

/**
 * Button style variants for Vibely POS
 */
enum class AppButtonStyle {
    Primary, // Main actions (e.g., "Complete Sale")
    Secondary, // Secondary actions (e.g., "Add to Cart")
    Tertiary, // Tertiary actions (e.g., "View Details")
    Destructive, // Destructive actions (e.g., "Delete", "Cancel Order")
    Outlined, // Outlined style for less prominent actions
    Text, // Text-only buttons for minimal emphasis
}

/**
 * Button size variants
 */
enum class AppButtonSize {
    Small, // Compact buttons
    Medium, // Standard buttons
    Large, // Prominent buttons (e.g., checkout)
}

/**
 * Custom button component for Vibely POS
 * Provides consistent button styling across the application
 *
 * @param text Button text
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param style Button style variant
 * @param size Button size variant
 * @param enabled Whether the button is enabled
 * @param loading Whether to show loading indicator
 * @param icon Optional leading icon
 * @param shape Button shape (defaults to ActionButton shape)
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    size: AppButtonSize = AppButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
    shape: Shape = PosShapes.ActionButton,
) {
    val colors = getButtonColors(style)
    val contentPadding = getButtonPadding(size)
    val textStyle = getButtonTextStyle(size)

    when (style) {
        AppButtonStyle.Primary, AppButtonStyle.Secondary,
        AppButtonStyle.Tertiary, AppButtonStyle.Destructive,
        -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    loading = loading,
                    textStyle = textStyle,
                )
            }
        }
        AppButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    loading = loading,
                    textStyle = textStyle,
                )
            }
        }
        AppButtonStyle.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = colors,
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    loading = loading,
                    textStyle = textStyle,
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(text: String, icon: (@Composable () -> Unit)?, loading: Boolean, textStyle: TextStyle) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(8.dp))
    } else if (icon != null) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
        text = text,
        style = textStyle,
    )
}

@Composable
private fun getButtonColors(style: AppButtonStyle): ButtonColors = when (style) {
    AppButtonStyle.Primary -> ButtonDefaults.buttonColors(
        containerColor = AppColors.Primary,
        contentColor = AppColors.OnPrimary,
    )
    AppButtonStyle.Secondary -> ButtonDefaults.buttonColors(
        containerColor = AppColors.Secondary,
        contentColor = AppColors.OnSecondary,
    )
    AppButtonStyle.Tertiary -> ButtonDefaults.buttonColors(
        containerColor = AppColors.Tertiary,
        contentColor = AppColors.OnTertiary,
    )
    AppButtonStyle.Destructive -> ButtonDefaults.buttonColors(
        containerColor = AppColors.Error,
        contentColor = AppColors.OnError,
    )
    AppButtonStyle.Outlined -> ButtonDefaults.outlinedButtonColors(
        contentColor = AppColors.Primary,
    )
    AppButtonStyle.Text -> ButtonDefaults.textButtonColors(
        contentColor = AppColors.Primary,
    )
}

@Composable
private fun getButtonPadding(size: AppButtonSize): PaddingValues = when (size) {
    AppButtonSize.Small -> PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    AppButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    AppButtonSize.Large -> PaddingValues(horizontal = 28.dp, vertical = 16.dp)
}

@Composable
private fun getButtonTextStyle(size: AppButtonSize): TextStyle = when (size) {
    AppButtonSize.Small -> MaterialTheme.typography.labelSmall
    AppButtonSize.Medium -> MaterialTheme.typography.labelLarge
    AppButtonSize.Large -> MaterialTheme.typography.titleMedium
}
