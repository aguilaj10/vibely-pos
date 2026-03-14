package com.vibely.pos.shared.domain.dashboard.usecase

import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.map

/**
 * Use case for retrieving products with low stock levels.
 *
 * Returns products where current_stock < min_stock_level (reorder point),
 * ordered by alert severity (critical alerts first).
 *
 * Business Flow:
 * 1. Fetch low stock products from repository
 * 2. Sort by severity (CRITICAL → HIGH → MEDIUM)
 * 3. Return sorted list for dashboard alerts
 *
 * Business Rule: Low stock is defined as current_stock < min_stock_level
 *
 * @param dashboardRepository The dashboard data repository.
 */
class GetLowStockProductsUseCase(private val dashboardRepository: DashboardRepository) {
    /**
     * Executes the use case to retrieve low stock products.
     *
     * @return [Result.Success] with list of [LowStockProduct] sorted by severity,
     *         [Result.Error] if retrieval fails.
     *
     * Possible error codes:
     * - "NETWORK_ERROR": Network request failed
     * - "UNAUTHORIZED": User is not authenticated
     * - "SERVER_ERROR": Backend service error
     */
    suspend operator fun invoke(): Result<List<LowStockProduct>> = dashboardRepository.getLowStockProducts()
        .map { products ->
            // Sort by alert severity (CRITICAL first, then HIGH, then MEDIUM)
            products.sortedByDescending { it.alertSeverity().ordinal }
        }
}
