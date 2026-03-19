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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import vibely_pos.composeapp.generated.resources.common_active
import vibely_pos.composeapp.generated.resources.common_cancel
import vibely_pos.composeapp.generated.resources.form_label_address
import vibely_pos.composeapp.generated.resources.form_label_contact_person
import vibely_pos.composeapp.generated.resources.form_label_email
import vibely_pos.composeapp.generated.resources.form_label_phone
import vibely_pos.composeapp.generated.resources.form_label_supplier_name

@Composable
fun SupplierFormDialog(isEdit: Boolean, initialData: SupplierFormData? = null, onSave: (SupplierFormData) -> Unit, onDismiss: () -> Unit) {
    var formData by remember {
        mutableStateOf(initialData ?: SupplierFormData())
    }

    val validationErrors = validateSupplierForm(formData)

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
                    text = if (isEdit) "Edit Supplier" else "Add Supplier",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = formData.name,
                    onValueChange = { formData = formData.copy(name = it) },
                    label = stringResource(Res.string.form_label_supplier_name),
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["name"] ?: ValidationState.None,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.contactPerson,
                    onValueChange = { formData = formData.copy(contactPerson = it) },
                    label = stringResource(Res.string.form_label_contact_person),
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.email,
                    onValueChange = { formData = formData.copy(email = it) },
                    label = stringResource(Res.string.form_label_email),
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["email"] ?: ValidationState.None,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.phone,
                    onValueChange = { formData = formData.copy(phone = it) },
                    label = stringResource(Res.string.form_label_phone),
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.address,
                    onValueChange = { formData = formData.copy(address = it) },
                    label = stringResource(Res.string.form_label_address),
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.common_active),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = formData.isActive,
                        onCheckedChange = { formData = formData.copy(isActive = it) },
                        colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }

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
                        text = if (isEdit) "Update" else "Create",
                        onClick = { onSave(formData) },
                        style = AppButtonStyle.Primary,
                        enabled = validationErrors.isEmpty(),
                    )
                }
            }
        }
    }
}

private fun validateSupplierForm(data: SupplierFormData): Map<String, ValidationState> {
    val errors = mutableMapOf<String, ValidationState>()

    if (data.name.isBlank()) {
        errors["name"] = ValidationState.Error("Supplier name is required")
    }

    if (data.email.isNotBlank() && !isValidEmail(data.email)) {
        errors["email"] = ValidationState.Error("Invalid email format")
    }

    return errors
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(email)
}

data class SupplierFormData(
    val id: String = "",
    val name: String = "",
    val contactPerson: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val isActive: Boolean = true,
)
