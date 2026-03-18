package com.vibely.pos.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Box
import compose.icons.fontawesomeicons.solid.ChartBar
import compose.icons.fontawesomeicons.solid.ChartLine
import compose.icons.fontawesomeicons.solid.ClipboardList
import compose.icons.fontawesomeicons.solid.Clock
import compose.icons.fontawesomeicons.solid.Cog
import compose.icons.fontawesomeicons.solid.ExchangeAlt
import compose.icons.fontawesomeicons.solid.Palette
import compose.icons.fontawesomeicons.solid.Receipt
import compose.icons.fontawesomeicons.solid.ShoppingCart
import compose.icons.fontawesomeicons.solid.Tags
import compose.icons.fontawesomeicons.solid.Truck
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.Users

/**
 * Sealed class representing navigation destinations in the app.
 *
 * Organized into logical groups:
 * - Authentication: Login
 * - Main: Dashboard
 * - Sales: Checkout, Sales History
 * - Inventory: Products, Categories, Stock
 * - Purchasing: Suppliers, Purchase Orders
 * - People: Customers, Users
 * - Operations: Shifts, Reports
 * - Settings: App Configuration
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    /**
     * Login screen - entry point for unauthenticated users.
     */
    data object Login : Screen("login", "Login")

    /**
     * Dashboard screen - main screen after successful login.
     * Shows key metrics, recent transactions, and quick actions.
     */
    data object Dashboard : Screen("dashboard", "Dashboard", FontAwesomeIcons.Solid.ChartLine)

    /**
     * Checkout screen - POS checkout for new sales.
     */
    data object Checkout : Screen("checkout", "New Sale", FontAwesomeIcons.Solid.ShoppingCart)

    /**
     * Sales history screen - view past transactions.
     */
    data object Sales : Screen("sales", "Sales", FontAwesomeIcons.Solid.Receipt)

    /**
     * Inventory screen - manage products and stock levels.
     */
    data object Inventory : Screen("inventory", "Inventory", FontAwesomeIcons.Solid.Box)

    /**
     * Categories screen - manage product categories.
     */
    data object Categories : Screen("categories", "Categories", FontAwesomeIcons.Solid.Tags)

    /**
     * Suppliers screen - manage suppliers.
     */
    data object Suppliers : Screen("suppliers", "Suppliers", FontAwesomeIcons.Solid.Truck)

    /**
     * Purchase orders screen - manage purchase orders.
     */
    data object PurchaseOrders : Screen("purchase-orders", "Purchase Orders", FontAwesomeIcons.Solid.ClipboardList)

    /**
     * Customers screen - manage customer information.
     */
    data object Customers : Screen("customers", "Customers", FontAwesomeIcons.Solid.Users)

    /**
     * Users screen - manage system users and permissions.
     */
    data object Users : Screen("users", "Users", FontAwesomeIcons.Solid.User)

    /**
     * Shifts screen - manage cash register shifts.
     */
    data object Shifts : Screen("shifts", "Shifts", FontAwesomeIcons.Solid.Clock)

    /**
     * Reports screen - view business reports and analytics.
     */
    data object Reports : Screen("reports", "Reports", FontAwesomeIcons.Solid.ChartBar)

    /**
     * Settings screen - app configuration and preferences.
     */
    data object Settings : Screen("settings", "Settings", FontAwesomeIcons.Solid.Cog)

    /**
     * Exchange rates screen - manage currency exchange rates.
     */
    data object ExchangeRates : Screen("exchange-rates", "Exchange Rates", FontAwesomeIcons.Solid.ExchangeAlt)

    /**
     * Theme demo screen - for testing UI components (dev only).
     */
    data object ThemeDemo : Screen("theme_demo", "Theme Demo", FontAwesomeIcons.Solid.Palette)

    companion object {
        /**
         * Returns all main navigation screens (excludes Login and ThemeDemo).
         */
        fun getMainScreens(): List<Screen> = listOf(
            Dashboard,
            Checkout,
            Sales,
            Inventory,
            Categories,
            Suppliers,
            PurchaseOrders,
            Customers,
            Users,
            Shifts,
            Reports,
            Settings,
            ExchangeRates,
        )

        /**
         * Returns primary navigation screens for bottom nav / drawer.
         */
        fun getPrimaryScreens(): List<Screen> = listOf(
            Dashboard,
            Checkout,
            Inventory,
            Sales,
            Reports,
        )

        /**
         * Returns screens that require authentication.
         */
        fun getAuthenticatedScreens(): List<Screen> = getMainScreens()
    }
}
