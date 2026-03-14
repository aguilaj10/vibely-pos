package com.vibely.pos.shared.data.dashboard.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for recent transaction from the backend.
 *
 * Represents a recent sale transaction for dashboard display.
 */
@Serializable
data class RecentTransactionDTO(
    @SerialName("id")
    val id: String,

    @SerialName("invoice_number")
    val invoiceNumber: String,

    @SerialName("total_cents")
    val totalCents: Long,

    @SerialName("status")
    val status: String,

    @SerialName("sale_date")
    val saleDate: String,

    @SerialName("customer_name")
    val customerName: String?,
)
