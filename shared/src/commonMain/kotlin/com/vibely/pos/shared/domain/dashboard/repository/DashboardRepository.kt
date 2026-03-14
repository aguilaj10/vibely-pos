package com.vibely.pos.shared.domain.dashboard.repository

import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.result.Result

/**
 * Repository interface for dashboard data operations.
 *
 * Defines the contract for retrieving aggregated dashboard metrics,
 * recent transactions, and inventory alerts. Implementations handle
 * data aggregation, caching, and communication with backend services.
 */
interface DashboardRepository {

    /**
     * Retrieves the dashboard summary for today.
     *
     * Aggregates today's sales, transaction counts, low stock alerts,
     * and active shift information into a single summary view.
     *
     * @return [Result.Success] with [DashboardSummary] if retrieval succeeds,
     *         [Result.Error] if an error occurs.
     *
     * Possible error codes:
     * - "NETWORK_ERROR": Network request failed
     * - "UNAUTHORIZED": Access token is invalid or expired
     * - "SERVER_ERROR": Backend service error
     */
    suspend fun getDashboardSummary(): Result<DashboardSummary>

    /**
     * Retrieves the most recent transactions for dashboard display.
     *
     * Returns up to the specified limit of recent sale transactions,
     * ordered by sale date descending (most recent first).
     *
     * @param limit Maximum number of transactions to retrieve (default: 10).
     * @return [Result.Success] with list of [RecentTransaction] if successful,
     *         [Result.Error] if an error occurs.
     *
     * Possible error codes:
     * - "NETWORK_ERROR": Network request failed
     * - "UNAUTHORIZED": Access token is invalid or expired
     * - "INVALID_LIMIT": Limit is out of acceptable range (1-100)
     */
    suspend fun getRecentTransactions(limit: Int = 10): Result<List<RecentTransaction>>

    /**
     * Retrieves products with stock levels below their reorder threshold.
     *
     * Returns products where current_stock < min_stock_level,
     * ordered by severity (out of stock first, then by deficit amount).
     *
     * @return [Result.Success] with list of [LowStockProduct] if successful,
     *         [Result.Error] if an error occurs.
     *
     * Possible error codes:
     * - "NETWORK_ERROR": Network request failed
     * - "UNAUTHORIZED": Access token is invalid or expired
     * - "SERVER_ERROR": Backend service error
     */
    suspend fun getLowStockProducts(): Result<List<LowStockProduct>>

    /**
     * Refreshes the dashboard data cache.
     *
     * Invalidates cached dashboard data and triggers a fresh retrieval
     * from the backend. Useful for pull-to-refresh functionality.
     *
     * @return [Result.Success] with Unit if refresh succeeds,
     *         [Result.Error] if an error occurs.
     */
    suspend fun refreshDashboard(): Result<Unit>
}
