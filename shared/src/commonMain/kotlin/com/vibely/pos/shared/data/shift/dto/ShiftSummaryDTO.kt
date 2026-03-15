package com.vibely.pos.shared.data.shift.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShiftSummaryDTO(
    @SerialName("total_sales")
    val totalSales: Double,
    @SerialName("transaction_count")
    val transactionCount: Int,
    @SerialName("cash_payments")
    val cashPayments: Double,
    @SerialName("card_payments")
    val cardPayments: Double,
    @SerialName("other_payments")
    val otherPayments: Double,
    @SerialName("expenses")
    val expenses: Double,
    @SerialName("opening_balance")
    val openingBalance: Double,
    @SerialName("expected_closing_balance")
    val expectedClosingBalance: Double,
)
