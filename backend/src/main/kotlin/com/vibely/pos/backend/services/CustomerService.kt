@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "LongParameterList")

package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.backend.dto.request.AddLoyaltyPointsRequest
import com.vibely.pos.backend.dto.request.CreateCustomerRequest
import com.vibely.pos.backend.dto.request.UpdateCustomerRequest
import com.vibely.pos.shared.data.customer.dto.CustomerDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ERROR_FETCH_FAILED = "Failed to fetch customers"
private const val ERROR_CUSTOMER_NOT_FOUND = "Customer not found"
private const val ERROR_CREATE_FAILED = "Failed to create customer"
private const val ERROR_UPDATE_FAILED = "Failed to update customer"
private const val ERROR_DELETE_FAILED = "Failed to delete customer"
private const val ERROR_LOYALTY_FAILED = "Failed to add loyalty points"
private const val ERROR_PURCHASE_HISTORY_FAILED = "Failed to fetch purchase history"

class CustomerService(
    private val supabaseClient: SupabaseClient,
) : BaseService() {
    suspend fun getAllCustomers(
        userId: String,
        isActive: Boolean?,
        searchQuery: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<CustomerDTO>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient
                .from(TableNames.CUSTOMERS)
                .select {
                    filter {
                        eq(DatabaseColumns.USER_ID, userId)
                        isActive?.let { eq(DatabaseColumns.IS_ACTIVE, it) }
                        searchQuery?.let { query ->
                            val searchPattern = "%$query%"
                            or {
                                ilike(DatabaseColumns.FULL_NAME, searchPattern)
                                ilike(DatabaseColumns.EMAIL, searchPattern)
                                ilike(DatabaseColumns.PHONE, searchPattern)
                                ilike(DatabaseColumns.CUSTOMER_CODE, searchPattern)
                            }
                        }
                    }
                    order(DatabaseColumns.FULL_NAME, Order.ASCENDING)
                    range(from, to)
                }.decodeList<CustomerDTO>()
        }
    }

    suspend fun getCustomerById(
        userId: String,
        customerId: String,
    ): Result<CustomerDTO> =
        executeQuery(ERROR_CUSTOMER_NOT_FOUND) {
            supabaseClient
                .from(TableNames.CUSTOMERS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }.decodeSingle<CustomerDTO>()
        }

    suspend fun createCustomer(
        userId: String,
        request: CreateCustomerRequest,
    ): Result<CustomerDTO> =
        executeQuery(ERROR_CREATE_FAILED) {
            val data =
                buildJsonObject {
                    put(DatabaseColumns.USER_ID, userId)
                    put(DatabaseColumns.CUSTOMER_CODE, request.code)
                    put(DatabaseColumns.FULL_NAME, request.fullName)
                    request.email?.let { put(DatabaseColumns.EMAIL, it) }
                    request.phone?.let { put(DatabaseColumns.PHONE, it) }
                    put(DatabaseColumns.LOYALTY_POINTS, request.loyaltyPoints)
                    request.loyaltyTier?.let { put(DatabaseColumns.LOYALTY_TIER, it) }
                    put(DatabaseColumns.TOTAL_PURCHASES, request.totalPurchases)
                    put(DatabaseColumns.IS_ACTIVE, request.isActive)
                }

            supabaseClient
                .from(TableNames.CUSTOMERS)
                .insert(data) {
                    select()
                }.decodeSingle<CustomerDTO>()
        }

    suspend fun updateCustomer(
        userId: String,
        customerId: String,
        request: UpdateCustomerRequest,
    ): Result<CustomerDTO> =
        executeQuery(ERROR_UPDATE_FAILED) {
            val data =
                buildJsonObject {
                    request.code?.let { put(DatabaseColumns.CUSTOMER_CODE, it) }
                    request.fullName?.let { put(DatabaseColumns.FULL_NAME, it) }
                    request.email?.let { put(DatabaseColumns.EMAIL, it) }
                    request.phone?.let { put(DatabaseColumns.PHONE, it) }
                    request.loyaltyPoints?.let { put(DatabaseColumns.LOYALTY_POINTS, it) }
                    request.loyaltyTier?.let { put(DatabaseColumns.LOYALTY_TIER, it) }
                    request.totalPurchases?.let { put(DatabaseColumns.TOTAL_PURCHASES, it) }
                    request.isActive?.let { put(DatabaseColumns.IS_ACTIVE, it) }
                }

            supabaseClient
                .from(TableNames.CUSTOMERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                    select()
                }.decodeSingle<CustomerDTO>()
        }

    suspend fun deleteCustomer(
        userId: String,
        customerId: String,
    ): Result<Unit> =
        executeQuery(ERROR_DELETE_FAILED) {
            supabaseClient
                .from(TableNames.CUSTOMERS)
                .delete {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }
        }

    suspend fun addLoyaltyPoints(
        userId: String,
        customerId: String,
        request: AddLoyaltyPointsRequest,
    ): Result<CustomerDTO> =
        executeQuery(ERROR_LOYALTY_FAILED) {
            val currentCustomer =
                supabaseClient
                    .from(TableNames.CUSTOMERS)
                    .select {
                        filter {
                            eq(DatabaseColumns.ID, customerId)
                            eq(DatabaseColumns.USER_ID, userId)
                        }
                    }.decodeSingle<CustomerDTO>()

            val newPoints = currentCustomer.loyaltyPoints + request.points
            val newTier = calculateLoyaltyTier(newPoints)

            val data =
                buildJsonObject {
                    put(DatabaseColumns.LOYALTY_POINTS, newPoints)
                    put(DatabaseColumns.LOYALTY_TIER, newTier)
                }

            supabaseClient
                .from(TableNames.CUSTOMERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                    select()
                }.decodeSingle<CustomerDTO>()
        }

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
    ): Result<List<JsonObject>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_PURCHASE_HISTORY_FAILED) {
            supabaseClient
                .from(TableNames.SALES)
                .select {
                    filter {
                        eq(DatabaseColumns.CUSTOMER_ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                    order(DatabaseColumns.SALE_DATE, Order.DESCENDING)
                    range(from, to)
                }.decodeList<JsonObject>()
        }
    }

    private fun calculateLoyaltyTier(points: Int): String =
        when {
            points >= DatabaseColumns.PLATINUM_THRESHOLD -> "Platinum"
            points >= DatabaseColumns.GOLD_THRESHOLD -> "Gold"
            points >= DatabaseColumns.SILVER_THRESHOLD -> "Silver"
            else -> "Bronze"
        }
}
