package com.vibely.pos.backend.data.room.datasource

import com.vibely.pos.backend.data.datasource.SaleBackendDataSource
import com.vibely.pos.backend.data.room.dao.ProductDao
import com.vibely.pos.backend.data.room.dao.SaleDao
import com.vibely.pos.backend.data.room.mapper.toDto
import com.vibely.pos.backend.data.room.mapper.toEntity
import com.vibely.pos.backend.dto.request.GetAllSalesRequest
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import com.vibely.pos.shared.domain.result.Result

private const val ERROR_NOT_FOUND = "Sale not found"
private const val ERROR_PRODUCT_NOT_FOUND = "Product not found"
private const val ERROR_INSUFFICIENT_STOCK = "Insufficient stock for"

/**
 * Room/SQLite-backed implementation of [SaleBackendDataSource].
 *
 * Stock deduction is applied directly to the products table. No inventory transaction
 * log is written in local mode.
 */
class RoomSaleDataSource(
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
) : SaleBackendDataSource {
    /**
     * Creates a new sale with items and deducts inventory in the local database.
     *
     * @param request Sale creation request with items
     * @param cashierId ID of the cashier creating the sale
     * @return Result containing created sale or error
     */
    override suspend fun createSale(request: CreateSaleRequest, cashierId: String): Result<SaleDTO> =
        runCatchingSuspend {
            validateStock(request)
            val nextCount = saleDao.count()
            val saleEntity = request.toEntity(cashierId, nextCount)
            saleDao.insertSale(saleEntity)
            insertSaleItems(request, saleEntity.id)
            deductStock(request)
            saleEntity.toDto()
        }

    /**
     * Retrieves all sales with optional filtering and pagination.
     *
     * @param request Request parameters for filtering and pagination
     * @return Result containing list of sales
     */
    override suspend fun getAll(request: GetAllSalesRequest): Result<List<SaleDTO>> =
        runCatchingSuspend {
            val offset = (request.page - 1) * request.pageSize
            saleDao.getAll(
                startDate = request.startDate,
                endDate = request.endDate,
                status = request.status,
                limit = request.pageSize,
                offset = offset,
            ).map { it.toDto() }
        }

    /**
     * Retrieves a sale by its ID.
     *
     * @param id Sale ID
     * @return Result containing the sale or error
     */
    override suspend fun getById(id: String): Result<SaleDTO> =
        runCatchingSuspend {
            saleDao.getById(id)?.toDto() ?: error(ERROR_NOT_FOUND)
        }

    /**
     * Retrieves all items for a specific sale.
     *
     * @param saleId Sale ID
     * @return Result containing list of sale items
     */
    override suspend fun getItems(saleId: String): Result<List<SaleItemDTO>> =
        runCatchingSuspend {
            saleDao.getItemsBySaleId(saleId).map { it.toDto() }
        }

    /**
     * Updates the status of a sale.
     *
     * @param saleId Sale ID
     * @param status New status value
     * @return Result containing updated sale or error
     */
    override suspend fun updateStatus(saleId: String, status: String): Result<SaleDTO> =
        runCatchingSuspend {
            val existing = saleDao.getById(saleId) ?: error(ERROR_NOT_FOUND)
            val updated = existing.copy(status = status)
            saleDao.updateSale(updated)
            updated.toDto()
        }

    private suspend fun validateStock(request: CreateSaleRequest) {
        for (item in request.items) {
            val product = productDao.getById(item.productId) ?: error(ERROR_PRODUCT_NOT_FOUND)
            check(product.currentStock >= item.quantity) {
                "$ERROR_INSUFFICIENT_STOCK ${product.name}"
            }
        }
    }

    private suspend fun insertSaleItems(request: CreateSaleRequest, saleId: String) {
        for (item in request.items) {
            val product = productDao.getById(item.productId) ?: error(ERROR_PRODUCT_NOT_FOUND)
            saleDao.insertSaleItem(item.toEntity(saleId, product.name))
        }
    }

    private suspend fun deductStock(request: CreateSaleRequest) {
        for (item in request.items) {
            val product = productDao.getById(item.productId) ?: error(ERROR_PRODUCT_NOT_FOUND)
            productDao.update(product.copy(currentStock = product.currentStock - item.quantity))
        }
    }
}
