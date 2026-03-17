package com.vibely.pos.shared.data.reports.mapper

import com.vibely.pos.shared.data.reports.dto.SalesReportDTO
import com.vibely.pos.shared.domain.reports.entity.SalesReport

/**
 * Mapper for converting between [SalesReportDTO] and [SalesReport] domain entity.
 */
object SalesReportMapper {

    /**
     * Converts a [SalesReportDTO] to a [SalesReport] domain entity.
     *
     * @param dto The data transfer object to convert.
     * @return The domain entity.
     */
    fun toDomain(dto: SalesReportDTO): SalesReport = SalesReport(
        totalRevenue = dto.totalRevenue,
        totalCost = dto.totalCost,
        totalProfit = dto.totalProfit,
        transactionCount = dto.transactionCount,
        averageTransactionValue = dto.averageTransactionValue,
    )
}
