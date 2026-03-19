package com.vibely.pos.ui.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.reports.entity.SalesTrend
import com.vibely.pos.shared.util.FormatUtils
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.utils.formatCurrency

@Composable
fun SalesTrendChart(data: List<SalesTrend>, modifier: Modifier = Modifier) {
    val primaryColor = AppColors.Primary
    val gridColor = AppColors.NeutralLight300
    val textColor = AppColors.TextSecondaryLight

    val maxRevenue = remember(data) { data.maxOfOrNull { it.revenue } ?: 1L }
    val minRevenue = remember(data) { data.minOfOrNull { it.revenue } ?: 0L }
    val revenueRange = maxRevenue - minRevenue

    Column(modifier = modifier) {
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                val width = size.width
                val height = size.height
                val padding = 40f

                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2

                // Draw grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = padding + (chartHeight / gridLines) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f,
                    )
                }

                if (data.isEmpty() || revenueRange == 0L) return@Canvas

                // Draw line chart
                val path = Path()
                val points =
                    data.mapIndexed { index, trend ->
                        val x = padding + (chartWidth / (data.size - 1).coerceAtLeast(1)) * index
                        val normalizedY = ((trend.revenue - minRevenue).toFloat() / revenueRange)
                        val y = padding + chartHeight * (1 - normalizedY)
                        Offset(x, y)
                    }

                points.forEachIndexed { index, point ->
                    if (index == 0) {
                        path.moveTo(point.x, point.y)
                    } else {
                        path.lineTo(point.x, point.y)
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3f),
                )

                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = primaryColor,
                        radius = 5f,
                        center = point,
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = point,
                    )
                }
            }
        }

        // Y-axis labels
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = maxRevenue.toDouble().formatCurrency(),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-axis labels (time)
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
        ) {
            data.take(5).forEachIndexed { index, trend ->
                if (index > 0) Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = FormatUtils.formatShortDate(trend.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
