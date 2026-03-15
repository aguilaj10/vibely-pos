package com.vibely.pos.shared.data.shift.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShiftDTO(
    @SerialName("id")
    val id: String,
    @SerialName("shift_number")
    val shiftNumber: String,
    @SerialName("cashier_id")
    val cashierId: String,
    @SerialName("cashier_name")
    val cashierName: String? = null,
    @SerialName("opening_balance")
    val openingBalance: Double,
    @SerialName("closing_balance")
    val closingBalance: Double? = null,
    @SerialName("expected_balance")
    val expectedBalance: Double? = null,
    @SerialName("discrepancy")
    val discrepancy: Double? = null,
    @SerialName("total_sales")
    val totalSales: Double = 0.0,
    @SerialName("total_cash")
    val totalCash: Double = 0.0,
    @SerialName("total_card")
    val totalCard: Double = 0.0,
    @SerialName("total_other")
    val totalOther: Double = 0.0,
    @SerialName("opened_at")
    val openedAt: String,
    @SerialName("closed_at")
    val closedAt: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)
