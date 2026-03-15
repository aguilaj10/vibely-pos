package com.vibely.pos.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.theme.PosShapes

sealed class ValidationState {
    object None : ValidationState()
    object Valid : ValidationState()
    data class Error(val message: String) : ValidationState()
    data class Warning(val message: String) : ValidationState()
}

enum class AppTextFieldVariant {
    Outlined,
    Filled,
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: AppTextFieldVariant = AppTextFieldVariant.Outlined,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    validationState: ValidationState = ValidationState.None,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = PosShapes.InputField,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    when (variant) {
        AppTextFieldVariant.Outlined -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon ?: getValidationIcon(validationState),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = validationState is ValidationState.Error,
            colors = getOutlinedTextFieldColors(validationState),
            shape = shape,
            interactionSource = interactionSource,
        )
        AppTextFieldVariant.Filled -> FilledTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            validationState = validationState,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = shape,
        )
    }
}

@Composable
private fun FilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    validationState: ValidationState = ValidationState.None,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = PosShapes.InputField,
) {
    val isError = validationState is ValidationState.Error
    val borderColor = when {
        isError -> AppColors.Error
        validationState is ValidationState.Warning -> AppColors.Warning
        validationState is ValidationState.Valid -> AppColors.Success
        else -> Color.Transparent
    }

    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedIndicatorColor = borderColor,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AppColors.Primary,
            ),
            shape = shape,
        )

        when (validationState) {
            is ValidationState.Error -> {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = validationState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Error,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            is ValidationState.Warning -> {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = validationState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Warning,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            ValidationState.Valid, ValidationState.None -> {}
        }
    }
}

@Composable
private fun getValidationIcon(state: ValidationState): (@Composable () -> Unit)? = when (state) {
    is ValidationState.Error -> null
    is ValidationState.Warning -> null
    ValidationState.Valid -> null
    ValidationState.None -> null
}

@Composable
private fun getOutlinedTextFieldColors(state: ValidationState): TextFieldColors = when (state) {
    is ValidationState.Error -> OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.Error,
        unfocusedBorderColor = AppColors.Error.copy(alpha = 0.5f),
    )
    is ValidationState.Warning -> OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.Warning,
        unfocusedBorderColor = AppColors.Warning.copy(alpha = 0.5f),
    )
    ValidationState.Valid -> OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.Success,
        unfocusedBorderColor = AppColors.Success.copy(alpha = 0.5f),
    )
    ValidationState.None -> OutlinedTextFieldDefaults.colors()
}

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    enabled: Boolean = true,
    variant: AppTextFieldVariant = AppTextFieldVariant.Filled,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        singleLine = true,
        variant = variant,
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Text("✕")
                }
            }
        } else {
            null
        },
    )
}
