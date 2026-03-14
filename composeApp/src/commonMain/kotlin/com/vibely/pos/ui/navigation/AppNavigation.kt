package com.vibely.pos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vibely.pos.config.DebugConfig
import com.vibely.pos.ui.auth.LoginScreen
import com.vibely.pos.ui.dashboard.DashboardScreen
import com.vibely.pos.ui.navigation.components.BottomNavigationBar
import com.vibely.pos.ui.screens.PlaceholderScreen
import com.vibely.pos.ui.screens.ThemeDemoScreen

/**
 * Main navigation controller for the application.
 *
 * Manages navigation between screens, authentication state, and layout structure.
 * Provides:
 * - Authentication guard (redirects to login if not authenticated)
 * - Bottom navigation bar for mobile layouts
 * - State-based navigation (simple but effective for Phase 1)
 *
 * Future enhancements:
 * - Side drawer for desktop/tablet layouts
 * - Deep linking support
 * - Back stack management
 * - Transition animations
 *
 * @param startDestination The initial screen to show. Defaults to Dashboard in debug mode, Login otherwise.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun AppNavigation(startDestination: Screen = if (DebugConfig.isDebugMode) Screen.Dashboard else Screen.Login, modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(startDestination) }
    var isAuthenticated by remember { mutableStateOf(DebugConfig.isDebugMode) }

    Box(modifier = modifier.fillMaxSize()) {
        // Authentication guard
        if (!isAuthenticated && currentScreen !in listOf(Screen.Login, Screen.ThemeDemo)) {
            currentScreen = Screen.Login
        }

        // Main content based on authentication state
        when {
            // Login screen (no navigation UI)
            currentScreen == Screen.Login -> {
                LoginScreen(
                    onLoginSuccess = {
                        isAuthenticated = true
                        currentScreen = Screen.Dashboard
                    },
                )
            }

            // Theme demo screen (no navigation UI)
            currentScreen == Screen.ThemeDemo -> {
                ThemeDemoScreen()
            }

            // Authenticated screens with navigation UI
            isAuthenticated -> {
                AuthenticatedScreenLayout(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        currentScreen = screen
                    },
                    onLogout = {
                        isAuthenticated = false
                        currentScreen = Screen.Login
                    },
                )
            }
        }
    }
}

/**
 * Layout for authenticated screens with bottom navigation.
 *
 * Provides consistent navigation structure across all authenticated screens.
 * Uses Scaffold for Material3 layout with bottom navigation bar.
 *
 * @param currentScreen The currently active screen.
 * @param onNavigate Callback when navigating to a different screen.
 * @param onLogout Callback when user logs out.
 */
@Composable
private fun AuthenticatedScreenLayout(currentScreen: Screen, onNavigate: (Screen) -> Unit, onLogout: () -> Unit) {
    Scaffold(
        bottomBar = {
            // Only show bottom nav for primary screens
            if (currentScreen in Screen.getPrimaryScreens()) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = onNavigate,
                )
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (currentScreen) {
                // Main screens
                Screen.Dashboard -> DashboardScreen(
                    onNavigate = onNavigate,
                    onLogout = onLogout,
                )

                // Sales screens
                Screen.Checkout -> PlaceholderScreen(
                    title = Screen.Checkout.title,
                    icon = Screen.Checkout.icon,
                    description = "Point of sale checkout screen for processing new sales transactions.",
                )
                Screen.Sales -> PlaceholderScreen(
                    title = Screen.Sales.title,
                    icon = Screen.Sales.icon,
                    description = "View and manage sales history, refunds, and transaction details.",
                )

                // Inventory screens
                Screen.Inventory -> PlaceholderScreen(
                    title = Screen.Inventory.title,
                    icon = Screen.Inventory.icon,
                    description = "Manage products, stock levels, and inventory adjustments.",
                )
                Screen.Categories -> PlaceholderScreen(
                    title = Screen.Categories.title,
                    icon = Screen.Categories.icon,
                    description = "Organize products into categories for better management.",
                )

                // Purchasing screens
                Screen.Suppliers -> PlaceholderScreen(
                    title = Screen.Suppliers.title,
                    icon = Screen.Suppliers.icon,
                    description = "Manage supplier information and contact details.",
                )
                Screen.PurchaseOrders -> PlaceholderScreen(
                    title = Screen.PurchaseOrders.title,
                    icon = Screen.PurchaseOrders.icon,
                    description = "Create and track purchase orders for restocking inventory.",
                )

                // People screens
                Screen.Customers -> PlaceholderScreen(
                    title = Screen.Customers.title,
                    icon = Screen.Customers.icon,
                    description = "Manage customer profiles, purchase history, and loyalty programs.",
                )
                Screen.Users -> PlaceholderScreen(
                    title = Screen.Users.title,
                    icon = Screen.Users.icon,
                    description = "Manage system users, roles, and permissions.",
                )

                // Operations screens
                Screen.Shifts -> PlaceholderScreen(
                    title = Screen.Shifts.title,
                    icon = Screen.Shifts.icon,
                    description = "Manage cash register shifts, opening/closing procedures.",
                )
                Screen.Reports -> PlaceholderScreen(
                    title = Screen.Reports.title,
                    icon = Screen.Reports.icon,
                    description = "View business reports, analytics, and performance metrics.",
                )

                // Settings
                Screen.Settings -> PlaceholderScreen(
                    title = Screen.Settings.title,
                    icon = Screen.Settings.icon,
                    description = "Configure app settings, preferences, and system options.",
                )

                // Fallback (should never happen)
                else -> PlaceholderScreen(
                    title = "Unknown Screen",
                    description = "This screen is not yet implemented.",
                )
            }
        }
    }
}
