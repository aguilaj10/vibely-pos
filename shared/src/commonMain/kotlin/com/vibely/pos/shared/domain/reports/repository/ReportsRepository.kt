package com.vibely.pos.shared.domain.reports.repository

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.CategoryBreakdown
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.shared.domain.reports.entity.SalesReport
import com.vibely.pos.shared.domain.reports.entity.SalesTrend
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Instant

/**
 * Repository interface for reports and analytics data access.
 *
 * Provides methods for retrieving aggregated sales data, performance metrics,
 * and analytics across various dimensions (products, categories, customers, time).
 * All methods support flexible time period filtering through [ReportPeriod] enum
 * with optional custom date range override.
 *
 * **Implementation Notes:**
 * - Data layer implementations should use optimized SQL aggregations
 * - Consider database views or materialized views for complex queries
 * - Cache frequently accessed reports (e.g., 5-minute TTL)
 * - All date filtering should handle timezone conversions appropriately
 * - Results should only include completed/successful transactions
 *
 * @see com.vibely.pos.shared.domain.reports.usecase
 */
interface ReportsRepository {
    /**
     * Retrieves a comprehensive sales report for the specified period.
     *
     * Aggregates all completed sales within the time period to calculate
     * total revenue, costs, profit, transaction count, and average transaction value.
     * Only includes completed transactions (excludes cancelled and refunded sales).
     *
     * @param period The predefined time period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     *               If CUSTOM is specified, [customStartDate] and [customEndDate] must be provided.
     * @param customStartDate Optional start date for custom period filtering (inclusive).
     *                        Required when [period] is CUSTOM, ignored otherwise.
     * @param customEndDate Optional end date for custom period filtering (inclusive).
     *                      Required when [period] is CUSTOM, ignored otherwise.
     * @return [Result.Success] containing [SalesReport] with aggregated metrics,
     *         or [Result.Error] if the query fails or dates are invalid.
     */
    suspend fun getSalesReport(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<SalesReport>

    /**
     * Retrieves sales trend data points over the specified period.
     *
     * Returns a time series of sales metrics grouped by the appropriate granularity
     * based on the period (hourly for TODAY, daily for THIS_WEEK, daily/weekly for THIS_MONTH).
     * Used for visualizing sales patterns and identifying trends over time.
     *
     * @param period The predefined time period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Optional start date for custom period filtering (inclusive).
     * @param customEndDate Optional end date for custom period filtering (inclusive).
     * @return [Result.Success] containing a list of [SalesTrend] ordered chronologically,
     *         or [Result.Error] if the query fails. Returns empty list if no sales in period.
     */
    suspend fun getSalesTrend(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<SalesTrend>>

    /**
     * Retrieves sales breakdown by product category for the specified period.
     *
     * Aggregates sales by category to show which product categories are driving
     * revenue and customer engagement. Useful for merchandising and inventory decisions.
     * Results are typically ordered by revenue descending.
     *
     * @param period The predefined time period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Optional start date for custom period filtering (inclusive).
     * @param customEndDate Optional end date for custom period filtering (inclusive).
     * @return [Result.Success] containing a list of [CategoryBreakdown] ordered by revenue,
     *         or [Result.Error] if the query fails. Returns empty list if no sales in period.
     */
    suspend fun getCategoryBreakdown(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<CategoryBreakdown>>

    /**
     * Retrieves top-performing products for the specified period.
     *
     * Returns products ranked by revenue with detailed performance metrics including
     * quantity sold, revenue, cost, and profit. Typically limited to top 10-20 products.
     * Used for identifying best sellers and inventory optimization.
     *
     * @param period The predefined time period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Optional start date for custom period filtering (inclusive).
     * @param customEndDate Optional end date for custom period filtering (inclusive).
     * @return [Result.Success] containing a list of [ProductPerformance] ordered by revenue,
     *         or [Result.Error] if the query fails. Returns empty list if no sales in period.
     */
    suspend fun getTopProducts(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<ProductPerformance>>

    /**
     * Retrieves customer analytics data for the specified period.
     *
     * Returns customer spending and visit patterns to identify top customers,
     * analyze customer lifetime value, and support targeted marketing efforts.
     * Includes both registered customers and walk-in customers.
     * Typically ordered by total spent descending.
     *
     * @param period The predefined time period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Optional start date for custom period filtering (inclusive).
     * @param customEndDate Optional end date for custom period filtering (inclusive).
     * @return [Result.Success] containing a list of [CustomerAnalytics] ordered by total spent,
     *         or [Result.Error] if the query fails. Returns empty list if no sales in period.
     */
    suspend fun getCustomerAnalytics(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<CustomerAnalytics>>
}
