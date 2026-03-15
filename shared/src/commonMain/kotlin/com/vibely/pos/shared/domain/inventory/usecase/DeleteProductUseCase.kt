package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import com.vibely.pos.shared.domain.sales.repository.ProductRepository

class DeleteProductUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(id: String): Result<Unit> {
        if (id.isBlank()) {
            return Result.Error("Product ID cannot be blank")
        }

        return productRepository.getById(id).flatMap { product ->
            if (!product.isActive) {
                return@flatMap Result.Error("Product is already inactive")
            }

            if (product.currentStock > 0) {
                return@flatMap Result.Error(
                    "Cannot delete product with non-zero stock. Current stock: ${product.currentStock}",
                )
            }

            productRepository.delete(id)
        }
    }
}
