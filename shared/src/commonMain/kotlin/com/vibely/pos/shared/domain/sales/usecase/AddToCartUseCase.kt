package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import com.vibely.pos.shared.domain.sales.valueobject.Cart

class AddToCartUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(cart: Cart, productId: String, quantity: Int): Result<Cart> {
        if (quantity <= 0) {
            return Result.Error("Quantity must be positive")
        }

        return productRepository.getById(productId).flatMap { product ->
            cart.add(product, quantity)
        }
    }
}
