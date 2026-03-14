package com.vibely.pos.shared.data.dashboard.repository

import com.vibely.pos.shared.data.dashboard.datasource.RemoteDashboardDataSource
import com.vibely.pos.shared.data.dashboard.mapper.DashboardSummaryMapper
import com.vibely.pos.shared.data.dashboard.mapper.LowStockProductMapper
import com.vibely.pos.shared.data.dashboard.mapper.RecentTransactionMapper
import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.map
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Implementation of [DashboardRepository] using remote data source with in-memory caching.
 *
 * Implements a simple cache with 5-minute TTL to reduce backend load for dashboard data.
 *
 * @param remoteDataSource Remote data source for backend API calls.
 */
class DashboardRepositoryImpl(private val remoteDataSource: RemoteDashboardDataSource) : DashboardRepository {

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
        // Check cache validity
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedSummaryLocal = cachedSummary
        val summaryFetchedAtLocal = summaryFetchedAt

        if (cachedSummaryLocal != null && summaryFetchedAtLocal != null) {
            val cacheAge = now - summaryFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                // Cache is still valid
                return Result.Success(cachedSummaryLocal)
            }
        }

        // Cache miss or expired - fetch from backend
        return remoteDataSource.getDashboardSummary()
            .map { dto ->
                val summary = DashboardSummaryMapper.toDomain(dto)
                // Update cache
                cachedSummary = summary
                summaryFetchedAt = now
                summary
            }
    }

    override suspend fun getRecentTransactions(limit: Int): Result<List<RecentTransaction>> {
        // Validate limit
        if (limit < 1 || limit > 100) {
            return Result.Error(
                message = "Limit must be between 1 and 100",
                code = "INVALID_LIMIT",
            )
        }

        // Check cache validity
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedTransactionsLocal = cachedTransactions
        val transactionsFetchedAtLocal = transactionsFetchedAt
        val cachedTransactionsLimitLocal = cachedTransactionsLimit

        if (cachedTransactionsLocal != null && transactionsFetchedAtLocal != null && cachedTransactionsLimitLocal == limit) {
            val cacheAge = now - transactionsFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                // Cache is still valid
                return Result.Success(cachedTransactionsLocal)
            }
        }

        // Cache miss or expired - fetch from backend
        return remoteDataSource.getRecentTransactions(limit)
            .map { dtoList ->
                val transactions = dtoList.map { dto -> RecentTransactionMapper.toDomain(dto) }
                // Update cache
                cachedTransactions = transactions
                transactionsFetchedAt = now
                cachedTransactionsLimit = limit
                transactions
            }
    }

    override suspend fun getLowStockProducts(): Result<List<LowStockProduct>> {
        // Check cache validity
        val now = Clock.System.now().toEpochMilliseconds()
        val cachedLowStockProductsLocal = cachedLowStockProducts
        val lowStockFetchedAtLocal = lowStockFetchedAt

        if (cachedLowStockProductsLocal != null && lowStockFetchedAtLocal != null) {
            val cacheAge = now - lowStockFetchedAtLocal
            if (cacheAge < cacheTTL.inWholeMilliseconds) {
                // Cache is still valid
                return Result.Success(cachedLowStockProductsLocal)
            }
        }

        // Cache miss or expired - fetch from backend
        return remoteDataSource.getLowStockProducts()
            .map { dtoList ->
                val products = dtoList.map { dto -> LowStockProductMapper.toDomain(dto) }
                // Update cache
                cachedLowStockProducts = products
                lowStockFetchedAt = now
                products
            }
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
