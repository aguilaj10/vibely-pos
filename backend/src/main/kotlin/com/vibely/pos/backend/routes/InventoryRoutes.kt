package com.vibely.pos.backend.routes

import com.vibely.pos.backend.dto.request.GetTransactionsRequest
import com.vibely.pos.backend.services.InventoryService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

private const val ERROR_KEY = "error"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50

/**
 * Configures inventory-related routes with JWT authentication.
 *
 * @param inventoryService Service handling inventory business logic
 */
fun Route.inventoryRoutes(inventoryService: InventoryService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api/inventory") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                get("/transactions") { call.handleGetTransactions(inventoryService) }
            }
        } else {
            authenticate("auth-jwt") {
                get("/transactions") { call.handleGetTransactions(inventoryService) }
            }
        }
    }
}

private suspend fun ApplicationCall.handleGetTransactions(inventoryService: InventoryService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("userId")
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val productId = request.queryParameters["product_id"]
    val type = request.queryParameters["type"]
    val startDate = request.queryParameters["start_date"]
    val endDate = request.queryParameters["end_date"]
    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    when (val result = inventoryService.getTransactions(
        userId = userId,
        request = GetTransactionsRequest(
            productId = productId,
            type = type,
            startDate = startDate,
            endDate = endDate,
            page = page,
            pageSize = pageSize
        )
    )) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(
            HttpStatusCode.InternalServerError,
            mapOf(ERROR_KEY to result.message)
        )
    }
}
