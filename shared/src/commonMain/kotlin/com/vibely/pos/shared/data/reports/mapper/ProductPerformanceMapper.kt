package com.vibely.pos.shared.data.reports.mapper

import com.vibely.pos.shared.data.reports.dto.ProductPerformanceDTO
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance

/**
 * Mapper for converting between [ProductPerformanceDTO] and [ProductPerformance] domain entity.
 */
object ProductPerformanceMapper {

    /**
     * Converts a [ProductPerformanceDTO] to a [ProductPerformance] domain entity.
     *
     * @param dto The data transfer object to convert.
     * @return The domain entity.
     */
    fun toDomain(dto: ProductPerformanceDTO): ProductPerformance = ProductPerformance(
        productId = dto.productId,
        productName = dto.productName,
        quantitySold = dto.quantitySold,
        revenue = dto.revenue,
        cost = dto.cost,
        profit = dto.profit,
    )
}
