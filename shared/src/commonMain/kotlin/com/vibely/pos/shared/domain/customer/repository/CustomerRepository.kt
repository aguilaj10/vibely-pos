package com.vibely.pos.shared.domain.customer.repository

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.result.Result

interface CustomerRepository {
    suspend fun getAll(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<Customer>>
    suspend fun getById(id: String): Result<Customer>
    suspend fun create(customer: Customer): Result<Customer>
    suspend fun update(customer: Customer): Result<Customer>
    suspend fun delete(id: String): Result<Unit>
    suspend fun search(query: String): Result<List<Customer>>
    suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer>
    suspend fun getPurchaseHistory(customerId: String, page: Int = 1, pageSize: Int = 50): Result<List<Map<String, Any>>>
}
