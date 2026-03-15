package com.vibely.pos.shared.domain.supplier.repository

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier

interface SupplierRepository {
    suspend fun getAll(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<Supplier>>
    suspend fun getById(id: String): Result<Supplier>
    suspend fun create(supplier: Supplier): Result<Supplier>
    suspend fun update(supplier: Supplier): Result<Supplier>
    suspend fun delete(id: String): Result<Unit>
    suspend fun search(query: String): Result<List<Supplier>>
}
