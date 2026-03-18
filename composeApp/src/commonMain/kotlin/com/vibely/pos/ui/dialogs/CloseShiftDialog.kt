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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.util.FormatUtils.formatCurrency
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.ValidationState
import com.vibely.pos.ui.theme.AppColors
import kotlin.math.abs

@Composable
fun CloseShiftDialog(shift: Shift, onSave: (closingBalance: Double, notes: String?) -> Unit, onDismiss: () -> Unit) {
    var actualCash by remember { mutableStateOf(shift.calculatedExpectedBalance.toString()) }
    var notes by remember { mutableStateOf("") }

    val expectedCash = shift.calculatedExpectedBalance
    val actualCashValue = actualCash.toDoubleOrNull() ?: 0.0
    val variance = actualCashValue - expectedCash

    var bills100 by remember { mutableStateOf("0") }
    var bills50 by remember { mutableStateOf("0") }
    var bills20 by remember { mutableStateOf("0") }
    var bills10 by remember { mutableStateOf("0") }
    var bills5 by remember { mutableStateOf("0") }
    var bills1 by remember { mutableStateOf("0") }
    var coins100 by remember { mutableStateOf("0") }
    var coins50 by remember { mutableStateOf("0") }
    var coins25 by remember { mutableStateOf("0") }
    var coins10 by remember { mutableStateOf("0") }
    var coins5 by remember { mutableStateOf("0") }
    var coins1 by remember { mutableStateOf("0") }

    val calculatedFromBills =
        (bills100.toIntOrNull() ?: 0) * 100.0 +
            (bills50.toIntOrNull() ?: 0) * 50.0 +
            (bills20.toIntOrNull() ?: 0) * 20.0 +
            (bills10.toIntOrNull() ?: 0) * 10.0 +
            (bills5.toIntOrNull() ?: 0) * 5.0 +
            (bills1.toIntOrNull() ?: 0) * 1.0 +
            (coins100.toIntOrNull() ?: 0) * 1.0 +
            (coins50.toIntOrNull() ?: 0) * 0.5 +
            (coins25.toIntOrNull() ?: 0) * 0.25 +
            (coins10.toIntOrNull() ?: 0) * 0.1 +
            (coins5.toIntOrNull() ?: 0) * 0.05 +
            (coins1.toIntOrNull() ?: 0) * 0.01

    val validationError = validateClosingBalance(actualCash)

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
                    text = "Close Shift ${shift.shiftNumber}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                ShiftSummaryCard(
                    openingBalance = shift.openingBalance,
                    totalCash = shift.totalCash,
                    totalCard = shift.totalCard,
                    expectedCash = expectedCash,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cash Reconciliation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = actualCash,
                    onValueChange = { actualCash = it },
                    label = "Actual Cash Counted *",
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )

                Spacer(modifier = Modifier.height(16.dp))

                VarianceDisplay(
                    expected = expectedCash,
                    actual = actualCashValue,
                    variance = variance,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Bill/Coin Breakdown (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                BillCoinRow("100s", bills100, { bills100 = it }, 100.0)
                BillCoinRow("50s", bills50, { bills50 = it }, 50.0)
                BillCoinRow("20s", bills20, { bills20 = it }, 20.0)
                BillCoinRow("10s", bills10, { bills10 = it }, 10.0)
                BillCoinRow("5s", bills5, { bills5 = it }, 5.0)
                BillCoinRow("1s", bills1, { bills1 = it }, 1.0)
                BillCoinRow("$1 coins", coins100, { coins100 = it }, 1.0)
                BillCoinRow("50c", coins50, { coins50 = it }, 0.5)
                BillCoinRow("25c", coins25, { coins25 = it }, 0.25)
                BillCoinRow("10c", coins10, { coins10 = it }, 0.1)
                BillCoinRow("5c", coins5, { coins5 = it }, 0.05)
                BillCoinRow("1c", coins1, { coins1 = it }, 0.01)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Calculated from breakdown:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = formatCurrency(calculatedFromBills),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (optional)",
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    AppButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        style = AppButtonStyle.Text,
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    AppButton(
                        text = "Close Shift",
                        onClick = { onSave(actualCashValue, notes.ifBlank { null }) },
                        style = AppButtonStyle.Primary,
                        enabled = validationError == ValidationState.None,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShiftSummaryCard(openingBalance: Double, totalCash: Double, totalCard: Double, expectedCash: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Opening Balance:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = formatCurrency(openingBalance),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Cash Sales:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "+ ${formatCurrency(totalCash)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Success,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Card Sales:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = formatCurrency(totalCard),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Expected Cash:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = formatCurrency(expectedCash),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary,
                )
            }
        }
    }
}

@Composable
private fun VarianceDisplay(expected: Double, actual: Double, variance: Double) {
    val varianceColor =
        when {
            abs(variance) < 0.01 -> MaterialTheme.colorScheme.onSurfaceVariant
            variance > 0 -> AppColors.Success
            else -> AppColors.ErrorDark
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
        CardDefaults.cardColors(
            containerColor = varianceColor.copy(alpha = 0.1f),
        ),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Variance:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${if (variance >= 0) "+" else ""}${formatCurrency(variance)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = varianceColor,
            )
        }
    }
}

@Composable
private fun BillCoinRow(label: String, value: String, onValueChange: (String) -> Unit, denomination: Double) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp),
        )
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            variant = AppTextFieldVariant.Outlined,
            modifier = Modifier.width(80.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Text(
            text = "= ${formatCurrency((value.toDoubleOrNull() ?: 0.0) * denomination)}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(80.dp),
        )
    }
}

private fun validateClosingBalance(value: String): ValidationState {
    if (value.isBlank()) {
        return ValidationState.Error("Cash count is required")
    }

    if (value.toDoubleOrNull() == null) {
        return ValidationState.Error("Invalid number")
    }

    return ValidationState.None
}
