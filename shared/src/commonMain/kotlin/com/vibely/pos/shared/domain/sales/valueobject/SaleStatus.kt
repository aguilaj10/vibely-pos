package com.vibely.pos.shared.domain.sales.valueobject

/**
 * Represents the status of a sale transaction.
 *
 * Tracks the lifecycle of a sale from draft to completion or cancellation.
 */
enum class SaleStatus {
    /**
     * Sale is being prepared but not yet finalized.
     */
    DRAFT,

    /**
     * Sale has been completed and finalized.
     */
    COMPLETED,

    /**
     * Sale has been cancelled before completion.
     */
    CANCELLED,

    /**
     * Sale has been fully refunded.
     */
    REFUNDED,

    /**
     * Sale has been partially refunded.
     */
    PARTIALLY_REFUNDED,

    ;

    /**
     * Returns true if the sale can be modified.
     */
    fun canModify(): Boolean = this == DRAFT

    /**
     * Returns true if the sale can be cancelled.
     */
    fun canCancel(): Boolean = this == DRAFT || this == COMPLETED

    /**
     * Returns true if the sale can be refunded.
     */
    fun canRefund(): Boolean = this == COMPLETED || this == PARTIALLY_REFUNDED

    /**
     * Returns true if the sale is finalized (cannot be changed).
     */
    fun isFinalized(): Boolean = this != DRAFT
}
