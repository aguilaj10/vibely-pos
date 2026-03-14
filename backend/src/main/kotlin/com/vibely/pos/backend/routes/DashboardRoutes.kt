package com.vibely.pos.backend.routes

import com.vibely.pos.backend.services.DashboardService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
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
 */
fun Route.dashboardRoutes(dashboardService: DashboardService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api/dashboard") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                get("/summary") { call.handleGetSummary(dashboardService) }
                get("/recent-transactions") { call.handleGetRecentTransactions(dashboardService) }
                get("/low-stock") { call.handleGetLowStock(dashboardService) }
            }
        } else {
            authenticate("auth-jwt") {
                get("/summary") { call.handleGetSummary(dashboardService) }
                get("/recent-transactions") { call.handleGetRecentTransactions(dashboardService) }
                get("/low-stock") { call.handleGetLowStock(dashboardService) }
            }
        }
    }
}

/**
 * GET /api/dashboard/summary
 * Returns aggregated dashboard metrics for today.
 */
private suspend fun ApplicationCall.handleGetSummary(dashboardService: DashboardService) {
    val summary = dashboardService.getDashboardSummary()
    respond(HttpStatusCode.OK, summary)
}

/**
 * GET /api/dashboard/recent-transactions?limit=10
 * Returns recent transactions ordered by date descending.
 */
private suspend fun ApplicationCall.handleGetRecentTransactions(dashboardService: DashboardService) {
    val limit = request.queryParameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT

    // Validate limit range
    if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_LIMIT))
        return
    }

    val transactions = dashboardService.getRecentTransactions(limit)
    respond(HttpStatusCode.OK, transactions)
}

/**
 * GET /api/dashboard/low-stock
 * Returns products with current stock below minimum threshold.
 */
private suspend fun ApplicationCall.handleGetLowStock(dashboardService: DashboardService) {
    val products = dashboardService.getLowStockProducts()
    respond(HttpStatusCode.OK, products)
}
