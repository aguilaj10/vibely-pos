package com.vibely.pos.shared.domain.reports.entity

/**
 * Domain entity representing category breakdown of sales.
 *
 * @param categoryId Unique identifier for the category.
 * @param categoryName Name of the category.
 * @param revenue Total revenue in cents.
 * @param transactionCount Number of transactions.
 */
data class CategoryBreakdown(val categoryId: String, val categoryName: String, val revenue: Long, val transactionCount: Int)
