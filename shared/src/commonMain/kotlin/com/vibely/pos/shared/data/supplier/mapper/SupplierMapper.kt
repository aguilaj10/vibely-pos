package com.vibely.pos.shared.data.supplier.mapper

import com.vibely.pos.shared.data.supplier.dto.SupplierDTO
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import kotlin.time.Instant

object SupplierMapper {
    fun toDomain(dto: SupplierDTO): Supplier = Supplier.create(
        id = dto.id,
        code = dto.code,
        name = dto.name,
        contactPerson = dto.contactPerson,
        email = dto.email,
        phone = dto.phone,
        address = dto.address,
        isActive = dto.isActive,
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    fun toDTO(supplier: Supplier): SupplierDTO = SupplierDTO(
        id = supplier.id,
        code = supplier.code,
        name = supplier.name,
        contactPerson = supplier.contactPerson,
        email = supplier.email,
        phone = supplier.phone,
        address = supplier.address,
        isActive = supplier.isActive,
        createdAt = supplier.createdAt.toString(),
        updatedAt = supplier.updatedAt.toString(),
    )
}
