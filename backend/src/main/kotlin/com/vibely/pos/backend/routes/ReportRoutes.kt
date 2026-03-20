package com.vibely.pos.backend.routes

import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.dto.request.TimePeriodRequest
import com.vibely.pos.backend.dto.request.TopProductsRequest
import com.vibely.pos.backend.dto.request.CategoryBreakdownRequest
import com.vibely.pos.backend.dto.request.CustomerAnalyticsRequest
import com.vibely.pos.backend.dto.request.SalesTrendRequest
import com.vibely.pos.backend.services.ReportService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

// Error message constants
private const val ERROR_INVALID_LIMIT = "Limit must be between 1 and 100"
private const val ERROR_INVALID_GRANULARITY = "Invalid granularity. Use: daily, weekly, monthly, or yearly"
private const val ERROR_MISSING_START_TIME = "Missing start_time"
private const val ERROR_MISSING_END_TIME = "Missing end_time"
private const val ERROR_KEY = "error"

// Limit constants
private const val DEFAULT_LIMIT = 10
private const val MIN_LIMIT = 1
private const val MAX_LIMIT = 100

// Valid granularity values
private val VALID_GRANULARITIES = listOf("daily", "weekly", "monthly", "yearly")

/**
 * Configures report routes.
 *
 * Endpoints:
 * - POST /api/reports/sales - Get sales report for time period
 * - GET /api/reports/top-products - Get top performing products
 * - POST /api/reports/category-breakdown - Get category sales breakdown
 * - GET /api/reports/customer-analytics - Get customer purchase analytics
 * - POST /api/reports/sales-trend - Get sales trend data
 *
 * All endpoints require JWT authentication.
 *
 * @param reportService Service handling report business logic
 * @param authProvider Environment-specific authentication provider
 */
fun Route.reportRoutes(reportService: ReportService, authProvider: RouteAuthProvider) {
    route("/api/reports") {
        with(authProvider) {
            withAuth { usePaths(reportService) }
        }
    }
}

private fun Route.usePaths(reportService: ReportService) {
    post("/sales") { call.handleGetSalesReport(reportService) }
    get("/top-products") { call.handleGetTopProducts(reportService) }
    post("/category-breakdown") { call.handleGetCategoryBreakdown(reportService) }
    get("/customer-analytics") { call.handleGetCustomerAnalytics(reportService) }
    post("/sales-trend") { call.handleGetSalesTrend(reportService) }
}

/**
 * POST /api/reports/sales
 * Returns aggregated sales metrics for a given time period.
 */
private suspend fun ApplicationCall.handleGetSalesReport(reportService: ReportService) {
    val request = receive<TimePeriodRequest>()

    if (request.startTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_START_TIME))
        return
    }

    if (request.endTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_END_TIME))
        return
    }

    val salesReport = reportService.getSalesReport(request.startTime, request.endTime)
    respond(HttpStatusCode.OK, salesReport)
}

/**
 * GET /api/reports/top-products?limit=10&start_time=...&end_time=...
 * Returns top performing products by revenue.
 */
private suspend fun ApplicationCall.handleGetTopProducts(reportService: ReportService) {
    val limit = request.queryParameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
    val startTime = request.queryParameters["start_time"]
    val endTime = request.queryParameters["end_time"]

    if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_LIMIT))
        return
    }

    if (startTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_START_TIME))
        return
    }

    if (endTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_END_TIME))
        return
    }

    val topProducts = reportService.getTopProducts(startTime, endTime, limit)
    respond(HttpStatusCode.OK, topProducts)
}

/**
 * POST /api/reports/category-breakdown
 * Returns category-wise sales breakdown.
 */
private suspend fun ApplicationCall.handleGetCategoryBreakdown(reportService: ReportService) {
    val request = receive<TimePeriodRequest>()

    if (request.startTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_START_TIME))
        return
    }

    if (request.endTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_END_TIME))
        return
    }

    val categoryBreakdown = reportService.getCategoryBreakdown(request.startTime, request.endTime)
    respond(HttpStatusCode.OK, categoryBreakdown)
}

/**
 * GET /api/reports/customer-analytics?limit=10&start_time=...&end_time=...
 * Returns customer purchase analytics.
 */
private suspend fun ApplicationCall.handleGetCustomerAnalytics(reportService: ReportService) {
    val limit = request.queryParameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
    val startTime = request.queryParameters["start_time"]
    val endTime = request.queryParameters["end_time"]

    if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_LIMIT))
        return
    }

    if (startTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_START_TIME))
        return
    }

    if (endTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_END_TIME))
        return
    }

    val customerAnalytics = reportService.getCustomerAnalytics(startTime, endTime, limit)
    respond(HttpStatusCode.OK, customerAnalytics)
}

/**
 * POST /api/reports/sales-trend
 * Returns sales trend data with configurable time granularity.
 */
private suspend fun ApplicationCall.handleGetSalesTrend(reportService: ReportService) {
    val request = receive<SalesTrendRequest>()

    if (request.startTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_START_TIME))
        return
    }

    if (request.endTime.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_END_TIME))
        return
    }

    val granularity = request.granularity ?: "daily"
    if (granularity.lowercase() !in VALID_GRANULARITIES) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_GRANULARITY))
        return
    }

    val salesTrend = reportService.getSalesTrend(request.startTime, request.endTime, granularity)
    respond(HttpStatusCode.OK, salesTrend)
}
