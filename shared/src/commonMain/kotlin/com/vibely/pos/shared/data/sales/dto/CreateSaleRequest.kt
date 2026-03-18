package com.vibely.pos.shared.data.sales.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSaleRequest(
    @SerialName("cashier_id")
    val cashierId: String,
    @SerialName("customer_id")
    val customerId: String? = null,
    @SerialName("subtotal")
    val subtotal: Double,
    @SerialName("tax_amount")
    val taxAmount: Double = 0.0,
    @SerialName("discount_amount")
    val discountAmount: Double = 0.0,
    @SerialName("total_amount")
    val totalAmount: Double,
    @SerialName("status")
    val status: String,
    @SerialName("payment_status")
    val paymentStatus: String,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("items")
    val items: List<CreateSaleItemRequest>,
)

@Serializable
data class CreateSaleItemRequest(
    @SerialName("product_id")
    val productId: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("unit_price")
    val unitPrice: Double,
    @SerialName("discount_amount")
    val discountAmount: Double = 0.0,
)
