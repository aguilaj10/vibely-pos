package com.vibely.pos.ui.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.utils.formatCurrency
import com.vibely.pos.ui.utils.formatPercentage
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.reports_metric_profit_margin
import vibely_pos.composeapp.generated.resources.reports_metric_total_profit
import vibely_pos.composeapp.generated.resources.reports_metric_total_sales
import vibely_pos.composeapp.generated.resources.reports_metric_transactions

@Composable
fun MetricsCards(totalSales: Long, totalProfit: Long, profitMargin: Float, transactionCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MetricCard(
            label = stringResource(Res.string.reports_metric_total_sales),
            value = totalSales.formatCurrency(),
            color = AppColors.Primary,
            modifier = Modifier.weight(1f),
        )

        MetricCard(
            label = stringResource(Res.string.reports_metric_total_profit),
            value = totalProfit.formatCurrency(),
            color = if (totalProfit >= 0) AppColors.Success else AppColors.Error,
            modifier = Modifier.weight(1f),
        )

        MetricCard(
            label = stringResource(Res.string.reports_metric_profit_margin),
            value = profitMargin.toDouble().formatPercentage(1),
            color =
            when {
                profitMargin >= 30 -> AppColors.Success
                profitMargin >= 15 -> AppColors.Warning
                else -> AppColors.Error
            },
            modifier = Modifier.weight(1f),
        )

        MetricCard(
            label = stringResource(Res.string.reports_metric_transactions),
            value = transactionCount.toString(),
            color = AppColors.Info,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        elevation = 2.dp,
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondaryLight,
            )

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(top = 4.dp),
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}
