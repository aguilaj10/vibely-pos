package com.vibely.pos.shared.domain.purchaseorder.entity

import kotlin.time.Clock
import kotlin.time.Instant

data class PurchaseOrderItem(
    val id: String,
    val purchaseOrderId: String,
    val productId: String,
    val productName: String?,
    val productSku: String?,
    val quantity: Int,
    val unitCost: Double,
    val costCurrencyCode: String = "USD",
    val subtotal: Double,
    val receivedQuantity: Int = 0,
    val createdAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "Purchase order item ID cannot be blank" }
        require(purchaseOrderId.isNotBlank()) { "Purchase order ID cannot be blank" }
        require(productId.isNotBlank()) { "Product ID cannot be blank" }
        require(quantity > 0) { "Quantity must be positive" }
        require(unitCost >= 0) { "Unit cost cannot be negative" }
        require(subtotal >= 0) { "Subtotal cannot be negative" }
        require(receivedQuantity >= 0) { "Received quantity cannot be negative" }
    }

    val isFullyReceived: Boolean
        get() = receivedQuantity >= quantity

    val pendingQuantity: Int
        get() = (quantity - receivedQuantity).coerceAtLeast(0)

    fun withReceivedQuantity(newReceivedQuantity: Int): PurchaseOrderItem {
        require(newReceivedQuantity >= 0) { "Received quantity cannot be negative" }
        require(newReceivedQuantity <= quantity) { "Received quantity cannot exceed ordered quantity" }
        return copy(receivedQuantity = newReceivedQuantity)
    }

    fun calculateSubtotal(): Double = quantity * unitCost

    companion object {
        fun create(
            id: String,
            purchaseOrderId: String,
            productId: String,
            quantity: Int,
            unitCost: Double,
            costCurrencyCode: String = "USD",
            productName: String? = null,
            productSku: String? = null,
            receivedQuantity: Int = 0,
            createdAt: Instant = Clock.System.now(),
        ): PurchaseOrderItem = PurchaseOrderItem(
            id = id,
            purchaseOrderId = purchaseOrderId,
            productId = productId,
            productName = productName,
            productSku = productSku,
            quantity = quantity,
            unitCost = unitCost,
            costCurrencyCode = costCurrencyCode,
            subtotal = quantity * unitCost,
            receivedQuantity = receivedQuantity,
            createdAt = createdAt,
        )
    }
}
