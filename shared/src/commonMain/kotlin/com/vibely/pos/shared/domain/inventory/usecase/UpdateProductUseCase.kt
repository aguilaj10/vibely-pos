package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import kotlin.time.Clock

class UpdateProductUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(
        id: String,
        sku: String,
        name: String,
        costPrice: Double,
        sellingPrice: Double,
        currentStock: Int,
        minStockLevel: Int,
        barcode: String? = null,
        description: String? = null,
        categoryId: String? = null,
        unit: String = "unit",
        imageUrl: String? = null,
        isActive: Boolean = true,
        costCurrencyCode: String = "USD",
    ): Result<Product> {
        if (id.isBlank()) {
            return Result.Error("Product ID cannot be blank")
        }

        if (name.isBlank()) {
            return Result.Error("Product name cannot be blank")
        }
        if (name.length < 3 || name.length > 100) {
            return Result.Error("Product name must be between 3 and 100 characters")
        }

        if (sku.isBlank()) {
            return Result.Error("SKU cannot be blank")
        }

        if (sellingPrice <= 0) {
            return Result.Error("Selling price must be greater than 0")
        }

        if (costPrice < 0) {
            return Result.Error("Cost price cannot be negative")
        }

        if (currentStock < 0) {
            return Result.Error("Current stock cannot be negative")
        }

        if (minStockLevel < 0) {
            return Result.Error("Minimum stock level cannot be negative")
        }

        return productRepository.getById(id).flatMap { existingProduct ->
            val productsWithSku = productRepository.search(sku).getOrNull() ?: emptyList()
            val duplicateSku = productsWithSku.find { it.sku == sku && it.id != id }
            if (duplicateSku != null) {
                return@flatMap Result.Error("A product with SKU '$sku' already exists")
            }

            val now = Clock.System.now()
            val updatedProduct = existingProduct.copy(
                sku = sku,
                name = name,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                currentStock = currentStock,
                minStockLevel = minStockLevel,
                barcode = barcode,
                description = description,
                categoryId = categoryId,
                unit = unit,
                imageUrl = imageUrl,
                isActive = isActive,
                updatedAt = now,
            )

            productRepository.update(updatedProduct)
        }
    }
}
