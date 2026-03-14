package com.vibely.pos.shared.data.dashboard.datasource

import com.vibely.pos.shared.data.dashboard.dto.DashboardSummaryDTO
import com.vibely.pos.shared.data.dashboard.dto.LowStockProductDTO
import com.vibely.pos.shared.data.dashboard.dto.RecentTransactionDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Remote data source for dashboard API calls.
 *
 * Handles HTTP requests to the backend dashboard endpoints.
 *
 * @param httpClient The HTTP client for making requests.
 * @param baseUrl The base URL of the backend API.
 */
class RemoteDashboardDataSource(private val httpClient: HttpClient, private val baseUrl: String) {

    /**
     * Fetches the dashboard summary from the backend.
     *
     * GET /api/dashboard/summary
     *
     * @return [Result.Success] with [DashboardSummaryDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getDashboardSummary(): Result<DashboardSummaryDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/dashboard/summary").body<DashboardSummaryDTO>()
    }

    /**
     * Fetches recent transactions from the backend.
     *
     * GET /api/dashboard/recent-transactions?limit={limit}
     *
     * @param limit Maximum number of transactions to retrieve.
     * @return [Result.Success] with list of [RecentTransactionDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getRecentTransactions(limit: Int): Result<List<RecentTransactionDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/dashboard/recent-transactions") {
            parameter("limit", limit)
        }.body<List<RecentTransactionDTO>>()
    }

    /**
     * Fetches low stock products from the backend.
     *
     * GET /api/dashboard/low-stock
     *
     * @return [Result.Success] with list of [LowStockProductDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getLowStockProducts(): Result<List<LowStockProductDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/dashboard/low-stock").body<List<LowStockProductDTO>>()
    }
}
