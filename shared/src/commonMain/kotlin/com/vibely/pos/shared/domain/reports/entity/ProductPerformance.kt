package com.vibely.pos.shared.domain.reports.entity

/**
 * Domain entity representing individual product performance metrics for reporting and analytics.
 *
 * Tracks sales performance for a specific product within a given time period, providing
 * insights into product popularity, revenue contribution, and profitability. Used for
 * inventory optimization, pricing strategies, and product lineup decisions.
 *
 * **Use Cases:**
 * - Top-selling products identification
 * - Product profitability analysis
 * - Inventory restocking prioritization
 * - Product portfolio optimization
 * - Sales trend analysis by product
 *
 * @property productId Unique identifier for the product (UUID format).
 *                     References the products table primary key.
 * @property productName Display name of the product for reporting purposes.
 *                       Example: "Organic Coffee Beans 500g"
 * @property quantitySold Total number of units sold in the reporting period.
 *                        Sum of all sale line items for this product.
 * @property revenue Total revenue generated from this product in cents.
 *                   Calculated as sum(selling_price * quantity) for all completed sales.
 * @property cost Total cost of goods sold (COGS) for this product in cents.
 *                Calculated as sum(cost_price * quantity) for all units sold.
 * @property profit Total profit margin for this product in cents (revenue - cost).
 *                  Gross profit before operating expenses and overhead.
 *
 * @see com.vibely.pos.shared.domain.reports.usecase.GetTopProductsUseCase
 * @see com.vibely.pos.shared.domain.reports.repository.ReportsRepository.getTopProducts
 */
data class ProductPerformance(
    val productId: String,
    val productName: String,
    val quantitySold: Int,
    val revenue: Long,
    val cost: Long,
    val profit: Long,
) {
    /**
     * Calculates the profit margin percentage for this product.
     *
     * @return Profit margin as a percentage (0-100), or 0.0 if revenue is zero.
     */
    fun profitMarginPercentage(): Double = if (revenue > 0) {
        (profit.toDouble() / revenue.toDouble()) * 100.0
    } else {
        0.0
    }

    /**
     * Calculates the average revenue per unit sold.
     *
     * @return Average selling price in cents, or 0 if no units were sold.
     */
    fun averageRevenuePerUnit(): Long = if (quantitySold > 0) {
        revenue / quantitySold
    } else {
        0L
    }

    /**
     * Returns `true` if this product is profitable (profit > 0).
     */
    val isProfitable: Boolean
        get() = profit > 0
}
