package com.vibely.pos.shared.domain.reports.usecase

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.SalesTrend
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Instant

class GetSalesTrendUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<SalesTrend>> =
        repository.getSalesTrend(period, customStartDate, customEndDate)
}
