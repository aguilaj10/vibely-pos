@file:Suppress("UndocumentedPublicFunction")

package com.vibely.pos.backend.routes

import com.vibely.pos.backend.dto.request.CloseShiftRequest
import com.vibely.pos.backend.dto.request.OpenShiftRequest
import com.vibely.pos.backend.services.ShiftService
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
import io.ktor.server.routing.route

private const val ERROR_KEY = "error"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_ID = "Missing shift ID"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50
private const val PATH_ID = "/{id}"
private const val CLAIM_USER_ID = "userId"

fun Route.shiftRoutes(shiftService: ShiftService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api/shifts") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                configureShiftRoutes(shiftService)
            }
        } else {
            authenticate("auth-jwt") {
                configureShiftRoutes(shiftService)
            }
        }
    }
}

private fun Route.configureShiftRoutes(shiftService: ShiftService) {
    get { call.handleGetShiftHistory(shiftService) }
    get("/current") { call.handleGetCurrentShift(shiftService) }
    get("/generate-shift-number") { call.handleGenerateShiftNumber(shiftService) }
    get(PATH_ID) { call.handleGetById(shiftService) }
    get("/{id}/summary") { call.handleGetShiftSummary(shiftService) }
    post("/open") { call.handleOpenShift(shiftService) }
    post("/{id}/close") { call.handleCloseShift(shiftService) }
}

private suspend fun ApplicationCall.handleGetShiftHistory(shiftService: ShiftService) {
    val userId = extractUserId() ?: return

    val cashierId = request.queryParameters["cashier_id"]
    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    when (val result = shiftService.getShiftHistory(userId, cashierId, page, pageSize)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetCurrentShift(shiftService: ShiftService) {
    val userId = extractUserId() ?: return

    when (val result = shiftService.getCurrentShift(userId)) {
        is Result.Success -> {
            val shift = result.data
            if (shift != null) {
                respond(HttpStatusCode.OK, shift)
            } else {
                respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to "No open shift found"))
            }
        }
        is Result.Error -> {
            respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
        }
    }
}

private suspend fun ApplicationCall.handleGetById(shiftService: ShiftService) {
    val shiftId = parameters["id"]
    if (shiftId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = extractUserId() ?: return

    when (val result = shiftService.getShiftById(userId, shiftId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleOpenShift(shiftService: ShiftService) {
    val userId = extractUserId() ?: return

    val requestBody = receive<OpenShiftRequest>()

    when (val result = shiftService.openShift(userId, requestBody.openingBalance)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleCloseShift(shiftService: ShiftService) {
    val shiftId = parameters["id"]
    if (shiftId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = extractUserId() ?: return

    val requestBody = receive<CloseShiftRequest>()

    when (val result = shiftService.closeShift(userId, shiftId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetShiftSummary(shiftService: ShiftService) {
    val shiftId = parameters["id"]
    if (shiftId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = extractUserId() ?: return

    when (val result = shiftService.getShiftSummary(userId, shiftId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGenerateShiftNumber(shiftService: ShiftService) {
    val userId = extractUserId() ?: return

    when (val result = shiftService.generateShiftNumber(userId)) {
        is Result.Success -> respond(HttpStatusCode.OK, mapOf("shift_number" to result.data))
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.extractUserId(): String? {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
    }
    return userId
}
