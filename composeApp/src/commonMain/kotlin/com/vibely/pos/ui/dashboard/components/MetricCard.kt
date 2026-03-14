package com.vibely.pos.ui.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle

/**
 * Metric card component for dashboard summary display.
 *
 * Displays a key performance indicator with an icon, label, and value.
 * Used for showing sales, transactions, stock alerts, and shift status.
 *
 * @param icon Emoji or icon character to display.
 * @param label Descriptive label for the metric.
 * @param value Metric value to display.
 * @param color Color for the value text (indicates status/severity).
 * @param modifier Optional modifier for customization.
 */
@Composable
fun MetricCard(icon: String, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        elevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                textAlign = TextAlign.Center,
            )
        }
    }
}
