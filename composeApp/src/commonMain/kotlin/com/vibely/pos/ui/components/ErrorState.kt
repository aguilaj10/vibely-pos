package com.vibely.pos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExclamationCircle
import compose.icons.fontawesomeicons.solid.Sync
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.error_something_went_wrong

/**
 * Reusable error state component for Vibely POS.
 * Displays a user-friendly error message with a retry button.
 *
 * @param message Error message to display (default: "Something went wrong")
 * @param onRetry Retry button click handler
 * @param modifier Modifier for customization
 * @param icon Optional custom error icon (defaults to FontAwesome CircleExclamation)
 * @param title Optional custom title (defaults to "Oops! Something went wrong")
 * @param retryButtonText Custom text for retry button (default: "Try Again")
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String? = null,
    retryButtonText: String = "Try Again",
) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Error icon
        Icon(
            imageVector = icon ?: FontAwesomeIcons.Solid.ExclamationCircle,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = AppColors.Error,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = title ?: stringResource(Res.string.error_something_went_wrong),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Error message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Retry button
        AppButton(
            text = retryButtonText,
            onClick = onRetry,
            style = AppButtonStyle.Primary,
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

/**
 * Compact error state for smaller spaces.
 * Displays error icon and message without the full layout.
 *
 * @param message Error message to display
 * @param onRetry Retry button click handler
 * @param modifier Modifier for customization
 */
@Composable
fun ErrorStateCompact(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ErrorState(
        message = message,
        onRetry = onRetry,
        modifier = modifier,
        title = null,
    )
}
