@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePurchaseOrderRequest(
    @SerialName("supplier_id")
    val supplierId: String,
    @SerialName("expected_delivery_date")
    val expectedDeliveryDate: String? = null,
    val notes: String? = null,
    val items: List<PurchaseOrderItemRequest> = emptyList(),
)

@Serializable
data class PurchaseOrderItemRequest(
    @SerialName("product_id")
    val productId: String,
    val quantity: Int,
    @SerialName("unit_cost")
    val unitCost: Double,
    @SerialName("cost_currency_code")
    val costCurrencyCode: String = "USD",
)

@Serializable
data class UpdatePurchaseOrderRequest(
    @SerialName("supplier_id")
    val supplierId: String? = null,
    @SerialName("expected_delivery_date")
    val expectedDeliveryDate: String? = null,
    val notes: String? = null,
    val items: List<PurchaseOrderItemRequest>? = null,
)

@Serializable
data class UpdatePurchaseOrderStatusRequest(
    val status: String,
)

@Serializable
data class ReceivePurchaseOrderItemRequest(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("received_quantity")
    val receivedQuantity: Int,
)

@Serializable
data class ReceivePurchaseOrderRequest(
    val items: List<ReceivePurchaseOrderItemRequest>,
)
