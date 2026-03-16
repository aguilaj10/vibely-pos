package com.vibely.pos.shared.domain.reports.entity

/**
 * Domain entity representing product performance metrics.
 *
 * @param productId Unique identifier for the product.
 * @param productName Name of the product.
 * @param quantitySold Total units sold.
 * @param revenue Total revenue in cents.
 * @param cost Total cost in cents.
 * @param profit Total profit in cents.
 */
data class ProductPerformance(
    val productId: String,
    val productName: String,
    val quantitySold: Int,
    val revenue: Long,
    val cost: Long,
    val profit: Long,
)
