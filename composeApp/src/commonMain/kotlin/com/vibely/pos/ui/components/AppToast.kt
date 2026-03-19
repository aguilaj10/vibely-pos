package com.vibely.pos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.theme.PosShapes
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.common_close_icon

/**
 * Toast/Snackbar type variants
 */
enum class ToastType {
    Success,
    Error,
    Warning,
    Info,
}

/**
 * Toast duration in milliseconds
 */
enum class ToastDuration(val millis: Long) {
    Short(2000),
    Medium(3500),
    Long(5000),
}

/**
 * Toast state holder
 */
class ToastState {
    var isVisible by mutableStateOf(false)
        private set
    var message by mutableStateOf("")
        private set
    var type by mutableStateOf(ToastType.Info)
        private set

    suspend fun show(message: String, type: ToastType = ToastType.Info, duration: ToastDuration = ToastDuration.Medium) {
        this.message = message
        this.type = type
        this.isVisible = true
        delay(duration.millis)
        this.isVisible = false
    }

    fun dismiss() {
        isVisible = false
    }
}

/**
 * Remember toast state
 */
@Composable
fun rememberToastState(): ToastState = remember { ToastState() }

/**
 * Custom toast/snackbar component for Vibely POS
 * Provides consistent notification styling
 *
 * @param state Toast state holder
 * @param modifier Modifier for customization
 */
@Composable
fun AppToast(state: ToastState, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter =
        slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300),
        ) + fadeIn(animationSpec = tween(300)),
        exit =
        slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300),
        ) + fadeOut(animationSpec = tween(300)),
    ) {
        Surface(
            modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = PosShapes.Toast,
            color = getToastColor(state.type),
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Icon
                Text(
                    text = getToastIcon(state.type),
                    style = MaterialTheme.typography.titleLarge,
                )

                // Message
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = getToastTextColor(state.type),
                    modifier = Modifier.weight(1f),
                )

                // Close button
                TextButton(
                    onClick = { state.dismiss() },
                    colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = getToastTextColor(state.type),
                    ),
                ) {
                    Text(stringResource(Res.string.common_close_icon))
                }
            }
        }
    }
}

/**
 * Snackbar variant that appears at the bottom
 */
@Composable
fun AppSnackbar(
    message: String,
    type: ToastType = ToastType.Info,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
) {
    Snackbar(
        modifier = modifier,
        action =
        if (actionLabel != null && onActionClick != null) {
            {
                TextButton(onClick = onActionClick) {
                    Text(actionLabel)
                }
            }
        } else {
            null
        },
        dismissAction = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_close_icon))
            }
        },
        containerColor = getToastColor(type),
        contentColor = getToastTextColor(type),
        shape = PosShapes.Toast,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = getToastIcon(type))
            Text(text = message)
        }
    }
}

@Composable
private fun getToastColor(type: ToastType): Color = when (type) {
    ToastType.Success -> AppColors.Success
    ToastType.Error -> AppColors.Error
    ToastType.Warning -> AppColors.Warning
    ToastType.Info -> AppColors.Info
}

@Composable
private fun getToastTextColor(type: ToastType): Color = when (type) {
    ToastType.Success -> AppColors.OnSuccess
    ToastType.Error -> AppColors.OnError
    ToastType.Warning -> AppColors.OnWarning
    ToastType.Info -> AppColors.OnInfo
}

private fun getToastIcon(type: ToastType): String = when (type) {
    ToastType.Success -> "✓"
    ToastType.Error -> "✕"
    ToastType.Warning -> "⚠"
    ToastType.Info -> "ℹ"
}

/**
 * Toast host for managing multiple toasts
 * Place this at the top level of your screen
 */
@Composable
fun AppToastHost(state: ToastState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        AppToast(state = state)
    }
}

/**
 * Extension function for easy toast display
 */
suspend fun ToastState.showSuccess(message: String, duration: ToastDuration = ToastDuration.Medium) {
    show(message, ToastType.Success, duration)
}

suspend fun ToastState.showError(message: String, duration: ToastDuration = ToastDuration.Long) {
    show(message, ToastType.Error, duration)
}

suspend fun ToastState.showWarning(message: String, duration: ToastDuration = ToastDuration.Medium) {
    show(message, ToastType.Warning, duration)
}

suspend fun ToastState.showInfo(message: String, duration: ToastDuration = ToastDuration.Short) {
    show(message, ToastType.Info, duration)
}
