package com.vibely.pos.shared.domain.reports.entity

import kotlin.time.Instant

/**
 * Domain entity representing customer analytics and purchasing behavior metrics.
 *
 * Tracks individual customer spending patterns, visit frequency, and engagement
 * to support customer relationship management, loyalty programs, and targeted
 * marketing strategies. Includes both registered customers and walk-in customers.
 *
 * **Use Cases:**
 * - Top customer identification (VIP/high-value customers)
 * - Customer lifetime value (CLV) analysis
 * - Customer segmentation and targeting
 * - Loyalty program optimization
 * - Churn risk identification
 * - Personalized marketing campaigns
 *
 * @property customerId Unique identifier for the customer (UUID format), or null for
 *                      walk-in/anonymous customers. References the customers table primary key.
 * @property customerName Display name of the customer for reporting purposes.
 *                        For walk-in customers, typically shows "Walk-in Customer".
 *                        Example: "John Doe", "Walk-in Customer #12345"
 * @property totalSpent Total amount spent by this customer across all completed purchases, in cents.
 *                      Sum of all completed sale totals for this customer.
 * @property visitCount Number of visits (completed transactions) by this customer.
 *                      Each completed sale counts as one visit.
 * @property lastVisit Timestamp of the customer's most recent completed purchase.
 *                     Used for recency analysis and churn detection.
 *
 * @see com.vibely.pos.shared.domain.reports.usecase.GetCustomerAnalyticsUseCase
 * @see com.vibely.pos.shared.domain.reports.repository.ReportsRepository.getCustomerAnalytics
 */
data class CustomerAnalytics(val customerId: String?, val customerName: String, val totalSpent: Long, val visitCount: Int, val lastVisit: Instant) {
    /**
     * Calculates the average spend per visit for this customer.
     *
     * @return Average transaction value in cents, or 0 if no visits occurred.
     */
    fun averageSpendPerVisit(): Long = if (visitCount > 0) {
        totalSpent / visitCount
    } else {
        0L
    }

    /**
     * Returns `true` if this is a registered customer (has a customer ID).
     * Returns `false` for walk-in/anonymous customers.
     */
    val isRegisteredCustomer: Boolean
        get() = customerId != null

    /**
     * Calculates days since the last visit from the current time.
     *
     * @param currentTime The current timestamp to compare against (defaults to now).
     * @return Number of days since last visit, or 0 if last visit is in the future.
     */
    fun daysSinceLastVisit(currentTime: Instant = kotlin.time.Clock.System.now()): Long {
        val duration = currentTime - lastVisit
        return maxOf(0L, duration.inWholeDays)
    }
}
