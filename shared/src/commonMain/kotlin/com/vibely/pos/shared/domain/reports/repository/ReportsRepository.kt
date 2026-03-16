package com.vibely.pos.shared.domain.reports.repository

import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.CategoryBreakdown
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.shared.domain.reports.entity.SalesReport
import com.vibely.pos.shared.domain.reports.entity.SalesTrend
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Instant

interface ReportsRepository {
    suspend fun getSalesReport(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<SalesReport>

    suspend fun getSalesTrend(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<SalesTrend>>

    suspend fun getCategoryBreakdown(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<CategoryBreakdown>>

    suspend fun getTopProducts(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<ProductPerformance>>

    suspend fun getCustomerAnalytics(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<CustomerAnalytics>>
}
