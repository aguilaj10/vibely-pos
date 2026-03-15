package com.vibely.pos.backend.routes

import com.vibely.pos.backend.services.ProductService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import com.vibely.pos.shared.domain.result.Result

private const val ERROR_MISSING_QUERY = "Missing search query parameter 'q'"
private const val ERROR_MISSING_ID = "Missing product ID"
private const val ERROR_KEY = "error"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50

/**
 * Configures product-related routes with JWT authentication.
 *
 * @param productService Service handling product business logic
 */
fun Route.productRoutes(productService: ProductService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api/products") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                get("/search") { call.handleSearch(productService) }
                get("/{id}") { call.handleGetById(productService) }
                get { call.handleGetAll(productService) }
            }
        } else {
            authenticate("auth-jwt") {
                get("/search") { call.handleSearch(productService) }
                get("/{id}") { call.handleGetById(productService) }
                get { call.handleGetAll(productService) }
            }
        }
    }
}

private suspend fun ApplicationCall.handleSearch(productService: ProductService) {
    val query = request.queryParameters["q"]
    if (query == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_QUERY))
        return
    }

    when (val result = productService.search(query)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetById(productService: ProductService) {
    val id = parameters["id"]
    if (id == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    when (val result = productService.getById(id)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetAll(productService: ProductService) {
    val categoryId = request.queryParameters["category_id"]
    val isActive = request.queryParameters["is_active"]?.toBoolean()
    val lowStockOnly = request.queryParameters["low_stock"]?.toBoolean() ?: false
    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    val getAllRequest = ProductService.GetAllRequest(
        categoryId = categoryId,
        isActive = isActive,
        lowStockOnly = lowStockOnly,
        page = page,
        pageSize = pageSize
    )

    when (val result = productService.getAll(getAllRequest)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(
            HttpStatusCode.InternalServerError,
            mapOf(ERROR_KEY to result.message)
        )
    }
}
