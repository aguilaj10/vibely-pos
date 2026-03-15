package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.dto.request.AdjustStockRequest
import com.vibely.pos.backend.dto.request.CreateProductRequest
import com.vibely.pos.backend.dto.request.GetAllProductsRequest
import com.vibely.pos.backend.dto.request.UpdateProductRequest
import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val TABLE_PRODUCTS = "products"
private const val TABLE_INVENTORY_TRANSACTIONS = "inventory_transactions"
private const val SEARCH_RESULT_LIMIT = 50L
private const val ERROR_SEARCH_FAILED = "Failed to search products"
private const val ERROR_PRODUCT_NOT_FOUND = "Product not found"
private const val ERROR_FETCH_FAILED = "Failed to fetch products"
private const val ERROR_CREATE_FAILED = "Failed to create product"
private const val ERROR_UPDATE_FAILED = "Failed to update product"
private const val ERROR_DELETE_FAILED = "Failed to delete product"
private const val ERROR_STOCK_ADJUST_FAILED = "Failed to adjust stock"

private fun buildProductCreateData(userId: String, request: CreateProductRequest): JsonObject {
    return buildJsonObject {
        put(DatabaseColumns.USER_ID, userId)
        put(DatabaseColumns.NAME, request.name)
        put(DatabaseColumns.UNIT_PRICE, request.unitPrice)
        put(DatabaseColumns.CURRENT_STOCK, request.currentStock)
        put(DatabaseColumns.IS_ACTIVE, request.isActive)
        request.sku?.let { put(DatabaseColumns.SKU, it) }
        request.barcode?.let { put(DatabaseColumns.BARCODE, it) }
        request.description?.let { put(DatabaseColumns.DESCRIPTION, it) }
        request.categoryId?.let { put(DatabaseColumns.CATEGORY_ID, it) }
        request.supplierId?.let { put(DatabaseColumns.SUPPLIER_ID, it) }
        request.costPrice?.let { put(DatabaseColumns.COST_PRICE, it) }
        request.minStockLevel?.let { put(DatabaseColumns.MIN_STOCK_LEVEL, it) }
        request.maxStockLevel?.let { put(DatabaseColumns.MAX_STOCK_LEVEL, it) }
        request.reorderPoint?.let { put(DatabaseColumns.REORDER_POINT, it) }
        request.unitOfMeasure?.let { put(DatabaseColumns.UNIT_OF_MEASURE, it) }
        request.taxRate?.let { put(DatabaseColumns.TAX_RATE, it) }
    }
}

private fun buildProductUpdateData(request: UpdateProductRequest): JsonObject {
    return buildJsonObject {
        put(DatabaseColumns.NAME, request.name).takeIf { it != null }
        put(DatabaseColumns.SKU, request.sku).takeIf { it != null }
        put(DatabaseColumns.BARCODE, request.barcode).takeIf { it != null }
        put(DatabaseColumns.DESCRIPTION, request.description).takeIf { it != null }
        put(DatabaseColumns.CATEGORY_ID, request.categoryId).takeIf { it != null }
        put(DatabaseColumns.SUPPLIER_ID, request.supplierId).takeIf { it != null }
        put(DatabaseColumns.UNIT_PRICE, request.unitPrice).takeIf { it != null }
        put(DatabaseColumns.COST_PRICE, request.costPrice).takeIf { it != null }
        put(DatabaseColumns.MIN_STOCK_LEVEL, request.minStockLevel).takeIf { it != null }
        put(DatabaseColumns.MAX_STOCK_LEVEL, request.maxStockLevel).takeIf { it != null }
        put(DatabaseColumns.REORDER_POINT, request.reorderPoint).takeIf { it != null }
        put(DatabaseColumns.UNIT_OF_MEASURE, request.unitOfMeasure).takeIf { it != null }
        put(DatabaseColumns.IS_ACTIVE, request.isActive).takeIf { it != null }
        put(DatabaseColumns.TAX_RATE, request.taxRate).takeIf { it != null }
    }
}

