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
import com.vibely.pos.ui.checkout.CheckoutScreen
import com.vibely.pos.ui.customers.CustomersScreen
import com.vibely.pos.ui.dashboard.DashboardScreen
import com.vibely.pos.ui.exchangerates.ExchangeRatesScreen
import com.vibely.pos.ui.inventory.InventoryScreen
import com.vibely.pos.ui.navigation.components.LeftSidebarNavigation
import com.vibely.pos.ui.purchaseorders.PurchaseOrdersScreen
import com.vibely.pos.ui.reports.ReportsScreen
import com.vibely.pos.ui.sales.SalesListScreen
import com.vibely.pos.ui.screens.ThemeDemoScreen
import com.vibely.pos.ui.screens.categories.CategoriesScreen
import com.vibely.pos.ui.settings.SettingsScreen
import com.vibely.pos.ui.shifts.ShiftsScreen
import com.vibely.pos.ui.suppliers.SuppliersScreen
import com.vibely.pos.ui.users.UsersScreen

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
 */
@Composable
private fun AuthenticatedScreenLayout(backStack: MutableList<Screen>) {
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
                        is Screen.Dashboard -> {
                            NavEntry(key) {
                                DashboardScreen(
                                    onNavigate = { screen -> backStack.add(screen) },
                                )
                            }
                        }

                        is Screen.Checkout -> {
                            NavEntry(key) {
                                CheckoutScreen(
                                    onNavigate = { screen -> backStack.add(screen) },
                                )
                            }
                        }

                        is Screen.CheckoutEdit -> {
                            NavEntry(key) {
                                CheckoutScreen(
                                    onNavigate = { navScreen -> backStack.add(navScreen) },
                                    saleId = key.saleId,
                                )
                            }
                        }

                        is Screen.Sales -> {
                            NavEntry(key) {
                                SalesListScreen(
                                    onNavigate = { screen -> backStack.add(screen) },
                                )
                            }
                        }

                        is Screen.Inventory -> {
                            NavEntry(key) {
                                InventoryScreen()
                            }
                        }

                        is Screen.Categories -> {
                            NavEntry(key) {
                                CategoriesScreen()
                            }
                        }

                        is Screen.Suppliers -> {
                            NavEntry(key) {
                                SuppliersScreen()
                            }
                        }

                        is Screen.PurchaseOrders -> {
                            NavEntry(key) {
                                PurchaseOrdersScreen()
                            }
                        }

                        is Screen.Customers -> {
                            NavEntry(key) {
                                CustomersScreen()
                            }
                        }

                        is Screen.Users -> {
                            NavEntry(key) {
                                UsersScreen()
                            }
                        }

                        is Screen.Shifts -> {
                            NavEntry(key) {
                                ShiftsScreen()
                            }
                        }

                        is Screen.Reports -> {
                            NavEntry(key) {
                                ReportsScreen(
                                    onNavigate = { screen -> backStack.add(screen) },
                                )
                            }
                        }

                        is Screen.Settings -> {
                            NavEntry(key) {
                                SettingsScreen(
                                    onNavigate = { screen -> backStack.add(screen) },
                                )
                            }
                        }

                        is Screen.ExchangeRates -> {
                            NavEntry(key) {
                                ExchangeRatesScreen()
                            }
                        }

                        else -> {
                            error("Unknown route: $key")
                        }
                    }
                },
            )
        }
    }
}
