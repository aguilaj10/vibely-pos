package com.vibely.pos.ui.navigation

/**
 * Sealed class representing navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    /**
     * Login screen - entry point for unauthenticated users.
     */
    data object Login : Screen("login")

    /**
     * Dashboard screen - main screen after successful login.
     */
    data object Dashboard : Screen("dashboard")

    /**
     * Theme demo screen - for testing UI components.
     */
    data object ThemeDemo : Screen("theme_demo")
}
