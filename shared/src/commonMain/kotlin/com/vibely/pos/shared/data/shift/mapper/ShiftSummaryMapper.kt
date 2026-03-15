package com.vibely.pos.shared.data.shift.mapper

import com.vibely.pos.shared.data.shift.dto.ShiftSummaryDTO
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary

object ShiftSummaryMapper {
    fun toDomain(dto: ShiftSummaryDTO): ShiftSummary = ShiftSummary(
        totalSales = dto.totalSales,
        transactionCount = dto.transactionCount,
        cashPayments = dto.cashPayments,
        cardPayments = dto.cardPayments,
        otherPayments = dto.otherPayments,
        expenses = dto.expenses,
        openingBalance = dto.openingBalance,
        expectedClosingBalance = dto.expectedClosingBalance,
    )

    fun toDTO(summary: ShiftSummary): ShiftSummaryDTO = ShiftSummaryDTO(
        totalSales = summary.totalSales,
        transactionCount = summary.transactionCount,
        cashPayments = summary.cashPayments,
        cardPayments = summary.cardPayments,
        otherPayments = summary.otherPayments,
        expenses = summary.expenses,
        openingBalance = summary.openingBalance,
        expectedClosingBalance = summary.expectedClosingBalance,
    )
}
