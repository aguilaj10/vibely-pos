package com.vibely.pos.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.entity.TransactionStatus
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle

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
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        elevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Table header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Invoice",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider()

            // Transaction rows
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) },
                    )
                    if (transaction != transactions.last()) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/**
 * Individual transaction row component.
 */
@Composable
private fun TransactionRow(transaction: RecentTransaction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Invoice number
        Text(
            text = transaction.invoiceNumber,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Time
        val timeText = formatTransactionTime(transaction.saleDate)
        Text(
            text = timeText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Amount
        Text(
            text = transaction.totalAmount.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Status badge
        StatusBadge(
            status = transaction.status,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Status badge component for transaction status.
 */
@Composable
private fun StatusBadge(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (text, color, backgroundColor) = when (status) {
        TransactionStatus.COMPLETED -> Triple(
            "✓ Completed",
            Color(0xFF1B5E20), // Dark green
            Color(0xFFE8F5E9), // Light green
        )
        TransactionStatus.CANCELLED -> Triple(
            "✕ Cancelled",
            Color(0xFF6D6D6D), // Dark gray
            Color(0xFFF5F5F5), // Light gray
        )
        TransactionStatus.REFUNDED -> Triple(
            "↩ Refunded",
            Color(0xFFB71C1C), // Dark red
            Color(0xFFFFEBEE), // Light red
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Formats transaction timestamp for display.
 * Returns a placeholder for now until full datetime support is added.
 */
private fun formatTransactionTime(instant: kotlin.time.Instant): String {
    // Placeholder - will be properly implemented with kotlinx-datetime
    return "00:00"
}
