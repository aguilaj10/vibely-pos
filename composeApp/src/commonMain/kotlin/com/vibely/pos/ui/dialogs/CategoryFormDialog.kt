package com.vibely.pos.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.ValidationState
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.common_add
import vibely_pos.composeapp.generated.resources.common_cancel
import vibely_pos.composeapp.generated.resources.common_edit
import vibely_pos.composeapp.generated.resources.form_label_color_hex
import vibely_pos.composeapp.generated.resources.form_label_description
import vibely_pos.composeapp.generated.resources.form_label_icon
import vibely_pos.composeapp.generated.resources.form_label_name_required

/**
 * Category form dialog for adding or editing categories.
 *
 * @param isEdit Whether this is edit mode (true) or add mode (false)
 * @param initialCategory Optional category data for edit mode
 * @param onSave Callback with category data when form is submitted
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun CategoryFormDialog(isEdit: Boolean, initialCategory: CategoryFormData? = null, onSave: (CategoryFormData) -> Unit, onDismiss: () -> Unit) {
    var formData by remember {
        mutableStateOf(
            initialCategory ?: CategoryFormData(),
        )
    }

    val validationErrors = validateCategoryForm(formData)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = if (isEdit) "Edit Category" else "Add Category",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = formData.name,
                    onValueChange = { formData = formData.copy(name = it) },
                    label = stringResource(Res.string.form_label_name_required),
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["name"] ?: ValidationState.None,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.description,
                    onValueChange = { formData = formData.copy(description = it) },
                    label = stringResource(Res.string.form_label_description),
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.color,
                    onValueChange = { formData = formData.copy(color = it) },
                    label = stringResource(Res.string.form_label_color_hex),
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["color"] ?: ValidationState.None,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.icon,
                    onValueChange = { formData = formData.copy(icon = it) },
                    label = stringResource(Res.string.form_label_icon),
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    AppButton(
                        text = stringResource(Res.string.common_cancel),
                        onClick = onDismiss,
                        style = AppButtonStyle.Text,
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    AppButton(
                        text =
                        if (isEdit) {
                            stringResource(
                                Res.string.common_edit,
                            )
                        } else {
                            stringResource(Res.string.common_add)
                        },
                        onClick = { onSave(formData) },
                        style = AppButtonStyle.Primary,
                        enabled = validationErrors.isEmpty(),
                    )
                }
            }
        }
    }
}

private fun validateCategoryForm(data: CategoryFormData): Map<String, ValidationState> {
    val errors = mutableMapOf<String, ValidationState>()

    if (data.name.isBlank()) {
        errors["name"] = ValidationState.Error("Name is required")
    } else if (data.name.length < 2 || data.name.length > 100) {
        errors["name"] = ValidationState.Error("Name must be 2-100 characters")
    }

    if (data.color.isNotBlank()) {
        val isValidHex =
            data.color.startsWith("#") &&
                (data.color.length == 7 || data.color.length == 9) &&
                data.color.drop(1).all { char -> char.isDigit() || char in 'a'..'f' || char in 'A'..'F' }

        if (!isValidHex) {
            errors["color"] = ValidationState.Error("Invalid hex color format")
        }
    }

    return errors
}

data class CategoryFormData(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val color: String = "",
    val icon: String = "",
    val isActive: Boolean = true,
)
