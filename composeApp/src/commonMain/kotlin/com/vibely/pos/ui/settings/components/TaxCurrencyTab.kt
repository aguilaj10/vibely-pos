package com.vibely.pos.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.util.FormatUtils.formatCurrency
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.theme.PosShapes
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.Percent

@Composable
fun TaxCurrencyTab(
    taxSettings: com.vibely.pos.shared.domain.settings.entity.TaxSettings?,
    onTaxSettingsChange: (Double, String) -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    var taxRate by remember(taxSettings) { mutableStateOf(taxSettings?.taxRate?.toString() ?: "16.0") }
    var currency by remember(taxSettings) { mutableStateOf(taxSettings?.currency ?: "USD") }
    var currencyExpanded by remember { mutableStateOf(false) }

    var taxRateError by remember { mutableStateOf<String?>(null) }

    val currencies = listOf("USD", "MXN", "EUR")
    val currencyLabels =
        mapOf(
            "USD" to "USD - US Dollar",
            "MXN" to "MXN - Mexican Peso",
            "EUR" to "EUR - Euro",
        )

    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        AppCard(
            style = AppCardStyle.Outlined,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Tax & Currency Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                OutlinedTextField(
                    value = taxRate,
                    onValueChange = { newValue ->
                        taxRate = newValue
                        val rate = newValue.toDoubleOrNull()
                        taxRateError =
                            when {
                                newValue.isBlank() -> "Tax rate is required"
                                rate == null -> "Invalid number"
                                rate < 0 || rate > 100 -> "Tax rate must be between 0 and 100"
                                else -> null
                            }
                    },
                    label = { Text("Tax Rate (%)") },
                    placeholder = { Text("e.g., 16.0") },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = FontAwesomeIcons.Solid.Percent,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    isError = taxRateError != null,
                    supportingText = taxRateError?.let { { Text(it) } },
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = PosShapes.InputField,
                    modifier = Modifier.fillMaxWidth(),
                )

                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = currencyLabels[currency] ?: currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        leadingIcon = {
                            androidx.compose.material3.Icon(
                                imageVector = FontAwesomeIcons.Solid.DollarSign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                        },
                        enabled = !isSaving,
                        singleLine = true,
                        shape = PosShapes.InputField,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false },
                    ) {
                        currencies.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(currencyLabels[curr] ?: curr) },
                                onClick = {
                                    currency = curr
                                    currencyExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tax rate is applied to all sales. Currency affects how amounts are displayed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondaryLight,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppCard(
            style = AppCardStyle.Outlined,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Sample amount:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatCurrency(100.0),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tax ($taxRate%):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val taxAmount = (taxRate.toDoubleOrNull() ?: 0.0) / 100 * 100
                    Text(
                        text = formatCurrency(taxAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Total:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = formatCurrency((100 + ((taxRate.toDoubleOrNull() ?: 0.0) / 100 * 100))),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
