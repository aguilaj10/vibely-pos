package com.vibely.pos.ui.reports.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.ui.components.DataTable
import com.vibely.pos.ui.components.DataTableCell
import com.vibely.pos.ui.components.TableColumn
import com.vibely.pos.ui.utils.formatCurrency
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.reports_column_product
import vibely_pos.composeapp.generated.resources.reports_column_profit
import vibely_pos.composeapp.generated.resources.reports_column_qty_sold
import vibely_pos.composeapp.generated.resources.reports_column_revenue

@Composable
fun TopProductsTable(products: List<ProductPerformance>, modifier: Modifier = Modifier) {
    DataTable(
        columns =
        listOf(
            TableColumn(key = "product", label = stringResource(Res.string.reports_column_product), width = 180.dp),
            TableColumn(key = "qty", label = stringResource(Res.string.reports_column_qty_sold), width = 100.dp),
            TableColumn(key = "revenue", label = stringResource(Res.string.reports_column_revenue), width = 120.dp),
            TableColumn(key = "profit", label = stringResource(Res.string.reports_column_profit), width = 120.dp),
        ),
        data = products,
        rowKey = { it.productId },
        cellContent = { column, product ->
            when (column.key) {
                "product" -> DataTableCell(product.productName)
                "qty" -> DataTableCell(product.quantitySold.toString())
                "revenue" -> DataTableCell(product.revenue.formatCurrency())
                "profit" -> DataTableCell(product.profit.formatCurrency())
                else -> DataTableCell("")
            }
        },
        modifier = modifier,
    )
}
