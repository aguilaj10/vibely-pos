package com.vibely.pos.ui.navigation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibely.pos.config.DebugConfig
import com.vibely.pos.debug.DebugUser
import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.usecase.GetCurrentUserUseCase
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.ui.navigation.Screen
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChevronLeft
import compose.icons.fontawesomeicons.solid.ChevronRight
import org.koin.compose.koinInject

/**
 * Left sidebar navigation for desktop/tablet layouts.
 * Matches design/checkout.png specifications:
 * - Fixed width: 240dp (expanded) / 64dp (collapsed)
 * - White background with dark text
 * - Selected state: black rounded pill + white content
 * - Unselected: dark gray icon/text
 * - Scrollable with LazyColumn
 * - Collapsible with smooth animations
 *
 * @param backStack The navigation back stack for navigation operations.
 * @param currentScreen The currently active screen.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun LeftSidebarNavigation(backStack: MutableList<Screen>, currentScreen: Screen, modifier: Modifier = Modifier) {
    val sidebarScreens = listOf(
        Screen.Dashboard,
        Screen.Checkout,
        Screen.Inventory,
        Screen.Categories,
        Screen.Suppliers,
        Screen.PurchaseOrders,
        Screen.Sales,
        Screen.Reports,
        Screen.Customers,
    )

    // Collapsible state with persistence across navigation
    var isExpanded by remember { mutableStateOf(true) }

    // Animated width transition
    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) 240.dp else 64.dp,
        label = "sidebarWidth",
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(sidebarWidth),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isExpanded) 16.dp else 8.dp, vertical = 24.dp),
            ) {
                if (isExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "POS",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Point of Sale",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Toggle button
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ChevronLeft,
                                contentDescription = "Collapse sidebar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                } else {
                    // Collapsed: Show only toggle button
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ChevronRight,
                                contentDescription = "Expand sidebar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }

            // User Info Section
            SidebarUserInfo(isExpanded = isExpanded)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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
                        isExpanded = isExpanded,
                    )
                }
            }
        }
    }
}

/**
 * Individual navigation item within the sidebar.
 *
 * @param screen The screen this item represents.
 * @param isSelected Whether this item is currently selected.
 * @param onClick Callback when the item is clicked.
 * @param isExpanded Whether the sidebar is in expanded state.
 */
@Composable
private fun SidebarNavItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit, isExpanded: Boolean) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isExpanded) 12.dp else 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (isExpanded) 12.dp else 8.dp,
                vertical = 10.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            screen.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = screen.title,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor,
                )
            }

            // Label - Animated visibility
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + androidx.compose.animation.fadeIn(),
                exit = shrinkVertically() + androidx.compose.animation.fadeOut(),
            ) {
                Row {
                    if (screen.icon != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
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

@Composable
private fun SidebarUserInfo(isExpanded: Boolean) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    val getCurrentUserUseCase: GetCurrentUserUseCase = koinInject()

    LaunchedEffect(Unit) {
        currentUser = if (DebugConfig.isDebugMode) {
            DebugUser.createMockUser()
        } else {
            when (val result = getCurrentUserUseCase()) {
                is Result.Success -> result.data
                is Result.Error -> null
            }
        }
    }

    val user = currentUser

    if (user == null && !DebugConfig.isDebugMode) {
        return
    }

    val displayUser = user ?: DebugUser.createMockUser()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isExpanded) 12.dp else 8.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isExpanded) 12.dp else 8.dp),
            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar with initials
            UserAvatar(
                fullName = displayUser.fullName,
                role = displayUser.role,
                size = if (isExpanded) 40.dp else 36.dp,
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayUser.fullName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        RoleBadge(role = displayUser.role)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserAvatar(fullName: String, role: UserRole, size: Dp) {
    val initials = fullName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "?" }

    val backgroundColor = getRoleColor(role)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = if (size.value >= 40) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    val backgroundColor = getRoleColor(role).copy(alpha = 0.15f)
    val textColor = getRoleColor(role)

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor,
    ) {
        Text(
            text = role.displayName(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

private fun getRoleColor(role: UserRole): Color = when (role) {
    UserRole.ADMIN -> Color(0xFF6366F1)
    UserRole.MANAGER -> Color(0xFF10B981)
    UserRole.CASHIER -> Color(0xFFF59E0B)
    UserRole.WAREHOUSE -> Color(0xFF8B5CF6)
    UserRole.VIEWER -> Color(0xFF6B7280)
}
