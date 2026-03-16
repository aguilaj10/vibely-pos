package com.vibely.pos.shared.data.reports.dto

import kotlinx.serialization.Serializable

/**
 * Data transfer object for product performance metrics.
 *
 * Represents performance data for a single product.
 *
 * @property productId Unique identifier for the product.
 * @property productName Name of the product.
 * @property quantitySold Total units sold.
 * @property revenue Total revenue generated in cents.
 * @property cost Total cost in cents.
 * @property profit Total profit in cents.
 */
@Serializable
data class ProductPerformanceDTO(
    val productId: String,
    val productName: String,
    val quantitySold: Int,
    val revenue: Long,
    val cost: Long,
    val profit: Long,
)
