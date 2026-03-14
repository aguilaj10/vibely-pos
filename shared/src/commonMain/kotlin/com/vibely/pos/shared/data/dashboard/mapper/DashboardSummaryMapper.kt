package com.vibely.pos.shared.data.dashboard.mapper

import com.vibely.pos.shared.data.dashboard.dto.ActiveShiftInfoDTO
import com.vibely.pos.shared.data.dashboard.dto.DashboardSummaryDTO
import com.vibely.pos.shared.domain.dashboard.entity.ActiveShiftInfo
import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.valueobject.Money
import kotlin.time.Instant

/**
 * Mapper for converting between [DashboardSummaryDTO] and [DashboardSummary] domain entity.
 */
object DashboardSummaryMapper {

    /**
     * Maps a [DashboardSummaryDTO] from the backend to a [DashboardSummary] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if any field cannot be parsed.
     */
    fun toDomain(dto: DashboardSummaryDTO): DashboardSummary {
        val todaySales = Money.fromCents(dto.todaySalesCents, "USD")
        val activeShift = dto.activeShift?.let { toDomain(it) }
        val generatedAt = Instant.parse(dto.generatedAt)

        return DashboardSummary.create(
            todaySales = todaySales,
            todayTransactionCount = dto.todayTransactionCount,
            lowStockCount = dto.lowStockCount,
            activeShift = activeShift,
            generatedAt = generatedAt,
        )
    }

    /**
     * Maps an [ActiveShiftInfoDTO] from the backend to an [ActiveShiftInfo] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if any field cannot be parsed.
     */
    private fun toDomain(dto: ActiveShiftInfoDTO): ActiveShiftInfo {
        val openedAt = Instant.parse(dto.openedAt)
        val openingBalance = Money.fromCents(dto.openingBalanceCents, "USD")

        return ActiveShiftInfo(
            shiftId = dto.shiftId,
            cashierId = dto.cashierId,
            cashierName = dto.cashierName,
            openedAt = openedAt,
            openingBalance = openingBalance,
        )
    }

    /**
     * Maps a [DashboardSummary] domain entity to a [DashboardSummaryDTO] for the backend.
     *
     * @param summary The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(summary: DashboardSummary): DashboardSummaryDTO {
        val activeShiftDTO = summary.activeShift?.let { toDTO(it) }

        return DashboardSummaryDTO(
            todaySalesCents = summary.todaySales.amountInCents,
            todayTransactionCount = summary.todayTransactionCount,
            lowStockCount = summary.lowStockCount,
            activeShift = activeShiftDTO,
            generatedAt = summary.generatedAt.toString(),
        )
    }

    /**
     * Maps an [ActiveShiftInfo] domain entity to an [ActiveShiftInfoDTO] for the backend.
     *
     * @param info The domain entity.
     * @return The DTO for the backend.
     */
    private fun toDTO(info: ActiveShiftInfo): ActiveShiftInfoDTO = ActiveShiftInfoDTO(
        shiftId = info.shiftId,
        cashierId = info.cashierId,
        cashierName = info.cashierName,
        openedAt = info.openedAt.toString(),
        openingBalanceCents = info.openingBalance.amountInCents,
    )
}
