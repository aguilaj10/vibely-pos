package com.vibely.pos.shared.data.reports.repository

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.CategoryBreakdown
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.shared.domain.reports.entity.SalesReport
import com.vibely.pos.shared.domain.reports.entity.SalesTrend
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Instant

internal class ReportsRepositoryImpl : ReportsRepository {
    override suspend fun getSalesReport(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<SalesReport> {
        TODO("Not yet implemented")
    }

    override suspend fun getSalesTrend(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<SalesTrend>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCategoryBreakdown(
        period: ReportPeriod,
        customStartDate: Instant?,
        customEndDate: Instant?,
    ): Result<List<CategoryBreakdown>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTopProducts(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<ProductPerformance>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCustomerAnalytics(
        period: ReportPeriod,
        customStartDate: Instant?,
        customEndDate: Instant?,
    ): Result<List<CustomerAnalytics>> {
        TODO("Not yet implemented")
    }
}
