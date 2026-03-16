package com.vibely.pos.ui.reports.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.ui.components.DataTable
import com.vibely.pos.ui.components.DataTableCell
import com.vibely.pos.ui.components.TableColumn
import com.vibely.pos.ui.utils.formatCurrency

@Composable
fun TopProductsTable(products: List<ProductPerformance>, modifier: Modifier = Modifier) {
    DataTable(
        columns = listOf(
            TableColumn(key = "product", label = "Product", width = 180.dp),
            TableColumn(key = "qty", label = "Qty Sold", width = 100.dp),
            TableColumn(key = "revenue", label = "Revenue", width = 120.dp),
            TableColumn(key = "profit", label = "Profit", width = 120.dp),
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
