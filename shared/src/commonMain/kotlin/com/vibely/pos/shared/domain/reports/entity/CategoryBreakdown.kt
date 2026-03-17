package com.vibely.pos.shared.domain.reports.entity

/**
 * Domain entity representing sales breakdown by product category.
 *
 * Provides category-level sales analytics to understand which product categories
 * are driving revenue and customer engagement. Useful for merchandising decisions,
 * shelf space allocation, and category management strategies.
 *
 * **Use Cases:**
 * - Category performance comparison
 * - Revenue mix analysis
 * - Category contribution to total sales
 * - Merchandising and display optimization
 * - Inventory allocation by category
 *
 * @property categoryId Unique identifier for the category (UUID format).
 *                      References the categories table primary key.
 * @property categoryName Display name of the category for reporting purposes.
 *                        Example: "Beverages", "Snacks", "Bakery"
 * @property revenue Total revenue generated from products in this category, in cents.
 *                   Sum of all completed sales for products assigned to this category.
 * @property transactionCount Number of transactions that included at least one product
 *                            from this category. A single transaction may include multiple
 *                            categories (counted once per category).
 *
 * @see com.vibely.pos.shared.domain.reports.usecase.GetCategoryBreakdownUseCase
 * @see com.vibely.pos.shared.domain.reports.repository.ReportsRepository.getCategoryBreakdown
 */
data class CategoryBreakdown(val categoryId: String, val categoryName: String, val revenue: Long, val transactionCount: Int) {
    /**
     * Calculates average revenue per transaction for this category.
     *
     * @return Average transaction value in cents, or 0 if no transactions occurred.
     */
    fun averageRevenuePerTransaction(): Long = if (transactionCount > 0) {
        revenue / transactionCount
    } else {
        0L
    }

    /**
     * Calculates the percentage contribution of this category to total revenue.
     *
     * @param totalRevenue The total revenue across all categories in cents.
     * @return Percentage contribution (0-100), or 0.0 if total revenue is zero.
     */
    fun percentageOfTotal(totalRevenue: Long): Double = if (totalRevenue > 0) {
        (revenue.toDouble() / totalRevenue.toDouble()) * 100.0
    } else {
        0.0
    }
}
