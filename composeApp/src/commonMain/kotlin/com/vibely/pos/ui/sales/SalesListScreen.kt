package com.vibely.pos.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.valueobject.PaymentStatus
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import com.vibely.pos.shared.util.FormatUtils
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.EmptyState
import com.vibely.pos.ui.components.EmptyStateSize
import com.vibely.pos.ui.components.PaginationControls
import com.vibely.pos.ui.dialogs.RefundDialog
import com.vibely.pos.ui.navigation.Screen
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.ExclamationCircle
import compose.icons.fontawesomeicons.solid.Receipt
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Sync
import compose.icons.fontawesomeicons.solid.TimesCircle
import org.koin.compose.koinInject

@Composable
fun SalesListScreen(onNavigate: (Screen) -> Unit = {}, modifier: Modifier = Modifier, viewModel: SalesListViewModel = koinInject()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorDismiss()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setOnEditSaleCallback { sale ->
            onNavigate(Screen.CheckoutEdit(sale.id))
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSuccessMessageDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Receipt,
                        contentDescription = "Sales history",
                        modifier = Modifier.size(28.dp),
                        tint = AppColors.Primary,
                    )
                    Text(
                        text = "Sales History",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                AppButton(
                    text = if (state.isRefreshing) "Refreshing..." else "Refresh",
                    onClick = { viewModel.onRefresh() },
                    style = AppButtonStyle.Outlined,
                    enabled = !state.isRefreshing,
                    icon = {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Sync,
                            contentDescription = "Refresh sales",
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = "Search by invoice number or ID...",
                leadingIcon = {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading && !state.hasSales) {
                LoadingState()
            } else if (state.errorMessage != null && !state.hasSales) {
                ErrorState(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.loadSales() },
                )
            } else if (!state.hasSales) {
                EmptyState(
                    icon = FontAwesomeIcons.Solid.Receipt,
                    title = "No sales found",
                    description = "Complete a sale to see it here",
                    size = EmptyStateSize.Large,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 2.dp,
                    ),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SalesTableHeader()

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(state.sales) { sale ->
                                SalesTableRow(
                                    sale = sale,
                                    onClick = { viewModel.onSaleSelected(sale) },
                                    onRefund = { viewModel.onRefundSale(sale) },
                                    onEdit = { viewModel.onEditSale(sale) },
                                )
                            }
                        }

                        PaginationControls(
                            paginationState = state.pagination,
                            onPreviousPage = viewModel::onPreviousPage,
                            onNextPage = viewModel::onNextPage,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        state.confirmRefundSaleId?.let { saleId ->
            val sale = state.sales.find { it.id == saleId }
            if (sale != null) {
                RefundDialog(
                    sale = sale,
                    itemsCount = state.refundItemsCount,
                    onConfirm = { reason -> viewModel.onConfirmRefund(reason) },
                    onDismiss = viewModel::onDismissRefundConfirmation,
                )
            }
        }
    }
}

@Composable
private fun SalesTableRow(sale: Sale, onClick: () -> Unit, onRefund: () -> Unit, onEdit: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = sale.invoiceNumber,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = FormatUtils.formatDate(sale.saleDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = FormatUtils.formatCurrency(sale.totalAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.8f),
            )

            PaymentStatusBadge(
                status = sale.paymentStatus,
                modifier = Modifier.weight(0.8f),
            )

            SaleStatusBadge(
                status = sale.status,
                modifier = Modifier.weight(0.8f),
            )

            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                when {
                    sale.status == SaleStatus.DRAFT -> {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Edit,
                                contentDescription = "Edit sale",
                                tint = AppColors.Primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    sale.status == SaleStatus.COMPLETED && sale.paymentStatus == PaymentStatus.COMPLETED -> {
                        IconButton(
                            onClick = onRefund,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.TimesCircle,
                                contentDescription = "Refund",
                                tint = AppColors.Error,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    else -> {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp,
        )
    }
}

@Composable
private fun SalesTableHeader() {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Invoice #",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Date",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Total",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.8f),
        )
        Text(
            text = "Payment",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.8f),
        )
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.8f),
        )
    }
}

@Composable
private fun PaymentStatusBadge(status: PaymentStatus, modifier: Modifier = Modifier) {
    val (text, color) =
        when (status) {
            PaymentStatus.PENDING -> "Pending" to AppColors.Warning
            PaymentStatus.COMPLETED -> "Completed" to AppColors.Success
            PaymentStatus.FAILED -> "Failed" to AppColors.Error
            PaymentStatus.REFUNDED -> "Refunded" to AppColors.TextSecondaryLight
            PaymentStatus.CANCELLED -> "Cancelled" to AppColors.Error
        }

    Box(
        modifier =
        modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp),
            ).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun SaleStatusBadge(status: SaleStatus, modifier: Modifier = Modifier) {
    val (text, color) =
        when (status) {
            SaleStatus.DRAFT -> "Draft" to AppColors.TextSecondaryLight
            SaleStatus.COMPLETED -> "Completed" to AppColors.Success
            SaleStatus.CANCELLED -> "Cancelled" to AppColors.Error
            SaleStatus.REFUNDED -> "Refunded" to AppColors.Warning
            SaleStatus.PARTIALLY_REFUNDED -> "Partially Refunded" to AppColors.Info
        }

    Box(
        modifier =
        modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp),
            ).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    EmptyState(
        icon = FontAwesomeIcons.Solid.ExclamationCircle,
        title = "Failed to load sales",
        description = message,
        size = EmptyStateSize.Large,
        action = {
            AppButton(
                text = "Retry",
                onClick = onRetry,
                style = AppButtonStyle.Primary,
            )
        },
        modifier = Modifier.fillMaxSize(),
    )
}
