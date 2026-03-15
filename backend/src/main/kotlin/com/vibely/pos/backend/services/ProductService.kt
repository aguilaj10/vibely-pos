package com.vibely.pos.backend.services

import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val TABLE_PRODUCTS = "products"
private const val TABLE_INVENTORY_TRANSACTIONS = "inventory_transactions"
private const val COLUMN_NAME = "name"
private const val COLUMN_SKU = "sku"
private const val COLUMN_BARCODE = "barcode"
private const val COLUMN_IS_ACTIVE = "is_active"
private const val COLUMN_CATEGORY_ID = "category_id"
private const val COLUMN_SUPPLIER_ID = "supplier_id"
private const val COLUMN_CURRENT_STOCK = "current_stock"
private const val COLUMN_MIN_STOCK_LEVEL = "min_stock_level"
private const val COLUMN_MAX_STOCK_LEVEL = "max_stock_level"
private const val COLUMN_REORDER_POINT = "reorder_point"
private const val COLUMN_UNIT_OF_MEASURE = "unit_of_measure"
private const val COLUMN_UNIT_PRICE = "unit_price"
private const val COLUMN_COST_PRICE = "cost_price"
private const val COLUMN_DESCRIPTION = "description"
private const val COLUMN_TAX_RATE = "tax_rate"
private const val COLUMN_USER_ID = "user_id"
private const val COLUMN_ID = "id"
private const val COLUMN_QUANTITY = "quantity"
private const val COLUMN_TRANSACTION_TYPE = "transaction_type"
private const val COLUMN_REFERENCE_TYPE = "reference_type"
private const val COLUMN_REFERENCE_ID = "reference_id"
private const val COLUMN_NOTES = "notes"
private const val SEARCH_RESULT_LIMIT = 50L
private const val ERROR_SEARCH_FAILED = "Failed to search products"
private const val ERROR_PRODUCT_NOT_FOUND = "Product not found"
private const val ERROR_FETCH_FAILED = "Failed to fetch products"
private const val ERROR_CREATE_FAILED = "Failed to create product"
private const val ERROR_UPDATE_FAILED = "Failed to update product"
private const val ERROR_DELETE_FAILED = "Failed to delete product"
private const val ERROR_STOCK_ADJUST_FAILED = "Failed to adjust stock"

private fun buildProductCreateData(userId: String, request: ProductService.CreateProductRequest): JsonObject {
    return buildJsonObject {
        put(COLUMN_USER_ID, userId)
        put(COLUMN_NAME, request.name)
        put(COLUMN_UNIT_PRICE, request.unitPrice)
        put(COLUMN_CURRENT_STOCK, request.currentStock)
        put(COLUMN_IS_ACTIVE, request.isActive)
        request.sku?.let { put(COLUMN_SKU, it) }
        request.barcode?.let { put(COLUMN_BARCODE, it) }
        request.description?.let { put(COLUMN_DESCRIPTION, it) }
        request.categoryId?.let { put(COLUMN_CATEGORY_ID, it) }
        request.supplierId?.let { put(COLUMN_SUPPLIER_ID, it) }
        request.costPrice?.let { put(COLUMN_COST_PRICE, it) }
        request.minStockLevel?.let { put(COLUMN_MIN_STOCK_LEVEL, it) }
        request.maxStockLevel?.let { put(COLUMN_MAX_STOCK_LEVEL, it) }
        request.reorderPoint?.let { put(COLUMN_REORDER_POINT, it) }
        request.unitOfMeasure?.let { put(COLUMN_UNIT_OF_MEASURE, it) }
        request.taxRate?.let { put(COLUMN_TAX_RATE, it) }
    }
}

private fun buildProductUpdateData(request: ProductService.UpdateProductRequest): JsonObject {
    return buildJsonObject {
        put(COLUMN_NAME, request.name).takeIf { it != null }
        put(COLUMN_SKU, request.sku).takeIf { it != null }
        put(COLUMN_BARCODE, request.barcode).takeIf { it != null }
        put(COLUMN_DESCRIPTION, request.description).takeIf { it != null }
        put(COLUMN_CATEGORY_ID, request.categoryId).takeIf { it != null }
        put(COLUMN_SUPPLIER_ID, request.supplierId).takeIf { it != null }
        put(COLUMN_UNIT_PRICE, request.unitPrice).takeIf { it != null }
        put(COLUMN_COST_PRICE, request.costPrice).takeIf { it != null }
        put(COLUMN_MIN_STOCK_LEVEL, request.minStockLevel).takeIf { it != null }
        put(COLUMN_MAX_STOCK_LEVEL, request.maxStockLevel).takeIf { it != null }
        put(COLUMN_REORDER_POINT, request.reorderPoint).takeIf { it != null }
        put(COLUMN_UNIT_OF_MEASURE, request.unitOfMeasure).takeIf { it != null }
        put(COLUMN_IS_ACTIVE, request.isActive).takeIf { it != null }
        put(COLUMN_TAX_RATE, request.taxRate).takeIf { it != null }
    }
}

private fun buildInventoryTransactionData(
    userId: String,
    productId: String,
    request: ProductService.AdjustStockRequest,
    newStock: Int
): JsonObject {
    return buildJsonObject {
        put(COLUMN_USER_ID, userId)
        put("product_id", productId)
        put(COLUMN_QUANTITY, request.quantity)
        put(COLUMN_TRANSACTION_TYPE, request.transactionType)
        put("quantity_after", newStock)
        request.referenceType?.let { put(COLUMN_REFERENCE_TYPE, it) }
        request.referenceId?.let { put(COLUMN_REFERENCE_ID, it) }
        request.notes?.let { put(COLUMN_NOTES, it) }
    }
}

