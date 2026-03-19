package com.vibely.pos.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.ValidationState
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.utils.formatCurrency
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.Trash
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.common_cancel
import vibely_pos.composeapp.generated.resources.form_currency_symbol_code
import vibely_pos.composeapp.generated.resources.form_label_notes
import vibely_pos.composeapp.generated.resources.form_label_product
import vibely_pos.composeapp.generated.resources.form_label_qty
import vibely_pos.composeapp.generated.resources.form_label_supplier
import vibely_pos.composeapp.generated.resources.form_label_unit_cost
import vibely_pos.composeapp.generated.resources.form_product_display
import vibely_pos.composeapp.generated.resources.purchase_orders_add_product
import vibely_pos.composeapp.generated.resources.purchase_orders_create
import vibely_pos.composeapp.generated.resources.purchase_orders_edit
import vibely_pos.composeapp.generated.resources.purchase_orders_line_items
import vibely_pos.composeapp.generated.resources.purchase_orders_no_items
import vibely_pos.composeapp.generated.resources.purchase_orders_total

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseOrderFormDialog(
    isEdit: Boolean,
    initialData: PurchaseOrderFormData? = null,
    suppliers: List<Supplier> = emptyList(),
    products: List<Product> = emptyList(),
    currencies: List<CurrencyOption> = emptyList(),
    onSave: (PurchaseOrderFormData) -> Unit,
    onDismiss: () -> Unit,
) {
    var formData by remember {
        mutableStateOf(initialData ?: PurchaseOrderFormData())
    }

    var supplierExpanded by remember { mutableStateOf(false) }
    val validationErrors = validatePurchaseOrderForm(formData)

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
                            Res.string.purchase_orders_edit,
                        )
                    } else {
                        stringResource(Res.string.purchase_orders_create)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (suppliers.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = supplierExpanded,
                        onExpandedChange = { supplierExpanded = it },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = suppliers.find { it.id == formData.supplierId }?.name ?: "Select Supplier",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.form_label_supplier)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierExpanded) },
                            modifier =
                            Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            isError = validationErrors["supplierId"] != null,
                        )

                        ExposedDropdownMenu(
                            expanded = supplierExpanded,
                            onDismissRequest = { supplierExpanded = false },
                        ) {
                            suppliers.forEach { supplier ->
                                DropdownMenuItem(
                                    text = { Text(supplier.name) },
                                    onClick = {
                                        formData = formData.copy(supplierId = supplier.id)
                                        supplierExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                AppTextField(
                    value = formData.notes,
                    onValueChange = { formData = formData.copy(notes = it) },
                    label = stringResource(Res.string.form_label_notes),
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.purchase_orders_line_items),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    AppButton(
                        text = stringResource(Res.string.purchase_orders_add_product),
                        onClick = {
                            formData =
                                formData.copy(
                                    lineItems = formData.lineItems + LineItemFormData(),
                                )
                        },
                        style = AppButtonStyle.Text,
                        icon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Plus,
                                contentDescription = "Add Item",
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (formData.lineItems.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.purchase_orders_no_items),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                } else {
                    LazyColumn(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    ) {
                        items(formData.lineItems.indices.toList()) { index ->
                            LineItemRow(
                                lineItem = formData.lineItems[index],
                                products = products,
                                currencies = currencies,
                                onUpdate = { updatedItem ->
                                    val newItems = formData.lineItems.toMutableList()
                                    newItems[index] = updatedItem
                                    formData = formData.copy(lineItems = newItems)
                                },
                                onRemove = {
                                    val newItems = formData.lineItems.toMutableList()
                                    newItems.removeAt(index)
                                    formData = formData.copy(lineItems = newItems)
                                },
                                isFirst = index == 0,
                                isLast = index == formData.lineItems.lastIndex,
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = stringResource(Res.string.purchase_orders_total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formData.calculateTotal().formatCurrency(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary,
                    )
                }

                if (validationErrors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    validationErrors.values.forEach { error ->
                        if (error is ValidationState.Error) {
                            Text(
                                text = error.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.ErrorDark,
                            )
                        }
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LineItemRow(
    lineItem: LineItemFormData,
    products: List<Product>,
    currencies: List<CurrencyOption>,
    onUpdate: (LineItemFormData) -> Unit,
    onRemove: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    var productExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExposedDropdownMenuBox(
                expanded = productExpanded,
                onExpandedChange = { productExpanded = it },
                modifier = Modifier.weight(1.5f),
            ) {
                OutlinedTextField(
                    value = products.find { it.id == lineItem.productId }?.name ?: "Select Product",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(Res.string.form_label_product)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                    modifier =
                    Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )

                ExposedDropdownMenu(
                    expanded = productExpanded,
                    onDismissRequest = { productExpanded = false },
                ) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.form_product_display, product.name, product.sku)) },
                            onClick = {
                                onUpdate(
                                    lineItem.copy(
                                        productId = product.id,
                                        unitCost = product.costPrice.toString(),
                                    ),
                                )
                                productExpanded = false
                            },
                        )
                    }
                }
            }

            AppTextField(
                value = lineItem.quantity,
                onValueChange = { onUpdate(lineItem.copy(quantity = it)) },
                label = stringResource(Res.string.form_label_qty),
                variant = AppTextFieldVariant.Outlined,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(70.dp),
            )

            AppTextField(
                value = lineItem.unitCost,
                onValueChange = { onUpdate(lineItem.copy(unitCost = it)) },
                label = stringResource(Res.string.form_label_unit_cost),
                variant = AppTextFieldVariant.Outlined,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(100.dp),
            )

            if (currencies.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it },
                    modifier = Modifier.width(80.dp),
                ) {
                    OutlinedTextField(
                        value = currencies.find { it.code == lineItem.costCurrencyCode }?.symbol ?: "USD",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                        modifier =
                        Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )

                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false },
                    ) {
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
                                    onUpdate(lineItem.copy(costCurrencyCode = currency.code))
                                    currencyExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            Text(
                text = lineItem.calculateSubtotal().formatCurrency(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(80.dp),
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Trash,
                    contentDescription = "Remove Item",
                    tint = AppColors.ErrorDark,
                )
            }
        }
    }
}

private fun validatePurchaseOrderForm(data: PurchaseOrderFormData): Map<String, ValidationState> {
    val errors = mutableMapOf<String, ValidationState>()

    if (data.supplierId.isBlank()) {
        errors["supplierId"] = ValidationState.Error("Supplier is required")
    }

    if (data.lineItems.isEmpty()) {
        errors["lineItems"] = ValidationState.Error("At least one line item is required")
    }

    val invalidItems =
        data.lineItems.filter {
            it.productId.isBlank() ||
                it.quantity.toIntOrNull() == null ||
                it.quantity.toInt() <= 0
        }
    if (invalidItems.isNotEmpty()) {
        errors["lineItems"] = ValidationState.Error("All line items must have a product and valid quantity")
    }

    return errors
}

data class PurchaseOrderFormData(
    val id: String = "",
    val supplierId: String = "",
    val notes: String = "",
    val lineItems: List<LineItemFormData> = emptyList(),
) {
    fun calculateTotal(): Double = lineItems.sumOf { it.calculateSubtotal() }
}

data class LineItemFormData(
    val id: String = "",
    val productId: String = "",
    val quantity: String = "1",
    val unitCost: String = "0.0",
    val costCurrencyCode: String = "USD",
) {
    fun calculateSubtotal(): Double {
        val qty = quantity.toDoubleOrNull() ?: 0.0
        val cost = unitCost.toDoubleOrNull() ?: 0.0
        return qty * cost
    }
}
