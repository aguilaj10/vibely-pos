package com.vibely.pos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vibely.pos.ui.auth.LoginScreen
import com.vibely.pos.ui.screens.ThemeDemoScreen

/**
 * Main navigation controller for the application.
 *
 * Manages navigation between screens and authentication state.
 * In a production app, this would use Compose Navigation or Voyager.
 *
 * For Phase 1, we implement a simple state-based navigation:
 * - Start at Login screen
 * - Navigate to Dashboard on successful login
 *
 * @param startDestination The initial screen to show.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun AppNavigation(startDestination: Screen = Screen.Login, modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(startDestination) }

    Box(modifier = modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.Login -> {
                LoginScreen(
                    onLoginSuccess = {
                        currentScreen = Screen.Dashboard
                    },
                )
            }
            Screen.Dashboard -> {
                DashboardScreen(
                    onLogout = {
                        currentScreen = Screen.Login
                    },
                )
            }
            Screen.ThemeDemo -> {
                ThemeDemoScreen()
            }
        }
    }
}

/**
 * Placeholder Dashboard screen.
 *
 * This will be replaced with the actual dashboard implementation
 * in future phases.
 */
@Composable
private fun DashboardScreen(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "🎉 Login Successful!\n\nDashboard coming soon...",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
