package com.vibely.pos.backend.routes

import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.services.DashboardService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

// Error message constants
private const val ERROR_INVALID_LIMIT = "Limit must be between 1 and 100"
private const val ERROR_KEY = "error"

// Limit constants
private const val DEFAULT_LIMIT = 10
private const val MIN_LIMIT = 1
private const val MAX_LIMIT = 100

/**
 * Configures dashboard routes.
 *
 * Endpoints:
 * - GET /api/dashboard/summary - Get dashboard summary (today's sales, transactions, low stock, active shift)
 * - GET /api/dashboard/recent-transactions?limit=10 - Get recent transactions
 * - GET /api/dashboard/low-stock - Get low stock products
 *
 * All endpoints require JWT authentication.
 *
 * @param dashboardService Service handling dashboard business logic
 * @param authProvider Environment-specific authentication provider
 */
fun Route.dashboardRoutes(dashboardService: DashboardService, authProvider: RouteAuthProvider) {
    route("/api/dashboard") {
        with(authProvider) {
            withAuth { usePaths(dashboardService) }
        }
    }
}

private fun Route.usePaths(dashboardService: DashboardService) {
    get("/summary") { call.handleGetSummary(dashboardService) }
    get("/recent-transactions") { call.handleGetRecentTransactions(dashboardService) }
    get("/low-stock") { call.handleGetLowStock(dashboardService) }
}

/**
 * GET /api/dashboard/summary
 * Returns aggregated dashboard metrics for today.
 */
private suspend fun ApplicationCall.handleGetSummary(dashboardService: DashboardService) {
    when (val result = dashboardService.getDashboardSummary()) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

/**
 * GET /api/dashboard/recent-transactions?limit=10
 * Returns recent transactions ordered by date descending.
 */
private suspend fun ApplicationCall.handleGetRecentTransactions(dashboardService: DashboardService) {
    val limit = request.queryParameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT

    if (limit !in MIN_LIMIT..MAX_LIMIT) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_LIMIT))
        return
    }

    when (val result = dashboardService.getRecentTransactions(limit)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

/**
 * GET /api/dashboard/low-stock
 * Returns products with current stock below minimum threshold.
 */
private suspend fun ApplicationCall.handleGetLowStock(dashboardService: DashboardService) {
    when (val result = dashboardService.getLowStockProducts()) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}
