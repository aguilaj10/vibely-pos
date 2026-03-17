package com.vibely.pos.shared.domain.reports.entity

import kotlin.time.Instant

/**
 * Domain entity representing a single data point in a sales trend time series.
 *
 * Captures sales metrics at a specific point in time to enable trend analysis,
 * seasonality detection, and performance tracking over periods. Used for visualizing
 * sales patterns in charts and identifying growth or decline trends.
 *
 * **Use Cases:**
 * - Sales trend line charts (daily/weekly/monthly)
 * - Seasonality and pattern analysis
 * - Year-over-year comparisons
 * - Sales forecasting and projections
 * - Performance anomaly detection
 *
 * @property timestamp Point in time for this data measurement.
 *                     Represents the start of the time bucket (e.g., start of day/week/month)
 *                     depending on the grouping granularity requested.
 * @property revenue Total revenue for this time period in cents.
 *                   Sum of all completed sales within the time bucket.
 * @property transactionCount Number of completed transactions within this time period.
 *                            Each completed sale counts as one transaction.
 *
 * @see com.vibely.pos.shared.domain.reports.usecase.GetSalesTrendUseCase
 * @see com.vibely.pos.shared.domain.reports.repository.ReportsRepository.getSalesTrend
 */
data class SalesTrend(val timestamp: Instant, val revenue: Long, val transactionCount: Int) {
    /**
     * Calculates the average transaction value for this time period.
     *
     * @return Average sale amount in cents, or 0 if no transactions occurred.
     */
    fun averageTransactionValue(): Long = if (transactionCount > 0) {
        revenue / transactionCount
    } else {
        0L
    }

    /**
     * Compares this trend data point to a previous data point to calculate growth.
     *
     * @param previous The previous SalesTrend data point to compare against.
     * @return Growth rate as a percentage (positive for growth, negative for decline).
     *         Returns 0.0 if the previous period had zero revenue.
     */
    fun revenueGrowthPercentage(previous: SalesTrend): Double = if (previous.revenue > 0) {
        ((revenue - previous.revenue).toDouble() / previous.revenue.toDouble()) * 100.0
    } else {
        0.0
    }
}
