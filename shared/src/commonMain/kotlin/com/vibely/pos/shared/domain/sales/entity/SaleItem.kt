package com.vibely.pos.shared.domain.sales.entity

import kotlin.time.Clock
import kotlin.time.Instant

data class SaleItem(
    val id: String,
    val saleId: String,
    val productId: String,
    val quantity: Int,
    val unitPrice: Double,
    val discountAmount: Double = 0.0,
    val subtotal: Double,
    val createdAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "SaleItem ID cannot be blank" }
        require(saleId.isNotBlank()) { "Sale ID cannot be blank" }
        require(productId.isNotBlank()) { "Product ID cannot be blank" }
        require(quantity > 0) { "Quantity must be positive" }
        require(unitPrice >= 0) { "Unit price cannot be negative" }
        require(discountAmount >= 0) { "Discount amount cannot be negative" }
        require(subtotal >= 0) { "Subtotal cannot be negative" }
    }

    companion object {
        fun create(
            id: String,
            saleId: String,
            productId: String,
            quantity: Int,
            unitPrice: Double,
            discountAmount: Double = 0.0,
            createdAt: Instant = Clock.System.now(),
        ): SaleItem {
            val subtotal = (unitPrice * quantity) - discountAmount
            return SaleItem(
                id = id,
                saleId = saleId,
                productId = productId,
                quantity = quantity,
                unitPrice = unitPrice,
                discountAmount = discountAmount,
                subtotal = subtotal,
                createdAt = createdAt,
            )
        }
    }
}
