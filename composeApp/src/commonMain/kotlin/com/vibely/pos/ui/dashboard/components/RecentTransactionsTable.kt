package com.vibely.pos.ui.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.entity.TransactionStatus
import com.vibely.pos.shared.util.FormatUtils
import com.vibely.pos.ui.components.DataTable
import com.vibely.pos.ui.components.DataTableCell
import com.vibely.pos.ui.components.StatusChip
import com.vibely.pos.ui.components.StatusChipVariant
import com.vibely.pos.ui.components.TableColumn
import com.vibely.pos.ui.utils.formatCurrency
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.dashboard_transaction_column_amount
import vibely_pos.composeapp.generated.resources.dashboard_transaction_column_invoice
import vibely_pos.composeapp.generated.resources.dashboard_transaction_column_status
import vibely_pos.composeapp.generated.resources.dashboard_transaction_column_time

/**
 * Recent transactions table component for dashboard.
 *
 * Displays a scrollable list of recent sale transactions with
 * invoice number, time, amount, and status badge.
 *
 * @param transactions List of recent transactions to display.
 * @param modifier Optional modifier for customization.
 * @param onTransactionClick Callback when a transaction is clicked.
 */
@Composable
fun RecentTransactionsTable(
    transactions: List<RecentTransaction>,
    modifier: Modifier = Modifier,
    onTransactionClick: (RecentTransaction) -> Unit = {},
) {
    DataTable(
        columns =
        listOf(
            TableColumn(
                key = "invoice",
                label = stringResource(Res.string.dashboard_transaction_column_invoice),
                width = 120.dp,
            ),
            TableColumn(
                key = "time",
                label = stringResource(Res.string.dashboard_transaction_column_time),
                width = 100.dp,
            ),
            TableColumn(
                key = "amount",
                label = stringResource(Res.string.dashboard_transaction_column_amount),
                width = 100.dp,
            ),
            TableColumn(
                key = "status",
                label = stringResource(Res.string.dashboard_transaction_column_status),
                width = 120.dp,
            ),
        ),
        data = transactions,
        rowKey = { it.invoiceNumber },
        cellContent = { column, transaction ->
            when (column.key) {
                "invoice" -> DataTableCell(transaction.invoiceNumber)
                "time" -> DataTableCell(FormatUtils.formatDateTime(transaction.saleDate).substringAfterLast(' '))
                "amount" -> DataTableCell(transaction.totalAmount.amount.let { it.formatCurrency() })
                "status" -> TransactionStatusChip(transaction.status)
                else -> DataTableCell("")
            }
        },
        onRowClick = onTransactionClick,
        modifier = modifier,
    )
}

@Composable
private fun TransactionStatusChip(status: TransactionStatus) {
    val (label, variant) =
        when (status) {
            TransactionStatus.DRAFT -> "Draft" to StatusChipVariant.Info
            TransactionStatus.COMPLETED -> "Completed" to StatusChipVariant.Success
            TransactionStatus.CANCELLED -> "Cancelled" to StatusChipVariant.Info
            TransactionStatus.REFUNDED -> "Refunded" to StatusChipVariant.Error
        }

    StatusChip(
        label = label,
        variant = variant,
    )
}
