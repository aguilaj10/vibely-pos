package com.vibely.pos.backend.data.datasource

import com.vibely.pos.backend.dto.request.AdjustStockRequest
import com.vibely.pos.backend.dto.request.CreateProductRequest
import com.vibely.pos.backend.dto.request.GetAllProductsRequest
import com.vibely.pos.backend.dto.request.UpdateProductRequest
import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.JsonObject

/**
 * Data source abstraction for product persistence operations.
 *
 * Implementations provide either Supabase (remote) or Room/SQLite (local) backends.
 */
interface ProductBackendDataSource {
    /**
     * Searches products by name, SKU, or barcode.
     *
     * @param query Search query string
     * @return Result containing list of matching products
     */
    suspend fun search(query: String): Result<List<ProductDTO>>

    /**
     * Retrieves a product by its ID.
     *
     * @param id Product ID
     * @return Result containing the product or error
     */
    suspend fun getById(id: String): Result<ProductDTO>

    /**
     * Retrieves all products with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of products
     */
    suspend fun getAll(request: GetAllProductsRequest): Result<List<ProductDTO>>

    /**
     * Creates a new product.
     *
     * @param userId ID of the user creating the product
     * @param request Product creation parameters
     * @return Result containing created product data or error
     */
    suspend fun createProduct(userId: String, request: CreateProductRequest): Result<JsonObject>

    /**
     * Updates an existing product.
     *
     * @param userId ID of the user updating the product
     * @param productId ID of the product to update
     * @param request Product update parameters
     * @return Result containing updated product data or error
     */
    suspend fun updateProduct(
        userId: String,
        productId: String,
        request: UpdateProductRequest,
    ): Result<JsonObject>

    /**
     * Deletes a product.
     *
     * @param userId ID of the user deleting the product
     * @param productId ID of the product to delete
     * @return Result indicating success or error
     */
    suspend fun deleteProduct(userId: String, productId: String): Result<Unit>

    /**
     * Adjusts product stock and records an inventory transaction.
     *
     * @param userId ID of the user performing the adjustment
     * @param productId ID of the product to adjust
     * @param request Stock adjustment parameters
     * @return Result containing updated product data or error
     */
    suspend fun adjustStock(
        userId: String,
        productId: String,
        request: AdjustStockRequest,
    ): Result<JsonObject>
}
