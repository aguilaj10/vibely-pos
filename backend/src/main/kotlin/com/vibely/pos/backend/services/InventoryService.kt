package com.vibely.pos.backend.services

import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject

private const val TABLE_INVENTORY_TRANSACTIONS = "inventory_transactions"
private const val COLUMN_PRODUCT_ID = "product_id"
private const val COLUMN_TRANSACTION_TYPE = "transaction_type"
private const val COLUMN_CREATED_AT = "created_at"
private const val COLUMN_USER_ID = "user_id"
private const val ERROR_FETCH_FAILED = "Failed to fetch inventory transactions"

/**
 * Service for managing inventory transaction operations.
 */
class InventoryService(
    private val supabaseClient: SupabaseClient,
) {
    /**
     * Request parameters for fetching inventory transactions.
     *
     * @property productId Optional product ID filter
     * @property type Optional transaction type filter
     * @property startDate Optional start date filter (ISO-8601)
     * @property endDate Optional end date filter (ISO-8601)
     * @property page Page number (1-indexed)
     * @property pageSize Number of items per page
     */
    data class GetTransactionsRequest(
        val productId: String?,
        val type: String?,
        val startDate: String?,
        val endDate: String?,
        val page: Int,
        val pageSize: Int
    )

    /**
     * Retrieves inventory transactions with optional filtering and pagination.
     *
     * @param userId ID of the user
     * @param request Transaction query parameters
     * @return Result containing list of transactions
     */
    suspend fun getTransactions(
        userId: String,
        request: GetTransactionsRequest
    ): Result<List<JsonObject>> {
        return try {
            val transactions = supabaseClient.from(TABLE_INVENTORY_TRANSACTIONS)
                .select {
                    filter {
                        eq(COLUMN_USER_ID, userId)
                        request.productId?.let { eq(COLUMN_PRODUCT_ID, it) }
                        request.type?.let { eq(COLUMN_TRANSACTION_TYPE, it) }
                        request.startDate?.let { gte(COLUMN_CREATED_AT, it) }
                        request.endDate?.let { lte(COLUMN_CREATED_AT, it) }
                    }
                    order(COLUMN_CREATED_AT, Order.DESCENDING)
                    range(
                        from = ((request.page - 1) * request.pageSize).toLong(),
                        to = (request.page * request.pageSize - 1).toLong()
                    )
                }
                .decodeList<JsonObject>()

            Result.Success(transactions)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_FETCH_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_FETCH_FAILED: ${e.message}", cause = e)
        }
    }
}
