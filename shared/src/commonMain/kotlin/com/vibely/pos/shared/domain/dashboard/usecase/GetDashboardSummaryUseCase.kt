package com.vibely.pos.shared.domain.dashboard.usecase

import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.result.Result

/**
 * Use case for retrieving today's dashboard summary.
 *
 * Fetches aggregated metrics including sales, transactions, low stock alerts,
 * and active shift information. Data may be cached with a TTL (typically 5 minutes)
 * to reduce backend load.
 *
 * Business Flow:
 * 1. Request dashboard summary from repository
 * 2. Repository checks cache (5-minute TTL)
 * 3. If cache miss or expired, fetch from backend
 * 4. Return aggregated summary
 *
 * @param dashboardRepository The dashboard data repository.
 */
class GetDashboardSummaryUseCase(private val dashboardRepository: DashboardRepository) {
    /**
     * Executes the use case to retrieve dashboard summary.
     *
     * @return [Result.Success] with [DashboardSummary] if successful,
     *         [Result.Error] if retrieval fails.
     *
     * Possible error codes:
     * - "NETWORK_ERROR": Network request failed
     * - "UNAUTHORIZED": User is not authenticated
     * - "SERVER_ERROR": Backend service error
     */
    suspend operator fun invoke(): Result<DashboardSummary> = dashboardRepository.getDashboardSummary()
}
