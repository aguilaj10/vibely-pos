package com.vibely.pos.backend.services

import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

private const val TABLE_PRODUCTS = "products"
private const val COLUMN_NAME = "name"
private const val COLUMN_SKU = "sku"
private const val COLUMN_BARCODE = "barcode"
private const val COLUMN_IS_ACTIVE = "is_active"
private const val COLUMN_CATEGORY_ID = "category_id"
private const val COLUMN_CURRENT_STOCK = "current_stock"
private const val COLUMN_MIN_STOCK_LEVEL = "min_stock_level"
private const val COLUMN_ID = "id"
private const val SEARCH_RESULT_LIMIT = 50L
private const val ERROR_SEARCH_FAILED = "Failed to search products"
private const val ERROR_PRODUCT_NOT_FOUND = "Product not found"
private const val ERROR_FETCH_FAILED = "Failed to fetch products"

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
}
