package com.vibely.pos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.theme.PosShapes

/**
 * Card style variants for Vibely POS
 */
enum class AppCardStyle {
    Elevated, // Card with elevation (shadow)
    Outlined, // Card with border
    Filled, // Card with background color
}

/**
 * Custom card component for Vibely POS
 * Provides consistent card styling across the application
 *
 * @param modifier Modifier for customization
 * @param style Card style variant
 * @param shape Card shape
 * @param containerColor Background color
 * @param contentColor Content color
 * @param elevation Elevation (for Elevated style)
 * @param border Border stroke (for Outlined style)
 * @param onClick Optional click handler (makes card clickable)
 * @param content Card content
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    style: AppCardStyle = AppCardStyle.Elevated,
    shape: Shape = PosShapes.ProductCard,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 2.dp,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    when (style) {
        AppCardStyle.Elevated -> {
            if (onClick != null) {
                ElevatedCard(
                    onClick = onClick,
                    modifier = modifier,
                    shape = shape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = elevation,
                    ),
                ) {
                    content()
                }
            } else {
                ElevatedCard(
                    modifier = modifier,
                    shape = shape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = elevation,
                    ),
                ) {
                    content()
                }
            }
        }
        AppCardStyle.Outlined -> {
            if (onClick != null) {
                OutlinedCard(
                    onClick = onClick,
                    modifier = modifier,
                    shape = shape,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                    border = border ?: BorderStroke(1.dp, AppColors.OutlineLight),
                ) {
                    content()
                }
            } else {
                OutlinedCard(
                    modifier = modifier,
                    shape = shape,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                    border = border ?: BorderStroke(1.dp, AppColors.OutlineLight),
                ) {
                    content()
                }
            }
        }
        AppCardStyle.Filled -> {
            if (onClick != null) {
                Card(
                    onClick = onClick,
                    modifier = modifier,
                    shape = shape,
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                ) {
                    content()
                }
            } else {
                Card(
                    modifier = modifier,
                    shape = shape,
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Specialized product card for POS system
 */
@Composable
fun ProductCard(name: String, price: String, modifier: Modifier = Modifier, imageContent: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Product image placeholder
            if (imageContent != null) {
                imageContent()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Product name
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Product price
            Text(
                text = price,
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.Primary,
            )
        }
    }
}

/**
 * Specialized summary card for dashboard/reports
 */
@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        containerColor = containerColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (icon != null) {
                icon()
            }
        }
    }
}
