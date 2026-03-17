package com.vibely.pos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A horizontal row skeleton with multiple shimmer rectangles.
 * Simulates table columns for product lists, sales lists, customer lists.
 *
 * @param modifier Modifier for customization
 * @param columnCount Number of column placeholders to show
 * @param columnWidths Optional list of widths for each column
 * @param height Height of each row (default: 48dp)
 * @param spacing Spacing between columns (default: 16dp)
 */
@Composable
fun SkeletonTableRow(modifier: Modifier = Modifier, columnCount: Int = 5, columnWidths: List<Dp>? = null, height: Dp = 48.dp, spacing: Dp = 16.dp) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(columnCount) { index ->
            val width = columnWidths?.getOrNull(index) ?: when (index) {
                0 -> 80.dp
                1 -> 150.dp
                else -> 100.dp
            }
            ShimmerPlaceholder(
                width = width,
                height = height,
                cornerRadius = 4.dp,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

/**
 * Multiple skeleton table rows for loading state.
 *
 * @param rowCount Number of rows to display
 * @param columnCount Number of columns per row
 * @param columnWidths Optional list of widths for each column
 * @param height Height of each row
 * @param spacing Spacing between columns
 * @param rowSpacing Spacing between rows
 */
@Composable
fun SkeletonTable(
    rowCount: Int = 5,
    columnCount: Int = 5,
    columnWidths: List<Dp>? = null,
    height: Dp = 48.dp,
    spacing: Dp = 16.dp,
    rowSpacing: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(rowCount) { index ->
            SkeletonTableRow(
                columnCount = columnCount,
                columnWidths = columnWidths,
                height = height,
                spacing = spacing,
            )
            if (index < rowCount - 1 && rowSpacing > 0.dp) {
                Spacer(modifier = Modifier.height(rowSpacing))
            }
        }
    }
}

/**
 * Skeleton table row that mimics the products table structure.
 * Columns: SKU, Name, Category, Price, Stock, Size, Weight, Status, Actions
 */
@Composable
fun SkeletonProductsTableRow(modifier: Modifier = Modifier) {
    SkeletonTableRow(
        modifier = modifier,
        columnCount = 9,
        columnWidths = listOf(
            80.dp,
            150.dp,
            120.dp,
            80.dp,
            60.dp,
            60.dp,
            80.dp,
            80.dp,
            80.dp,
        ),
    )
}

/**
 * Multiple skeleton product table rows.
 *
 * @param rowCount Number of rows to display
 */
@Composable
fun SkeletonProductsTable(rowCount: Int = 8, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(rowCount) { index ->
            SkeletonProductsTableRow()
        }
    }
}

/**
 * Skeleton table row that mimics the sales/transaction table structure.
 * Columns: ID, Date, Customer, Total, Status, Actions
 */
@Composable
fun SkeletonSalesTableRow(modifier: Modifier = Modifier) {
    SkeletonTableRow(
        modifier = modifier,
        columnCount = 6,
        columnWidths = listOf(
            80.dp,
            120.dp,
            150.dp,
            100.dp,
            100.dp,
            80.dp,
        ),
    )
}

/**
 * Multiple skeleton sales table rows.
 *
 * @param rowCount Number of rows to display
 */
@Composable
fun SkeletonSalesTable(rowCount: Int = 8, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(rowCount) { index ->
            SkeletonSalesTableRow()
        }
    }
}
