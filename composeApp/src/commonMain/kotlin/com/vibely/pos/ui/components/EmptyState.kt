package com.vibely.pos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors

enum class EmptyStateSize {
    Small,
    Medium,
    Large,
}

@Composable
fun EmptyState(
    icon: ImageVector?,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    size: EmptyStateSize = EmptyStateSize.Medium,
    action: (@Composable () -> Unit)? = null,
) {
    val (iconSize, spacing) = when (size) {
        EmptyStateSize.Small -> 48.dp to 12.dp
        EmptyStateSize.Medium -> 64.dp to 16.dp
        EmptyStateSize.Large -> 80.dp to 24.dp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = AppColors.NeutralLight500,
            )
            Spacer(modifier = Modifier.height(spacing))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimaryLight,
            textAlign = TextAlign.Center,
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondaryLight,
                textAlign = TextAlign.Center,
            )
        }

        if (action != null) {
            Spacer(modifier = Modifier.height(spacing))
            action()
        }
    }
}

@Composable
fun EmptyListState(itemName: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    EmptyState(
        icon = null,
        title = "No $itemName found",
        description = "Get started by adding your first $itemName",
        modifier = modifier,
        action = action,
    )
}

@Composable
fun EmptySearchState(searchQuery: String, modifier: Modifier = Modifier) {
    EmptyState(
        icon = null,
        title = "No results found",
        description = "No results for \"$searchQuery\"",
        modifier = modifier,
    )
}

@Composable
fun EmptyDataState(modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    EmptyState(
        icon = null,
        title = "No data available",
        description = "There is no data to display at the moment",
        modifier = modifier,
        action = action,
    )
}
