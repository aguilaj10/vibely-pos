@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.vibely.pos.ui.purchaseorders

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
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.util.FormatUtils.formatCurrency
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.dialogs.ConfirmationDialog
import com.vibely.pos.ui.dialogs.PurchaseOrderFormData
import com.vibely.pos.ui.dialogs.PurchaseOrderFormDialog
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Calculator
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Trash
import compose.icons.fontawesomeicons.solid.Truck
import compose.icons.fontawesomeicons.solid.TruckLoading
import org.koin.compose.koinInject
import kotlin.time.Instant

@Composable
fun PurchaseOrdersScreen(
    onNavigate: (com.vibely.pos.ui.navigation.Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PurchaseOrdersViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorDismiss()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSuccessMessageDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PurchaseOrdersHeader(
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onClearSearch = viewModel::onClearSearch,
                onCreatePurchaseOrder = viewModel::onCreatePurchaseOrder,
            )

            Spacer(modifier = Modifier.height(16.dp))

            KpiCardsRow(
                totalOrders = state.totalOrders,
                pendingOrders = state.pendingOrders,
                totalAmount = state.totalAmount,
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
                PurchaseOrdersTable(
                    purchaseOrders = state.purchaseOrders,
                    onViewPurchaseOrder = viewModel::onViewPurchaseOrder,
                    onEditPurchaseOrder = viewModel::onEditPurchaseOrder,
                    onDeletePurchaseOrder = viewModel::onDeletePurchaseOrder,
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

        if (state.showPODialog) {
            val editingPO = state.editingPO
            PurchaseOrderFormDialog(
                isEdit = editingPO != null,
                initialData = editingPO?.let {
                    PurchaseOrderFormData(
                        id = it.id,
                        supplierId = it.supplierId,
                        notes = it.notes ?: "",
                        lineItems = it.items.map { item ->
                            com.vibely.pos.ui.dialogs.LineItemFormData(
                                id = item.id,
                                productId = item.productId,
                                quantity = item.quantity.toString(),
                                unitCost = item.unitCost.toString(),
                            )
                        },
                    )
                },
                suppliers = state.suppliers,
                products = state.products,
                onSave = viewModel::onSavePurchaseOrder,
                onDismiss = viewModel::onDismissPODialog,
            )
        }

        if (state.showDeleteDialog) {
            ConfirmationDialog(
                title = "Delete Purchase Order",
                message = "Are you sure you want to delete this purchase order? This action cannot be undone.",
                confirmText = "Delete",
                onConfirm = viewModel::onConfirmDelete,
                onDismiss = viewModel::onDismissDeleteDialog,
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun PurchaseOrdersHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCreatePurchaseOrder: () -> Unit,
) {
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
            placeholder = "Search by PO number, supplier...",
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
                            imageVector = FontAwesomeIcons.Solid.Truck,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            singleLine = true,
        )

        AppButton(
            text = "Create PO",
            onClick = onCreatePurchaseOrder,
            style = AppButtonStyle.Primary,
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.TruckLoading,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

@Composable
private fun KpiCardsRow(totalOrders: Int, pendingOrders: Int, totalAmount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KpiCard(
            icon = FontAwesomeIcons.Solid.Truck,
            label = "Total Orders",
            value = totalOrders.toString(),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.TruckLoading,
            label = "Pending Orders",
            value = pendingOrders.toString(),
            valueColor = AppColors.Warning,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.Calculator,
            label = "Total Amount",
            value = formatCurrency(totalAmount),
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
private fun PurchaseOrdersTable(
    purchaseOrders: List<PurchaseOrder>,
    onViewPurchaseOrder: (String) -> Unit,
    onEditPurchaseOrder: (String) -> Unit,
    onDeletePurchaseOrder: (String) -> Unit,
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

            if (purchaseOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Truck,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No purchase orders found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(purchaseOrders) { order ->
                        TableRow(
                            purchaseOrder = order,
                            onView = { onViewPurchaseOrder(order.id) },
                            onEdit = { onEditPurchaseOrder(order.id) },
                            onDelete = { onDeletePurchaseOrder(order.id) },
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
        TableHeaderCell("PO Number", modifier = Modifier.width(100.dp))
        TableHeaderCell("Supplier", modifier = Modifier.weight(1f))
        TableHeaderCell("Status", modifier = Modifier.width(100.dp))
        TableHeaderCell("Order Date", modifier = Modifier.width(120.dp))
        TableHeaderCell("Expected Delivery", modifier = Modifier.width(140.dp))
        TableHeaderCell("Total Amount", modifier = Modifier.width(120.dp))
        TableHeaderCell("Actions", modifier = Modifier.width(120.dp))
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
private fun TableRow(purchaseOrder: PurchaseOrder, onView: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(purchaseOrder.poNumber, modifier = Modifier.width(100.dp))
        TableCell(purchaseOrder.supplierName ?: "-", modifier = Modifier.weight(1f))

        StatusChip(status = purchaseOrder.status, modifier = Modifier.width(100.dp))

        TableCell(
            formatDate(purchaseOrder.orderDate),
            modifier = Modifier.width(120.dp),
        )

        TableCell(
            purchaseOrder.expectedDeliveryDate?.let { formatDate(it) } ?: "-",
            modifier = Modifier.width(140.dp),
        )

        TableCell(
            formatCurrency(purchaseOrder.totalAmount),
            modifier = Modifier.width(120.dp),
        )

        Row(
            modifier = Modifier.width(120.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(
                onClick = onView,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Eye,
                    contentDescription = "View",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

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
private fun TableCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun StatusChip(status: PurchaseOrderStatus, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, label) = when (status) {
        PurchaseOrderStatus.DRAFT -> Triple(AppColors.NeutralLight400, AppColors.TextPrimaryLight, "Draft")
        PurchaseOrderStatus.PENDING -> Triple(AppColors.Warning, Color.White, "Pending")
        PurchaseOrderStatus.APPROVED -> Triple(AppColors.Primary, Color.White, "Approved")
        PurchaseOrderStatus.RECEIVED -> Triple(AppColors.Success, Color.White, "Received")
        PurchaseOrderStatus.CANCELLED -> Triple(AppColors.ErrorDark, Color.White, "Cancelled")
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

private fun formatDate(instant: Instant): String {
    val epochMillis = instant.toEpochMilliseconds()
    val seconds = epochMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    val year = 1970 + (days / 365).toInt()
    val dayOfYear = (days % 365).toInt()
    val month = (dayOfYear / 30).coerceIn(1, 12)
    val day = (dayOfYear % 30).coerceIn(1, 31)

    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${monthNames[month - 1]} $day, $year"
}
