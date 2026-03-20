@file:Suppress("UndocumentedPublicFunction", "TooManyFunctions")

package com.vibely.pos.backend.routes

// Suppress TooManyFunctions - Route files follow a consistent pattern where each
// endpoint (getAll, getById, create, update, delete) requires its own handler function.

import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.dto.request.CreatePurchaseOrderRequest
import com.vibely.pos.backend.dto.request.ReceivePurchaseOrderRequest
import com.vibely.pos.backend.dto.request.UpdatePurchaseOrderRequest
import com.vibely.pos.backend.dto.request.UpdatePurchaseOrderStatusRequest
import com.vibely.pos.backend.services.PurchaseOrderService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

private const val ERROR_KEY = "error"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_ID = "Missing purchase order ID"
private const val ERROR_MISSING_SUPPLIER = "Missing supplier ID"
private const val ERROR_MISSING_STATUS = "Missing status"
private const val MSG_PO_DELETED = "Purchase order deleted successfully"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50
private const val PATH_ID = "/{id}"
private const val CLAIM_USER_ID = "userId"

/**
 * Configures purchase order routes with JWT authentication.
 *
 * @param purchaseOrderService Service handling purchase order business logic
 * @param authProvider Environment-specific authentication provider
 */
fun Route.purchaseOrderRoutes(purchaseOrderService: PurchaseOrderService, authProvider: RouteAuthProvider) {
    route("/api/purchase-orders") {
        with(authProvider) {
            withAuth { configureRoutes(purchaseOrderService) }
        }
    }
}

private fun Route.configureRoutes(purchaseOrderService: PurchaseOrderService) {
    get { call.handleGetAllPurchaseOrders(purchaseOrderService) }
    get("/generate-po-number") { call.handleGeneratePoNumber(purchaseOrderService) }
    get(PATH_ID) { call.handleGetById(purchaseOrderService) }
    post { call.handleCreatePurchaseOrder(purchaseOrderService) }
    put(PATH_ID) { call.handleUpdatePurchaseOrder(purchaseOrderService) }
    patch("/{id}/status") { call.handleUpdateStatus(purchaseOrderService) }
    post("/{id}/receive") { call.handleReceivePurchaseOrder(purchaseOrderService) }
    delete(PATH_ID) { call.handleDeletePurchaseOrder(purchaseOrderService) }
}

private suspend fun ApplicationCall.handleGetAllPurchaseOrders(purchaseOrderService: PurchaseOrderService) {
    val userId = extractUserId() ?: return

    val supplierId = request.queryParameters["supplier_id"]
    val status = request.queryParameters["status"]
    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    when (val result = purchaseOrderService.getAllPurchaseOrders(userId, supplierId, status, page, pageSize)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetById(purchaseOrderService: PurchaseOrderService) {
    val purchaseOrderId = parameters["id"]
    if (purchaseOrderId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = extractUserId() ?: return

    when (val result = purchaseOrderService.getPurchaseOrderById(userId, purchaseOrderId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleCreatePurchaseOrder(purchaseOrderService: PurchaseOrderService) {
    val userId = extractUserId() ?: return

    val requestBody = receive<CreatePurchaseOrderRequest>()

    if (requestBody.supplierId.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_SUPPLIER))
        return
    }

    when (val result = purchaseOrderService.createPurchaseOrder(userId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdatePurchaseOrder(purchaseOrderService: PurchaseOrderService) {
    val purchaseOrderId = parameters["id"]
    if (purchaseOrderId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = extractUserId() ?: return

    val requestBody = receive<UpdatePurchaseOrderRequest>()

    when (val result = purchaseOrderService.updatePurchaseOrder(userId, purchaseOrderId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateStatus(purchaseOrderService: PurchaseOrderService) {
    val purchaseOrderId = parameters["id"]
    if (purchaseOrderId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = extractUserId() ?: return

    val requestBody = receive<UpdatePurchaseOrderStatusRequest>()

    if (requestBody.status.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_STATUS))
        return
    }

    when (val result = purchaseOrderService.updatePurchaseOrderStatus(userId, purchaseOrderId, requestBody.status)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleReceivePurchaseOrder(purchaseOrderService: PurchaseOrderService) {
    val purchaseOrderId = parameters["id"]
    if (purchaseOrderId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = extractUserId() ?: return

    val requestBody = receive<ReceivePurchaseOrderRequest>()

    when (val result = purchaseOrderService.receivePurchaseOrder(userId, purchaseOrderId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleDeletePurchaseOrder(purchaseOrderService: PurchaseOrderService) {
    val purchaseOrderId = parameters["id"]
    if (purchaseOrderId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = extractUserId() ?: return

    when (val result = purchaseOrderService.deletePurchaseOrder(userId, purchaseOrderId)) {
        is Result.Success -> {
            respond(HttpStatusCode.NoContent, mapOf("message" to MSG_PO_DELETED))
        }
        is Result.Error -> {
            respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
        }
    }
}

private suspend fun ApplicationCall.handleGeneratePoNumber(purchaseOrderService: PurchaseOrderService) {
    val userId = extractUserId() ?: return

    when (val result = purchaseOrderService.generatePoNumber(userId)) {
        is Result.Success -> respond(HttpStatusCode.OK, mapOf("po_number" to result.data))
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
