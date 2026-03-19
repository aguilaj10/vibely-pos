package com.vibely.pos.ui.reports

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.EmptyState
import com.vibely.pos.ui.components.EmptyStateSize
import com.vibely.pos.ui.components.ErrorState
import com.vibely.pos.ui.reports.components.CategoryBreakdownChart
import com.vibely.pos.ui.reports.components.DateRangePicker
import com.vibely.pos.ui.reports.components.MetricsCards
import com.vibely.pos.ui.reports.components.SalesTrendChart
import com.vibely.pos.ui.reports.components.TopCustomersTable
import com.vibely.pos.ui.reports.components.TopProductsTable
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Box
import compose.icons.fontawesomeicons.solid.ChartBar
import compose.icons.fontawesomeicons.solid.ChartLine
import compose.icons.fontawesomeicons.solid.ChartPie
import compose.icons.fontawesomeicons.solid.Sync
import compose.icons.fontawesomeicons.solid.Users
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.reports_category_breakdown
import vibely_pos.composeapp.generated.resources.reports_loading
import vibely_pos.composeapp.generated.resources.reports_sales_trend
import vibely_pos.composeapp.generated.resources.reports_title
import vibely_pos.composeapp.generated.resources.reports_top_customers
import vibely_pos.composeapp.generated.resources.reports_top_products

/**
 * Reports screen composable displaying business analytics and performance metrics.
 *
 * Shows comprehensive reports including sales summaries, trends, category breakdowns,
 * top products, and customer analytics for selected time periods.
 *
 * @param onNavigate Navigation callback for screen transitions.
 * @param onLogout Callback for logout action.
 * @param modifier Optional modifier for customization.
 * @param viewModel ViewModel for managing report state and data loading.
 */
@Composable
fun ReportsScreen(
    onNavigate: (com.vibely.pos.ui.navigation.Screen) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // Header with title and controls
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
                            imageVector = FontAwesomeIcons.Solid.ChartBar,
                            contentDescription = "Reports overview",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(Res.string.reports_title),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }

                    AppButton(
                        text = if (state.isRefreshing) "Refreshing..." else "Refresh",
                        onClick = { viewModel.refreshReports() },
                        style = AppButtonStyle.Outlined,
                        enabled = !state.isRefreshing,
                        icon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Sync,
                                contentDescription = "Refresh reports",
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Range Picker
                DateRangePicker(
                    selectedPeriod = state.selectedPeriod,
                    customStartDate = state.customStartDate,
                    customEndDate = state.customEndDate,
                    onPeriodSelected = { period, startDate, endDate ->
                        viewModel.onPeriodSelected(period, startDate, endDate)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Loading state for initial load
                if (state.isLoading && !state.hasData) {
                    LoadingState()
                }
                // Error state for initial load
                else if (state.errorMessage != null && !state.hasData) {
                    ErrorState(
                        message = state.errorMessage!!,
                        onRetry = { viewModel.refreshReports() },
                        title = "Failed to load reports",
                    )
                }
                // Data display
                else if (state.hasData) {
                    // KPI Metrics Cards
                    state.salesReport?.let { report ->
                        MetricsCards(
                            totalSales = report.totalRevenue,
                            totalProfit = report.totalProfit,
                            profitMargin =
                            if (report.totalRevenue > 0) {
                                (report.totalProfit.toDouble() / report.totalRevenue * 100).toFloat()
                            } else {
                                0f
                            },
                            transactionCount = report.transactionCount,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sales Trend Chart
                    Text(
                        text = stringResource(Res.string.reports_sales_trend),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    if (state.salesTrend.isEmpty()) {
                        EmptyState(
                            icon = FontAwesomeIcons.Solid.ChartLine,
                            title = "No trend data available",
                            description = "No sales data for this period",
                            size = EmptyStateSize.Medium,
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        )
                    } else {
                        SalesTrendChart(
                            data = state.salesTrend,
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Category Breakdown
                    Text(
                        text = stringResource(Res.string.reports_category_breakdown),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    if (state.categoryBreakdown.isEmpty()) {
                        EmptyState(
                            icon = FontAwesomeIcons.Solid.ChartPie,
                            title = "No category data available",
                            description = "No sales data by category for this period",
                            size = EmptyStateSize.Medium,
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        )
                    } else {
                        CategoryBreakdownChart(
                            data = state.categoryBreakdown,
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Top Products Table
                    Text(
                        text = stringResource(Res.string.reports_top_products),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    if (state.topProducts.isEmpty()) {
                        EmptyState(
                            icon = FontAwesomeIcons.Solid.Box,
                            title = "No product data available",
                            description = "No product sales for this period",
                            size = EmptyStateSize.Medium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        TopProductsTable(
                            products = state.topProducts,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Top Customers Table
                    Text(
                        text = stringResource(Res.string.reports_top_customers),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    if (state.topCustomers.isEmpty()) {
                        EmptyState(
                            icon = FontAwesomeIcons.Solid.Users,
                            title = "No customer data available",
                            description = "No customer purchases for this period",
                            size = EmptyStateSize.Medium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        TopCustomersTable(
                            customers = state.topCustomers,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.reports_loading),
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondaryLight,
            )
        }
    }
}