private data class InventoryTransactionParams(
    val userId: String,
    val productId: String,
    val request: ProductService.AdjustStockRequest,
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
                eq(COLUMN_ID, productId)
                eq(COLUMN_USER_ID, userId)
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
        .update(mapOf(COLUMN_CURRENT_STOCK to newStock)) {
            filter {
                eq(COLUMN_ID, productId)
                eq(COLUMN_USER_ID, userId)
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
     * Request parameters for fetching products with filtering and pagination.
     *
     * @property categoryId Optional category filter
     * @property isActive Optional active status filter
     * @property lowStockOnly Filter for low stock products
     * @property page Page number (1-indexed)
     * @property pageSize Number of items per page
     */
    data class GetAllRequest(
        val categoryId: String?,
        val isActive: Boolean?,
        val lowStockOnly: Boolean,
        val page: Int,
        val pageSize: Int
    )

    /**
     * Request parameters for creating a product.
     *
     * @property name Product name
     * @property sku Optional SKU
     * @property barcode Optional barcode
     * @property description Optional description
     * @property categoryId Optional category ID
     * @property supplierId Optional supplier ID
     * @property unitPrice Unit price
     * @property costPrice Optional cost price
     * @property currentStock Initial stock quantity
     * @property minStockLevel Optional minimum stock level
     * @property maxStockLevel Optional maximum stock level
     * @property reorderPoint Optional reorder point
     * @property unitOfMeasure Optional unit of measure
     * @property isActive Whether product is active
     * @property taxRate Optional tax rate
     */
    data class CreateProductRequest(
        val name: String,
        val sku: String?,
        val barcode: String?,
        val description: String?,
        val categoryId: String?,
        val supplierId: String?,
        val unitPrice: Double,
        val costPrice: Double?,
        val currentStock: Int,
        val minStockLevel: Int?,
        val maxStockLevel: Int?,
        val reorderPoint: Int?,
        val unitOfMeasure: String?,
        val isActive: Boolean,
        val taxRate: Double?
    )

    /**
     * Request parameters for updating a product.
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
    data class UpdateProductRequest(
        val name: String?,
        val sku: String?,
        val barcode: String?,
        val description: String?,
        val categoryId: String?,
        val supplierId: String?,
        val unitPrice: Double?,
        val costPrice: Double?,
        val minStockLevel: Int?,
        val maxStockLevel: Int?,
        val reorderPoint: Int?,
        val unitOfMeasure: String?,
        val isActive: Boolean?,
        val taxRate: Double?
    )

    /**
     * Request parameters for adjusting stock.
     *
     * @property quantity Quantity to add (positive) or remove (negative)
     * @property transactionType Type of transaction
     * @property referenceType Optional reference type
     * @property referenceId Optional reference ID
     * @property notes Optional notes
     */
    data class AdjustStockRequest(
        val quantity: Int,
        val transactionType: String,
        val referenceType: String?,
        val referenceId: String?,
        val notes: String?
    )

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
                        eq(COLUMN_IS_ACTIVE, true)
                        or {
                            ilike(COLUMN_NAME, searchPattern)
                            ilike(COLUMN_SKU, searchPattern)
                            ilike(COLUMN_BARCODE, searchPattern)
                        }
                    }
                    order(COLUMN_NAME, Order.ASCENDING)
                    limit(SEARCH_RESULT_LIMIT)
                }
                .decodeList<ProductDTO>()

            Result.Success(products)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_SEARCH_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
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
                        eq(COLUMN_ID, id)
                    }
                }
                .decodeSingle<ProductDTO>()

            Result.Success(product)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error(ERROR_PRODUCT_NOT_FOUND, cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error(ERROR_PRODUCT_NOT_FOUND, cause = e)
        }
    }

    /**
     * Retrieves all products with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of products
     */
    suspend fun getAll(request: GetAllRequest): Result<List<ProductDTO>> {
        return try {
            val products = supabaseClient.from(TABLE_PRODUCTS)
                .select {
                    filter {
                        request.categoryId?.let { eq(COLUMN_CATEGORY_ID, it) }
                        request.isActive?.let { eq(COLUMN_IS_ACTIVE, it) }
                        if (request.lowStockOnly) {
                            lte(COLUMN_CURRENT_STOCK, COLUMN_MIN_STOCK_LEVEL)
                        }
                    }
                    order(COLUMN_NAME, Order.ASCENDING)
                    range(
                        from = ((request.page - 1) * request.pageSize).toLong(),
                        to = (request.page * request.pageSize - 1).toLong()
                    )
                }
                .decodeList<ProductDTO>()

            Result.Success(products)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_FETCH_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
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
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_CREATE_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
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
                        eq(COLUMN_ID, productId)
                        eq(COLUMN_USER_ID, userId)
                    }
                    select()
                }
                .decodeSingle<JsonObject>()

            Result.Success(product)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_UPDATE_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
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
                        eq(COLUMN_ID, productId)
                        eq(COLUMN_USER_ID, userId)
                    }
                }

            Result.Success(Unit)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_DELETE_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
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
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_STOCK_ADJUST_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_STOCK_ADJUST_FAILED: ${e.message}", cause = e)
        }
    }
}
