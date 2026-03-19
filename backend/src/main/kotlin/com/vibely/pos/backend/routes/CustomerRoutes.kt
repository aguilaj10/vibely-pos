@file:Suppress("UndocumentedPublicFunction", "ComplexCondition")

package com.vibely.pos.backend.routes

import com.vibely.pos.backend.dto.request.AddLoyaltyPointsRequest
import com.vibely.pos.backend.dto.request.CreateCustomerRequest
import com.vibely.pos.backend.dto.request.UpdateCustomerRequest
import com.vibely.pos.backend.services.CustomerService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

private const val ERROR_KEY = "error"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_ID = "Missing customer ID"
private const val ERROR_MISSING_REQUIRED = "Missing required field"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50
private const val PATH_ID = "/{id}"
private const val PATH_LOYALTY_POINTS = "/{id}/loyalty-points"
private const val PATH_PURCHASE_HISTORY = "/{id}/purchase-history"
private const val CLAIM_USER_ID = "userId"

fun Route.customerRoutes(customerService: CustomerService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api/customers") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                usePaths(customerService)
            }
        } else {
            authenticate("auth-jwt") {
                usePaths(customerService)
            }
        }
    }
}

private fun Route.usePaths(customerService: CustomerService) {
    get { call.handleGetAllCustomers(customerService) }
    get(PATH_ID) { call.handleGetById(customerService) }
    post { call.handleCreateCustomer(customerService) }
    put(PATH_ID) { call.handleUpdateCustomer(customerService) }
    delete(PATH_ID) { call.handleDeleteCustomer(customerService) }
    post(PATH_LOYALTY_POINTS) { call.handleAddLoyaltyPoints(customerService) }
    get(PATH_PURCHASE_HISTORY) { call.handleGetPurchaseHistory(customerService) }
}

private suspend fun ApplicationCall.handleGetAllCustomers(customerService: CustomerService) {
    val userId =
        principal<JWTPrincipal>()
            ?.payload
            ?.getClaim(CLAIM_USER_ID)
            ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val isActive = request.queryParameters["is_active"]?.toBooleanStrictOrNull()
    val search = request.queryParameters["search"]
    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    when (val result = customerService.getAllCustomers(userId, isActive, search, page, pageSize)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetById(customerService: CustomerService) {
    val customerId = parameters["id"]
    if (customerId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId =
        principal<JWTPrincipal>()
            ?.payload
            ?.getClaim(CLAIM_USER_ID)
            ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = customerService.getCustomerById(userId, customerId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleCreateCustomer(customerService: CustomerService) {
    val userId =
        principal<JWTPrincipal>()
            ?.payload
            ?.getClaim(CLAIM_USER_ID)
            ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<CreateCustomerRequest>()

    if (requestBody.code.isBlank() || requestBody.fullName.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_REQUIRED))
        return
    }

    when (val result = customerService.createCustomer(userId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateCustomer(customerService: CustomerService) {
    val customerId = parameters["id"]
    if (customerId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId =
        principal<JWTPrincipal>()
            ?.payload
            ?.getClaim(CLAIM_USER_ID)
            ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<UpdateCustomerRequest>()

    when (val result = customerService.updateCustomer(userId, customerId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleDeleteCustomer(customerService: CustomerService) {
    val customerId = parameters["id"]
    if (customerId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId =
        principal<JWTPrincipal>()
            ?.payload
            ?.getClaim(CLAIM_USER_ID)
            ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = customerService.deleteCustomer(userId, customerId)) {
        is Result.Success -> respond(HttpStatusCode.NoContent, mapOf("message" to "Customer deleted successfully"))
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleAddLoyaltyPoints(customerService: CustomerService) {
    val customerId = parameters["id"]
    if (customerId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId =
        principal<JWTPrincipal>()
            ?.payload
            ?.getClaim(CLAIM_USER_ID)
            ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<AddLoyaltyPointsRequest>()

    when (val result = customerService.addLoyaltyPoints(userId, customerId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetPurchaseHistory(customerService: CustomerService) {
    val customerId = parameters["id"]
    if (customerId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId =
        principal<JWTPrincipal>()
            ?.payload
            ?.getClaim(CLAIM_USER_ID)
            ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    when (val result = customerService.getPurchaseHistory(userId, customerId, page, pageSize)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}
