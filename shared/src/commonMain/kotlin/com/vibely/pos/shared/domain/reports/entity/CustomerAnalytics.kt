package com.vibely.pos.shared.domain.reports.entity

import kotlin.time.Instant

/**
 * Domain entity representing customer analytics data.
 *
 * @param customerId Unique identifier for the customer (null for walk-in customers).
 * @param customerName Display name of the customer.
 * @param totalSpent Total amount spent in cents.
 * @param visitCount Number of visits.
 * @param lastVisit Timestamp of last visit.
 */
data class CustomerAnalytics(val customerId: String?, val customerName: String, val totalSpent: Long, val visitCount: Int, val lastVisit: Instant)
