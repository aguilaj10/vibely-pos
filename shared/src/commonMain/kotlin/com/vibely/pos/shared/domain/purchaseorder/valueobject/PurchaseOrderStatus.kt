package com.vibely.pos.shared.domain.purchaseorder.valueobject

enum class PurchaseOrderStatus {
    DRAFT,
    PENDING,
    APPROVED,
    RECEIVED,
    CANCELLED,
    ;

    fun canModify(): Boolean = this == DRAFT || this == PENDING

    fun canCancel(): Boolean = this == DRAFT || this == PENDING || this == APPROVED

    fun canApprove(): Boolean = this == PENDING

    fun canReceive(): Boolean = this == APPROVED

    companion object {
        fun fromString(value: String): PurchaseOrderStatus = entries.find { it.name.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown purchase order status: $value")
    }
}
