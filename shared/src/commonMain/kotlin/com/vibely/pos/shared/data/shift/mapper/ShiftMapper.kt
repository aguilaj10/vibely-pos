package com.vibely.pos.shared.data.shift.mapper

import com.vibely.pos.shared.data.shift.dto.ShiftDTO
import com.vibely.pos.shared.domain.shift.entity.Shift
import kotlin.time.Instant

object ShiftMapper {
    fun toDomain(dto: ShiftDTO): Shift = Shift.create(
        id = dto.id,
        shiftNumber = dto.shiftNumber,
        cashierId = dto.cashierId,
        cashierName = dto.cashierName,
        openingBalance = dto.openingBalance,
        closingBalance = dto.closingBalance,
        expectedBalance = dto.expectedBalance,
        discrepancy = dto.discrepancy,
        totalSales = dto.totalSales,
        totalCash = dto.totalCash,
        totalCard = dto.totalCard,
        totalOther = dto.totalOther,
        openedAt = Instant.parse(dto.openedAt),
        closedAt = dto.closedAt?.let { Instant.parse(it) },
        notes = dto.notes,
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    fun toDTO(shift: Shift): ShiftDTO = ShiftDTO(
        id = shift.id,
        shiftNumber = shift.shiftNumber,
        cashierId = shift.cashierId,
        cashierName = shift.cashierName,
        openingBalance = shift.openingBalance,
        closingBalance = shift.closingBalance,
        expectedBalance = shift.expectedBalance,
        discrepancy = shift.discrepancy,
        totalSales = shift.totalSales,
        totalCash = shift.totalCash,
        totalCard = shift.totalCard,
        totalOther = shift.totalOther,
        openedAt = shift.openedAt.toString(),
        closedAt = shift.closedAt?.toString(),
        notes = shift.notes,
        createdAt = shift.createdAt.toString(),
        updatedAt = shift.updatedAt.toString(),
    )
}
