package com.vibely.pos.backend.data.datasource

import com.vibely.pos.backend.dto.request.GetAllSalesRequest
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import com.vibely.pos.shared.domain.result.Result

/**
 * Data source abstraction for sale persistence operations.
 *
 * Implementations provide either Supabase (remote) or Room/SQLite (local) backends.
 */
interface SaleBackendDataSource {
    /**
     * Creates a new sale with items and updates inventory.
     *
     * @param request Sale creation request with items
     * @param cashierId ID of the cashier creating the sale
     * @return Result containing created sale or error
     */
    suspend fun createSale(request: CreateSaleRequest, cashierId: String): Result<SaleDTO>

    /**
     * Retrieves all sales with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of sales
     */
    suspend fun getAll(request: GetAllSalesRequest): Result<List<SaleDTO>>

    /**
     * Retrieves a sale by its ID.
     *
     * @param id Sale ID
     * @return Result containing the sale or error
     */
    suspend fun getById(id: String): Result<SaleDTO>

    /**
     * Retrieves all items for a specific sale.
     *
     * @param saleId Sale ID
     * @return Result containing list of sale items
     */
    suspend fun getItems(saleId: String): Result<List<SaleItemDTO>>

    /**
     * Updates the status of a sale.
     *
     * @param saleId Sale ID
     * @param status New status value
     * @return Result containing updated sale or error
     */
    suspend fun updateStatus(saleId: String, status: String): Result<SaleDTO>
}
