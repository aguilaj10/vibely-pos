package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.entity.SaleItem
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.sales.valueobject.Cart
import com.vibely.pos.shared.domain.sales.valueobject.PaymentStatus
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CompleteSaleUseCase(private val saleRepository: SaleRepository, private val productRepository: ProductRepository) {
    suspend operator fun invoke(cart: Cart, cashierId: String, customerId: String? = null, notes: String? = null): Result<Sale> {
        if (cart.isEmpty) {
            return Result.Error("Cart cannot be empty")
        }

        if (cashierId.isBlank()) {
            return Result.Error("Cashier ID cannot be blank")
        }

        val productIds = cart.items.map { it.productId }
        val productsMap = mutableMapOf<String, com.vibely.pos.shared.domain.sales.entity.Product>()

        for (productId in productIds) {
            when (val result = productRepository.getById(productId)) {
                is Result.Success -> productsMap[productId] = result.data
                is Result.Error -> return Result.Error("Failed to fetch product $productId: ${result.message}")
            }
        }

        return cart.validateStock(productsMap).flatMap {
            // Deduct stock for each item in the cart
            for (cartItem in cart.items) {
                val product = productsMap[cartItem.productId] ?: continue
                val newStock = product.currentStock - cartItem.quantity
                val updatedProduct = product.copy(
                    currentStock = newStock,
                    updatedAt = Clock.System.now(),
                )
                when (val updateResult = productRepository.update(updatedProduct)) {
                    is Result.Success -> productsMap[cartItem.productId] = updateResult.data
                    is Result.Error -> return Result.Error("Failed to update stock: ${updateResult.message}")
                }
            }

            val now = Clock.System.now()
            val invoiceNumber = generateInvoiceNumber()
            val saleId = generateSaleId()

            val sale = Sale.create(
                id = saleId,
                invoiceNumber = invoiceNumber,
                cashierId = cashierId,
                customerId = customerId,
                subtotal = cart.subtotal,
                totalAmount = cart.totalAmount,
                status = SaleStatus.COMPLETED,
                paymentStatus = PaymentStatus.COMPLETED,
                notes = notes,
                saleDate = now,
                createdAt = now,
                updatedAt = now,
            )

            val saleItems = cart.items.map { cartItem ->
                SaleItem.create(
                    id = "$saleId-${cartItem.productId}",
                    saleId = saleId,
                    productId = cartItem.productId,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.unitPrice,
                    createdAt = now,
                )
            }

            saleRepository.create(sale, saleItems)
        }
    }

    private fun generateSaleId(): String {
        val now = Clock.System.now()
        return "sale-${now.toEpochMilliseconds()}"
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateInvoiceNumber(): String {
        val now = Clock.System.now()
        val year = now.toString().substring(0, 4)
        val timestamp = now.toEpochMilliseconds()
        val sequence = timestamp % 100000
        val uniqueId = Uuid.random().toString().take(8)
        return "SAL-$year-${sequence.toString().padStart(5, '0')}-$uniqueId"
    }
}
