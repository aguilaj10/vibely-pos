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
import vibely_pos.composeapp.generated.resources.form_currency_symbol_code
import vibely_pos.composeapp.generated.resources.form_error_no_categories
import vibely_pos.composeapp.generated.resources.form_error_no_currencies
import vibely_pos.composeapp.generated.resources.form_label_barcode
import vibely_pos.composeapp.generated.resources.form_label_category
import vibely_pos.composeapp.generated.resources.form_label_cost_price
import vibely_pos.composeapp.generated.resources.form_label_currency
import vibely_pos.composeapp.generated.resources.form_label_description
import vibely_pos.composeapp.generated.resources.form_label_min_stock
import vibely_pos.composeapp.generated.resources.form_label_name_required
import vibely_pos.composeapp.generated.resources.form_label_selling_price_mxn
import vibely_pos.composeapp.generated.resources.form_label_sku
import vibely_pos.composeapp.generated.resources.form_label_stock
import vibely_pos.composeapp.generated.resources.form_label_unit
import vibely_pos.composeapp.generated.resources.form_option_no_category

/**
 * Product form dialog for adding or editing products.
 *
 * @param isEdit Whether this is edit mode (true) or add mode (false)
 * @param initialProduct Optional product data for edit mode
 * @param categories List of available categories for selection
 * @param onSave Callback with product data when form is submitted
 * @param onDismiss Callback when dialog is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    isEdit: Boolean,
    initialProduct: ProductFormData? = null,
    categories: List<CategoryOption> = emptyList(),
    currencies: List<CurrencyOption> = emptyList(),
    onSave: (ProductFormData) -> Unit,
    onDismiss: () -> Unit,
) {
    var formData by remember {
        mutableStateOf(
            initialProduct ?: ProductFormData(),
        )
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    val validationErrors = validateProductForm(formData)

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
                    text = if (isEdit) "Edit Product" else "Add Product",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = formData.sku,
                    onValueChange = { formData = formData.copy(sku = it) },
                    label = stringResource(Res.string.form_label_sku),
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["sku"] ?: ValidationState.None,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == formData.categoryId }?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.form_label_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier =
                        Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.form_option_no_category)) },
                            onClick = {
                                formData = formData.copy(categoryId = null)
                                categoryExpanded = false
                            },
                        )
                        if (categories.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.form_error_no_categories)) },
                                onClick = { categoryExpanded = false },
                                enabled = false,
                            )
                        } else {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        formData = formData.copy(categoryId = category.id)
                                        categoryExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        AppTextField(
                            value = formData.costPrice,
                            onValueChange = { formData = formData.copy(costPrice = it) },
                            label = stringResource(Res.string.form_label_cost_price),
                            variant = AppTextFieldVariant.Outlined,
                            validationState = validationErrors["costPrice"] ?: ValidationState.None,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = currencyExpanded,
                            onExpandedChange = { currencyExpanded = it },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = currencies.find { it.code == formData.costCurrencyCode }?.symbol ?: "USD",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(Res.string.form_label_currency)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = currencyExpanded,
                                    )
                                },
                                modifier =
                                Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                            )

                            ExposedDropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false },
                            ) {
                                if (currencies.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.form_error_no_currencies)) },
                                        onClick = { currencyExpanded = false },
                                        enabled = false,
                                    )
                                } else {
                                    currencies.forEach { currency ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    stringResource(
                                                        Res.string.form_currency_symbol_code,
                                                        currency.symbol,
                                                        currency.code,
                                                    ),
                                                )
                                            },
                                            onClick = {
                                                formData = formData.copy(costCurrencyCode = currency.code)
                                                currencyExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = formData.sellingPrice,
                    onValueChange = { formData = formData.copy(sellingPrice = it) },
                    label = stringResource(Res.string.form_label_selling_price_mxn),
                    variant = AppTextFieldVariant.Outlined,
                    validationState = validationErrors["sellingPrice"] ?: ValidationState.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppTextField(
                        value = formData.currentStock,
                        onValueChange = { formData = formData.copy(currentStock = it) },
                        label = stringResource(Res.string.form_label_stock),
                        variant = AppTextFieldVariant.Outlined,
                        validationState = validationErrors["currentStock"] ?: ValidationState.None,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )

                    AppTextField(
                        value = formData.minStockLevel,
                        onValueChange = { formData = formData.copy(minStockLevel = it) },
                        label = stringResource(Res.string.form_label_min_stock),
                        variant = AppTextFieldVariant.Outlined,
                        validationState = validationErrors["minStockLevel"] ?: ValidationState.None,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppTextField(
                        value = formData.unit,
                        onValueChange = { formData = formData.copy(unit = it) },
                        label = stringResource(Res.string.form_label_unit),
                        variant = AppTextFieldVariant.Outlined,
                        modifier = Modifier.weight(1f),
                    )

                    AppTextField(
                        value = formData.barcode,
                        onValueChange = { formData = formData.copy(barcode = it) },
                        label = stringResource(Res.string.form_label_barcode),
                        variant = AppTextFieldVariant.Outlined,
                        modifier = Modifier.weight(1f),
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

private fun validateProductForm(data: ProductFormData): Map<String, ValidationState> {
    val errors = mutableMapOf<String, ValidationState>()

    if (data.sku.isBlank()) {
        errors["sku"] = ValidationState.Error("SKU is required")
    }

    if (data.name.isBlank()) {
        errors["name"] = ValidationState.Error("Name is required")
    } else if (data.name.length !in 3..100) {
        errors["name"] = ValidationState.Error("Name must be 3-100 characters")
    }

    val costPrice = data.costPrice.toDoubleOrNull()
    if (costPrice != null && costPrice < 0) {
        errors["costPrice"] = ValidationState.Error("Cost price cannot be negative")
    }

    val sellingPrice = data.sellingPrice.toDoubleOrNull()
    if (sellingPrice == null || sellingPrice <= 0) {
        errors["sellingPrice"] = ValidationState.Error("Selling price must be > 0")
    }

    val currentStock = data.currentStock.toIntOrNull()
    if (currentStock == null || currentStock < 0) {
        errors["currentStock"] = ValidationState.Error("Stock must be >= 0")
    }

    val minStockLevel = data.minStockLevel.toIntOrNull()
    if (minStockLevel != null && minStockLevel < 0) {
        errors["minStockLevel"] = ValidationState.Error("Min stock level cannot be negative")
    }

    return errors
}

data class ProductFormData(
    val id: String = "",
    val sku: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String? = null,
    val costPrice: String = "",
    val costCurrencyCode: String = "USD",
    val sellingPrice: String = "",
    val currentStock: String = "0",
    val minStockLevel: String = "10",
    val unit: String = "unit",
    val barcode: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
)

data class CategoryOption(val id: String, val name: String)

data class CurrencyOption(val code: String, val symbol: String, val name: String)
