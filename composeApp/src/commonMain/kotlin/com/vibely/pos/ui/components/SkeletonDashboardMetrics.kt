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
 * Row of 4 metric card skeletons for dashboard.
 * Each card shows: icon circle + 2 text lines.
 * Use case: Dashboard top metrics loading state.
 *
 * @param modifier Modifier for customization
 * @param horizontalSpacing Spacing between cards
 */
@Composable
fun SkeletonDashboardMetrics(modifier: Modifier = Modifier, horizontalSpacing: Dp = 16.dp) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
    ) {
        SkeletonMetricCard(modifier = Modifier.weight(1f))
        SkeletonMetricCard(modifier = Modifier.weight(1f))
        SkeletonMetricCard(modifier = Modifier.weight(1f))
        SkeletonMetricCard(modifier = Modifier.weight(1f))
    }
}

/**
 * Single KPI card skeleton for inventory screen.
 */
@Composable
fun SkeletonKpiCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            ShimmerPlaceholder(
                width = 24.dp,
                height = 24.dp,
                cornerRadius = 4.dp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ShimmerPlaceholder(
            width = 100.dp,
            height = 14.dp,
            cornerRadius = 4.dp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ShimmerPlaceholder(
            width = 80.dp,
            height = 28.dp,
            cornerRadius = 4.dp,
        )
    }
}

/**
 * Row of KPI card skeletons for inventory screen.
 * Similar to dashboard metrics but different layout.
 *
 * @param modifier Modifier for customization
 * @param cardCount Number of KPI cards to show
 * @param horizontalSpacing Spacing between cards
 */
@Composable
fun SkeletonKpiCardsRow(modifier: Modifier = Modifier, cardCount: Int = 4, horizontalSpacing: Dp = 16.dp) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
    ) {
        repeat(cardCount) {
            SkeletonKpiCard(modifier = Modifier.weight(1f))
        }
    }
}
