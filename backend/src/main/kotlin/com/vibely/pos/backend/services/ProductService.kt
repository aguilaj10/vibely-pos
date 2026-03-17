package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.backend.common.putIfNotNull
import com.vibely.pos.backend.dto.request.AdjustStockRequest
import com.vibely.pos.backend.dto.request.CreateProductRequest
import com.vibely.pos.backend.dto.request.GetAllProductsRequest
import com.vibely.pos.backend.dto.request.UpdateProductRequest
import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
        put(DatabaseColumns.SELLING_PRICE, request.unitPrice)
        put(DatabaseColumns.CURRENT_STOCK, request.currentStock)
        put(DatabaseColumns.IS_ACTIVE, request.isActive)
        request.sku?.let { put(DatabaseColumns.SKU, it) }
        request.barcode?.let { put(DatabaseColumns.BARCODE, it) }
        request.description?.let { put(DatabaseColumns.DESCRIPTION, it) }
        request.categoryId?.let { put(DatabaseColumns.CATEGORY_ID, it) }
        request.supplierId?.let { put(DatabaseColumns.SUPPLIER_ID, it) }
        request.costPrice?.let { put(DatabaseColumns.COST_PRICE, it) }
        request.costCurrencyCode?.let { put(DatabaseColumns.COST_CURRENCY_CODE, it) }
        request.minStockLevel?.let { put(DatabaseColumns.MIN_STOCK_LEVEL, it) }
        request.maxStockLevel?.let { put(DatabaseColumns.MAX_STOCK_LEVEL, it) }
        request.reorderPoint?.let { put(DatabaseColumns.REORDER_POINT, it) }
        request.unitOfMeasure?.let { put(DatabaseColumns.UNIT, it) }
        request.taxRate?.let { put(DatabaseColumns.TAX_RATE, it) }
    }
}

