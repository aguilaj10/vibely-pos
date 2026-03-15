package com.vibely.pos.shared.data.sales.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaleDTO(
    @SerialName("id")
    val id: String,
    @SerialName("invoice_number")
    val invoiceNumber: String,
    @SerialName("customer_id")
    val customerId: String? = null,
    @SerialName("cashier_id")
    val cashierId: String,
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
    @SerialName("sale_date")
    val saleDate: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)
