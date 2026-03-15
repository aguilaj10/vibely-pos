package com.vibely.pos.shared.data.purchaseorder.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseOrderItemDTO(
    @SerialName("id")
    val id: String,
    @SerialName("purchase_order_id")
    val purchaseOrderId: String,
    @SerialName("product_id")
    val productId: String,
    @SerialName("product_name")
    val productName: String? = null,
    @SerialName("product_sku")
    val productSku: String? = null,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("unit_cost")
    val unitCost: Double,
    @SerialName("subtotal")
    val subtotal: Double,
    @SerialName("received_quantity")
    val receivedQuantity: Int = 0,
    @SerialName("created_at")
    val createdAt: String,
)
