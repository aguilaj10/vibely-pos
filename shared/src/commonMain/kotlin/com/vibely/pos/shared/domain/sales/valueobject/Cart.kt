package com.vibely.pos.shared.domain.sales.valueobject

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product

data class Cart(val items: List<CartItem> = emptyList(), val customerId: String? = null) {
    val isEmpty: Boolean
        get() = items.isEmpty()

    val totalItems: Int
        get() = items.sumOf { it.quantity }

    val subtotal: Double
        get() = items.sumOf { it.subtotal }

    val totalAmount: Double
        get() = subtotal

    fun add(product: Product, quantity: Int): Result<Cart> {
        if (!product.canSell(quantity)) {
            return Result.Error("Product '${product.name}' is not available for sale")
        }

        val existingItem = items.find { it.productId == product.id }
        val newItems =
            if (existingItem != null) {
                val newQuantity = existingItem.quantity + quantity
                if (!product.canSell(newQuantity)) {
                    return Result.Error(
                        "Insufficient stock for '${product.name}'. Available: ${product.currentStock}",
                    )
                }
                items.map {
                    if (it.productId == product.id) {
                        it.copy(quantity = newQuantity)
                    } else {
                        it
                    }
                }
            } else {
                items + CartItem(
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    unitPrice = product.sellingPrice,
                )
            }

        return Result.Success(copy(items = newItems))
    }

    fun remove(productId: String): Cart = copy(items = items.filterNot { it.productId == productId })

    fun updateQuantity(productId: String, quantity: Int): Cart {
        require(quantity > 0) { "Quantity must be positive" }

        return copy(
            items = items.map {
                if (it.productId == productId) {
                    it.copy(quantity = quantity)
                } else {
                    it
                }
            },
        )
    }

    fun clear(): Cart = Cart()

    fun validateStock(products: Map<String, Product>): Result<Unit> {
        items.forEach { item ->
            val product = products[item.productId]
                ?: return Result.Error("Product ${item.productId} not found")

            if (!product.canSell(item.quantity)) {
                return Result.Error(
                    "Insufficient stock for '${product.name}'. Available: ${product.currentStock}, Requested: ${item.quantity}",
                )
            }
        }
        return Result.Success(Unit)
    }
}

data class CartItem(val productId: String, val productName: String, val quantity: Int, val unitPrice: Double) {
    init {
        require(productId.isNotBlank()) { "Product ID cannot be blank" }
        require(productName.isNotBlank()) { "Product name cannot be blank" }
        require(quantity > 0) { "Quantity must be positive" }
        require(unitPrice >= 0) { "Unit price cannot be negative" }
    }

    val subtotal: Double
        get() = unitPrice * quantity
}
