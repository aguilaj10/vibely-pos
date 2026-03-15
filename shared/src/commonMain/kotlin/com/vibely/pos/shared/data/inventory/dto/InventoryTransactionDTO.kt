package com.vibely.pos.shared.data.inventory.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryTransactionDTO(
    @SerialName("id")
    val id: String,
    @SerialName("product_id")
    val productId: String,
    @SerialName("transaction_type")
    val transactionType: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("reference_id")
    val referenceId: String? = null,
    @SerialName("reference_type")
    val referenceType: String? = null,
    @SerialName("reason")
    val reason: String? = null,
    @SerialName("performed_by")
    val performedBy: String,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String,
)
