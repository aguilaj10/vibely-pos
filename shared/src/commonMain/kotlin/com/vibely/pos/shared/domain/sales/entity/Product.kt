package com.vibely.pos.shared.domain.sales.entity

import com.vibely.pos.shared.domain.exception.BusinessRuleException
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Domain entity representing a product in the inventory.
 *
 * Encapsulates product information, pricing, and stock management.
 * Enforces business rules around stock availability and pricing.
 *
 * @param id Unique identifier (UUID from database).
 * @param sku Stock Keeping Unit - unique identifier for the product.
 * @param barcode Optional EAN/UPC barcode for scanner integration.
 * @param name Product name.
 * @param description Optional product description.
 * @param categoryId Optional reference to product category.
 * @param categoryName Optional display name of the product category.
 * @param costPrice Purchase/cost price of the product.
 * @param sellingPrice Retail selling price.
 * @param currentStock Current quantity in stock.
 * @param minStockLevel Minimum stock level before low stock alert.
 * @param unit Unit of measurement (unit, kg, liter, etc.).
 * @param imageUrl Optional URL to product image.
 * @param isActive Whether the product is active for sale.
 * @param createdAt When the product was created.
 * @param updatedAt When the product was last updated.
 */
data class Product(
    val id: String,
    val sku: String,
    val barcode: String?,
    val name: String,
    val description: String?,
    val categoryId: String?,
    val categoryName: String?,
    val costPrice: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val minStockLevel: Int,
    val unit: String = "unit",
    val imageUrl: String?,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "Product ID cannot be blank" }
        require(sku.isNotBlank()) { "SKU cannot be blank" }
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(costPrice >= 0) { "Cost price cannot be negative" }
        require(sellingPrice >= 0) { "Selling price cannot be negative" }
        require(minStockLevel >= 0) { "Minimum stock level cannot be negative" }
        require(unit.isNotBlank()) { "Unit cannot be blank" }
    }

    /**
     * Returns true if the product is below minimum stock level.
     *
     * Business Rule: Low stock threshold is defined by minStockLevel.
     */
    val isLowStock: Boolean
        get() = currentStock <= minStockLevel

    /**
     * Returns true if the product is out of stock.
     */
    val isOutOfStock: Boolean
        get() = currentStock <= 0

    /**
     * Returns the profit margin as a percentage.
     */
    val profitMargin: Double
        get() = if (costPrice > 0) {
            ((sellingPrice - costPrice) / costPrice) * 100
        } else {
            0.0
        }

    /**
     * Returns true if the product can be sold with the specified quantity.
     *
     * Business Rule: Product must be active and have sufficient stock.
     *
     * @param quantity The quantity to check availability for.
     */
    fun canSell(quantity: Int): Boolean {
        require(quantity > 0) { "Quantity must be positive" }
        return isActive && currentStock >= quantity
    }

    /**
     * Validates that the product can be sold with the specified quantity.
     *
     * @throws BusinessRuleException if the product cannot be sold.
     */
    fun requireCanSell(quantity: Int) {
        require(quantity > 0) { "Quantity must be positive" }

        if (!isActive) {
            throw BusinessRuleException(
                rule = "Product '$name' is not active for sale",
                code = "PRODUCT_NOT_ACTIVE",
            )
        }

        if (currentStock < quantity) {
            throw BusinessRuleException(
                rule = "Insufficient stock for product '$name'. Available: $currentStock, Requested: $quantity",
                code = "INSUFFICIENT_STOCK",
            )
        }
    }

    /**
     * Returns a copy of this product with updated stock.
     *
     * @param newStock The new stock quantity.
     */
    fun withStock(newStock: Int): Product {
        require(newStock >= 0) { "Stock cannot be negative" }
        return copy(
            currentStock = newStock,
            updatedAt = Clock.System.now(),
        )
    }

    /**
     * Returns a copy of this product with stock deducted.
     *
     * @param quantity The quantity to deduct.
     * @throws IllegalArgumentException if resulting stock would be negative.
     */
    fun deductStock(quantity: Int): Product {
        require(quantity > 0) { "Quantity must be positive" }
        val newStock = currentStock - quantity
        require(newStock >= 0) { "Cannot deduct $quantity from stock of $currentStock" }
        return withStock(newStock)
    }

    /**
     * Returns a copy of this product with stock added.
     *
     * @param quantity The quantity to add.
     */
    fun addStock(quantity: Int): Product {
        require(quantity > 0) { "Quantity must be positive" }
        return withStock(currentStock + quantity)
    }

    companion object {
        /**
         * Creates a new Product instance with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            id: String,
            sku: String,
            name: String,
            costPrice: Double,
            sellingPrice: Double,
            currentStock: Int,
            minStockLevel: Int,
            barcode: String? = null,
            description: String? = null,
            categoryId: String? = null,
            categoryName: String? = null,
            unit: String = "unit",
            imageUrl: String? = null,
            isActive: Boolean = true,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): Product = Product(
            id = id,
            sku = sku,
            barcode = barcode,
            name = name,
            description = description,
            categoryId = categoryId,
            categoryName = categoryName,
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            currentStock = currentStock,
            minStockLevel = minStockLevel,
            unit = unit,
            imageUrl = imageUrl,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
