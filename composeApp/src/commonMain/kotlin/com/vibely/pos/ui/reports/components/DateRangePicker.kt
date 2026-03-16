package com.vibely.pos.ui.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Calendar
import compose.icons.fontawesomeicons.solid.CalendarAlt
import compose.icons.fontawesomeicons.solid.Clock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Composable
fun DateRangePicker(
    selectedPeriod: ReportPeriod,
    customStartDate: Instant?,
    customEndDate: Instant?,
    onPeriodSelected: (ReportPeriod, Instant?, Instant?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PeriodChip(
            label = "Today",
            icon = FontAwesomeIcons.Solid.Clock,
            isSelected = selectedPeriod == ReportPeriod.TODAY,
            onClick = { onPeriodSelected(ReportPeriod.TODAY, null, null) },
        )

        PeriodChip(
            label = "This Week",
            icon = FontAwesomeIcons.Solid.CalendarAlt,
            isSelected = selectedPeriod == ReportPeriod.THIS_WEEK,
            onClick = { onPeriodSelected(ReportPeriod.THIS_WEEK, null, null) },
        )

        PeriodChip(
            label = "This Month",
            icon = FontAwesomeIcons.Solid.Calendar,
            isSelected = selectedPeriod == ReportPeriod.THIS_MONTH,
            onClick = { onPeriodSelected(ReportPeriod.THIS_MONTH, null, null) },
        )

        PeriodChip(
            label = if (selectedPeriod == ReportPeriod.CUSTOM && customStartDate != null && customEndDate != null) {
                "Custom: ${formatShortDate(customStartDate)} - ${formatShortDate(customEndDate)}"
            } else {
                "Custom"
            },
            icon = FontAwesomeIcons.Solid.Calendar,
            isSelected = selectedPeriod == ReportPeriod.CUSTOM,
            onClick = {
                val now = Clock.System.now()
                val thirtyDaysAgo = now - 30.days
                onPeriodSelected(ReportPeriod.CUSTOM, thirtyDaysAgo, now)
            },
        )
    }
}

@Composable
private fun PeriodChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) {
        AppColors.Primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        Color.White
    } else {
        AppColors.TextPrimaryLight
    }

    val borderColor = if (isSelected) {
        AppColors.Primary
    } else {
        AppColors.NeutralLight300
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = contentColor,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}

private fun formatShortDate(timestamp: Instant): String {
    val epochMillis = timestamp.toEpochMilliseconds()
    val day = (epochMillis / (24 * 60 * 60 * 1000)) % 30 + 1
    return "Day $day"
}
