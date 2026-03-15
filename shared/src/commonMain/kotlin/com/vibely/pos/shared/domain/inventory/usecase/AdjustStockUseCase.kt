package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.inventory.entity.InventoryTransaction
import com.vibely.pos.shared.domain.inventory.entity.TransactionType
import com.vibely.pos.shared.domain.inventory.repository.InventoryRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import kotlin.time.Clock

class AdjustStockUseCase(private val productRepository: ProductRepository, private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(productId: String, quantity: Int, reason: String, performedBy: String, notes: String? = null): Result<Product> {
        if (productId.isBlank()) {
            return Result.Error("Product ID cannot be blank")
        }

        if (quantity == 0) {
            return Result.Error("Quantity cannot be zero")
        }

        if (reason.isBlank()) {
            return Result.Error("Reason cannot be blank")
        }

        if (performedBy.isBlank()) {
            return Result.Error("Performed by user ID cannot be blank")
        }

        return productRepository.getById(productId).flatMap { product ->
            val newStock = product.currentStock + quantity

            if (newStock < 0) {
                return@flatMap Result.Error(
                    "Insufficient stock. Current: ${product.currentStock}, Adjustment: $quantity, Result: $newStock",
                )
            }

            val transactionType = determineTransactionType(quantity, reason)
            val now = Clock.System.now()
            val transactionId = "adj-${now.toEpochMilliseconds()}"

            val transaction = InventoryTransaction.createAdjustment(
                id = transactionId,
                productId = productId,
                quantity = quantity,
                performedBy = performedBy,
                reason = reason,
                notes = notes,
            )

            when (val transactionResult = inventoryRepository.create(transaction)) {
                is Result.Error -> return@flatMap Result.Error(
                    "Failed to create inventory transaction: ${transactionResult.message}",
                )
                is Result.Success -> {
                    val updatedProduct = product.withStock(newStock)
                    when (val updateResult = productRepository.update(updatedProduct)) {
                        is Result.Error -> return@flatMap Result.Error(
                            "Failed to update product stock: ${updateResult.message}",
                        )
                        is Result.Success -> updateResult
                    }
                }
            }
        }
    }

    private fun determineTransactionType(quantity: Int, reason: String): TransactionType {
        val lowerReason = reason.lowercase()
        return when {
            quantity > 0 -> {
                when {
                    lowerReason.contains(
                        "purchase",
                    ) ||
                        lowerReason.contains("restock") ||
                        lowerReason.contains("received") -> TransactionType.PURCHASE
                    lowerReason.contains("return") -> TransactionType.RETURN
                    else -> TransactionType.ADJUSTMENT
                }
            }
            quantity < 0 -> {
                when {
                    lowerReason.contains("damage") || lowerReason.contains("broken") || lowerReason.contains("lost") -> TransactionType.DAMAGE
                    lowerReason.contains("sale") || lowerReason.contains("sold") -> TransactionType.SALE
                    else -> TransactionType.ADJUSTMENT
                }
            }
            else -> TransactionType.ADJUSTMENT
        }
    }
}
