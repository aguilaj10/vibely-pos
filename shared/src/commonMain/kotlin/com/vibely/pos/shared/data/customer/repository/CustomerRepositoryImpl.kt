package com.vibely.pos.shared.data.customer.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.customer.datasource.RemoteCustomerDataSource
import com.vibely.pos.shared.data.customer.mapper.CustomerMapper
import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CustomerRepositoryImpl(private val remoteDataSource: RemoteCustomerDataSource) :
    BaseRepository(),
    CustomerRepository {
    override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Customer>> = mapList(
        remoteDataSource.getAllCustomers(isActive, page, pageSize),
        CustomerMapper::toDomain,
    )

    override suspend fun getById(id: String): Result<Customer> = mapSingle(
        remoteDataSource.getCustomerById(id),
        CustomerMapper::toDomain,
    )

    override suspend fun create(customer: Customer): Result<Customer> {
        val dto = CustomerMapper.toDTO(customer)
        val json =
            buildJsonObject {
                put("code", dto.code)
                put("full_name", dto.fullName)
                dto.email?.let { put("email", it) }
                dto.phone?.let { put("phone", it) }
                put("loyalty_points", dto.loyaltyPoints)
                dto.loyaltyTier?.let { put("loyalty_tier", it) }
                put("total_purchases", dto.totalPurchases)
                put("is_active", dto.isActive)
            }
        return mapSingle(
            remoteDataSource.createCustomer(json),
            CustomerMapper::toDomain,
        )
    }

    override suspend fun update(customer: Customer): Result<Customer> {
        val dto = CustomerMapper.toDTO(customer)
        val json =
            buildJsonObject {
                put("code", dto.code)
                put("full_name", dto.fullName)
                dto.email?.let { put("email", it) }
                dto.phone?.let { put("phone", it) }
                put("loyalty_points", dto.loyaltyPoints)
                dto.loyaltyTier?.let { put("loyalty_tier", it) }
                put("total_purchases", dto.totalPurchases)
                put("is_active", dto.isActive)
            }
        return mapSingle(
            remoteDataSource.updateCustomer(customer.id, json),
            CustomerMapper::toDomain,
        )
    }

    override suspend fun delete(id: String): Result<Unit> = remoteDataSource.deleteCustomer(id)

    override suspend fun search(query: String): Result<List<Customer>> = mapList(
        remoteDataSource.searchCustomers(query),
        CustomerMapper::toDomain,
    )

    override suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer> = mapSingle(
        remoteDataSource.addLoyaltyPoints(customerId, points),
        CustomerMapper::toDomain,
    )

    override suspend fun getPurchaseHistory(customerId: String, page: Int, pageSize: Int): Result<List<Map<String, Any>>> =
        remoteDataSource.getPurchaseHistory(customerId, page, pageSize)
}
