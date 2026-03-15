package com.vibely.pos.shared.data.purchaseorder.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseOrderWithItemsDTO(
    @SerialName("id")
    val id: String,
    @SerialName("po_number")
    val poNumber: String,
    @SerialName("supplier_id")
    val supplierId: String,
    @SerialName("supplier_name")
    val supplierName: String? = null,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("total_amount")
    val totalAmount: Double,
    @SerialName("status")
    val status: String,
    @SerialName("order_date")
    val orderDate: String,
    @SerialName("expected_delivery_date")
    val expectedDeliveryDate: String? = null,
    @SerialName("received_date")
    val receivedDate: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("purchase_order_items")
    val items: List<PurchaseOrderItemDTO> = emptyList(),
)
