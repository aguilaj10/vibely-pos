package com.vibely.pos.shared.data.reports.mapper

import com.vibely.pos.shared.data.reports.dto.CategoryBreakdownDTO
import com.vibely.pos.shared.domain.reports.entity.CategoryBreakdown

/**
 * Mapper for converting between [CategoryBreakdownDTO] and [CategoryBreakdown] domain entity.
 */
object CategoryBreakdownMapper {

    /**
     * Converts a [CategoryBreakdownDTO] to a [CategoryBreakdown] domain entity.
     *
     * @param dto The data transfer object to convert.
     * @return The domain entity.
     */
    fun toDomain(dto: CategoryBreakdownDTO): CategoryBreakdown = CategoryBreakdown(
        categoryId = dto.categoryId,
        categoryName = dto.categoryName,
        revenue = dto.revenue,
        transactionCount = dto.transactionCount,
    )
}
