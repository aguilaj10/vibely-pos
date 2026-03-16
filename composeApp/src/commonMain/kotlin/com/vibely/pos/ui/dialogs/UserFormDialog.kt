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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.ValidationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormDialog(isEdit: Boolean, initialData: UserFormData? = null, onSave: (UserFormData) -> Unit, onDismiss: () -> Unit) {
    var formData by remember {
        mutableStateOf(initialData ?: UserFormData())
    }

    var roleExpanded by remember { mutableStateOf(false) }
    val validationErrors = validateUserForm(formData, isEdit)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = if (isEdit) "Edit User" else "Add User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = formData.email,
                    onValueChange = { formData = formData.copy(email = it) },
                    label = "Email *",
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["email"] ?: ValidationState.None,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.fullName,
                    onValueChange = { formData = formData.copy(fullName = it) },
                    label = "Full Name *",
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["fullName"] ?: ValidationState.None,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = formData.role.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        isError = validationErrors["role"] != null,
                    )

                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false },
                    ) {
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.displayName()) },
                                onClick = {
                                    formData = formData.copy(role = role)
                                    roleExpanded = false
                                },
                            )
                        }
                    }
                }

                if (!isEdit) {
                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = formData.password,
                        onValueChange = { formData = formData.copy(password = it) },
                        label = "Password *",
                        variant = AppTextFieldVariant.Outlined,
                        validationState = validationErrors["password"] ?: ValidationState.None,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = formData.confirmPassword,
                        onValueChange = { formData = formData.copy(confirmPassword = it) },
                        label = "Confirm Password *",
                        variant = AppTextFieldVariant.Outlined,
                        validationState = validationErrors["confirmPassword"] ?: ValidationState.None,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = formData.status == UserStatus.ACTIVE,
                        onCheckedChange = {
                            formData = formData.copy(
                                status = if (it) UserStatus.ACTIVE else UserStatus.INACTIVE,
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }

                if (validationErrors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    validationErrors.values.forEach { error ->
                        if (error is ValidationState.Error) {
                            Text(
                                text = error.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

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

private fun validateUserForm(data: UserFormData, isEdit: Boolean): Map<String, ValidationState> {
    val errors = mutableMapOf<String, ValidationState>()

    if (data.email.isBlank()) {
        errors["email"] = ValidationState.Error("Email is required")
    } else if (!isValidEmail(data.email)) {
        errors["email"] = ValidationState.Error("Invalid email format")
    }

    if (data.fullName.isBlank()) {
        errors["fullName"] = ValidationState.Error("Full name is required")
    } else if (data.fullName.length < 2 || data.fullName.length > 100) {
        errors["fullName"] = ValidationState.Error("Full name must be 2-100 characters")
    }

    if (!isEdit) {
        if (data.password.isBlank()) {
            errors["password"] = ValidationState.Error("Password is required")
        } else if (data.password.length < 8) {
            errors["password"] = ValidationState.Error("Password must be at least 8 characters")
        } else if (!hasPasswordComplexity(data.password)) {
            errors["password"] = ValidationState.Error("Password must contain uppercase, lowercase, and a number")
        }

        if (data.confirmPassword.isBlank()) {
            errors["confirmPassword"] = ValidationState.Error("Please confirm your password")
        } else if (data.password != data.confirmPassword) {
            errors["confirmPassword"] = ValidationState.Error("Passwords do not match")
        }
    }

    return errors
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(email)
}

private fun hasPasswordComplexity(password: String): Boolean {
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    return hasUppercase && hasLowercase && hasDigit
}

data class UserFormData(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: UserRole = UserRole.CASHIER,
    val status: UserStatus = UserStatus.ACTIVE,
)
