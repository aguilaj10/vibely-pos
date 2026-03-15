package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.dto.request.GetTransactionsRequest
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject

private const val TABLE_INVENTORY_TRANSACTIONS = "inventory_transactions"
private const val ERROR_FETCH_FAILED = "Failed to fetch inventory transactions"

/**
 * Service for managing inventory transaction operations.
 */
class InventoryService(
    private val supabaseClient: SupabaseClient,
) : BaseService() {
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
        val (from, to) = calculatePaginationRange(request.page, request.pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient.from(TABLE_INVENTORY_TRANSACTIONS)
                .select {
                    filter {
                        eq(DatabaseColumns.USER_ID, userId)
                        request.productId?.let { eq(DatabaseColumns.PRODUCT_ID, it) }
                        request.type?.let { eq(DatabaseColumns.TRANSACTION_TYPE, it) }
                        request.startDate?.let { gte(DatabaseColumns.CREATED_AT, it) }
                        request.endDate?.let { lte(DatabaseColumns.CREATED_AT, it) }
                    }
                    order(DatabaseColumns.CREATED_AT, Order.DESCENDING)
                    range(from, to)
                }
                .decodeList<JsonObject>()
        }
    }
}
