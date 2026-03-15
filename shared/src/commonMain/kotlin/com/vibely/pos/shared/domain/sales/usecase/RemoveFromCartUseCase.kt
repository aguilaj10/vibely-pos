package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.valueobject.Cart

class RemoveFromCartUseCase {
    operator fun invoke(cart: Cart, productId: String): Result<Cart> {
        if (productId.isBlank()) {
            return Result.Error("Product ID cannot be blank")
        }

        val item = cart.items.find { it.productId == productId }
            ?: return Result.Error("Product not found in cart")

        return Result.Success(cart.remove(productId))
    }
}
