package com.vibely.pos.shared.data.reports.mapper

import com.vibely.pos.shared.data.reports.dto.SalesTrendDTO
import com.vibely.pos.shared.domain.reports.entity.SalesTrend
import kotlin.time.Instant

/**
 * Mapper for converting between [SalesTrendDTO] and [SalesTrend] domain entity.
 */
object SalesTrendMapper {

    /**
     * Converts a [SalesTrendDTO] to a [SalesTrend] domain entity.
     *
     * @param dto The data transfer object to convert.
     * @return The domain entity.
     */
    fun toDomain(dto: SalesTrendDTO): SalesTrend = SalesTrend(
        timestamp = Instant.fromEpochMilliseconds(dto.timestamp),
        revenue = dto.revenue,
        transactionCount = dto.transactionCount,
    )
}
