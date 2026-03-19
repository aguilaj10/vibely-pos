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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
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
import vibely_pos.composeapp.generated.resources.common_add
import vibely_pos.composeapp.generated.resources.common_cancel
import vibely_pos.composeapp.generated.resources.common_edit
import vibely_pos.composeapp.generated.resources.customers_add
import vibely_pos.composeapp.generated.resources.customers_edit
import vibely_pos.composeapp.generated.resources.customers_loyalty_tier
import vibely_pos.composeapp.generated.resources.form_label_email
import vibely_pos.composeapp.generated.resources.form_label_full_name
import vibely_pos.composeapp.generated.resources.form_label_phone

/**
 * Form dialog for adding or editing customers.
 *
 * @param isEdit Whether this is edit mode (true) or add mode (false)
 * @param initialData Optional customer data for edit mode
 * @param onSave Callback with customer form data when submitted
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun CustomerFormDialog(isEdit: Boolean, initialData: CustomerFormData? = null, onSave: (CustomerFormData) -> Unit, onDismiss: () -> Unit) {
    var formData by remember {
        mutableStateOf(initialData ?: CustomerFormData())
    }

    val validationErrors = validateCustomerForm(formData, isEdit)

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
                    text =
                    if (isEdit) {
                        stringResource(
                            Res.string.customers_edit,
                        )
                    } else {
                        stringResource(Res.string.customers_add)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = formData.fullName,
                    onValueChange = { formData = formData.copy(fullName = it) },
                    label = stringResource(Res.string.form_label_full_name),
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["fullName"] ?: ValidationState.None,
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

                if (isEdit) {
                    LoyaltyTierDisplay(tier = formData.loyaltyTier)
                    Spacer(modifier = Modifier.height(12.dp))
                }

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

@Composable
private fun LoyaltyTierDisplay(tier: String?) {
    val (backgroundColor, textColor, label) =
        when (tier) {
            "Platinum" -> {
                Triple(Color(0xFF7C3AED), Color.White, "Platinum")
            }

            "Gold" -> {
                Triple(Color(0xFFFFD700), Color.Black, "Gold")
            }

            "Silver" -> {
                Triple(Color(0xFF9CA3AF), Color.White, "Silver")
            }

            "Bronze" -> {
                Triple(Color(0xFFCD7F32), Color.White, "Bronze")
            }

            else -> {
                Triple(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    "Bronze",
                )
            }
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.customers_loyalty_tier),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
}

private fun validateCustomerForm(data: CustomerFormData, isEdit: Boolean): Map<String, ValidationState> {
    val errors = mutableMapOf<String, ValidationState>()

    if (data.fullName.isBlank()) {
        errors["fullName"] = ValidationState.Error("Full name is required")
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

/**
 * Data class for customer form fields.
 */
data class CustomerFormData(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val loyaltyPoints: Int = 0,
    val loyaltyTier: String? = null,
    val totalPurchases: Double = 0.0,
    val isActive: Boolean = true,
)
