package com.vibely.pos.backend.routes

import com.vibely.pos.backend.dto.request.AdjustStockRequest
import com.vibely.pos.backend.dto.request.CreateProductRequest
import com.vibely.pos.backend.dto.request.GetAllProductsRequest
import com.vibely.pos.backend.dto.request.UpdateProductRequest
import com.vibely.pos.backend.services.ProductService
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
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

private const val ERROR_MISSING_QUERY = "Missing search query parameter 'q'"
private const val ERROR_MISSING_ID = "Missing product ID"
private const val ERROR_KEY = "error"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_NAME = "Missing required field: name"
private const val ERROR_MISSING_UNIT_PRICE = "Missing required field: unitPrice"
private const val ERROR_MISSING_QUANTITY = "Missing required field: quantity"
private const val ERROR_MISSING_TRANSACTION_TYPE = "Missing required field: transactionType"
private const val PATH_ID = "/{id}"
private const val CLAIM_USER_ID = "userId"

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
                get(PATH_ID) { call.handleGetById(productService) }
                get { call.handleGetAll(productService) }
                post { call.handleCreateProduct(productService) }
                put(PATH_ID) { call.handleUpdateProduct(productService) }
                delete(PATH_ID) { call.handleDeleteProduct(productService) }
                post("/{id}/adjust-stock") { call.handleAdjustStock(productService) }
            }
        } else {
            authenticate("auth-jwt") {
                get("/search") { call.handleSearch(productService) }
                get(PATH_ID) { call.handleGetById(productService) }
                get { call.handleGetAll(productService) }
                post { call.handleCreateProduct(productService) }
                put(PATH_ID) { call.handleUpdateProduct(productService) }
                delete(PATH_ID) { call.handleDeleteProduct(productService) }
                post("/{id}/adjust-stock") { call.handleAdjustStock(productService) }
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

    val getAllRequest = GetAllProductsRequest(
        categoryId = categoryId,
        isActive = isActive,
        lowStock = lowStockOnly,
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

private suspend fun ApplicationCall.handleCreateProduct(productService: ProductService) {
    val requestBody = receive<CreateProductRequest>()
    val userId = getUserIdFromPrincipal()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    if (!validateCreateProductRequest(requestBody)) {
        return
    }

    val createRequest = CreateProductRequest(
        name = requestBody.name,
        sku = requestBody.sku,
        barcode = requestBody.barcode,
        description = requestBody.description,
        categoryId = requestBody.categoryId,
        supplierId = requestBody.supplierId,
        unitPrice = requestBody.unitPrice,
        costPrice = requestBody.costPrice,
        currentStock = requestBody.currentStock,
        minStockLevel = requestBody.minStockLevel,
        maxStockLevel = requestBody.maxStockLevel,
        reorderPoint = requestBody.reorderPoint,
        unitOfMeasure = requestBody.unitOfMeasure,
        isActive = requestBody.isActive,
        taxRate = requestBody.taxRate
    )

    when (val result = productService.createProduct(userId, createRequest)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private fun ApplicationCall.getUserIdFromPrincipal(): String? {
    return principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()
}

private suspend fun ApplicationCall.validateCreateProductRequest(request: CreateProductRequest): Boolean {
    if (request.name.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_NAME))
        return false
    }

    if (request.unitPrice < 0) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_UNIT_PRICE))
        return false
    }

    return true
}

private suspend fun ApplicationCall.handleUpdateProduct(productService: ProductService) {
    val productId = parameters["id"]
    if (productId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = getUserIdFromPrincipal()
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<UpdateProductRequest>()

    val updateRequest = UpdateProductRequest(
        name = requestBody.name,
        sku = requestBody.sku,
        barcode = requestBody.barcode,
        description = requestBody.description,
        categoryId = requestBody.categoryId,
        supplierId = requestBody.supplierId,
        unitPrice = requestBody.unitPrice,
        costPrice = requestBody.costPrice,
        minStockLevel = requestBody.minStockLevel,
        maxStockLevel = requestBody.maxStockLevel,
        reorderPoint = requestBody.reorderPoint,
        unitOfMeasure = requestBody.unitOfMeasure,
        isActive = requestBody.isActive,
        taxRate = requestBody.taxRate
    )

    when (val result = productService.updateProduct(userId, productId, updateRequest)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleDeleteProduct(productService: ProductService) {
    val productId = parameters["id"]
    if (productId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = getUserIdFromPrincipal()
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = productService.deleteProduct(userId, productId)) {
        is Result.Success -> respond(HttpStatusCode.NoContent, mapOf("message" to "Product deleted successfully"))
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleAdjustStock(productService: ProductService) {
    val productId = parameters["id"]
    if (productId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = getUserIdFromPrincipal()
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<AdjustStockRequest>()
    val validationError = when {
        requestBody.quantity == 0 -> ERROR_MISSING_QUANTITY
        requestBody.transactionType.isBlank() -> ERROR_MISSING_TRANSACTION_TYPE
        else -> null
    }
    if (validationError != null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to validationError))
        return
    }

    val adjustRequest = AdjustStockRequest(
        quantity = requestBody.quantity,
        transactionType = requestBody.transactionType,
        referenceType = requestBody.referenceType,
        referenceId = requestBody.referenceId,
        notes = requestBody.notes
    )

    when (val result = productService.adjustStock(userId, productId, adjustRequest)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}
