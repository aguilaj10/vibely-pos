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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.vibely.pos.shared.data.currency.dto.CurrencyDTO
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateFormDialog(
    isEdit: Boolean,
    currencies: List<CurrencyDTO>,
    initialData: ExchangeRateFormData? = null,
    onSave: (ExchangeRateFormData) -> Unit,
    onDismiss: () -> Unit,
) {
    var formData by remember {
        mutableStateOf(initialData ?: ExchangeRateFormData())
    }

    val validationErrors = validateExchangeRateForm(formData, currencies)

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
                    text = if (isEdit) "Edit Exchange Rate" else "Add Exchange Rate",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                var fromCurrencyExpanded by remember { mutableStateOf(false) }
                var toCurrencyExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = fromCurrencyExpanded,
                    onExpandedChange = { fromCurrencyExpanded = !fromCurrencyExpanded },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = formData.currencyFrom,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Currency *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromCurrencyExpanded) },
                        isError = validationErrors["currencyFrom"] != null,
                        supportingText = validationErrors["currencyFrom"]?.let { { Text(it) } },
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = fromCurrencyExpanded,
                        onDismissRequest = { fromCurrencyExpanded = false },
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text("${currency.code} - ${currency.name}") },
                                onClick = {
                                    formData = formData.copy(currencyFrom = currency.code)
                                    fromCurrencyExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = toCurrencyExpanded,
                    onExpandedChange = { toCurrencyExpanded = !toCurrencyExpanded },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = formData.currencyTo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Currency *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toCurrencyExpanded) },
                        isError = validationErrors["currencyTo"] != null,
                        supportingText = validationErrors["currencyTo"]?.let { { Text(it) } },
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = toCurrencyExpanded,
                        onDismissRequest = { toCurrencyExpanded = false },
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text("${currency.code} - ${currency.name}") },
                                onClick = {
                                    formData = formData.copy(currencyTo = currency.code)
                                    toCurrencyExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = formData.rate.toString(),
                    onValueChange = { newValue ->
                        val rate = newValue.toDoubleOrNull()
                        if (rate != null || newValue.isEmpty()) {
                            formData = formData.copy(rate = rate ?: 0.0)
                        }
                    },
                    label = { Text("Rate *") },
                    isError = validationErrors["rate"] != null,
                    supportingText = validationErrors["rate"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = formData.effectiveDate,
                    onValueChange = { formData = formData.copy(effectiveDate = it) },
                    label = { Text("Effective Date (YYYY-MM-DD) *") },
                    placeholder = { Text("2024-01-01") },
                    isError = validationErrors["effectiveDate"] != null,
                    supportingText = validationErrors["effectiveDate"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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

private fun validateExchangeRateForm(data: ExchangeRateFormData, currencies: List<CurrencyDTO>): Map<String, String> {
    val errors = mutableMapOf<String, String>()

    if (data.currencyFrom.isBlank()) {
        errors["currencyFrom"] = "From currency is required"
    }

    if (data.currencyTo.isBlank()) {
        errors["currencyTo"] = "To currency is required"
    }

    if (data.currencyFrom.isNotBlank() && data.currencyTo.isNotBlank() && data.currencyFrom == data.currencyTo) {
        errors["currencyTo"] = "Cannot convert to the same currency"
    }

    if (data.rate <= 0) {
        errors["rate"] = "Rate must be greater than 0"
    }

    if (data.effectiveDate.isBlank()) {
        errors["effectiveDate"] = "Effective date is required"
    } else if (!isValidDate(data.effectiveDate)) {
        errors["effectiveDate"] = "Invalid date format. Use YYYY-MM-DD"
    }

    return errors
}

private fun isValidDate(date: String): Boolean {
    val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
    if (!dateRegex.matches(date)) return false

    return try {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        year in 1900..2100 && month in 1..12 && day in 1..31
    } catch (e: Exception) {
        false
    }
}

data class ExchangeRateFormData(
    val id: String = "",
    val currencyFrom: String = "",
    val currencyTo: String = "",
    val rate: Double = 0.0,
    val effectiveDate: String = "",
)
