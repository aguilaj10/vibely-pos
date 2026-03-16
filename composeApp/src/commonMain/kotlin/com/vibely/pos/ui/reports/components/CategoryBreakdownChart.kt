package com.vibely.pos.ui.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.reports.entity.CategoryBreakdown
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.utils.formatCurrency
import com.vibely.pos.ui.utils.formatDecimal

@Composable
fun CategoryBreakdownChart(data: List<CategoryBreakdown>, modifier: Modifier = Modifier) {
    val chartColors = listOf(
        AppColors.Primary,
        AppColors.Info,
        AppColors.Warning,
        AppColors.AccentPurple,
        AppColors.AccentOrange,
        AppColors.Secondary,
    )

    val totalRevenue = remember(data) { data.sumOf { it.revenue } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Pie Chart
        Box(
            modifier = Modifier
                .weight(1f)
                .height(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier.size(180.dp),
            ) {
                val canvasSize = size.minDimension
                val radius = canvasSize / 2
                val innerRadius = radius * 0.6f

                var startAngle = -90f

                data.forEachIndexed { index, category ->
                    val sweepAngle = if (totalRevenue > 0) {
                        (category.revenue.toFloat() / totalRevenue) * 360f
                    } else {
                        0f
                    }

                    drawArc(
                        color = chartColors[index % chartColors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(
                            (canvasSize - radius * 2) / 2,
                            (canvasSize - radius * 2) / 2,
                        ),
                        size = Size(radius * 2, radius * 2),
                    )

                    startAngle += sweepAngle
                }

                // Inner circle for donut effect
                drawCircle(
                    color = Color.White,
                    radius = innerRadius,
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            data.take(6).forEachIndexed { index, category ->
                val percentage = if (totalRevenue > 0) {
                    (category.revenue.toFloat() / totalRevenue * 100)
                } else {
                    0f
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(
                            color = chartColors[index % chartColors.size],
                            radius = size.minDimension / 2,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.categoryName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "${category.revenue.formatCurrency()} (${percentage.toDouble().formatDecimal(1)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondaryLight,
                        )
                    }
                }
            }
        }
    }
}
