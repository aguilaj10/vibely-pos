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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.config.DebugConfig
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.DebugModeBadge
import com.vibely.pos.ui.components.EmptyState
import com.vibely.pos.ui.components.EmptyStateSize
import com.vibely.pos.ui.components.ErrorState
import com.vibely.pos.ui.components.ShimmerPlaceholder
import com.vibely.pos.ui.components.SkeletonDashboardMetrics
import com.vibely.pos.ui.components.SkeletonSalesTable
import com.vibely.pos.ui.dashboard.components.MetricCard
import com.vibely.pos.ui.dashboard.components.QuickActionButtons
import com.vibely.pos.ui.dashboard.components.RecentTransactionsTable
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChartLine
import compose.icons.fontawesomeicons.solid.CheckCircle
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.Receipt
import compose.icons.fontawesomeicons.solid.Sync
import compose.icons.fontawesomeicons.solid.TimesCircle
import org.koin.compose.koinInject

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
            if (DebugConfig.isDebugMode) {
                DebugModeBadge()
            }

            if (state.isLoading && !state.hasData) {
                LoadingState()
                return@Column
            }

            if (state.errorMessage != null && !state.hasData) {
                ErrorState(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.loadDashboard() },
                    title = "Failed to load dashboard",
                )
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ChartLine,
                            contentDescription = "Dashboard overview",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }

                    AppButton(
                        text = if (state.isRefreshing) "Refreshing..." else "Refresh",
                        onClick = { viewModel.onRefresh() },
                        style = AppButtonStyle.Outlined,
                        enabled = !state.isRefreshing,
                        icon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Sync,
                                contentDescription = "Refresh dashboard",
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                state.summary?.let { summary ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        MetricCard(
                            icon = FontAwesomeIcons.Solid.ChartLine,
                            label = "Today's Sales",
                            value = summary.todaySales.toString(),
                            color = AppColors.Success,
                            modifier = Modifier.weight(1f),
                        )

                        MetricCard(
                            icon = FontAwesomeIcons.Solid.Receipt,
                            label = "Transactions",
                            value = summary.todayTransactionCount.toString(),
                            color = AppColors.Info,
                            modifier = Modifier.weight(1f),
                        )

                        MetricCard(
                            icon = FontAwesomeIcons.Solid.ExclamationTriangle,
                            label = "Low Stock",
                            value = summary.lowStockCount.toString(),
                            color = if (summary.hasLowStockAlerts()) {
                                AppColors.Warning
                            } else {
                                AppColors.Success
                            },
                            modifier = Modifier.weight(1f),
                        )

                        MetricCard(
                            icon = if (summary.hasActiveShift()) {
                                FontAwesomeIcons.Solid.CheckCircle
                            } else {
                                FontAwesomeIcons.Solid.TimesCircle
                            },
                            label = "Shift",
                            value = if (summary.hasActiveShift()) "Open" else "Closed",
                            color = if (summary.hasActiveShift()) {
                                AppColors.Success
                            } else {
                                AppColors.TextSecondaryLight
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                            // Navigate to Sales screen (transaction detail view not yet implemented)
                            println("📊 Transaction clicked: ${transaction.id} - Navigate to Sales screen")
                            onNavigate(com.vibely.pos.ui.navigation.Screen.Sales)
                        },
                    )
                } else {
                    EmptyState(
                        icon = FontAwesomeIcons.Solid.Receipt,
                        title = "No recent transactions",
                        description = "Start a new sale to see transactions here",
                        size = EmptyStateSize.Medium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

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
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerPlaceholder(
                    width = 28.dp,
                    height = 28.dp,
                    cornerRadius = 4.dp,
                )
                ShimmerPlaceholder(
                    width = 150.dp,
                    height = 32.dp,
                    cornerRadius = 4.dp,
                )
            }
            ShimmerPlaceholder(
                width = 100.dp,
                height = 36.dp,
                cornerRadius = 8.dp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SkeletonDashboardMetrics()

        Spacer(modifier = Modifier.height(32.dp))

        ShimmerPlaceholder(
            width = 200.dp,
            height = 24.dp,
            cornerRadius = 4.dp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SkeletonSalesTable(rowCount = 5)

        Spacer(modifier = Modifier.height(32.dp))

        ShimmerPlaceholder(
            width = 150.dp,
            height = 24.dp,
            cornerRadius = 4.dp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(3) {
                ShimmerPlaceholder(
                    width = 150.dp,
                    height = 48.dp,
                    cornerRadius = 8.dp,
                )
            }
        }
    }
}
