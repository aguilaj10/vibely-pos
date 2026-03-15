package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.dto.request.GetAllSalesRequest
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

private const val TABLE_SALES = "sales"
private const val TABLE_SALE_ITEMS = "sale_items"
private const val ERROR_CREATE_SALE = "Failed to create sale"
private const val ERROR_FETCH_SALES = "Failed to fetch sales"
private const val ERROR_SALE_NOT_FOUND = "Sale not found"
private const val ERROR_FETCH_ITEMS = "Failed to fetch sale items"
private const val ERROR_UPDATE_STATUS = "Failed to update sale status"

/**
 * Service for managing sale operations.
 */
class SaleService(
    private val supabaseClient: SupabaseClient,
) {
    private val creationHelper = SaleCreationHelper(supabaseClient)
    
    /**
     * Creates a new sale with items and updates inventory.
     *
     * @param request Sale creation request with items
     * @param cashierId ID of the cashier creating the sale
     * @return Result containing created sale or error
     */
    suspend fun createSale(request: CreateSaleRequest, cashierId: String): Result<SaleDTO> {
        return try {
            val saleItemsData = creationHelper.validateAndBuildSaleItems(request)
            val subtotal = saleItemsData.second
            
            val sale = creationHelper.insertSale(request, cashierId, subtotal)
            creationHelper.insertSaleItems(saleItemsData.first, sale.id)
            creationHelper.deductStockAndLogTransactions(request, sale, cashierId)
            
            Result.Success(sale)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_CREATE_SALE: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_CREATE_SALE: ${e.message}", cause = e)
        }
    }

    /**
     * Retrieves all sales with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of sales
     */
    suspend fun getAll(request: GetAllSalesRequest): Result<List<SaleDTO>> {
        return try {
            val sales = supabaseClient.from(TABLE_SALES)
                .select {
                    filter {
                        request.startDate?.let { gte(DatabaseColumns.SALE_DATE, it) }
                        request.endDate?.let { lte(DatabaseColumns.SALE_DATE, it) }
                        request.status?.let { eq(DatabaseColumns.STATUS, it) }
                    }
                    order(DatabaseColumns.SALE_DATE, Order.DESCENDING)
                    range(
                        from = ((request.page - 1) * request.pageSize).toLong(),
                        to = (request.page * request.pageSize - 1).toLong()
                    )
                }
                .decodeList<SaleDTO>()

            Result.Success(sales)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_FETCH_SALES: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_FETCH_SALES: ${e.message}", cause = e)
        }
    }

    /**
     * Retrieves a sale by its ID.
     *
     * @param id Sale ID
     * @return Result containing the sale or error
     */
    suspend fun getById(id: String): Result<SaleDTO> {
        return try {
            val sale = supabaseClient.from(TABLE_SALES)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, id)
                    }
                }
                .decodeSingle<SaleDTO>()

            Result.Success(sale)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error(ERROR_SALE_NOT_FOUND, cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error(ERROR_SALE_NOT_FOUND, cause = e)
        }
    }

    /**
     * Retrieves all items for a specific sale.
     *
     * @param saleId Sale ID
     * @return Result containing list of sale items
     */
    suspend fun getItems(saleId: String): Result<List<SaleItemDTO>> {
        return try {
            val items = supabaseClient.from(TABLE_SALE_ITEMS)
                .select {
                    filter {
                        eq(DatabaseColumns.SALE_ID, saleId)
                    }
                }
                .decodeList<SaleItemDTO>()

            Result.Success(items)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_FETCH_ITEMS: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_FETCH_ITEMS: ${e.message}", cause = e)
        }
    }

    /**
     * Updates the status of a sale.
     *
     * @param saleId Sale ID
     * @param status New status value
     * @return Result containing updated sale or error
     */
    suspend fun updateStatus(saleId: String, status: String): Result<SaleDTO> {
        return try {
            val sale = supabaseClient.from(TABLE_SALES)
                .update(mapOf(DatabaseColumns.STATUS to status)) {
                    filter {
                        eq(DatabaseColumns.ID, saleId)
                    }
                    select()
                }
                .decodeSingle<SaleDTO>()

            Result.Success(sale)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_UPDATE_STATUS: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_UPDATE_STATUS: ${e.message}", cause = e)
        }
    }
}
