package com.vibely.pos.ui.reports.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.ui.components.DataTable
import com.vibely.pos.ui.components.DataTableCell
import com.vibely.pos.ui.components.TableColumn
import com.vibely.pos.ui.utils.formatCurrency

@Composable
fun TopCustomersTable(customers: List<CustomerAnalytics>, modifier: Modifier = Modifier) {
    DataTable(
        columns = listOf(
            TableColumn(key = "customer", label = "Customer", width = 160.dp),
            TableColumn(key = "totalSpent", label = "Total Spent", width = 120.dp),
            TableColumn(key = "visits", label = "Visits", width = 80.dp),
            TableColumn(key = "lastVisit", label = "Last Visit", width = 120.dp),
        ),
        data = customers,
        rowKey = { it.customerId ?: "walk-in" },
        cellContent = { column, customer ->
            when (column.key) {
                "customer" -> DataTableCell(customer.customerName)
                "totalSpent" -> DataTableCell(customer.totalSpent.formatCurrency())
                "visits" -> DataTableCell(customer.visitCount.toString())
                "lastVisit" -> DataTableCell(formatDate(customer.lastVisit))
                else -> DataTableCell("")
            }
        },
        modifier = modifier,
    )
}

private fun formatDate(timestamp: kotlin.time.Instant): String {
    val daysSinceEpoch = (timestamp.toEpochMilliseconds() / (24 * 60 * 60 * 1000)).toInt()
    val year = 1970 + (daysSinceEpoch / 365)
    val dayOfYear = daysSinceEpoch % 365
    val month = (dayOfYear / 30) + 1
    val day = (dayOfYear % 30) + 1
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${months[month - 1]} ${day.toString().padStart(2, '0')}, $year"
}
