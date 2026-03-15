package com.vibely.pos.ui.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.theme.AppColors

/**
 * Metric card component for dashboard summary display.
 *
 * Displays a key performance indicator with an icon, label, and value.
 * Used for showing sales, transactions, stock alerts, and shift status.
 *
 * @param icon Font Awesome icon to display.
 * @param label Descriptive label for the metric.
 * @param value Metric value to display.
 * @param color Color for the icon and value text (indicates status/severity).
 * @param modifier Optional modifier for customization.
 */
@Composable
fun MetricCard(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        elevation = 2.dp,
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextSecondaryLight,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color,
                    fontWeight = FontWeight.Bold,
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp).align(Alignment.TopEnd),
                tint = color,
            )
        }
    }
}
