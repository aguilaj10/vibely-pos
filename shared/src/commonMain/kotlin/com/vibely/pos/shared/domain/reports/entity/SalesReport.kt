package com.vibely.pos.shared.domain.reports.entity

/**
 * Domain entity representing a comprehensive sales report summary for a given period.
 *
 * Provides aggregated metrics for sales performance analysis including revenue,
 * costs, profitability, and transaction statistics. All monetary values are stored
 * as Long in cents to avoid floating-point precision issues.
 *
 * **Use Cases:**
 * - Daily/weekly/monthly sales performance tracking
 * - Profit margin analysis
 * - Business performance dashboards
 * - Financial reporting and forecasting
 *
 * @property totalRevenue Total revenue from all completed sales in cents.
 *                        Only includes completed transactions (excludes cancelled/refunded).
 * @property totalCost Total cost of goods sold (COGS) in cents.
 *                     Sum of all product costs for items sold in the period.
 * @property totalProfit Total profit margin in cents (totalRevenue - totalCost).
 *                       Gross profit before operating expenses.
 * @property transactionCount Total number of completed transactions in the period.
 *                            Each completed sale counts as one transaction.
 * @property averageTransactionValue Average sale amount in cents (totalRevenue / transactionCount).
 *                                   Zero if no transactions occurred in the period.
 *
 * @see com.vibely.pos.shared.domain.reports.usecase.GetSalesReportUseCase
 * @see com.vibely.pos.shared.domain.reports.repository.ReportsRepository.getSalesReport
 */
data class SalesReport(
    val totalRevenue: Long,
    val totalCost: Long,
    val totalProfit: Long,
    val transactionCount: Int,
    val averageTransactionValue: Long,
) {
    /**
     * Calculates the profit margin percentage.
     *
     * @return Profit margin as a percentage (0-100), or 0.0 if revenue is zero.
     */
    fun profitMarginPercentage(): Double = if (totalRevenue > 0) {
        (totalProfit.toDouble() / totalRevenue.toDouble()) * 100.0
    } else {
        0.0
    }

    /**
     * Returns `true` if the report indicates profitability (profit > 0).
     */
    val isProfitable: Boolean
        get() = totalProfit > 0
}
