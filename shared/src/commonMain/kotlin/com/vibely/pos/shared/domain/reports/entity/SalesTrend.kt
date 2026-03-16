package com.vibely.pos.shared.domain.reports.entity

import kotlin.time.Instant

/**
 * Domain entity representing sales trend data point.
 *
 * @param timestamp Point in time for this data.
 * @param revenue Revenue in cents.
 * @param transactionCount Number of transactions.
 */
data class SalesTrend(val timestamp: Instant, val revenue: Long, val transactionCount: Int)
