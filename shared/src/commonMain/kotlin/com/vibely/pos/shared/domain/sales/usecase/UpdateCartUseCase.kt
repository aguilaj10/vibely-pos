package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.sales.valueobject.Cart

class UpdateCartUseCase {
    suspend operator fun invoke(cart: Cart, productId: String, newQuantity: Int): Cart = cart.updateQuantity(productId, newQuantity)
}
