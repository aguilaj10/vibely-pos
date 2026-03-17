package com.vibely.pos.shared.data.reports.datasource

import com.vibely.pos.shared.data.reports.dto.CategoryBreakdownDTO
import com.vibely.pos.shared.data.reports.dto.CustomerAnalyticsDTO
import com.vibely.pos.shared.data.reports.dto.ProductPerformanceDTO
import com.vibely.pos.shared.data.reports.dto.SalesReportDTO
import com.vibely.pos.shared.data.reports.dto.SalesTrendDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Remote data source for reports API calls.
 *
 * Handles HTTP requests to backend analytics and reporting endpoints.
 *
 * @param httpClient The HTTP client for making requests.
 * @param baseUrl The base URL of the backend API.
 */
class RemoteReportsDataSource(private val httpClient: HttpClient, private val baseUrl: String) {

    /**
     * Fetches aggregated sales report from the backend.
     *
     * GET /api/reports/sales?period={period}&startDate={startDate}&endDate={endDate}
     *
     * @param period Report period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Start date for CUSTOM period (epoch milliseconds).
     * @param customEndDate End date for CUSTOM period (epoch milliseconds).
     * @return [Result.Success] with [SalesReportDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getSalesReport(period: String, customStartDate: Long?, customEndDate: Long?): Result<SalesReportDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/reports/sales") {
            parameter("period", period)
            customStartDate?.let { parameter("startDate", it) }
            customEndDate?.let { parameter("endDate", it) }
        }.body<SalesReportDTO>()
    }

    /**
     * Fetches sales trend data from the backend.
     *
     * GET /api/reports/trends?period={period}&startDate={startDate}&endDate={endDate}
     *
     * @param period Report period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Start date for CUSTOM period (epoch milliseconds).
     * @param customEndDate End date for CUSTOM period (epoch milliseconds).
     * @return [Result.Success] with list of [SalesTrendDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getSalesTrend(period: String, customStartDate: Long?, customEndDate: Long?): Result<List<SalesTrendDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/reports/trends") {
            parameter("period", period)
            customStartDate?.let { parameter("startDate", it) }
            customEndDate?.let { parameter("endDate", it) }
        }.body<List<SalesTrendDTO>>()
    }

    /**
     * Fetches category performance breakdown from the backend.
     *
     * GET /api/reports/categories/breakdown?period={period}&startDate={startDate}&endDate={endDate}
     *
     * @param period Report period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Start date for CUSTOM period (epoch milliseconds).
     * @param customEndDate End date for CUSTOM period (epoch milliseconds).
     * @return [Result.Success] with list of [CategoryBreakdownDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getCategoryBreakdown(period: String, customStartDate: Long?, customEndDate: Long?): Result<List<CategoryBreakdownDTO>> =
        Result.runCatching {
            httpClient.get("$baseUrl/api/reports/categories/breakdown") {
                parameter("period", period)
                customStartDate?.let { parameter("startDate", it) }
                customEndDate?.let { parameter("endDate", it) }
            }.body<List<CategoryBreakdownDTO>>()
        }

    /**
     * Fetches top performing products from the backend.
     *
     * GET /api/reports/products/top?period={period}&startDate={startDate}&endDate={endDate}
     *
     * @param period Report period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Start date for CUSTOM period (epoch milliseconds).
     * @param customEndDate End date for CUSTOM period (epoch milliseconds).
     * @return [Result.Success] with list of [ProductPerformanceDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getTopProducts(period: String, customStartDate: Long?, customEndDate: Long?): Result<List<ProductPerformanceDTO>> =
        Result.runCatching {
            httpClient.get("$baseUrl/api/reports/products/top") {
                parameter("period", period)
                customStartDate?.let { parameter("startDate", it) }
                customEndDate?.let { parameter("endDate", it) }
            }.body<List<ProductPerformanceDTO>>()
        }

    /**
     * Fetches customer analytics (top customers) from the backend.
     *
     * GET /api/reports/customers/top?period={period}&startDate={startDate}&endDate={endDate}
     *
     * @param period Report period (TODAY, THIS_WEEK, THIS_MONTH, CUSTOM).
     * @param customStartDate Start date for CUSTOM period (epoch milliseconds).
     * @param customEndDate End date for CUSTOM period (epoch milliseconds).
     * @return [Result.Success] with list of [CustomerAnalyticsDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getCustomerAnalytics(period: String, customStartDate: Long?, customEndDate: Long?): Result<List<CustomerAnalyticsDTO>> =
        Result.runCatching {
            httpClient.get("$baseUrl/api/reports/customers/top") {
                parameter("period", period)
                customStartDate?.let { parameter("startDate", it) }
                customEndDate?.let { parameter("endDate", it) }
            }.body<List<CustomerAnalyticsDTO>>()
        }
}
