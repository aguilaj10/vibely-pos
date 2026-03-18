package com.vibely.pos.ui.reports.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.shared.util.FormatUtils
import com.vibely.pos.ui.components.DataTable
import com.vibely.pos.ui.components.DataTableCell
import com.vibely.pos.ui.components.TableColumn

@Composable
fun TopCustomersTable(customers: List<CustomerAnalytics>, modifier: Modifier = Modifier) {
    DataTable(
        columns =
        listOf(
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
                "totalSpent" -> DataTableCell(FormatUtils.formatCurrency(customer.totalSpent / 100.0))
                "visits" -> DataTableCell(customer.visitCount.toString())
                "lastVisit" -> DataTableCell(FormatUtils.formatDate(customer.lastVisit))
                else -> DataTableCell("")
            }
        },
        modifier = modifier,
    )
}
