package com.vibely.pos.shared.data.reports.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.reports.datasource.RemoteReportsDataSource
import com.vibely.pos.shared.data.reports.mapper.CategoryBreakdownMapper
import com.vibely.pos.shared.data.reports.mapper.CustomerAnalyticsMapper
import com.vibely.pos.shared.data.reports.mapper.ProductPerformanceMapper
import com.vibely.pos.shared.data.reports.mapper.SalesReportMapper
import com.vibely.pos.shared.data.reports.mapper.SalesTrendMapper
import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.CategoryBreakdown
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.shared.domain.reports.entity.SalesReport
import com.vibely.pos.shared.domain.reports.entity.SalesTrend
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Implementation of [ReportsRepository] using remote data source with in-memory caching.
 *
 * Implements a simple cache with 5-minute TTL to reduce backend load for report data.
 * Cache keys are based on report type and period parameters to ensure correct data retrieval.
 *
 * @param remoteDataSource Remote data source for backend API calls.
 */
internal class ReportsRepositoryImpl(private val remoteDataSource: RemoteReportsDataSource) :
    BaseRepository(),
    ReportsRepository {

    private data class CacheKey(val reportType: String, val period: ReportPeriod, val customStartDate: Instant?, val customEndDate: Instant?)

    private val cache = mutableMapOf<CacheKey, Pair<Any, Long>>()
    private val cacheTTL = 5.minutes

    override suspend fun getSalesReport(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<SalesReport> {
        val cacheKey = CacheKey("sales_report", period, customStartDate, customEndDate)
        val cached = getCachedData<SalesReport>(cacheKey)
        if (cached != null) return Result.Success(cached)

        val result = mapSingle(
            remoteDataSource.getSalesReport(
                period = period.name,
                customStartDate = customStartDate?.toEpochMilliseconds(),
                customEndDate = customEndDate?.toEpochMilliseconds(),
            ),
            SalesReportMapper::toDomain,
        )

        if (result is Result.Success) {
            cacheData(cacheKey, result.data)
        }
        return result
    }

    override suspend fun getSalesTrend(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<SalesTrend>> {
        val cacheKey = CacheKey("sales_trend", period, customStartDate, customEndDate)
        val cached = getCachedData<List<SalesTrend>>(cacheKey)
        if (cached != null) return Result.Success(cached)

        val result = mapList(
            remoteDataSource.getSalesTrend(
                period = period.name,
                customStartDate = customStartDate?.toEpochMilliseconds(),
                customEndDate = customEndDate?.toEpochMilliseconds(),
            ),
            SalesTrendMapper::toDomain,
        )

        if (result is Result.Success) {
            cacheData(cacheKey, result.data)
        }
        return result
    }

    override suspend fun getCategoryBreakdown(
        period: ReportPeriod,
        customStartDate: Instant?,
        customEndDate: Instant?,
    ): Result<List<CategoryBreakdown>> {
        val cacheKey = CacheKey("category_breakdown", period, customStartDate, customEndDate)
        val cached = getCachedData<List<CategoryBreakdown>>(cacheKey)
        if (cached != null) return Result.Success(cached)

        val result = mapList(
            remoteDataSource.getCategoryBreakdown(
                period = period.name,
                customStartDate = customStartDate?.toEpochMilliseconds(),
                customEndDate = customEndDate?.toEpochMilliseconds(),
            ),
            CategoryBreakdownMapper::toDomain,
        )

        if (result is Result.Success) {
            cacheData(cacheKey, result.data)
        }
        return result
    }

    override suspend fun getTopProducts(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?): Result<List<ProductPerformance>> {
        val cacheKey = CacheKey("top_products", period, customStartDate, customEndDate)
        val cached = getCachedData<List<ProductPerformance>>(cacheKey)
        if (cached != null) return Result.Success(cached)

        val result = mapList(
            remoteDataSource.getTopProducts(
                period = period.name,
                customStartDate = customStartDate?.toEpochMilliseconds(),
                customEndDate = customEndDate?.toEpochMilliseconds(),
            ),
            ProductPerformanceMapper::toDomain,
        )

        if (result is Result.Success) {
            cacheData(cacheKey, result.data)
        }
        return result
    }

    override suspend fun getCustomerAnalytics(
        period: ReportPeriod,
        customStartDate: Instant?,
        customEndDate: Instant?,
    ): Result<List<CustomerAnalytics>> {
        val cacheKey = CacheKey("customer_analytics", period, customStartDate, customEndDate)
        val cached = getCachedData<List<CustomerAnalytics>>(cacheKey)
        if (cached != null) return Result.Success(cached)

        val result = mapList(
            remoteDataSource.getCustomerAnalytics(
                period = period.name,
                customStartDate = customStartDate?.toEpochMilliseconds(),
                customEndDate = customEndDate?.toEpochMilliseconds(),
            ),
            CustomerAnalyticsMapper::toDomain,
        )

        if (result is Result.Success) {
            cacheData(cacheKey, result.data)
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getCachedData(key: CacheKey): T? {
        val (data, timestamp) = cache[key] ?: return null
        val now = Clock.System.now().toEpochMilliseconds()
        val cacheAge = now - timestamp

        return if (cacheAge < cacheTTL.inWholeMilliseconds) {
            data as? T
        } else {
            cache.remove(key)
            null
        }
    }

    private fun cacheData(key: CacheKey, data: Any) {
        val now = Clock.System.now().toEpochMilliseconds()
        cache[key] = data to now
    }
}
