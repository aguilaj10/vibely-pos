package com.vibely.pos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.PosShapes

/**
 * A card skeleton with shimmer effect for loading states.
 * Matches the dimensions and styling of AppCard for visual consistency.
 *
 * @param modifier Modifier for customization
 * @param height Height of the skeleton card (default: matches card content)
 * @param width Width of the skeleton card
 * @param cornerRadius Corner radius for rounded corners
 * @param showIcon Whether to show an icon placeholder at the top
 * @param showTitle Whether to show a title line placeholder
 * @param showSubtitle Whether to show a subtitle/value line placeholder
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp? = null,
    width: Dp? = null,
    cornerRadius: Dp = 10.dp,
    showIcon: Boolean = true,
    showTitle: Boolean = true,
    showSubtitle: Boolean = true,
) {
    Column(
        modifier = modifier
            .then(
                if (width != null) {
                    Modifier.width(width)
                } else {
                    Modifier.fillMaxWidth()
                },
            )
            .then(
                if (height != null) {
                    Modifier.height(height)
                } else {
                    Modifier
                },
            )
            .clip(RoundedCornerShape(cornerRadius))
            .padding(16.dp),
    ) {
        if (showIcon) {
            ShimmerPlaceholder(
                width = 40.dp,
                height = 40.dp,
                cornerRadius = 8.dp,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showTitle) {
            ShimmerPlaceholder(
                width = 80.dp,
                height = 14.dp,
                cornerRadius = 4.dp,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showSubtitle) {
            ShimmerPlaceholder(
                width = 120.dp,
                height = 24.dp,
                cornerRadius = 4.dp,
            )
        }
    }
}

/**
 * A compact skeleton card for dashboard metrics.
 * Shows an icon circle placeholder with title and value lines.
 *
 * @param modifier Modifier for customization
 * @param iconSize Size of the icon placeholder circle
 */
@Composable
fun SkeletonMetricCard(modifier: Modifier = Modifier, iconSize: Dp = 40.dp) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PosShapes.ProductCard)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerPlaceholder(
                width = iconSize,
                height = iconSize,
                cornerRadius = 8.dp,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ShimmerPlaceholder(
            width = 60.dp,
            height = 12.dp,
            cornerRadius = 4.dp,
        )

        Spacer(modifier = Modifier.height(6.dp))

        ShimmerPlaceholder(
            width = 100.dp,
            height = 20.dp,
            cornerRadius = 4.dp,
        )
    }
}

/**
 * A skeleton card with icon, title, and value (like dashboard metric cards).
 *
 * @param modifier Modifier for customization
 * @param showIcon Whether to show icon placeholder
 */
@Composable
fun SkeletonSummaryCard(modifier: Modifier = Modifier, showIcon: Boolean = true) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PosShapes.ProductCard)
            .padding(16.dp),
    ) {
        if (showIcon) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                ShimmerPlaceholder(
                    width = 24.dp,
                    height = 24.dp,
                    cornerRadius = 4.dp,
                )
            }
        }

        ShimmerPlaceholder(
            width = 80.dp,
            height = 14.dp,
            cornerRadius = 4.dp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ShimmerPlaceholder(
            width = 120.dp,
            height = 32.dp,
            cornerRadius = 4.dp,
        )
    }
}
