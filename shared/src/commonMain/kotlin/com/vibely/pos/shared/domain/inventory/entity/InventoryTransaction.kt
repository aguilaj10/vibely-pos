package com.vibely.pos.shared.domain.inventory.entity

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Type of inventory transaction.
 */
enum class TransactionType {
    /** Stock purchased from supplier/vendor */
    PURCHASE,

    /** Stock sold to customer */
    SALE,

    /** Manual adjustment (correction, recount, etc.) */
    ADJUSTMENT,

    /** Stock damaged/lost/stolen */
    DAMAGE,

    /** Stock returned from customer */
    RETURN,
}

/**
 * Domain entity representing an inventory transaction (audit trail).
 *
 * Records all stock changes with who, when, why, and how much.
 * Immutable record - once created cannot be modified.
 *
 * @param id Unique identifier (UUID from database).
 * @param productId Reference to the product affected.
 * @param transactionType Type of transaction (purchase, sale, adjustment, damage, return).
 * @param quantity Quantity changed (positive for increase, negative for decrease).
 * @param referenceId Optional reference to external document (purchase order ID, sale ID, etc.).
 * @param referenceType Optional reference type (e.g., "purchase_order", "sale", "audit").
 * @param reason Reason for the transaction.
 * @param performedBy User ID who performed the action.
 * @param notes Optional notes about the transaction.
 * @param createdAt When the transaction occurred.
 */
data class InventoryTransaction(
    val id: String,
    val productId: String,
    val transactionType: TransactionType,
    val quantity: Int,
    val referenceId: String?,
    val referenceType: String?,
    val reason: String?,
    val performedBy: String,
    val notes: String?,
    val createdAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "Transaction ID cannot be blank" }
        require(productId.isNotBlank()) { "Product ID cannot be blank" }
        require(quantity != 0) { "Quantity cannot be zero" }
        require(performedBy.isNotBlank()) { "Performed by user ID cannot be blank" }

        when (transactionType) {
            TransactionType.PURCHASE, TransactionType.RETURN -> {
                require(quantity > 0) {
                    "Quantity must be positive for ${transactionType.name} transactions"
                }
            }
            TransactionType.SALE, TransactionType.DAMAGE -> {
                require(quantity < 0) {
                    "Quantity must be negative for ${transactionType.name} transactions"
                }
            }
            TransactionType.ADJUSTMENT -> {
                // Adjustment can be positive or negative
            }
        }
    }

    /**
     * Returns true if this transaction increases stock.
     */
    val isStockIncrease: Boolean
        get() = quantity > 0

    /**
     * Returns true if this transaction decreases stock.
     */
    val isStockDecrease: Boolean
        get() = quantity < 0

    /**
     * Returns the absolute quantity change.
     */
    val absoluteQuantity: Int
        get() = kotlin.math.abs(quantity)

    companion object {
        /**
         * Creates a new InventoryTransaction instance with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            id: String,
            productId: String,
            transactionType: TransactionType,
            quantity: Int,
            performedBy: String,
            referenceId: String? = null,
            referenceType: String? = null,
            reason: String? = null,
            notes: String? = null,
            createdAt: Instant = Clock.System.now(),
        ): InventoryTransaction = InventoryTransaction(
            id = id,
            productId = productId,
            transactionType = transactionType,
            quantity = quantity,
            referenceId = referenceId,
            referenceType = referenceType,
            reason = reason,
            performedBy = performedBy,
            notes = notes,
            createdAt = createdAt,
        )

        /**
         * Creates a purchase transaction (stock increase).
         */
        fun createPurchase(
            id: String,
            productId: String,
            quantity: Int,
            performedBy: String,
            referenceId: String? = null,
            reason: String? = null,
            notes: String? = null,
        ): InventoryTransaction = create(
            id = id,
            productId = productId,
            transactionType = TransactionType.PURCHASE,
            quantity = quantity,
            performedBy = performedBy,
            referenceId = referenceId,
            referenceType = "purchase_order",
            reason = reason,
            notes = notes,
        )

        /**
         * Creates a sale transaction (stock decrease).
         */
        fun createSale(
            id: String,
            productId: String,
            quantity: Int,
            performedBy: String,
            referenceId: String? = null,
            notes: String? = null,
        ): InventoryTransaction = create(
            id = id,
            productId = productId,
            transactionType = TransactionType.SALE,
            quantity = -kotlin.math.abs(quantity),
            performedBy = performedBy,
            referenceId = referenceId,
            referenceType = "sale",
            reason = "Sale",
            notes = notes,
        )

        /**
         * Creates an adjustment transaction.
         */
        fun createAdjustment(
            id: String,
            productId: String,
            quantity: Int,
            performedBy: String,
            reason: String,
            notes: String? = null,
        ): InventoryTransaction = create(
            id = id,
            productId = productId,
            transactionType = TransactionType.ADJUSTMENT,
            quantity = quantity,
            performedBy = performedBy,
            referenceType = "audit",
            reason = reason,
            notes = notes,
        )

        /**
         * Creates a damage transaction (stock decrease).
         */
        fun createDamage(
            id: String,
            productId: String,
            quantity: Int,
            performedBy: String,
            reason: String,
            notes: String? = null,
        ): InventoryTransaction = create(
            id = id,
            productId = productId,
            transactionType = TransactionType.DAMAGE,
            quantity = -kotlin.math.abs(quantity),
            performedBy = performedBy,
            reason = reason,
            notes = notes,
        )

        /**
         * Creates a return transaction (stock increase).
         */
        fun createReturn(
            id: String,
            productId: String,
            quantity: Int,
            performedBy: String,
            referenceId: String? = null,
            reason: String? = null,
            notes: String? = null,
        ): InventoryTransaction = create(
            id = id,
            productId = productId,
            transactionType = TransactionType.RETURN,
            quantity = quantity,
            performedBy = performedBy,
            referenceId = referenceId,
            referenceType = "sale",
            reason = reason,
            notes = notes,
        )
    }
}
