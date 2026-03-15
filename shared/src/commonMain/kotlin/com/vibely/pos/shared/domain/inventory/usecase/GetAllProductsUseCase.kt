package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository

class GetAllProductsUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(
        categoryId: String? = null,
        isActive: Boolean? = null,
        lowStockOnly: Boolean = false,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<Product>> = productRepository.getAll(categoryId, isActive, lowStockOnly, page, pageSize)
}
