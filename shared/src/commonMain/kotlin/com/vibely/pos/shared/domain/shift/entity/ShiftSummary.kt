package com.vibely.pos.shared.domain.shift.entity

data class ShiftSummary(
    val totalSales: Double,
    val transactionCount: Int,
    val cashPayments: Double,
    val cardPayments: Double,
    val otherPayments: Double,
    val expenses: Double,
    val openingBalance: Double,
    val expectedClosingBalance: Double,
) {
    val totalPayments: Double
        get() = cashPayments + cardPayments + otherPayments

    val netCash: Double
        get() = openingBalance + cashPayments - expenses
}
