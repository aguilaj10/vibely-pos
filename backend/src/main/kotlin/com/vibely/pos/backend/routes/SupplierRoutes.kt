@file:Suppress("UndocumentedPublicFunction")
package com.vibely.pos.backend.routes

import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.dto.request.CreateSupplierRequest
import com.vibely.pos.backend.dto.request.UpdateSupplierRequest
import com.vibely.pos.backend.services.SupplierService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
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
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("SupplierRoutes")

private const val ERROR_KEY = "error"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_ID = "Missing supplier ID"
private const val ERROR_MISSING_REQUIRED = "Missing required field"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50
private const val PATH_ID = "/{id}"
private const val CLAIM_USER_ID = "userId"

/**
 * Configures supplier-related routes with JWT authentication.
 *
 * @param supplierService Service handling supplier business logic
 * @param authProvider Environment-specific authentication provider
 */
fun Route.supplierRoutes(supplierService: SupplierService, authProvider: RouteAuthProvider) {
    route("/api/suppliers") {
        with(authProvider) {
            withAuth { usePaths(supplierService) }
        }
    }
}

private fun Route.usePaths(supplierService: SupplierService) {
    get { call.handleGetAllSuppliers(supplierService) }
    get(PATH_ID) { call.handleGetById(supplierService) }
    post { call.handleCreateSupplier(supplierService) }
    put(PATH_ID) { call.handleUpdateSupplier(supplierService) }
    delete(PATH_ID) { call.handleDeleteSupplier(supplierService) }
}

private suspend fun ApplicationCall.handleGetAllSuppliers(supplierService: SupplierService) {
    val userId = principal<JWTPrincipal>()
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

    when (val result = supplierService.getAllSuppliers(userId, isActive, search, page, pageSize)) {
        is Result.Success -> {
            respond(HttpStatusCode.OK, result.data)
        }
        is Result.Error -> {
            logger.error("Failed to fetch suppliers: ${result.message}", result.cause)
            respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
        }
    }
}

private suspend fun ApplicationCall.handleGetById(supplierService: SupplierService) {
    val supplierId = parameters["id"]
    if (supplierId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = supplierService.getSupplierById(userId, supplierId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleCreateSupplier(supplierService: SupplierService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<CreateSupplierRequest>()

    if (requestBody.code.isBlank() || requestBody.name.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_REQUIRED))
        return
    }

    when (val result = supplierService.createSupplier(userId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateSupplier(supplierService: SupplierService) {
    val supplierId = parameters["id"]
    if (supplierId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<UpdateSupplierRequest>()

    when (val result = supplierService.updateSupplier(userId, supplierId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleDeleteSupplier(supplierService: SupplierService) {
    val supplierId = parameters["id"]
    if (supplierId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = supplierService.deleteSupplier(userId, supplierId)) {
        is Result.Success -> respond(HttpStatusCode.NoContent, mapOf("message" to "Supplier deleted successfully"))
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}
