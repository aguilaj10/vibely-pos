package com.vibely.pos.shared.data.sales.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaleItemDTO(
    @SerialName("id")
    val id: String,
    @SerialName("sale_id")
    val saleId: String,
    @SerialName("product_id")
    val productId: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("unit_price")
    val unitPrice: Double,
    @SerialName("discount_amount")
    val discountAmount: Double = 0.0,
    @SerialName("subtotal")
    val subtotal: Double,
    @SerialName("created_at")
    val createdAt: String,
)
