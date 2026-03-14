package com.vibely.pos.shared.domain.dashboard.entity

import com.vibely.pos.shared.domain.valueobject.Money
import kotlin.time.Instant

/**
 * Domain entity representing a recent sale transaction for dashboard display.
 *
 * Provides a lightweight view of sale transactions optimized for dashboard rendering.
 * This is a read model - not the full Sale aggregate.
 *
 * @param id Unique sale identifier.
 * @param invoiceNumber Human-readable invoice number.
 * @param totalAmount Final sale amount after tax and discounts.
 * @param status Current sale status.
 * @param saleDate When the sale was completed.
 * @param customerName Customer name (null if walk-in customer).
 */
data class RecentTransaction(
    val id: String,
    val invoiceNumber: String,
    val totalAmount: Money,
    val status: TransactionStatus,
    val saleDate: Instant,
    val customerName: String?,
) {
    /**
     * Returns true if this transaction can be refunded.
     */
    fun canRefund(): Boolean = status == TransactionStatus.COMPLETED

    companion object {
        /**
         * Creates a RecentTransaction with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            id: String,
            invoiceNumber: String,
            totalAmount: Money,
            status: TransactionStatus,
            saleDate: Instant,
            customerName: String?,
        ): RecentTransaction {
            require(id.isNotBlank()) { "Transaction ID cannot be blank" }
            require(invoiceNumber.isNotBlank()) { "Invoice number cannot be blank" }

            return RecentTransaction(
                id = id,
                invoiceNumber = invoiceNumber,
                totalAmount = totalAmount,
                status = status,
                saleDate = saleDate,
                customerName = customerName,
            )
        }
    }
}

/**
 * Transaction status for dashboard display.
 * Subset of full sale_status enum focused on relevant states.
 */
enum class TransactionStatus {
    COMPLETED,
    CANCELLED,
    REFUNDED,
    ;

    companion object {
        /**
         * Maps database sale_status values to TransactionStatus.
         *
         * @throws IllegalArgumentException if the value is unknown.
         */
        fun fromDatabaseValue(value: String): TransactionStatus = when (value.lowercase()) {
            "completed" -> COMPLETED
            "cancelled" -> CANCELLED
            "refunded", "partially_refunded" -> REFUNDED
            else -> throw IllegalArgumentException("Unknown transaction status: $value")
        }
    }
}
