package com.vibely.pos.backend.routes

import com.vibely.pos.backend.services.SaleService
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_ID = "Missing sale ID"
private const val ERROR_MISSING_STATUS = "Missing status field"
private const val ERROR_KEY = "error"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50

/**
 * Configures sales-related routes with JWT authentication.
 *
 * @param saleService Service handling sales business logic
 */
fun Route.salesRoutes(saleService: SaleService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api/sales") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                post { call.handleCreateSale(saleService) }
                get { call.handleGetAll(saleService) }
                get("/{id}") { call.handleGetById(saleService) }
                get("/{id}/items") { call.handleGetItems(saleService) }
                put("/{id}/status") { call.handleUpdateStatus(saleService) }
            }
        } else {
            authenticate("auth-jwt") {
                post { call.handleCreateSale(saleService) }
                get { call.handleGetAll(saleService) }
                get("/{id}") { call.handleGetById(saleService) }
                get("/{id}/items") { call.handleGetItems(saleService) }
                put("/{id}/status") { call.handleUpdateStatus(saleService) }
            }
        }
    }
}

private suspend fun ApplicationCall.handleCreateSale(saleService: SaleService) {
    val request = receive<CreateSaleRequest>()
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("userId")
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = saleService.createSale(request, userId)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetAll(saleService: SaleService) {
    val startDate = request.queryParameters["start_date"]
    val endDate = request.queryParameters["end_date"]
    val status = request.queryParameters["status"]
    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    val getAllRequest = SaleService.GetAllRequest(
        startDate = startDate,
        endDate = endDate,
        status = status,
        page = page,
        pageSize = pageSize
    )

    when (val result = saleService.getAll(getAllRequest)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(
            HttpStatusCode.InternalServerError,
            mapOf(ERROR_KEY to result.message)
        )
    }
}

private suspend fun ApplicationCall.handleGetById(saleService: SaleService) {
    val id = parameters["id"]
    if (id == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    when (val result = saleService.getById(id)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetItems(saleService: SaleService) {
    val id = parameters["id"]
    if (id == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    when (val result = saleService.getItems(id)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(
            HttpStatusCode.InternalServerError,
            mapOf(ERROR_KEY to result.message)
        )
    }
}

private suspend fun ApplicationCall.handleUpdateStatus(saleService: SaleService) {
    val id = parameters["id"]
    if (id == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val requestBody = receive<Map<String, String>>()
    val status = requestBody["status"]
    if (status == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_STATUS))
        return
    }

    when (val result = saleService.updateStatus(id, status)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}
