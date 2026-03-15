package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository

class SearchProductsUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(query: String): Result<List<Product>> {
        if (query.isBlank()) {
            return Result.Error("Search query cannot be empty")
        }

        if (query.length < 2) {
            return Result.Error("Search query must be at least 2 characters")
        }

        return productRepository.search(query.trim())
    }
}
