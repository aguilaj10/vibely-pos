package com.vibely.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors

data class TableColumn(
    val key: String,
    val label: String,
    val width: Dp = 120.dp,
    val sortable: Boolean = false,
    val alignment: TextAlign = TextAlign.Start,
    val headerIcon: ImageVector? = null,
)

enum class SortDirection {
    Ascending,
    Descending,
    None,
}

data class SortState(val columnKey: String = "", val direction: SortDirection = SortDirection.None)

@Composable
fun <T> DataTable(
    columns: List<TableColumn>,
    data: List<T>,
    rowKey: (T) -> String,
    modifier: Modifier = Modifier,
    sortState: SortState = SortState(),
    onSort: ((String) -> Unit)? = null,
    onRowClick: ((T) -> Unit)? = null,
    actionsColumn: (@Composable (T) -> Unit)? = null,
    isLoading: Boolean = false,
    emptyContent: (@Composable () -> Unit)? = null,
    cellContent: @Composable (TableColumn, T) -> Unit,
) {
    var currentSort by remember(sortState) { mutableStateOf(sortState) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, AppColors.OutlineLight, RoundedCornerShape(10.dp))
            .background(AppColors.SurfaceLight),
    ) {
        TableHeader(
            columns = columns,
            sortState = currentSort,
            onSort = { columnKey ->
                val newDirection = when {
                    currentSort.columnKey == columnKey -> when (currentSort.direction) {
                        SortDirection.None -> SortDirection.Ascending
                        SortDirection.Ascending -> SortDirection.Descending
                        SortDirection.Descending -> SortDirection.None
                    }
                    else -> SortDirection.Ascending
                }
                currentSort = SortState(
                    columnKey = if (newDirection == SortDirection.None) "" else columnKey,
                    direction = newDirection,
                )
                onSort?.invoke(columnKey)
            },
            hasActions = actionsColumn != null,
        )

        HorizontalDivider(color = AppColors.OutlineLight, thickness = 1.dp)

        if (data.isEmpty() && !isLoading) {
            if (emptyContent != null) {
                emptyContent()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondaryLight,
                    )
                }
            }
        } else {
            data.forEachIndexed { index, item ->
                TableRow(
                    columns = columns,
                    item = item,
                    cellContent = cellContent,
                    onRowClick = onRowClick,
                    actionsContent = actionsColumn?.let { { it(item) } },
                )
                if (index < data.size - 1) {
                    HorizontalDivider(color = AppColors.OutlineLight.copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun TableHeader(columns: List<TableColumn>, sortState: SortState, onSort: (String) -> Unit, hasActions: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.NeutralLight100)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { column ->
            TableHeaderCell(
                column = column,
                sortState = sortState,
                onSort = onSort,
            )
        }
        if (hasActions) {
            Box(modifier = Modifier.width(100.dp))
        }
    }
}

@Composable
private fun TableHeaderCell(column: TableColumn, sortState: SortState, onSort: (String) -> Unit) {
    val isSorted = sortState.columnKey == column.key

    Row(
        modifier = Modifier
            .width(column.width)
            .then(
                if (column.sortable) {
                    Modifier.clickable { onSort(column.key) }
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (column.alignment) {
            TextAlign.End -> Arrangement.End
            TextAlign.Center -> Arrangement.Center
            else -> Arrangement.Start
        },
    ) {
        if (column.headerIcon != null) {
            androidx.compose.material3.Icon(
                imageVector = column.headerIcon,
                contentDescription = null,
                modifier = Modifier
                    .height(16.dp)
                    .width(16.dp),
                tint = AppColors.TextSecondaryLight,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = column.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isSorted) AppColors.Primary else AppColors.TextSecondaryLight,
        )
        if (column.sortable) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when {
                    !isSorted || sortState.direction == SortDirection.None -> ""
                    sortState.direction == SortDirection.Ascending -> "↑"
                    else -> "↓"
                },
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Primary,
            )
        }
    }
}

@Composable
private fun <T> TableRow(
    columns: List<TableColumn>,
    item: T,
    cellContent: @Composable (TableColumn, T) -> Unit,
    onRowClick: ((T) -> Unit)?,
    actionsContent: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.SurfaceLight)
            .then(
                if (onRowClick != null) {
                    Modifier.clickable { onRowClick(item) }
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { column ->
            Box(
                modifier = Modifier.width(column.width),
                contentAlignment = when (column.alignment) {
                    TextAlign.End -> Alignment.CenterEnd
                    TextAlign.Center -> Alignment.Center
                    else -> Alignment.CenterStart
                },
            ) {
                cellContent(column, item)
            }
        }
        if (actionsContent != null) {
            Box(modifier = Modifier.width(100.dp)) {
                actionsContent()
            }
        }
    }
}

@Composable
fun DataTableCell(text: String, modifier: Modifier = Modifier, color: Color = AppColors.TextPrimaryLight) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = modifier,
    )
}

@Composable
fun DataTableActions(onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null, onView: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (onView != null) {
            AppIconButton(
                text = "View",
                contentDescription = "View",
                onClick = onView,
                size = AppIconButtonSize.Small,
                variant = AppIconButtonVariant.Ghost,
            )
        }
        if (onEdit != null) {
            AppIconButton(
                text = "Edit",
                contentDescription = "Edit",
                onClick = onEdit,
                size = AppIconButtonSize.Small,
                variant = AppIconButtonVariant.Ghost,
            )
        }
        if (onDelete != null) {
            AppIconButton(
                text = "Delete",
                contentDescription = "Delete",
                onClick = onDelete,
                size = AppIconButtonSize.Small,
                variant = AppIconButtonVariant.Error,
            )
        }
    }
}
