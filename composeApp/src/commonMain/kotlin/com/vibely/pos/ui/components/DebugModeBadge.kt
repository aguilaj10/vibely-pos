package com.vibely.pos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.config.DebugConfig

/**
 * Badge that displays when debug mode is active.
 * Shows a prominent "🔧 DEBUG MODE" indicator in the UI.
 */
@Composable
fun DebugModeBadge(modifier: Modifier = Modifier) {
    if (DebugConfig.isDebugMode) {
        Surface(
            modifier = modifier.padding(8.dp),
            color = MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(4.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🔧",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = "DEBUG MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}
