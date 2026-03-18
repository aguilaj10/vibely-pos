package com.vibely.pos.shared.data.sales.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDTO(
    @SerialName("id")
    val id: String,
    @SerialName("sale_id")
    val saleId: String,
    @SerialName("amount")
    val amount: Double,
    @SerialName("payment_type")
    val paymentType: String,
    @SerialName("status")
    val status: String,
    @SerialName("reference_number")
    val referenceNumber: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("payment_date")
    val paymentDate: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)

@Serializable
data class CreatePaymentRequest(
    @SerialName("sale_id")
    val saleId: String,
    @SerialName("amount")
    val amount: Double,
    @SerialName("payment_type")
    val paymentType: String,
    @SerialName("reference_number")
    val referenceNumber: String? = null,
    @SerialName("notes")
    val notes: String? = null,
)
