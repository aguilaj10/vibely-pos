package com.vibely.pos.shared.data.reports.mapper

import com.vibely.pos.shared.data.reports.dto.CustomerAnalyticsDTO
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import kotlin.time.Instant

/**
 * Mapper for converting between [CustomerAnalyticsDTO] and [CustomerAnalytics] domain entity.
 */
object CustomerAnalyticsMapper {

    /**
     * Converts a [CustomerAnalyticsDTO] to a [CustomerAnalytics] domain entity.
     *
     * @param dto The data transfer object to convert.
     * @return The domain entity.
     */
    fun toDomain(dto: CustomerAnalyticsDTO): CustomerAnalytics = CustomerAnalytics(
        customerId = dto.customerId,
        customerName = dto.customerName,
        totalSpent = dto.totalSpent,
        visitCount = dto.visitCount,
        lastVisit = Instant.fromEpochMilliseconds(dto.lastVisit),
    )
}
