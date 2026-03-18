package com.vibely.pos.shared.data.dashboard.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.dashboard.datasource.RemoteDashboardDataSource
import com.vibely.pos.shared.data.dashboard.mapper.DashboardSummaryMapper
import com.vibely.pos.shared.data.dashboard.mapper.LowStockProductMapper
import com.vibely.pos.shared.data.dashboard.mapper.RecentTransactionMapper
import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Implementation of [DashboardRepository] using remote data source with in-memory caching.
 *
 * Implements a simple cache with 5-minute TTL to reduce backend load for dashboard data.
 *
 * @param remoteDataSource Remote data source for backend API calls.
 */
class DashboardRepositoryImpl(private val remoteDataSource: RemoteDashboardDataSource) :
    BaseRepository(),
    DashboardRepository {

    // Cache for dashboard summary with 5-minute TTL
    private var cachedSummary: DashboardSummary? = null
    private var summaryFetchedAt: Long? = null

    // Cache for recent transactions with 5-minute TTL
    private var cachedTransactions: List<RecentTransaction>? = null
    private var transactionsFetchedAt: Long? = null
    private var cachedTransactionsLimit: Int? = null

    // Cache for low stock products with 5-minute TTL
    private var cachedLowStockProducts: List<LowStockProduct>? = null
    private var lowStockFetchedAt: Long? = null

    private val cacheTTL = 5.minutes

    override suspend fun getDashboardSummary(): Result<DashboardSummary> {
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedSummaryLocal = cachedSummary
        val summaryFetchedAtLocal = summaryFetchedAt

        if (cachedSummaryLocal != null && summaryFetchedAtLocal != null) {
            val cacheAge = now - summaryFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                return Result.Success(cachedSummaryLocal)
            }
        }

        val result = mapSingle(remoteDataSource.getDashboardSummary(), DashboardSummaryMapper::toDomain)
        if (result is Result.Success) {
            cachedSummary = result.data
            summaryFetchedAt = now
        }
        return result
    }

    override suspend fun getRecentTransactions(limit: Int): Result<List<RecentTransaction>> {
        if (limit !in 1..100) {
            return Result.Error(
                message = "Limit must be between 1 and 100",
                code = "INVALID_LIMIT",
            )
        }

        val now = Clock.System.now().toEpochMilliseconds()
        val cachedTransactionsLocal = cachedTransactions
        val transactionsFetchedAtLocal = transactionsFetchedAt
        val cachedTransactionsLimitLocal = cachedTransactionsLimit

        if (cachedTransactionsLocal != null && transactionsFetchedAtLocal != null && cachedTransactionsLimitLocal == limit) {
            val cacheAge = now - transactionsFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                return Result.Success(cachedTransactionsLocal)
            }
        }

        val result = mapList(remoteDataSource.getRecentTransactions(limit), RecentTransactionMapper::toDomain)
        if (result is Result.Success) {
            cachedTransactions = result.data
            transactionsFetchedAt = now
            cachedTransactionsLimit = limit
        }
        return result
    }

    override suspend fun getLowStockProducts(): Result<List<LowStockProduct>> {
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedLowStockProductsLocal = cachedLowStockProducts
        val lowStockFetchedAtLocal = lowStockFetchedAt

        if (cachedLowStockProductsLocal != null && lowStockFetchedAtLocal != null) {
            val cacheAge = now - lowStockFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                return Result.Success(cachedLowStockProductsLocal)
            }
        }

        val result = mapList(remoteDataSource.getLowStockProducts(), LowStockProductMapper::toDomain)
        if (result is Result.Success) {
            cachedLowStockProducts = result.data
            lowStockFetchedAt = now
        }
        return result
    }

    override suspend fun refreshDashboard(): Result<Unit> {
        // Clear all caches
        cachedSummary = null
        summaryFetchedAt = null
        cachedTransactions = null
        transactionsFetchedAt = null
        cachedTransactionsLimit = null
        cachedLowStockProducts = null
        lowStockFetchedAt = null

        return Result.Success(Unit)
    }
}
