package com.vibely.pos.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.ValidationState
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.utils.formatCurrency
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.Percent
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.tax_currency_currency_label
import vibely_pos.composeapp.generated.resources.tax_currency_description
import vibely_pos.composeapp.generated.resources.tax_currency_error_invalid
import vibely_pos.composeapp.generated.resources.tax_currency_error_range
import vibely_pos.composeapp.generated.resources.tax_currency_error_required
import vibely_pos.composeapp.generated.resources.tax_currency_eur
import vibely_pos.composeapp.generated.resources.tax_currency_label
import vibely_pos.composeapp.generated.resources.tax_currency_mxn
import vibely_pos.composeapp.generated.resources.tax_currency_placeholder
import vibely_pos.composeapp.generated.resources.tax_currency_preview
import vibely_pos.composeapp.generated.resources.tax_currency_sample_amount
import vibely_pos.composeapp.generated.resources.tax_currency_tax_label
import vibely_pos.composeapp.generated.resources.tax_currency_title
import vibely_pos.composeapp.generated.resources.tax_currency_total_label
import vibely_pos.composeapp.generated.resources.tax_currency_usd

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

    val errorRequired = stringResource(Res.string.tax_currency_error_required)
    val errorInvalid = stringResource(Res.string.tax_currency_error_invalid)
    val errorRange = stringResource(Res.string.tax_currency_error_range)

    val currencies = listOf("USD", "MXN", "EUR")
    val currencyLabels =
        mapOf(
            "USD" to stringResource(Res.string.tax_currency_usd),
            "MXN" to stringResource(Res.string.tax_currency_mxn),
            "EUR" to stringResource(Res.string.tax_currency_eur),
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
                    text = stringResource(Res.string.tax_currency_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                AppTextField(
                    value = taxRate,
                    onValueChange = { newValue ->
                        taxRate = newValue
                        val rate = newValue.toDoubleOrNull()
                        taxRateError =
                            when {
                                newValue.isBlank() -> errorRequired
                                rate == null -> errorInvalid
                                rate !in 0.0..100.0 -> errorRange
                                else -> null
                            }
                    },
                    label = stringResource(Res.string.tax_currency_label),
                    placeholder = stringResource(Res.string.tax_currency_placeholder),
                    leadingIcon = {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Percent,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 20.dp).size(20.dp),
                        )
                    },
                    validationState =
                    if (taxRateError !=
                        null
                    ) {
                        ValidationState.Error(taxRateError!!)
                    } else {
                        ValidationState.None
                    },
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AppTextField(
                        value = currencyLabels[currency] ?: currency,
                        onValueChange = {},
                        readOnly = true,
                        label = stringResource(Res.string.tax_currency_currency_label),
                        leadingIcon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.DollarSign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 20.dp).size(20.dp),
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                        },
                        enabled = !isSaving,
                        singleLine = true,
                        variant = AppTextFieldVariant.Outlined,
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
                    text = stringResource(Res.string.tax_currency_description),
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
                    text = stringResource(Res.string.tax_currency_preview),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.tax_currency_sample_amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = 100.0.formatCurrency(),
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
                        text = stringResource(Res.string.tax_currency_tax_label, taxRate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val taxAmount = (taxRate.toDoubleOrNull() ?: 0.0) / 100 * 100
                    Text(
                        text = taxAmount.formatCurrency(),
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
                        text = stringResource(Res.string.tax_currency_total_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = (100 + ((taxRate.toDoubleOrNull() ?: 0.0) / 100 * 100)).formatCurrency(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
