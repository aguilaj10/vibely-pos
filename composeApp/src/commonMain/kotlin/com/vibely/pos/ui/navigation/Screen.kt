package com.vibely.pos.ui.navigation

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
sealed class Screen(val route: String, val title: String, val icon: String? = null) {
    // ==================== Authentication ====================
    /**
     * Login screen - entry point for unauthenticated users.
     */
    data object Login : Screen("login", "Login")

    // ==================== Main ====================
    /**
     * Dashboard screen - main screen after successful login.
     * Shows key metrics, recent transactions, and quick actions.
     */
    data object Dashboard : Screen("dashboard", "Dashboard", "📊")

    // ==================== Sales ====================
    /**
     * Checkout screen - POS checkout for new sales.
     */
    data object Checkout : Screen("checkout", "New Sale", "🛒")

    /**
     * Sales history screen - view past transactions.
     */
    data object Sales : Screen("sales", "Sales", "💰")

    // ==================== Inventory ====================
    /**
     * Inventory screen - manage products and stock levels.
     */
    data object Inventory : Screen("inventory", "Inventory", "📦")

    /**
     * Categories screen - manage product categories.
     */
    data object Categories : Screen("categories", "Categories", "🏷️")

    // ==================== Purchasing ====================
    /**
     * Suppliers screen - manage suppliers.
     */
    data object Suppliers : Screen("suppliers", "Suppliers", "🏭")

    /**
     * Purchase orders screen - manage purchase orders.
     */
    data object PurchaseOrders : Screen("purchase-orders", "Purchase Orders", "📋")

    // ==================== People ====================
    /**
     * Customers screen - manage customer information.
     */
    data object Customers : Screen("customers", "Customers", "👥")

    /**
     * Users screen - manage system users and permissions.
     */
    data object Users : Screen("users", "Users", "👤")

    // ==================== Operations ====================
    /**
     * Shifts screen - manage cash register shifts.
     */
    data object Shifts : Screen("shifts", "Shifts", "⏰")

    /**
     * Reports screen - view business reports and analytics.
     */
    data object Reports : Screen("reports", "Reports", "📈")

    // ==================== Settings ====================
    /**
     * Settings screen - app configuration and preferences.
     */
    data object Settings : Screen("settings", "Settings", "⚙️")

    // ==================== Development ====================
    /**
     * Theme demo screen - for testing UI components (dev only).
     */
    data object ThemeDemo : Screen("theme_demo", "Theme Demo", "🎨")

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
