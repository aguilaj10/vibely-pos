package com.vibely.pos.shared.domain.dashboard.usecase

import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.exception.ValidationException
import com.vibely.pos.shared.domain.result.Result

/**
 * Use case for retrieving recent sale transactions for dashboard display.
 *
 * Returns the most recent transactions ordered by sale date descending.
 * Validates the requested limit to prevent excessive data retrieval.
 *
 * Business Flow:
 * 1. Validate limit parameter (1-100)
 * 2. Fetch recent transactions from repository
 * 3. Return transactions ordered by date (most recent first)
 *
 * @param dashboardRepository The dashboard data repository.
 */
class GetRecentTransactionsUseCase(private val dashboardRepository: DashboardRepository) {
    /**
     * Executes the use case to retrieve recent transactions.
     *
     * @param limit Maximum number of transactions to retrieve (default: 10, max: 100).
     * @return [Result.Success] with list of [RecentTransaction] if successful,
     *         [Result.Error] if validation or retrieval fails.
     *
     * Possible error codes:
     * - "VALIDATION_ERROR": Limit is out of range (1-100)
     * - "NETWORK_ERROR": Network request failed
     * - "UNAUTHORIZED": User is not authenticated
     */
    suspend operator fun invoke(limit: Int = DEFAULT_LIMIT): Result<List<RecentTransaction>> {
        // Validate limit
        if (limit !in MIN_LIMIT..MAX_LIMIT) {
            return Result.Error(
                message = "Transaction limit must be between $MIN_LIMIT and $MAX_LIMIT",
                code = "VALIDATION_ERROR",
                cause = ValidationException(
                    field = "limit",
                    message = "Limit $limit is out of acceptable range ($MIN_LIMIT-$MAX_LIMIT)",
                ),
            )
        }

        return dashboardRepository.getRecentTransactions(limit)
    }

    companion object {
        private const val DEFAULT_LIMIT = 10
        private const val MIN_LIMIT = 1
        private const val MAX_LIMIT = 100
    }
}
