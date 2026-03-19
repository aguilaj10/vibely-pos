package com.vibely.pos.ui.reports.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.shared.util.FormatUtils
import com.vibely.pos.ui.components.DataTable
import com.vibely.pos.ui.components.DataTableCell
import com.vibely.pos.ui.components.TableColumn
import com.vibely.pos.ui.utils.formatCurrency
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.reports_column_customer
import vibely_pos.composeapp.generated.resources.reports_column_last_visit
import vibely_pos.composeapp.generated.resources.reports_column_total_spent
import vibely_pos.composeapp.generated.resources.reports_column_visits

@Composable
fun TopCustomersTable(customers: List<CustomerAnalytics>, modifier: Modifier = Modifier) {
    DataTable(
        columns =
        listOf(
            TableColumn(
                key = "customer",
                label = stringResource(Res.string.reports_column_customer),
                width = 160.dp,
            ),
            TableColumn(
                key = "totalSpent",
                label = stringResource(Res.string.reports_column_total_spent),
                width = 120.dp,
            ),
            TableColumn(key = "visits", label = stringResource(Res.string.reports_column_visits), width = 80.dp),
            TableColumn(
                key = "lastVisit",
                label = stringResource(Res.string.reports_column_last_visit),
                width = 120.dp,
            ),
        ),
        data = customers,
        rowKey = { it.customerId ?: "walk-in" },
        cellContent = { column, customer ->
            when (column.key) {
                "customer" -> DataTableCell(customer.customerName)
                "totalSpent" -> DataTableCell((customer.totalSpent / 100.0).formatCurrency())
                "visits" -> DataTableCell(customer.visitCount.toString())
                "lastVisit" -> DataTableCell(FormatUtils.formatDate(customer.lastVisit))
                else -> DataTableCell("")
            }
        },
        modifier = modifier,
    )
}
