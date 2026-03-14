package com.vibely.pos.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.pos.config.DebugConfig
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.DebugModeBadge
import com.vibely.pos.ui.dashboard.components.MetricCard
import com.vibely.pos.ui.dashboard.components.QuickActionButtons
import com.vibely.pos.ui.dashboard.components.RecentTransactionsTable
import org.koin.compose.koinInject

/**
 * Dashboard screen - main view after login.
 *
 * Displays:
 * - Summary metrics (sales, transactions, stock alerts, shift status)
 * - Recent transactions table
 * - Quick action buttons for common operations
 *
 * Features:
 * - Pull-to-refresh support
 * - Loading states with skeleton screens
 * - Error handling with retry capability
 * - Empty states for no data
 * - Debug mode badge in development
 *
 * @param onNavigate Callback for navigation to other screens.
 * @param onLogout Callback when user logs out.
 * @param modifier Optional modifier for customization.
 * @param viewModel The DashboardViewModel (injected via Koin).
 */
@Composable
fun DashboardScreen(
    onNavigate: (com.vibely.pos.ui.navigation.Screen) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Debug mode badge
            if (DebugConfig.isDebugMode) {
                DebugModeBadge()
            }

            // Loading state (initial load)
            if (state.isLoading && !state.hasData) {
                LoadingState()
                return@Column
            }

            // Error state (no data available)
            if (state.errorMessage != null && !state.hasData) {
                ErrorState(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.loadDashboard() },
                )
                return@Column
            }

            // Dashboard content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "📊 Dashboard",
                        style = MaterialTheme.typography.headlineLarge,
                    )

                    // Refresh button
                    AppButton(
                        text = if (state.isRefreshing) "↻ Refreshing..." else "↻ Refresh",
                        onClick = { viewModel.onRefresh() },
                        style = AppButtonStyle.Outlined,
                        enabled = !state.isRefreshing,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Metric cards
                state.summary?.let { summary ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Today's Sales
                        MetricCard(
                            icon = "💰",
                            label = "Today's Sales",
                            value = summary.todaySales.toString(),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )

                        // Transactions Count
                        MetricCard(
                            icon = "🧾",
                            label = "Transactions",
                            value = summary.todayTransactionCount.toString(),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                        )

                        // Low Stock Alerts
                        MetricCard(
                            icon = "⚠️",
                            label = "Low Stock",
                            value = summary.lowStockCount.toString(),
                            color = if (summary.hasLowStockAlerts()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.weight(1f),
                        )

                        // Shift Status
                        MetricCard(
                            icon = if (summary.hasActiveShift()) "✅" else "❌",
                            label = "Shift",
                            value = if (summary.hasActiveShift()) "Open" else "Closed",
                            color = if (summary.hasActiveShift()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Recent Transactions section
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                if (state.hasRecentTransactions) {
                    RecentTransactionsTable(
                        transactions = state.recentTransactions,
                        modifier = Modifier.fillMaxWidth(),
                        onTransactionClick = { transaction ->
                            // TODO: Navigate to transaction details
                        },
                    )
                } else {
                    EmptyTransactionsState()
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Quick Actions section
                Text(
                    text = "🚀 Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                QuickActionButtons(
                    onActionClick = { action ->
                        when (action) {
                            QuickAction.NEW_SALE -> onNavigate(com.vibely.pos.ui.navigation.Screen.Checkout)
                            QuickAction.INVENTORY -> onNavigate(com.vibely.pos.ui.navigation.Screen.Inventory)
                            QuickAction.REPORTS -> onNavigate(com.vibely.pos.ui.navigation.Screen.Reports)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Error toast (overlay)
        if (state.errorMessage != null && state.hasData) {
            ErrorToast(
                message = state.errorMessage!!,
                onDismiss = { viewModel.onErrorDismiss() },
            )
        }
    }
}

/**
 * Loading state component with spinner.
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading dashboard...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Error state component with retry button.
 */
@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "❌",
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Failed to load dashboard",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            AppButton(
                text = "Retry",
                onClick = onRetry,
                style = AppButtonStyle.Primary,
            )
        }
    }
}

/**
 * Empty state for when there are no recent transactions.
 */
@Composable
private fun EmptyTransactionsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "📭",
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No recent transactions",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Error toast component for displaying errors while data is visible.
 */
@Composable
private fun ErrorToast(message: String, onDismiss: () -> Unit) {
    // Simple error display - in production would use Snackbar or custom toast
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Dismiss",
                    onClick = onDismiss,
                    style = AppButtonStyle.Text,
                )
            }
        }
    }
}
