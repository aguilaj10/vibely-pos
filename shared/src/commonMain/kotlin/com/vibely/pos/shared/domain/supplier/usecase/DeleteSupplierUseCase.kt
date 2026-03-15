package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository

class DeleteSupplierUseCase(private val supplierRepository: SupplierRepository) {
    suspend operator fun invoke(supplierId: String): Result<Unit> = supplierRepository.delete(supplierId)
}
