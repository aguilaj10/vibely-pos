package com.vibely.pos.backend.routes

import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.dto.request.GetTransactionsRequest
import com.vibely.pos.backend.services.InventoryService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonObject

private const val ERROR_KEY = "error"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_TRANSACTION_ID_REQUIRED = "Transaction ID is required"
private const val CLAIM_USER_ID = "userId"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50

/**
 * Configures inventory-related routes with JWT authentication.
 *
 * @param inventoryService Service handling inventory business logic
 * @param authProvider Environment-specific authentication provider
 */
fun Route.inventoryRoutes(inventoryService: InventoryService, authProvider: RouteAuthProvider) {
    route("/api/inventory") {
        with(authProvider) {
            withAuth { usePaths(inventoryService) }
        }
    }
}

private fun Route.usePaths(inventoryService: InventoryService) {
    get("/transactions") { call.handleGetTransactions(inventoryService) }
    get("/transactions/{id}") { call.handleGetTransactionById(inventoryService) }
    post("/transactions") { call.handleCreateTransaction(inventoryService) }
}

private suspend fun ApplicationCall.handleGetTransactions(inventoryService: InventoryService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
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

private suspend fun ApplicationCall.handleGetTransactionById(inventoryService: InventoryService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val transactionId = parameters["id"]
    if (transactionId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_TRANSACTION_ID_REQUIRED))
        return
    }

    when (val result = inventoryService.getTransactionById(userId, transactionId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(
            HttpStatusCode.InternalServerError,
            mapOf(ERROR_KEY to result.message)
        )
    }
}

private suspend fun ApplicationCall.handleCreateTransaction(inventoryService: InventoryService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val transactionData = receive<JsonObject>()

    when (val result = inventoryService.createTransaction(userId, transactionData)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(
            HttpStatusCode.InternalServerError,
            mapOf(ERROR_KEY to result.message)
        )
    }
}