private fun buildProductUpdateData(request: UpdateProductRequest): JsonObject {
    return buildJsonObject {
        putIfNotNull(DatabaseColumns.NAME, request.name)
        putIfNotNull(DatabaseColumns.SKU, request.sku)
        putIfNotNull(DatabaseColumns.BARCODE, request.barcode)
        putIfNotNull(DatabaseColumns.DESCRIPTION, request.description)
        putIfNotNull(DatabaseColumns.CATEGORY_ID, request.categoryId)
        putIfNotNull(DatabaseColumns.SUPPLIER_ID, request.supplierId)
        putIfNotNull(DatabaseColumns.SELLING_PRICE, request.unitPrice)
        putIfNotNull(DatabaseColumns.COST_PRICE, request.costPrice)
        putIfNotNull(DatabaseColumns.COST_CURRENCY_CODE, request.costCurrencyCode)
        putIfNotNull(DatabaseColumns.MIN_STOCK_LEVEL, request.minStockLevel)
        putIfNotNull(DatabaseColumns.MAX_STOCK_LEVEL, request.maxStockLevel)
        putIfNotNull(DatabaseColumns.REORDER_POINT, request.reorderPoint)
        putIfNotNull(DatabaseColumns.UNIT, request.unitOfMeasure)
        putIfNotNull(DatabaseColumns.IS_ACTIVE, request.isActive)
        putIfNotNull(DatabaseColumns.TAX_RATE, request.taxRate)
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

private data class StockUpdateParams(
    val userId: String,
    val productId: String,
    val expectedCurrentStock: Int,
    val newStock: Int
)

private suspend fun insertInventoryTransaction(
    supabaseClient: SupabaseClient,
    params: InventoryTransactionParams
) {
    val transactionData = buildInventoryTransactionData(
        params.userId, params.productId, params.request, params.newStock
    )
    supabaseClient.from(TableNames.INVENTORY_TRANSACTIONS)
        .insert(transactionData)
}

private suspend fun fetchProduct(
    supabaseClient: SupabaseClient,
    userId: String,
    productId: String
): ProductDTO {
    return supabaseClient.from(TableNames.PRODUCTS)
        .select(
            columns = Columns.list(
                DatabaseColumns.ID,
                DatabaseColumns.SKU,
                DatabaseColumns.BARCODE,
                DatabaseColumns.NAME,
                DatabaseColumns.DESCRIPTION,
                DatabaseColumns.CATEGORY_ID,
                DatabaseColumns.COST_PRICE,
                DatabaseColumns.SELLING_PRICE,
                DatabaseColumns.CURRENT_STOCK,
                DatabaseColumns.MIN_STOCK_LEVEL,
                DatabaseColumns.UNIT,
                DatabaseColumns.IMAGE_URL,
                DatabaseColumns.IS_ACTIVE,
                DatabaseColumns.CREATED_AT,
                DatabaseColumns.UPDATED_AT,
                DatabaseColumns.CATEGORIES_NAME,
            )
        ) {
            filter {
                eq(DatabaseColumns.ID, productId)
                eq(DatabaseColumns.USER_ID, userId)
            }
        }
        .decodeSingle<ProductDTO>()
}

private suspend fun updateStockWithOptimisticLock(
    supabaseClient: SupabaseClient,
    params: StockUpdateParams
): JsonObject {
    return supabaseClient.from(TableNames.PRODUCTS)
        .update(mapOf(DatabaseColumns.CURRENT_STOCK to params.newStock)) {
            filter {
                eq(DatabaseColumns.ID, params.productId)
                eq(DatabaseColumns.USER_ID, params.userId)
                eq(DatabaseColumns.CURRENT_STOCK, params.expectedCurrentStock)
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
) : BaseService() {
    /**
     * Searches products by name, SKU, or barcode.
     *
     * @param query Search query string
     * @return Result containing list of matching products
     */
    suspend fun search(query: String): Result<List<ProductDTO>> {
        return executeQuery(ERROR_SEARCH_FAILED) {
            val searchPattern = "%$query%"
            supabaseClient.from(TableNames.PRODUCTS)
                .select(
                    columns = Columns.list(
                        DatabaseColumns.ID,
                        DatabaseColumns.SKU,
                        DatabaseColumns.BARCODE,
                        DatabaseColumns.NAME,
                        DatabaseColumns.DESCRIPTION,
                        DatabaseColumns.CATEGORY_ID,
                        DatabaseColumns.COST_PRICE,
                        DatabaseColumns.SELLING_PRICE,
                        DatabaseColumns.CURRENT_STOCK,
                        DatabaseColumns.MIN_STOCK_LEVEL,
                        DatabaseColumns.UNIT,
                        DatabaseColumns.IMAGE_URL,
                        DatabaseColumns.IS_ACTIVE,
                        DatabaseColumns.CREATED_AT,
                        DatabaseColumns.UPDATED_AT,
                        DatabaseColumns.CATEGORIES_NAME,
                    )
                ) {
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
        }
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id Product ID
     * @return Result containing the product or error
     */
    suspend fun getById(id: String): Result<ProductDTO> {
        return executeQuery(ERROR_PRODUCT_NOT_FOUND) {
            supabaseClient.from(TableNames.PRODUCTS)
                .select(
                    columns = Columns.list(
                        DatabaseColumns.ID,
                        DatabaseColumns.SKU,
                        DatabaseColumns.BARCODE,
                        DatabaseColumns.NAME,
                        DatabaseColumns.DESCRIPTION,
                        DatabaseColumns.CATEGORY_ID,
                        DatabaseColumns.COST_PRICE,
                        DatabaseColumns.SELLING_PRICE,
                        DatabaseColumns.CURRENT_STOCK,
                        DatabaseColumns.MIN_STOCK_LEVEL,
                        DatabaseColumns.UNIT,
                        DatabaseColumns.IMAGE_URL,
                        DatabaseColumns.IS_ACTIVE,
                        DatabaseColumns.CREATED_AT,
                        DatabaseColumns.UPDATED_AT,
                        DatabaseColumns.CATEGORIES_NAME,
                    )
                ) {
                    filter {
                        eq(DatabaseColumns.ID, id)
                    }
                }
                .decodeSingle<ProductDTO>()
        }
    }

    /**
     * Retrieves all products with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of products
     */
    suspend fun getAll(request: GetAllProductsRequest): Result<List<ProductDTO>> {
        val (from, to) = calculatePaginationRange(request.page, request.pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient.from(TableNames.PRODUCTS)
                .select(
                    columns = Columns.list(
                        DatabaseColumns.ID,
                        DatabaseColumns.SKU,
                        DatabaseColumns.BARCODE,
                        DatabaseColumns.NAME,
                        DatabaseColumns.DESCRIPTION,
                        DatabaseColumns.CATEGORY_ID,
                        DatabaseColumns.COST_PRICE,
                        DatabaseColumns.SELLING_PRICE,
                        DatabaseColumns.CURRENT_STOCK,
                        DatabaseColumns.MIN_STOCK_LEVEL,
                        DatabaseColumns.UNIT,
                        DatabaseColumns.IMAGE_URL,
                        DatabaseColumns.IS_ACTIVE,
                        DatabaseColumns.CREATED_AT,
                        DatabaseColumns.UPDATED_AT,
                        DatabaseColumns.CATEGORIES_NAME,
                    )
                ) {
                    filter {
                        request.categoryId?.let { eq(DatabaseColumns.CATEGORY_ID, it) }
                        request.isActive?.let { eq(DatabaseColumns.IS_ACTIVE, it) }
                        if (request.lowStock == true) {
                            lte(DatabaseColumns.CURRENT_STOCK, DatabaseColumns.MIN_STOCK_LEVEL)
                        }
                    }
                    order(DatabaseColumns.NAME, Order.ASCENDING)
                    range(from, to)
                }
                .decodeList<ProductDTO>()
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
        return executeQuery(ERROR_CREATE_FAILED) {
            val data = buildProductCreateData(userId, request)

            supabaseClient.from(TableNames.PRODUCTS)
                .insert(data) {
                    select()
                }
                .decodeSingle<JsonObject>()
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
        return executeQuery(ERROR_UPDATE_FAILED) {
            val data = buildProductUpdateData(request)

            supabaseClient.from(TableNames.PRODUCTS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, productId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                    select()
                }
                .decodeSingle<JsonObject>()
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
        return executeQuery(ERROR_DELETE_FAILED) {
            supabaseClient.from(TableNames.PRODUCTS)
                .delete {
                    filter {
                        eq(DatabaseColumns.ID, productId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }
        }
    }

    /**
     * Adjusts product stock and creates an inventory transaction record.
     *
     * Uses optimistic locking to prevent race conditions by validating the current stock
     * during the update operation. Prevents negative stock values to ensure inventory integrity.
     *
     * @param userId ID of the user performing the adjustment
     * @param productId ID of the product to adjust
     * @param request Stock adjustment parameters
     * @return Result containing updated product or error
     * @throws IllegalArgumentException if the adjustment would result in negative stock
     */
    suspend fun adjustStock(
        userId: String,
        productId: String,
        request: AdjustStockRequest
    ): Result<JsonObject> {
        return executeQuery(ERROR_STOCK_ADJUST_FAILED) {
            val currentProduct = fetchProduct(supabaseClient, userId, productId)
            val newStockValue = currentProduct.currentStock + request.quantity

            require(newStockValue >= 0) {
                "Stock adjustment would result in negative stock. " +
                    "Current: ${currentProduct.currentStock}, Adjustment: ${request.quantity}, " +
                    "Resulting: $newStockValue"
            }

            val updateParams = StockUpdateParams(
                userId = userId,
                productId = productId,
                expectedCurrentStock = currentProduct.currentStock,
                newStock = newStockValue
            )
            val updatedProduct = updateStockWithOptimisticLock(supabaseClient, updateParams)
            val transactionParams = InventoryTransactionParams(userId, productId, request, newStockValue)
            insertInventoryTransaction(supabaseClient, transactionParams)
            updatedProduct
        }
    }
}