private fun buildInventoryTransactionData(
    userId: String,
    productId: String,
    request: AdjustStockRequest,
    newStock: Int
): JsonObject {
    return buildJsonObject {
        put(DatabaseColumns.USER_ID, userId)
        put("product_id", productId)
        put(DatabaseColumns.QUANTITY, request.quantity)
        put(DatabaseColumns.TRANSACTION_TYPE, request.transactionType)
        put("quantity_after", newStock)
        request.referenceType?.let { put(DatabaseColumns.REFERENCE_TYPE, it) }
        request.referenceId?.let { put(DatabaseColumns.REFERENCE_ID, it) }
        request.notes?.let { put(DatabaseColumns.NOTES, it) }
    }
}

private data class InventoryTransactionParams(
    val userId: String,
    val productId: String,
    val request: AdjustStockRequest,
    val newStock: Int
)

private suspend fun insertInventoryTransaction(
    supabaseClient: SupabaseClient,
    params: InventoryTransactionParams
) {
    val transactionData = buildInventoryTransactionData(
        params.userId, params.productId, params.request, params.newStock
    )
    supabaseClient.from(TABLE_INVENTORY_TRANSACTIONS)
        .insert(transactionData)
}

private suspend fun fetchProduct(
    supabaseClient: SupabaseClient,
    userId: String,
    productId: String
): ProductDTO {
    return supabaseClient.from(TABLE_PRODUCTS)
        .select {
            filter {
                eq(DatabaseColumns.ID, productId)
                eq(DatabaseColumns.USER_ID, userId)
            }
        }
        .decodeSingle<ProductDTO>()
}

private suspend fun updateStock(
    supabaseClient: SupabaseClient,
    userId: String,
    productId: String,
    newStock: Int
): JsonObject {
    return supabaseClient.from(TABLE_PRODUCTS)
        .update(mapOf(DatabaseColumns.CURRENT_STOCK to newStock)) {
            filter {
                eq(DatabaseColumns.ID, productId)
                eq(DatabaseColumns.USER_ID, userId)
            }
            select()
        }
        .decodeSingle<JsonObject>()
}

/**
 * Service for managing product operations.
 */
