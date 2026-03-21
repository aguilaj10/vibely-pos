@file:Suppress("UndocumentedPublicFunction", "LongParameterList")

package com.vibely.pos.backend.data.room.datasource

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.data.datasource.CustomerBackendDataSource
import com.vibely.pos.backend.data.room.dao.CustomerDao
import com.vibely.pos.backend.data.room.mapper.toDto
import com.vibely.pos.backend.data.room.mapper.toEntity
import com.vibely.pos.backend.dto.request.AddLoyaltyPointsRequest
import com.vibely.pos.backend.dto.request.CreateCustomerRequest
import com.vibely.pos.backend.dto.request.UpdateCustomerRequest
import com.vibely.pos.shared.data.customer.dto.CustomerDTO
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock

private const val ERROR_NOT_FOUND = "Customer not found"

/**
 * Room/SQLite-backed implementation of [CustomerBackendDataSource].
 *
 * User-scoped filtering is not enforced in local mode — all customers are accessible
 * to any operator connected to the local network backend.
 */
class RoomCustomerDataSource(
    private val customerDao: CustomerDao,
) : CustomerBackendDataSource {
    override suspend fun getAllCustomers(
        userId: String,
        isActive: Boolean?,
        searchQuery: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<CustomerDTO>> =
        runCatchingSuspend {
            val offset = (page - 1) * pageSize
            val searchPattern = searchQuery?.let { "%$it%" }
            customerDao.getAll(
                isActive = isActive,
                searchPattern = searchPattern,
                limit = pageSize,
                offset = offset,
            ).map { it.toDto() }
        }

    override suspend fun getCustomerById(userId: String, customerId: String): Result<CustomerDTO> =
        runCatchingSuspend {
            customerDao.getById(customerId)?.toDto() ?: error(ERROR_NOT_FOUND)
        }

    override suspend fun createCustomer(
        userId: String,
        request: CreateCustomerRequest,
    ): Result<CustomerDTO> =
        runCatchingSuspend {
            val entity = request.toEntity()
            customerDao.insert(entity)
            entity.toDto()
        }

    override suspend fun updateCustomer(
        userId: String,
        customerId: String,
        request: UpdateCustomerRequest,
    ): Result<CustomerDTO> =
        runCatchingSuspend {
            val existing = customerDao.getById(customerId) ?: error(ERROR_NOT_FOUND)
            val updated = existing.copy(
                code = request.code ?: existing.code,
                fullName = request.fullName ?: existing.fullName,
                email = request.email ?: existing.email,
                phone = request.phone ?: existing.phone,
                loyaltyPoints = request.loyaltyPoints ?: existing.loyaltyPoints,
                loyaltyTier = request.loyaltyTier ?: existing.loyaltyTier,
                totalPurchases = request.totalPurchases ?: existing.totalPurchases,
                isActive = request.isActive ?: existing.isActive,
                updatedAt = Clock.System.now().toString(),
            )
            customerDao.update(updated)
            updated.toDto()
        }

    override suspend fun deleteCustomer(userId: String, customerId: String): Result<Unit> =
        runCatchingSuspend { customerDao.delete(customerId) }

    override suspend fun addLoyaltyPoints(
        userId: String,
        customerId: String,
        request: AddLoyaltyPointsRequest,
    ): Result<CustomerDTO> =
        runCatchingSuspend {
            val existing = customerDao.getById(customerId) ?: error(ERROR_NOT_FOUND)
            val newPoints = existing.loyaltyPoints + request.points
            val newTier = calculateLoyaltyTier(newPoints)
            val updated = existing.copy(
                loyaltyPoints = newPoints,
                loyaltyTier = newTier,
                updatedAt = Clock.System.now().toString(),
            )
            customerDao.update(updated)
            updated.toDto()
        }

    /**
     * Retrieves purchase history for a customer from the local sales table.
     *
     * Returns an empty list in local mode as sales history querying across tables
     * is delegated to [RoomSaleDataSource].
     *
     * @param userId The ID of the user making the request
     * @param customerId The ID of the customer whose history to retrieve
     * @param page The page number for pagination
     * @param pageSize The number of items per page
     * @return Result containing an empty list (history available via SaleBackendDataSource)
     */
    override suspend fun getPurchaseHistory(
        userId: String,
        customerId: String,
        page: Int,
        pageSize: Int,
    ): Result<List<JsonObject>> = Result.Success(emptyList())

    private fun calculateLoyaltyTier(points: Int): String =
        when {
            points >= DatabaseColumns.PLATINUM_THRESHOLD -> "Platinum"
            points >= DatabaseColumns.GOLD_THRESHOLD -> "Gold"
            points >= DatabaseColumns.SILVER_THRESHOLD -> "Silver"
            else -> "Bronze"
        }
}
