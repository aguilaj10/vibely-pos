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
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.theme.PosShapes

/**
 * Validation state for text fields
 */
sealed class ValidationState {
    object None : ValidationState()
    object Valid : ValidationState()
    data class Error(val message: String) : ValidationState()
    data class Warning(val message: String) : ValidationState()
}

/**
 * Custom text field component for Vibely POS
 * Provides consistent input styling with validation states
 *
 * @param value Current text value
 * @param onValueChange Value change callback
 * @param modifier Modifier for customization
 * @param label Optional label text
 * @param placeholder Optional placeholder text
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param validationState Current validation state
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param singleLine Whether to restrict input to single line
 * @param maxLines Maximum number of lines
 * @param visualTransformation Visual transformation (e.g., password masking)
 * @param keyboardOptions Keyboard configuration
 * @param keyboardActions Keyboard action handlers
 * @param shape Field shape
 * @param interactionSource Interaction source for state handling
 */
@Composable
fun AppTextField(
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
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
            colors = getTextFieldColors(validationState),
            shape = shape,
            interactionSource = interactionSource,
        )

        // Show validation message
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
            ValidationState.Valid, ValidationState.None -> {
                // No message
            }
        }
    }
}

@Composable
private fun getValidationIcon(state: ValidationState): (@Composable () -> Unit)? = when (state) {
    is ValidationState.Error -> null // Icon shown by Material3
    is ValidationState.Warning -> null
    ValidationState.Valid -> null
    ValidationState.None -> null
}

@Composable
private fun getTextFieldColors(state: ValidationState): TextFieldColors = when (state) {
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

/**
 * Specialized search text field with clear button
 */
@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    enabled: Boolean = true,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        singleLine = true,
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Text("✕") // Clear icon
                }
            }
        } else {
            null
        },
    )
}
