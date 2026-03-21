@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "LongParameterList")

package com.vibely.pos.backend.services

import com.vibely.pos.backend.data.datasource.CustomerBackendDataSource
import com.vibely.pos.backend.dto.request.AddLoyaltyPointsRequest
import com.vibely.pos.backend.dto.request.CreateCustomerRequest
import com.vibely.pos.backend.dto.request.UpdateCustomerRequest
import com.vibely.pos.shared.data.customer.dto.CustomerDTO
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.JsonObject

/**
 * Service for managing customer operations.
 *
 * Delegates all persistence to [CustomerBackendDataSource], which is either Supabase or
 * Room/SQLite depending on the active [com.vibely.pos.backend.data.DatabaseStrategy].
 */
class CustomerService(
    private val dataSource: CustomerBackendDataSource,
) : BaseService() {
    suspend fun getAllCustomers(
        userId: String,
        isActive: Boolean?,
        searchQuery: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<CustomerDTO>> = dataSource.getAllCustomers(userId, isActive, searchQuery, page, pageSize)

    suspend fun getCustomerById(userId: String, customerId: String): Result<CustomerDTO> =
        dataSource.getCustomerById(userId, customerId)

    suspend fun createCustomer(userId: String, request: CreateCustomerRequest): Result<CustomerDTO> =
        dataSource.createCustomer(userId, request)

    suspend fun updateCustomer(
        userId: String,
        customerId: String,
        request: UpdateCustomerRequest,
    ): Result<CustomerDTO> = dataSource.updateCustomer(userId, customerId, request)

    suspend fun deleteCustomer(userId: String, customerId: String): Result<Unit> =
        dataSource.deleteCustomer(userId, customerId)

    suspend fun addLoyaltyPoints(
        userId: String,
        customerId: String,
        request: AddLoyaltyPointsRequest,
    ): Result<CustomerDTO> = dataSource.addLoyaltyPoints(userId, customerId, request)

    /**
     * Retrieves purchase history for a customer.
     *
     * @param userId The ID of the user making the request (for authorization)
     * @param customerId The ID of the customer whose history to retrieve
     * @param page The page number for pagination
     * @param pageSize The number of items per page
     * @return Result containing list of sales records or error
     */
    suspend fun getPurchaseHistory(
        userId: String,
        customerId: String,
        page: Int,
        pageSize: Int,
    ): Result<List<JsonObject>> = dataSource.getPurchaseHistory(userId, customerId, page, pageSize)
}
