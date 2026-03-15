package com.vibely.pos.backend.routes

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
import kotlinx.serialization.Serializable

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
 * Request body for creating a product.
 *
 * @property name Product name (required)
 * @property sku Optional SKU
 * @property barcode Optional barcode
 * @property description Optional description
 * @property categoryId Optional category ID
 * @property supplierId Optional supplier ID
 * @property unitPrice Unit price (required)
 * @property costPrice Optional cost price
 * @property currentStock Initial stock quantity (required)
 * @property minStockLevel Optional minimum stock level
 * @property maxStockLevel Optional maximum stock level
 * @property reorderPoint Optional reorder point
 * @property unitOfMeasure Optional unit of measure
 * @property isActive Whether product is active (default: true)
 * @property taxRate Optional tax rate
 */
@Serializable
data class CreateProductRequest(
    val name: String,
    val sku: String? = null,
    val barcode: String? = null,
    val description: String? = null,
    val categoryId: String? = null,
    val supplierId: String? = null,
    val unitPrice: Double,
    val costPrice: Double? = null,
    val currentStock: Int,
    val minStockLevel: Int? = null,
    val maxStockLevel: Int? = null,
    val reorderPoint: Int? = null,
    val unitOfMeasure: String? = null,
    val isActive: Boolean = true,
    val taxRate: Double? = null
)

/**
 * Request body for updating a product.
 *
 * @property name Optional new name
 * @property sku Optional new SKU
 * @property barcode Optional new barcode
 * @property description Optional new description
 * @property categoryId Optional new category ID
 * @property supplierId Optional new supplier ID
 * @property unitPrice Optional new unit price
 * @property costPrice Optional new cost price
 * @property minStockLevel Optional new minimum stock level
 * @property maxStockLevel Optional new maximum stock level
 * @property reorderPoint Optional new reorder point
 * @property unitOfMeasure Optional new unit of measure
 * @property isActive Optional new active status
 * @property taxRate Optional new tax rate
 */
@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val description: String? = null,
    val categoryId: String? = null,
    val supplierId: String? = null,
    val unitPrice: Double? = null,
    val costPrice: Double? = null,
    val minStockLevel: Int? = null,
    val maxStockLevel: Int? = null,
    val reorderPoint: Int? = null,
    val unitOfMeasure: String? = null,
    val isActive: Boolean? = null,
    val taxRate: Double? = null
)

/**
 * Request body for adjusting product stock.
 *
 * @property quantity Quantity to add (positive) or remove (negative)
 * @property transactionType Type of transaction (required)
 * @property referenceType Optional reference type
 * @property referenceId Optional reference ID
 * @property notes Optional notes
 */
@Serializable
data class AdjustStockRequest(
    val quantity: Int,
    val transactionType: String,
    val referenceType: String? = null,
    val referenceId: String? = null,
    val notes: String? = null
)

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

    val createRequest = ProductService.CreateProductRequest(
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

    val updateRequest = ProductService.UpdateProductRequest(
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

    val adjustRequest = ProductService.AdjustStockRequest(
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
