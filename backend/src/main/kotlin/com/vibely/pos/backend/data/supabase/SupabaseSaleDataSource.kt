package com.vibely.pos.backend.data.supabase

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.backend.data.datasource.SaleBackendDataSource
import com.vibely.pos.backend.dto.request.GetAllSalesRequest
import com.vibely.pos.backend.services.BaseService
import com.vibely.pos.backend.services.SaleCreationHelper
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.jsonPrimitive

private const val ERROR_CREATE_SALE = "Failed to create sale"
private const val ERROR_FETCH_SALES = "Failed to fetch sales"
private const val ERROR_SALE_NOT_FOUND = "Sale not found"
private const val ERROR_FETCH_ITEMS = "Failed to fetch sale items"
private const val ERROR_UPDATE_STATUS = "Failed to update sale status"

/**
 * Supabase-backed implementation of [SaleBackendDataSource].
 *
 * Delegates all persistence to Supabase PostgREST using [supabaseClient].
 */
class SupabaseSaleDataSource(
    private val supabaseClient: SupabaseClient,
) : BaseService(), SaleBackendDataSource {

    private val creationHelper = SaleCreationHelper(supabaseClient)

    /**
     * Creates a new sale with items and updates inventory.
     *
     * @param request Sale creation request with items
     * @param cashierId ID of the cashier creating the sale
     * @return Result containing created sale or error
     */
    override suspend fun createSale(request: CreateSaleRequest, cashierId: String): Result<SaleDTO> =
        executeQuery(ERROR_CREATE_SALE) {
            val validationResult = creationHelper.validateAndBuildSaleItems(request)
            check(validationResult is Result.Success) {
                (validationResult as Result.Error).message
            }

            val (validatedItems, subtotal) = validationResult.data
            val saleJson = creationHelper.insertSale(request, cashierId, subtotal)
            val saleId = saleJson[DatabaseColumns.ID]?.jsonPrimitive?.content
                ?: error("Sale ID not returned from insert")
            val invoiceNumber = saleJson["invoice_number"]?.jsonPrimitive?.content
                ?: error("Invoice number not returned from insert")

            creationHelper.insertSaleItems(saleId, validatedItems)
            creationHelper.deductStockAndLogTransactions(validatedItems, saleId, invoiceNumber, cashierId)

            supabaseClient.from(TableNames.SALES)
                .select { filter { eq(DatabaseColumns.ID, saleId) } }
                .decodeSingle<SaleDTO>()
        }

    /**
     * Retrieves all sales with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of sales
     */
    override suspend fun getAll(request: GetAllSalesRequest): Result<List<SaleDTO>> =
        executeQuery(ERROR_FETCH_SALES) {
            supabaseClient.from(TableNames.SALES)
                .select {
                    filter {
                        request.startDate?.let { gte(DatabaseColumns.SALE_DATE, it) }
                        request.endDate?.let { lte(DatabaseColumns.SALE_DATE, it) }
                        request.status?.let { eq(DatabaseColumns.STATUS, it) }
                    }
                    order(DatabaseColumns.SALE_DATE, Order.DESCENDING)
                    range(
                        from = ((request.page - 1) * request.pageSize).toLong(),
                        to = (request.page * request.pageSize - 1).toLong(),
                    )
                }
                .decodeList<SaleDTO>()
        }

    /**
     * Retrieves a sale by its ID.
     *
     * @param id Sale ID
     * @return Result containing the sale or error
     */
    override suspend fun getById(id: String): Result<SaleDTO> =
        executeQuery(ERROR_SALE_NOT_FOUND) {
            supabaseClient.from(TableNames.SALES)
                .select { filter { eq(DatabaseColumns.ID, id) } }
                .decodeSingle<SaleDTO>()
        }

    /**
     * Retrieves all items for a specific sale.
     *
     * @param saleId Sale ID
     * @return Result containing list of sale items
     */
    override suspend fun getItems(saleId: String): Result<List<SaleItemDTO>> =
        executeQuery(ERROR_FETCH_ITEMS) {
            supabaseClient.from(TableNames.SALE_ITEMS)
                .select { filter { eq(DatabaseColumns.SALE_ID, saleId) } }
                .decodeList<SaleItemDTO>()
        }

    /**
     * Updates the status of a sale.
     *
     * @param saleId Sale ID
     * @param status New status value
     * @return Result containing updated sale or error
     */
    override suspend fun updateStatus(saleId: String, status: String): Result<SaleDTO> =
        executeQuery(ERROR_UPDATE_STATUS) {
            supabaseClient.from(TableNames.SALES)
                .update(mapOf(DatabaseColumns.STATUS to status)) {
                    filter { eq(DatabaseColumns.ID, saleId) }
                    select()
                }
                .decodeSingle<SaleDTO>()
        }
}
