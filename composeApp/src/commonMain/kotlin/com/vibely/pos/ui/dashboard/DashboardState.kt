package com.vibely.pos.ui.dashboard

import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction

/**
 * State for the Dashboard screen.
 *
 * Manages the complete state of the dashboard including summary metrics,
 * recent transactions, low stock alerts, and UI states (loading, errors).
 *
 * @param summary Aggregated dashboard metrics (sales, transactions, stock alerts).
 * @param recentTransactions List of recent sale transactions.
 * @param lowStockProducts Products requiring reorder attention.
 * @param isLoading True during initial data load.
 * @param isRefreshing True during pull-to-refresh.
 * @param errorMessage Error message to display (null if no error).
 */
data class DashboardState(
    val summary: DashboardSummary? = null,
    val recentTransactions: List<RecentTransaction> = emptyList(),
    val lowStockProducts: List<LowStockProduct> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
) {
    /**
     * Returns true if dashboard has loaded data.
     */
    val hasData: Boolean get() = summary != null

    /**
     * Returns true if there are recent transactions to display.
     */
    val hasRecentTransactions: Boolean get() = recentTransactions.isNotEmpty()

    /**
     * Returns true if there are low stock alerts.
     */
    val hasLowStockAlerts: Boolean get() = lowStockProducts.isNotEmpty()

    /**
     * Returns true if any loading operation is in progress.
     */
    val isAnyLoading: Boolean get() = isLoading || isRefreshing
}
