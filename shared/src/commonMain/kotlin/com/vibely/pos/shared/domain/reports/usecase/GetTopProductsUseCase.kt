package com.vibely.pos.shared.domain.reports.usecase

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Instant

class GetTopProductsUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<ProductPerformance>> =
        repository.getTopProducts(period, customStartDate, customEndDate)
}
