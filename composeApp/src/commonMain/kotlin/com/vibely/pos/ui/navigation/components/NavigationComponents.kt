package com.vibely.pos.ui.navigation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.navigation.Screen

/**
 * Bottom navigation bar for mobile layouts.
 *
 * Shows primary navigation destinations with icons and labels.
 * Highlights the currently selected screen.
 *
 * @param currentScreen The currently active screen.
 * @param onNavigate Callback when a navigation item is clicked.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun BottomNavigationBar(currentScreen: Screen, onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier) {
    val primaryScreens = Screen.getPrimaryScreens()

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
    ) {
        primaryScreens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Text(
                        text = screen.icon ?: "•",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = currentScreen.route == screen.route,
                onClick = { onNavigate(screen) },
            )
        }
    }
}

/**
 * Side navigation drawer for desktop/tablet layouts.
 *
 * Shows all navigation destinations grouped by category.
 * Provides more comprehensive navigation than bottom bar.
 *
 * @param currentScreen The currently active screen.
 * @param onNavigate Callback when a navigation item is clicked.
 * @param onLogout Callback when logout is requested.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun NavigationDrawer(currentScreen: Screen, onNavigate: (Screen) -> Unit, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // Header
        Text(
            text = "📱 Vibely POS",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // Main Navigation
        NavigationSection(
            title = "Main",
            screens = listOf(Screen.Dashboard),
            currentScreen = currentScreen,
            onNavigate = onNavigate,
        )

        // Sales
        NavigationSection(
            title = "Sales",
            screens = listOf(Screen.Checkout, Screen.Sales),
            currentScreen = currentScreen,
            onNavigate = onNavigate,
        )

        // Inventory
        NavigationSection(
            title = "Inventory",
            screens = listOf(Screen.Inventory, Screen.Categories),
            currentScreen = currentScreen,
            onNavigate = onNavigate,
        )

        // Purchasing
        NavigationSection(
            title = "Purchasing",
            screens = listOf(Screen.Suppliers, Screen.PurchaseOrders),
            currentScreen = currentScreen,
            onNavigate = onNavigate,
        )

        // People
        NavigationSection(
            title = "People",
            screens = listOf(Screen.Customers, Screen.Users),
            currentScreen = currentScreen,
            onNavigate = onNavigate,
        )

        // Operations
        NavigationSection(
            title = "Operations",
            screens = listOf(Screen.Shifts, Screen.Reports),
            currentScreen = currentScreen,
            onNavigate = onNavigate,
        )

        // Settings
        NavigationSection(
            title = "Settings",
            screens = listOf(Screen.Settings),
            currentScreen = currentScreen,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Navigation section within the drawer.
 * Groups related screens under a section title.
 */
@Composable
private fun NavigationSection(title: String, screens: List<Screen>, currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
        )

        screens.forEach { screen ->
            NavigationItem(
                screen = screen,
                isSelected = currentScreen.route == screen.route,
                onClick = { onNavigate(screen) },
            )
        }
    }
}

/**
 * Individual navigation item within a section.
 */
@Composable
private fun NavigationItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    androidx.compose.material3.Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Text(
                text = screen.icon ?: "•",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
            )

            Spacer(modifier = Modifier.padding(end = 12.dp))

            // Label
            Text(
                text = screen.title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        }
    }
}
