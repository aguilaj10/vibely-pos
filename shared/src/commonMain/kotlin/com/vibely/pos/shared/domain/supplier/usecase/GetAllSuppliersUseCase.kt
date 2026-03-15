package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository

class GetAllSuppliersUseCase(private val supplierRepository: SupplierRepository) {
    suspend operator fun invoke(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<Supplier>> =
        supplierRepository.getAll(isActive, page, pageSize)
}
