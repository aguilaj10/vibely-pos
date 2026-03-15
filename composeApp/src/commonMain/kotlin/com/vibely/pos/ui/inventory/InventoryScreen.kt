package com.vibely.pos.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.util.FormatUtils.formatCurrency
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.navigation.Screen
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Boxes
import compose.icons.fontawesomeicons.solid.Cube
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Tags
import compose.icons.fontawesomeicons.solid.Trash
import compose.icons.fontawesomeicons.solid.Wallet
import org.koin.compose.koinInject

@Composable
fun InventoryScreen(onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier, viewModel: InventoryViewModel = koinInject()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            InventoryHeader(
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onClearSearch = viewModel::onClearSearch,
                onAddProduct = viewModel::onAddProduct,
            )

            Spacer(modifier = Modifier.height(16.dp))

            KpiCardsRow(
                totalProducts = state.totalProducts,
                lowStockCount = state.lowStockCount,
                totalValue = state.totalValue,
                categoriesCount = state.categoriesCount,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ProductsTable(
                    products = state.products,
                    onEditProduct = viewModel::onEditProduct,
                    onDeleteProduct = viewModel::onDeleteProduct,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun InventoryHeader(searchQuery: String, onSearchQueryChange: (String) -> Unit, onClearSearch: () -> Unit, onAddProduct: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.width(480.dp),
            variant = AppTextFieldVariant.Outlined,
            placeholder = "Search by name or SKU...",
            leadingIcon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Boxes,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            singleLine = true,
        )

        AppButton(
            text = "Add Product",
            onClick = onAddProduct,
            style = AppButtonStyle.Primary,
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

@Composable
private fun KpiCardsRow(totalProducts: Int, lowStockCount: Int, totalValue: Double, categoriesCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KpiCard(
            icon = FontAwesomeIcons.Solid.Boxes,
            label = "Total Products",
            value = totalProducts.toString(),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.ExclamationTriangle,
            label = "Low Stock",
            value = lowStockCount.toString(),
            valueColor = AppColors.ErrorDark,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.Wallet,
            label = "Total Value",
            value = formatCurrency(totalValue),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.Tags,
            label = "Categories",
            value = categoriesCount.toString(),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun KpiCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        elevation = 2.dp,
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = valueColor,
                    fontWeight = FontWeight.Bold,
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp).align(Alignment.TopEnd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProductsTable(
    products: List<Product>,
    onEditProduct: (String) -> Unit,
    onDeleteProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.padding(horizontal = 16.dp),
        style = AppCardStyle.Elevated,
        elevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TableHeader()

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Cube,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(products) { product ->
                        TableRow(
                            product = product,
                            onEdit = { onEditProduct(product.id) },
                            onDelete = { onDeleteProduct(product.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableHeaderCell("SKU", modifier = Modifier.width(80.dp))
        TableHeaderCell("Name", modifier = Modifier.weight(0.8f))
        TableHeaderCell("Category", modifier = Modifier.width(150.dp))
        TableHeaderCell("Price", modifier = Modifier.width(80.dp))
        TableHeaderCell("Stock", modifier = Modifier.width(60.dp))
        TableHeaderCell("Size", modifier = Modifier.width(60.dp))
        TableHeaderCell("Weight", modifier = Modifier.width(80.dp))
        TableHeaderCell("Status", modifier = Modifier.width(100.dp))
        TableHeaderCell("Actions", modifier = Modifier.width(80.dp))
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun TableRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    val status = getStockStatus(product)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(product.sku, modifier = Modifier.width(80.dp))
        TableCell(product.name, modifier = Modifier.weight(0.8f))
        TableCell(product.categoryName ?: "Uncategorized", modifier = Modifier.width(150.dp))
        TableCell(
            text = formatCurrency(product.sellingPrice),
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End,
        )
        TableCell(product.currentStock.toString(), modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
        TableCell(product.unit, modifier = Modifier.width(60.dp))
        TableCell("-", modifier = Modifier.width(80.dp))

        StatusChip(status, modifier = Modifier.width(100.dp))

        Row(
            modifier = Modifier.width(80.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Trash,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.ErrorDark,
                )
            }
        }
    }
}

@Composable
private fun TableCell(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(end = 8.dp),
        textAlign = textAlign,
    )
}

@Composable
private fun StatusChip(status: StockStatus, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, label) = when (status) {
        StockStatus.Good -> Triple(AppColors.Success, Color.White, "Good")
        StockStatus.Medium -> Triple(AppColors.NeutralLight600, Color.White, "Medium")
        StockStatus.Low -> Triple(AppColors.ErrorDark, Color.White, "Low")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}
