package com.vibely.pos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Placeholder screen for features not yet implemented.
 *
 * Displays a "Coming Soon" message with the screen title.
 * Used for navigation structure while features are being developed.
 *
 * @param title The screen title to display.
 * @param icon Optional emoji icon for the screen.
 * @param description Optional description of what the screen will contain.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun PlaceholderScreen(title: String, icon: String? = null, description: String? = null, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon
            if (icon != null) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.displayLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Coming Soon
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Description
            if (description != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
