package com.vibely.pos.shared.domain.sales.valueobject

/**
 * Represents the payment status of a sale transaction.
 *
 * Tracks whether payment has been received and processed.
 */
enum class PaymentStatus {
    /**
     * Payment has not been received yet.
     */
    PENDING,

    /**
     * Payment has been received and processed successfully.
     */
    COMPLETED,

    /**
     * Payment processing failed.
     */
    FAILED,

    /**
     * Payment has been refunded to the customer.
     */
    REFUNDED,

    /**
     * Payment transaction was cancelled.
     */
    CANCELLED,

    ;

    /**
     * Returns true if payment has been successfully received.
     */
    fun isPaid(): Boolean = this == COMPLETED

    /**
     * Returns true if payment can be processed.
     */
    fun canProcess(): Boolean = this == PENDING || this == FAILED

    /**
     * Returns true if payment can be refunded.
     */
    fun canRefund(): Boolean = this == COMPLETED
}
