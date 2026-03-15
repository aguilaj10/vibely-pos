package com.vibely.pos.ui.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.navigation.Screen
import com.vibely.pos.ui.theme.AppColors

/**
 * Left sidebar navigation for desktop/tablet layouts.
 * Matches design/checkout.png specifications:
 * - Fixed width: 240dp
 * - White background with dark text
 * - Selected state: black rounded pill + white content
 * - Unselected: dark gray icon/text
 * - Scrollable with LazyColumn
 *
 * @param backStack The navigation back stack for navigation operations.
 * @param currentScreen The currently active screen.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun LeftSidebarNavigation(backStack: MutableList<Screen>, currentScreen: Screen, modifier: Modifier = Modifier) {
    val sidebarScreens = listOf(
        Screen.Checkout,
        Screen.Inventory,
        Screen.Categories,
        Screen.Suppliers,
        Screen.PurchaseOrders,
        Screen.Sales,
        Screen.Reports,
        Screen.Customers,
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                Text(
                    text = "POS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Point of Sale",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondaryLight,
                )
            }

            HorizontalDivider(color = AppColors.NeutralLight300)

            // Navigation Items - Scrollable
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(sidebarScreens) { screen ->
                    SidebarNavItem(
                        screen = screen,
                        isSelected = currentScreen.route == screen.route,
                        onClick = { backStack.add(screen) },
                    )
                }
            }
        }
    }
}

/**
 * Individual navigation item within the sidebar.
 */
@Composable
private fun SidebarNavItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) AppColors.Primary else Color.Transparent
    val contentColor = if (isSelected) Color.White else AppColors.TextPrimaryLight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            screen.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = screen.title,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Label
            Text(
                text = screen.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

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
                    screen.icon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(20.dp),
                        )
                    }
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
            text = "Vibely POS",
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
            screen.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = screen.title,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = screen.title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        }
    }
}
