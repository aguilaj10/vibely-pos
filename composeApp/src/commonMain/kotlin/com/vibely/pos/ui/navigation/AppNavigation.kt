package com.vibely.pos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.vibely.pos.config.DebugConfig
import com.vibely.pos.ui.auth.LoginScreen
import com.vibely.pos.ui.customers.CustomersScreen
import com.vibely.pos.ui.dashboard.DashboardScreen
import com.vibely.pos.ui.inventory.InventoryScreen
import com.vibely.pos.ui.navigation.components.LeftSidebarNavigation
import com.vibely.pos.ui.screens.PlaceholderScreen
import com.vibely.pos.ui.screens.ThemeDemoScreen
import com.vibely.pos.ui.screens.categories.CategoriesScreen
import com.vibely.pos.ui.suppliers.SuppliersScreen

/**
 * Main navigation controller for the application.
 *
 * Manages navigation between screens, authentication state, and layout structure.
 * Uses Navigation3 for Kotlin Multiplatform navigation support.
 *
 * @param startDestination The initial screen to show. Defaults to Dashboard in debug mode, Login otherwise.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun AppNavigation(startDestination: Screen = if (DebugConfig.isDebugMode) Screen.Dashboard else Screen.Login, modifier: Modifier = Modifier) {
    var isAuthenticated by remember { mutableStateOf(DebugConfig.isDebugMode) }

    val backStack = remember { mutableStateListOf(startDestination) }
    val currentScreen = backStack.lastOrNull() ?: startDestination

    Box(modifier = modifier.fillMaxSize()) {
        // Authentication guard
        if (!isAuthenticated && currentScreen !in listOf(Screen.Login, Screen.ThemeDemo)) {
            backStack.clear()
            backStack.add(Screen.Login)
        }

        // Main content based on authentication state
        when {
            // Login screen (no navigation UI)
            currentScreen == Screen.Login && !isAuthenticated -> {
                LoginScreen(
                    onLoginSuccess = {
                        isAuthenticated = true
                        backStack.clear()
                        backStack.add(Screen.Dashboard)
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
                    backStack = backStack,
                    onLogout = {
                        isAuthenticated = false
                        backStack.clear()
                        backStack.add(Screen.Login)
                    },
                )
            }

            // Fallback to login
            else -> {
                LoginScreen(
                    onLoginSuccess = {
                        isAuthenticated = true
                        backStack.clear()
                        backStack.add(Screen.Dashboard)
                    },
                )
            }
        }
    }
}

/**
 * Layout for authenticated screens with left sidebar navigation.
 *
 * Provides consistent navigation structure across all authenticated screens.
 * Uses left sidebar for desktop/tablet layouts.
 *
 * @param backStack The navigation back stack.
 * @param onLogout Callback when user logs out.
 */
@Composable
private fun AuthenticatedScreenLayout(backStack: MutableList<Screen>, onLogout: () -> Unit) {
    val currentScreen = backStack.lastOrNull() ?: Screen.Dashboard

    Row(modifier = Modifier.fillMaxSize()) {
        LeftSidebarNavigation(
            backStack = backStack,
            currentScreen = currentScreen,
            modifier = Modifier.fillMaxHeight(),
        )

        Box(modifier = Modifier.weight(1f)) {
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeLast() },
                entryProvider = { key ->
                    when (key) {
                        is Screen.Dashboard -> NavEntry(key) {
                            DashboardScreen(
                                onNavigate = { screen -> backStack.add(screen) },
                                onLogout = onLogout,
                            )
                        }

                        is Screen.Checkout -> NavEntry(key) {
                            PlaceholderScreen(
                                title = Screen.Checkout.title,
                                icon = Screen.Checkout.icon,
                                description = "Point of sale checkout screen for processing new sales transactions.",
                            )
                        }

                        is Screen.Sales -> NavEntry(key) {
                            PlaceholderScreen(
                                title = Screen.Sales.title,
                                icon = Screen.Sales.icon,
                                description = "View and manage sales history, refunds, and transaction details.",
                            )
                        }

                        is Screen.Inventory -> NavEntry(key) {
                            InventoryScreen(
                                onNavigate = { screen -> backStack.add(screen) },
                            )
                        }

                        is Screen.Categories -> NavEntry(key) {
                            CategoriesScreen(
                                onNavigate = { screen -> backStack.add(screen) },
                            )
                        }

                        is Screen.Suppliers -> NavEntry(key) {
                            SuppliersScreen(
                                onNavigate = { screen -> backStack.add(screen) },
                            )
                        }

                        is Screen.PurchaseOrders -> NavEntry(key) {
                            PlaceholderScreen(
                                title = Screen.PurchaseOrders.title,
                                icon = Screen.PurchaseOrders.icon,
                                description = "Create and track purchase orders for restocking inventory.",
                            )
                        }

                        is Screen.Customers -> NavEntry(key) {
                            CustomersScreen(
                                onNavigate = { screen -> backStack.add(screen) },
                            )
                        }

                        is Screen.Users -> NavEntry(key) {
                            PlaceholderScreen(
                                title = Screen.Users.title,
                                icon = Screen.Users.icon,
                                description = "Manage system users, roles, and permissions.",
                            )
                        }

                        is Screen.Shifts -> NavEntry(key) {
                            PlaceholderScreen(
                                title = Screen.Shifts.title,
                                icon = Screen.Shifts.icon,
                                description = "Manage cash register shifts, opening/closing procedures.",
                            )
                        }

                        is Screen.Reports -> NavEntry(key) {
                            PlaceholderScreen(
                                title = Screen.Reports.title,
                                icon = Screen.Reports.icon,
                                description = "View business reports, analytics, and performance metrics.",
                            )
                        }

                        is Screen.Settings -> NavEntry(key) {
                            PlaceholderScreen(
                                title = Screen.Settings.title,
                                icon = Screen.Settings.icon,
                                description = "Configure app settings, preferences, and system options.",
                            )
                        }

                        else -> error("Unknown route: $key")
                    }
                },
            )
        }
    }
}
