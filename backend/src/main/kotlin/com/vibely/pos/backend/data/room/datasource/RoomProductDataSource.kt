package com.vibely.pos.backend.data.room.datasource

import com.vibely.pos.backend.data.datasource.ProductBackendDataSource
import com.vibely.pos.backend.data.room.dao.ProductDao
import com.vibely.pos.backend.data.room.mapper.toDto
import com.vibely.pos.backend.data.room.mapper.toEntity
import com.vibely.pos.backend.dto.request.AdjustStockRequest
import com.vibely.pos.backend.dto.request.CreateProductRequest
import com.vibely.pos.backend.dto.request.GetAllProductsRequest
import com.vibely.pos.backend.dto.request.UpdateProductRequest
import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ERROR_NOT_FOUND = "Product not found"

/**
 * Room/SQLite-backed implementation of [ProductBackendDataSource].
 *
 * User-scoped operations (userId filtering) are not enforced in local mode because the
 * SQLite database is assumed to be single-tenant when running on a local network.
 */
class RoomProductDataSource(
    private val productDao: ProductDao,
) : ProductBackendDataSource {
    /**
     * Searches products by name, SKU, or barcode.
     *
     * @param query Search query string
     * @return Result containing list of matching products
     */
    override suspend fun search(query: String): Result<List<ProductDTO>> =
        runCatchingSuspend { productDao.search("%$query%").map { it.toDto() } }

    /**
     * Retrieves a product by its ID.
     *
     * @param id Product ID
     * @return Result containing the product or error
     */
    override suspend fun getById(id: String): Result<ProductDTO> =
        runCatchingSuspend { productDao.getById(id)?.toDto() ?: error(ERROR_NOT_FOUND) }

    /**
     * Retrieves all products with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of products
     */
    override suspend fun getAll(request: GetAllProductsRequest): Result<List<ProductDTO>> =
        runCatchingSuspend {
            val offset = (request.page - 1) * request.pageSize
            productDao.getAll(
                categoryId = request.categoryId,
                isActive = request.isActive,
                limit = request.pageSize,
                offset = offset,
            ).map { it.toDto() }
        }

    /**
     * Creates a new product.
     *
     * @param userId ID of the user creating the product (stored as reference; not enforced locally)
     * @param request Product creation parameters
     * @return Result containing created product data or error
     */
    override suspend fun createProduct(userId: String, request: CreateProductRequest): Result<JsonObject> =
        runCatchingSuspend {
            val entity = request.toEntity(userId)
            productDao.insert(entity)
            buildJsonObject { put("id", entity.id) }
        }

    /**
     * Updates an existing product.
     *
     * @param userId ID of the user updating the product (not enforced locally)
     * @param productId ID of the product to update
     * @param request Product update parameters
     * @return Result containing updated product data or error
     */
    override suspend fun updateProduct(
        userId: String,
        productId: String,
        request: UpdateProductRequest,
    ): Result<JsonObject> =
        runCatchingSuspend {
            val existing = productDao.getById(productId) ?: error(ERROR_NOT_FOUND)
            val updated = existing.copy(
                name = request.name ?: existing.name,
                sku = request.sku ?: existing.sku,
                barcode = request.barcode ?: existing.barcode,
                description = request.description ?: existing.description,
                categoryId = request.categoryId ?: existing.categoryId,
                sellingPrice = request.unitPrice ?: existing.sellingPrice,
                costPrice = request.costPrice ?: existing.costPrice,
                costCurrencyCode = request.costCurrencyCode ?: existing.costCurrencyCode,
                minStockLevel = request.minStockLevel ?: existing.minStockLevel,
                unit = request.unitOfMeasure ?: existing.unit,
                isActive = request.isActive ?: existing.isActive,
            )
            productDao.update(updated)
            buildJsonObject { put("id", productId) }
        }

    /**
     * Deletes a product.
     *
     * @param userId ID of the user deleting the product (not enforced locally)
     * @param productId ID of the product to delete
     * @return Result indicating success or error
     */
    override suspend fun deleteProduct(userId: String, productId: String): Result<Unit> =
        runCatchingSuspend { productDao.delete(productId) }

    /**
     * Adjusts product stock directly in the local database.
     *
     * Inventory transaction logging is skipped in local mode. Stock cannot go negative.
     *
     * @param userId ID of the user performing the adjustment (not enforced locally)
     * @param productId ID of the product to adjust
     * @param request Stock adjustment parameters
     * @return Result containing updated product data or error
     */
    override suspend fun adjustStock(
        userId: String,
        productId: String,
        request: AdjustStockRequest,
    ): Result<JsonObject> =
        runCatchingSuspend {
            val existing = productDao.getById(productId) ?: error(ERROR_NOT_FOUND)
            val newStock = existing.currentStock + request.quantity

            require(newStock >= 0) {
                "Stock adjustment would result in negative stock. " +
                    "Current: ${existing.currentStock}, Adjustment: ${request.quantity}, " +
                    "Resulting: $newStock"
            }

            productDao.update(existing.copy(currentStock = newStock))
            buildJsonObject {
                put("id", productId)
                put("current_stock", newStock)
            }
        }
}