class ProductService(
    private val supabaseClient: SupabaseClient,
) {
    /**
     * Searches products by name, SKU, or barcode.
     *
     * @param query Search query string
     * @return Result containing list of matching products
     */
    suspend fun search(query: String): Result<List<ProductDTO>> {
        return try {
            val searchPattern = "%$query%"
            val products = supabaseClient.from(TABLE_PRODUCTS)
                .select {
                    filter {
                        eq(DatabaseColumns.IS_ACTIVE, true)
                        or {
                            ilike(DatabaseColumns.NAME, searchPattern)
                            ilike(DatabaseColumns.SKU, searchPattern)
                            ilike(DatabaseColumns.BARCODE, searchPattern)
                        }
                    }
                    order(DatabaseColumns.NAME, Order.ASCENDING)
                    limit(SEARCH_RESULT_LIMIT)
                }
                .decodeList<ProductDTO>()

            Result.Success(products)
        } catch (e: RestException) {
            Result.Error("$ERROR_SEARCH_FAILED: ${e.message}", cause = e)
        } catch (e: SerializationException) {
            Result.Error("$ERROR_SEARCH_FAILED: ${e.message}", cause = e)
        }
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id Product ID
     * @return Result containing the product or error
     */
    suspend fun getById(id: String): Result<ProductDTO> {
        return try {
            val product = supabaseClient.from(TABLE_PRODUCTS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, id)
                    }
                }
                .decodeSingle<ProductDTO>()

            Result.Success(product)
        } catch (e: RestException) {
            Result.Error(ERROR_PRODUCT_NOT_FOUND, cause = e)
        } catch (e: SerializationException) {
            Result.Error(ERROR_PRODUCT_NOT_FOUND, cause = e)
        }
    }

    /**
     * Retrieves all products with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of products
     */
    suspend fun getAll(request: GetAllProductsRequest): Result<List<ProductDTO>> {
        return try {
            val products = supabaseClient.from(TABLE_PRODUCTS)
                .select {
                    filter {
                        request.categoryId?.let { eq(DatabaseColumns.CATEGORY_ID, it) }
                        request.isActive?.let { eq(DatabaseColumns.IS_ACTIVE, it) }
                        if (request.lowStock == true) {
                            lte(DatabaseColumns.CURRENT_STOCK, DatabaseColumns.MIN_STOCK_LEVEL)
                        }
                    }
                    order(DatabaseColumns.NAME, Order.ASCENDING)
                    range(
                        from = ((request.page - 1) * request.pageSize).toLong(),
                        to = (request.page * request.pageSize - 1).toLong()
                    )
                }
                .decodeList<ProductDTO>()

            Result.Success(products)
        } catch (e: RestException) {
            Result.Error("$ERROR_FETCH_FAILED: ${e.message}", cause = e)
        } catch (e: SerializationException) {
            Result.Error("$ERROR_FETCH_FAILED: ${e.message}", cause = e)
        }
    }

    /**
     * Creates a new product.
     *
     * @param userId ID of the user creating the product
     * @param request Product creation parameters
     * @return Result containing created product or error
     */
    suspend fun createProduct(
        userId: String,
        request: CreateProductRequest
    ): Result<JsonObject> {
        return try {
            val data = buildProductCreateData(userId, request)

            val product = supabaseClient.from(TABLE_PRODUCTS)
                .insert(data) {
                    select()
                }
                .decodeSingle<JsonObject>()

            Result.Success(product)
        } catch (e: RestException) {
            Result.Error("$ERROR_CREATE_FAILED: ${e.message}", cause = e)
        } catch (e: SerializationException) {
            Result.Error("$ERROR_CREATE_FAILED: ${e.message}", cause = e)
        }
    }

    /**
     * Updates an existing product.
     *
     * @param userId ID of the user updating the product
     * @param productId ID of the product to update
     * @param request Product update parameters
     * @return Result containing updated product or error
     */
    suspend fun updateProduct(
        userId: String,
        productId: String,
        request: UpdateProductRequest
    ): Result<JsonObject> {
        return try {
            val data = buildProductUpdateData(request)

            val product = supabaseClient.from(TABLE_PRODUCTS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, productId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                    select()
                }
                .decodeSingle<JsonObject>()

            Result.Success(product)
        } catch (e: RestException) {
            Result.Error("$ERROR_UPDATE_FAILED: ${e.message}", cause = e)
        } catch (e: SerializationException) {
            Result.Error("$ERROR_UPDATE_FAILED: ${e.message}", cause = e)
        }
    }

    /**
     * Deletes a product.
     *
     * @param userId ID of the user deleting the product
     * @param productId ID of the product to delete
     * @return Result indicating success or error
     */
    suspend fun deleteProduct(userId: String, productId: String): Result<Unit> {
        return try {
            supabaseClient.from(TABLE_PRODUCTS)
                .delete {
                    filter {
                        eq(DatabaseColumns.ID, productId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }

            Result.Success(Unit)
        } catch (e: RestException) {
            Result.Error("$ERROR_DELETE_FAILED: ${e.message}", cause = e)
        } catch (e: SerializationException) {
            Result.Error("$ERROR_DELETE_FAILED: ${e.message}", cause = e)
        }
    }

    /**
     * Adjusts product stock and creates an inventory transaction record.
     *
     * @param userId ID of the user performing the adjustment
     * @param productId ID of the product to adjust
     * @param request Stock adjustment parameters
     * @return Result containing updated product or error
     */
    suspend fun adjustStock(
        userId: String,
        productId: String,
        request: AdjustStockRequest
    ): Result<JsonObject> {
        return try {
            val currentProduct = fetchProduct(supabaseClient, userId, productId)
            val newStockValue = currentProduct.currentStock + request.quantity
            val updatedProduct = updateStock(supabaseClient, userId, productId, newStockValue)
            val transactionParams = InventoryTransactionParams(userId, productId, request, newStockValue)
            insertInventoryTransaction(supabaseClient, transactionParams)
            Result.Success(updatedProduct)
        } catch (e: RestException) {
            Result.Error("$ERROR_STOCK_ADJUST_FAILED: ${e.message}", cause = e)
        } catch (e: SerializationException) {
            Result.Error("$ERROR_STOCK_ADJUST_FAILED: ${e.message}", cause = e)
        }
    }
}
