package com.vibely.pos.backend.data.datasource

import com.vibely.pos.backend.dto.request.AddLoyaltyPointsRequest
import com.vibely.pos.backend.dto.request.CreateCustomerRequest
import com.vibely.pos.backend.dto.request.UpdateCustomerRequest
import com.vibely.pos.shared.data.customer.dto.CustomerDTO
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.JsonObject

/**
 * Data source abstraction for customer persistence operations.
 *
 * Implementations provide either Supabase (remote) or Room/SQLite (local) backends.
 */
@Suppress("UndocumentedPublicFunction", "LongParameterList")
interface CustomerBackendDataSource {
    /**
     * Retrieves all customers with optional filtering and pagination.
     *
     * @param userId ID of the user (for data scoping)
     * @param isActive Optional active status filter
     * @param searchQuery Optional search query for name, email, phone, or code
     * @param page Page number (1-indexed)
     * @param pageSize Number of items per page
     * @return Result containing list of customers
     */
    suspend fun getAllCustomers(
        userId: String,
        isActive: Boolean?,
        searchQuery: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<CustomerDTO>>

    /**
     * Retrieves a customer by their ID.
     *
     * @param userId ID of the user (for data scoping)
     * @param customerId Customer ID
     * @return Result containing the customer or error
     */
    suspend fun getCustomerById(userId: String, customerId: String): Result<CustomerDTO>

    /**
     * Creates a new customer.
     *
     * @param userId ID of the user creating the customer
     * @param request Customer creation parameters
     * @return Result containing created customer or error
     */
    suspend fun createCustomer(userId: String, request: CreateCustomerRequest): Result<CustomerDTO>

    /**
     * Updates an existing customer.
     *
     * @param userId ID of the user updating the customer
     * @param customerId ID of the customer to update
     * @param request Customer update parameters
     * @return Result containing updated customer or error
     */
    suspend fun updateCustomer(
        userId: String,
        customerId: String,
        request: UpdateCustomerRequest,
    ): Result<CustomerDTO>

    /**
     * Deletes a customer.
     *
     * @param userId ID of the user deleting the customer
     * @param customerId ID of the customer to delete
     * @return Result indicating success or error
     */
    suspend fun deleteCustomer(userId: String, customerId: String): Result<Unit>

    /**
     * Adds loyalty points to a customer and recalculates their tier.
     *
     * @param userId ID of the user performing the update
     * @param customerId ID of the customer
     * @param request Loyalty points request
     * @return Result containing updated customer or error
     */
    suspend fun addLoyaltyPoints(
        userId: String,
        customerId: String,
        request: AddLoyaltyPointsRequest,
    ): Result<CustomerDTO>

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
    ): Result<List<JsonObject>>
}
