package com.vibely.pos.shared.domain.dashboard.entity

import com.vibely.pos.shared.domain.valueobject.Money
import com.vibely.pos.shared.domain.valueobject.SKU

/**
 * Domain entity representing a product with low stock levels.
 *
 * Used for dashboard alerts to notify users of products requiring reorder.
 * This is a read model focused on inventory alerts.
 *
 * @param id Product unique identifier.
 * @param sku Stock Keeping Unit code.
 * @param name Product name.
 * @param currentStock Current quantity in inventory.
 * @param minStockLevel Minimum stock level threshold (reorder point).
 * @param sellingPrice Current selling price.
 * @param categoryName Product category for context.
 */
data class LowStockProduct(
    val id: String,
    val sku: SKU,
    val name: String,
    val currentStock: Int,
    val minStockLevel: Int,
    val sellingPrice: Money,
    val categoryName: String?,
) {
    /**
     * Returns the stock deficit (how many units below threshold).
     */
    fun stockDeficit(): Int = (minStockLevel - currentStock).coerceAtLeast(0)

    /**
     * Returns true if product is completely out of stock.
     */
    fun isOutOfStock(): Boolean = currentStock <= 0

    /**
     * Returns the severity level for UI display.
     */
    fun alertSeverity(): AlertSeverity = when {
        currentStock <= 0 -> AlertSeverity.CRITICAL
        currentStock < minStockLevel * 0.5 -> AlertSeverity.HIGH
        else -> AlertSeverity.MEDIUM
    }

    companion object {
        /**
         * Creates a LowStockProduct with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            id: String,
            sku: SKU,
            name: String,
            currentStock: Int,
            minStockLevel: Int,
            sellingPrice: Money,
            categoryName: String?,
        ): LowStockProduct {
            require(id.isNotBlank()) { "Product ID cannot be blank" }
            require(name.isNotBlank()) { "Product name cannot be blank" }
            require(currentStock >= 0) { "Current stock cannot be negative" }
            require(minStockLevel >= 0) { "Min stock level cannot be negative" }

            return LowStockProduct(
                id = id,
                sku = sku,
                name = name,
                currentStock = currentStock,
                minStockLevel = minStockLevel,
                sellingPrice = sellingPrice,
                categoryName = categoryName,
            )
        }
    }
}

/**
 * Alert severity levels for stock alerts.
 */
enum class AlertSeverity {
    MEDIUM, // Below threshold but > 50% of min
    HIGH, // Less than 50% of min stock
    CRITICAL, // Out of stock
}
