package com.vibely.pos.shared.domain.reports.usecase

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.SalesReport
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Instant

class GetSalesReportUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<SalesReport> =
        repository.getSalesReport(period, customStartDate, customEndDate)
}
