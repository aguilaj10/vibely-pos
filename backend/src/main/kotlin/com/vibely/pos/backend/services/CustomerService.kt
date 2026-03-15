@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "LongParameterList", "StringLiteralDuplication", "MagicNumber", "MaxLineLength")
package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
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

private const val TABLE_CUSTOMERS = "customers"
private const val TABLE_SALES = "sales"
private const val ERROR_FETCH_FAILED = "Failed to fetch customers"
private const val ERROR_CUSTOMER_NOT_FOUND = "Customer not found"
private const val ERROR_CREATE_FAILED = "Failed to create customer"
private const val ERROR_UPDATE_FAILED = "Failed to update customer"
private const val ERROR_DELETE_FAILED = "Failed to delete customer"
private const val ERROR_LOYALTY_FAILED = "Failed to add loyalty points"
private const val ERROR_PURCHASE_HISTORY_FAILED = "Failed to fetch purchase history"

class CustomerService(private val supabaseClient: SupabaseClient) : BaseService() {

    suspend fun getAllCustomers(
        userId: String,
        isActive: Boolean?,
        searchQuery: String?,
        page: Int,
        pageSize: Int
    ): Result<List<CustomerDTO>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient.from(TABLE_CUSTOMERS)
                .select {
                    filter {
                        eq(DatabaseColumns.USER_ID, userId)
                        isActive?.let { eq(DatabaseColumns.IS_ACTIVE, it) }
                        searchQuery?.let { query ->
                            val searchPattern = "%$query%"
                            or {
                                ilike(DatabaseColumns.NAME, searchPattern)
                                ilike("first_name", searchPattern)
                                ilike("last_name", searchPattern)
                                ilike("email", searchPattern)
                                ilike("phone", searchPattern)
                                ilike("code", searchPattern)
                            }
                        }
                    }
                    order(DatabaseColumns.NAME, Order.ASCENDING)
                    range(from, to)
                }
                .decodeList<CustomerDTO>()
        }
    }

    suspend fun getCustomerById(userId: String, customerId: String): Result<CustomerDTO> {
        return executeQuery(ERROR_CUSTOMER_NOT_FOUND) {
            supabaseClient.from(TABLE_CUSTOMERS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }
                .decodeSingle<CustomerDTO>()
        }
    }

    suspend fun createCustomer(userId: String, request: CreateCustomerRequest): Result<CustomerDTO> {
        return executeQuery(ERROR_CREATE_FAILED) {
            val data = buildJsonObject {
                put(DatabaseColumns.USER_ID, userId)
                put("code", request.code)
                put("first_name", request.firstName)
                put("last_name", request.lastName)
                request.email?.let { put("email", it) }
                request.phone?.let { put("phone", it) }
                put("loyalty_points", request.loyaltyPoints)
                request.loyaltyTier?.let { put("loyalty_tier", it) }
                put("total_purchases", request.totalPurchases)
                put(DatabaseColumns.IS_ACTIVE, request.isActive)
            }

            supabaseClient.from(TABLE_CUSTOMERS)
                .insert(data) {
                    select()
                }
                .decodeSingle<CustomerDTO>()
        }
    }

    suspend fun updateCustomer(userId: String, customerId: String, request: UpdateCustomerRequest): Result<CustomerDTO> {
        return executeQuery(ERROR_UPDATE_FAILED) {
            val data = buildJsonObject {
                request.code?.let { put("code", it) }
                request.firstName?.let { put("first_name", it) }
                request.lastName?.let { put("last_name", it) }
                request.email?.let { put("email", it) }
                request.phone?.let { put("phone", it) }
                request.loyaltyPoints?.let { put("loyalty_points", it) }
                request.loyaltyTier?.let { put("loyalty_tier", it) }
                request.totalPurchases?.let { put("total_purchases", it) }
                request.isActive?.let { put(DatabaseColumns.IS_ACTIVE, it) }
            }

            supabaseClient.from(TABLE_CUSTOMERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                    select()
                }
                .decodeSingle<CustomerDTO>()
        }
    }

    suspend fun deleteCustomer(userId: String, customerId: String): Result<Unit> {
        return executeQuery(ERROR_DELETE_FAILED) {
            supabaseClient.from(TABLE_CUSTOMERS)
                .delete {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }
        }
    }

    suspend fun addLoyaltyPoints(userId: String, customerId: String, request: AddLoyaltyPointsRequest): Result<CustomerDTO> {
        return executeQuery(ERROR_LOYALTY_FAILED) {
            val currentCustomer = supabaseClient.from(TABLE_CUSTOMERS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                }
                .decodeSingle<CustomerDTO>()

            val newPoints = currentCustomer.loyaltyPoints + request.points
            val newTier = calculateLoyaltyTier(newPoints)

            val data = buildJsonObject {
                put("loyalty_points", newPoints)
                put("loyalty_tier", newTier)
            }

            supabaseClient.from(TABLE_CUSTOMERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, customerId)
                        eq(DatabaseColumns.USER_ID, userId)
                    }
                    select()
                }
                .decodeSingle<CustomerDTO>()
        }
    }

    suspend fun getPurchaseHistory(
        userId: String,
        customerId: String,
        page: Int,
        pageSize: Int
    ): Result<List<JsonObject>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_PURCHASE_HISTORY_FAILED) {
            supabaseClient.from(TABLE_SALES)
                .select {
                    filter {
                        eq("customer_id", customerId)
                    }
                    order(DatabaseColumns.SALE_DATE, Order.DESCENDING)
                    range(from, to)
                }
                .decodeList<JsonObject>()
        }
    }

    private fun calculateLoyaltyTier(points: Int): String = when {
        points >= 5000 -> "Platinum"
        points >= 2000 -> "Gold"
        points >= 500 -> "Silver"
        else -> "Bronze"
    }
}
