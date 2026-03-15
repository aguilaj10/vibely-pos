package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.inventory.repository.CategoryRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import kotlin.time.Clock

class CreateProductUseCase(private val productRepository: ProductRepository, private val categoryRepository: CategoryRepository) {
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
    ): Result<Product> {
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

        return productRepository.search(sku).flatMap { products ->
            val existingProduct = products.find { it.sku == sku }
            if (existingProduct != null) {
                return@flatMap Result.Error("A product with SKU '$sku' already exists")
            }

            if (categoryId != null) {
                when (val categoryResult = categoryRepository.getById(categoryId)) {
                    is Result.Error -> return@flatMap Result.Error("Category not found: ${categoryResult.message}")
                    is Result.Success -> {
                        if (!categoryResult.data.isActive) {
                            return@flatMap Result.Error("Cannot assign product to inactive category")
                        }
                    }
                }
            }

            val now = Clock.System.now()
            val product = Product.create(
                id = id,
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
                createdAt = now,
                updatedAt = now,
            )

            productRepository.create(product)
        }
    }
}
