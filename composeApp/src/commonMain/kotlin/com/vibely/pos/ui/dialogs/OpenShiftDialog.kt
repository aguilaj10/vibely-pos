package com.vibely.pos.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.ValidationState
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.common_cancel
import vibely_pos.composeapp.generated.resources.shifts_open
import vibely_pos.composeapp.generated.resources.shifts_open_new
import vibely_pos.composeapp.generated.resources.shifts_opening_balance
import vibely_pos.composeapp.generated.resources.shifts_opening_balance_prompt

@Composable
fun OpenShiftDialog(onSave: (Double) -> Unit, onDismiss: () -> Unit) {
    var openingBalance by remember { mutableStateOf("0.00") }
    val validationError = validateOpeningBalance(openingBalance)

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
                    .padding(24.dp),
            ) {
                Text(
                    text = stringResource(Res.string.shifts_open_new),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.shifts_opening_balance_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = stringResource(Res.string.shifts_opening_balance),
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )

                if (validationError is ValidationState.Error) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = validationError.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
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
                        text = stringResource(Res.string.shifts_open),
                        onClick = {
                            val balance = openingBalance.toDoubleOrNull() ?: 0.0
                            onSave(balance)
                        },
                        style = AppButtonStyle.Primary,
                        enabled = validationError == ValidationState.None,
                    )
                }
            }
        }
    }
}

private fun validateOpeningBalance(value: String): ValidationState {
    if (value.isBlank()) {
        return ValidationState.None
    }

    val balance = value.toDoubleOrNull()
    if (balance == null) {
        return ValidationState.Error("Invalid number")
    }

    if (balance < 0) {
        return ValidationState.Error("Balance cannot be negative")
    }

    return ValidationState.None
}
