package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository

class UpdateSupplierUseCase(private val supplierRepository: SupplierRepository) {
    suspend operator fun invoke(supplier: Supplier): Result<Supplier> = supplierRepository.update(supplier)
}
