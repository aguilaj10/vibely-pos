package com.vibely.pos.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.valueobject.CartItem
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.customers.CustomersViewModel
import com.vibely.pos.ui.dialogs.PaymentDialog
import com.vibely.pos.ui.navigation.Screen
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.utils.formatCurrency
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Cube
import compose.icons.fontawesomeicons.solid.Minus
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.ShoppingCart
import compose.icons.fontawesomeicons.solid.Times
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.checkout_cart_empty
import vibely_pos.composeapp.generated.resources.checkout_cart_title
import vibely_pos.composeapp.generated.resources.checkout_complete_sale
import vibely_pos.composeapp.generated.resources.checkout_no_products_found
import vibely_pos.composeapp.generated.resources.checkout_search_products
import vibely_pos.composeapp.generated.resources.checkout_select_customer
import vibely_pos.composeapp.generated.resources.checkout_title
import vibely_pos.composeapp.generated.resources.checkout_walkin_customer
import vibely_pos.composeapp.generated.resources.common_checkout
import vibely_pos.composeapp.generated.resources.common_total
import vibely_pos.composeapp.generated.resources.inventory_low_stock
import vibely_pos.composeapp.generated.resources.inventory_out_of_stock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    saleId: String? = null,
    viewModel: CheckoutViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val checkoutCompleteSaleText = stringResource(Res.string.checkout_complete_sale)

    val customersViewModel: CustomersViewModel = koinInject()
    val customersState by customersViewModel.state.collectAsState()
    var customerExpanded by remember { mutableStateOf(false) }
    val selectedCustomer = customersState.customers.find { it.id == state.customerId }

    LaunchedEffect(saleId) {
        saleId?.let { viewModel.loadSale(it) }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorDismiss()
        }
    }

    LaunchedEffect(state.checkoutSuccess) {
        if (state.checkoutSuccess) {
            snackbarHostState.showSnackbar(checkoutCompleteSaleText)
            viewModel.onCheckoutSuccessDismiss()
            onNavigate(Screen.Sales)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                text = stringResource(Res.string.checkout_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(24.dp))
            ExposedDropdownMenuBox(
                expanded = customerExpanded,
                onExpandedChange = { customerExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppTextField(
                    value = selectedCustomer?.fullName ?: stringResource(Res.string.checkout_walkin_customer),
                    onValueChange = {},
                    readOnly = true,
                    label = stringResource(Res.string.checkout_select_customer),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                    variant = AppTextFieldVariant.Outlined,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )

                ExposedDropdownMenu(
                    expanded = customerExpanded,
                    onDismissRequest = { customerExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.checkout_walkin_customer)) },
                        onClick = {
                            viewModel.onCustomerSelected(null)
                            customerExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                    customersState.customers
                        .filter { it.isActive }
                        .forEach { customer ->
                            DropdownMenuItem(
                                text = { Text(customer.fullName) },
                                onClick = {
                                    viewModel.onCustomerSelected(customer.id)
                                    customerExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = state.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            onSearch = {},
                            expanded = state.shouldExpand,
                            onExpandedChange = { if (!it) viewModel.onSearchQueryChange("") },
                            placeholder = { Text(stringResource(Res.string.checkout_search_products)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = FontAwesomeIcons.Solid.Times,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    expanded = state.shouldExpand,
                    onExpandedChange = { if (!it) viewModel.onSearchQueryChange("") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    when {
                        state.isSearching -> {
                            Box(
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }

                        state.searchResults.isEmpty() -> {
                            Box(
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(Res.string.checkout_no_products_found),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        else -> {
                            LazyColumn(modifier = Modifier.wrapContentHeight()) {
                                items(state.searchResults) { product ->
                                    ProductSearchItem(
                                        product = product,
                                        onClick = {
                                            viewModel.onProductSelected(product)
                                            viewModel.onSearchQueryChange("")
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(Res.string.checkout_cart_title, state.totalItems),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.cart.items.isEmpty()) {
                        Box(
                            modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.ShoppingCart,
                                    contentDescription = "Empty cart",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp),
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(Res.string.checkout_cart_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(Res.string.checkout_search_products),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.cart.items) { item ->
                                CartItemCard(
                                    item = item,
                                    onQuantityChange = { newQuantity ->
                                        viewModel.onUpdateQuantity(item.productId, newQuantity)
                                    },
                                    onRemove = { viewModel.onRemoveFromCart(item.productId) },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.common_total),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = state.totalAmount.formatCurrency(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AppButton(
                        text = stringResource(Res.string.common_checkout),
                        onClick = viewModel::onCheckout,
                        style = AppButtonStyle.Primary,
                        enabled = state.canCheckout && state.cart.items.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (state.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(
                    modifier =
                    Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (state.showPaymentDialog) {
            PaymentDialog(
                totalAmount = state.totalAmount,
                paymentTenders = state.paymentTenders,
                remainingAmount = state.remainingAmount,
                canComplete = state.canCompleteSale,
                isProcessing = state.isProcessingPayment,
                onAddTender = { type, amount -> viewModel.addPaymentTender(type, amount) },
                onRemoveTender = viewModel::removePaymentTender,
                onComplete = viewModel::recordPayments,
                onDismiss = viewModel::onPaymentDialogDismiss,
            )
        }
    }
}

@Composable
private fun ProductSearchItem(product: Product, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Cube,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = product.categoryName ?: "Uncategorized",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (product.currentStock <= 0) {
                    Text(
                        text = stringResource(Res.string.inventory_out_of_stock),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                    )
                } else if (product.currentStock < 10) {
                    Text(
                        text = stringResource(Res.string.inventory_low_stock, product.currentStock),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Text(
                text = product.sellingPrice.formatCurrency(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CartItemCard(item: CartItem, onQuantityChange: (Int) -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Times,
                        contentDescription = "Remove",
                        tint = AppColors.Error,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Minus,
                            contentDescription = "Decrease",
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Plus,
                            contentDescription = "Increase",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Text(
                    text = item.subtotal.formatCurrency(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "${item.unitPrice.formatCurrency()} each",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
