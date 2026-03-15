package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository

class SearchSuppliersUseCase(private val supplierRepository: SupplierRepository) {
    suspend operator fun invoke(query: String): Result<List<Supplier>> = supplierRepository.search(query)
}
