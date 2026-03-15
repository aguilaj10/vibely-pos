package com.vibely.pos.shared.data.supplier.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.supplier.datasource.RemoteSupplierDataSource
import com.vibely.pos.shared.data.supplier.mapper.SupplierMapper
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupplierRepositoryImpl(private val remoteDataSource: RemoteSupplierDataSource) :
    BaseRepository(),
    SupplierRepository {

    override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Supplier>> = mapList(
        remoteDataSource.getAllSuppliers(isActive, page, pageSize),
        SupplierMapper::toDomain,
    )

    override suspend fun getById(id: String): Result<Supplier> = mapSingle(
        remoteDataSource.getSupplierById(id),
        SupplierMapper::toDomain,
    )

    override suspend fun create(supplier: Supplier): Result<Supplier> {
        val dto = SupplierMapper.toDTO(supplier)
        val json = buildJsonObject {
            put("code", dto.code)
            put("name", dto.name)
            dto.contactPerson?.let { put("contact_person", it) }
            dto.email?.let { put("email", it) }
            dto.phone?.let { put("phone", it) }
            dto.address?.let { put("address", it) }
            put("is_active", dto.isActive)
        }
        return mapSingle(
            remoteDataSource.createSupplier(json),
            SupplierMapper::toDomain,
        )
    }

    override suspend fun update(supplier: Supplier): Result<Supplier> {
        val dto = SupplierMapper.toDTO(supplier)
        val json = buildJsonObject {
            put("code", dto.code)
            put("name", dto.name)
            dto.contactPerson?.let { put("contact_person", it) }
            dto.email?.let { put("email", it) }
            dto.phone?.let { put("phone", it) }
            dto.address?.let { put("address", it) }
            put("is_active", dto.isActive)
        }
        return mapSingle(
            remoteDataSource.updateSupplier(supplier.id, json),
            SupplierMapper::toDomain,
        )
    }

    override suspend fun delete(id: String): Result<Unit> = remoteDataSource.deleteSupplier(id)

    override suspend fun search(query: String): Result<List<Supplier>> = mapList(
        remoteDataSource.searchSuppliers(query),
        SupplierMapper::toDomain,
    )
}
