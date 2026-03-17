package com.vibely.pos.shared.data.sales.mapper

import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.sales.entity.Product
import kotlin.time.Instant

/**
 * Mapper for converting between [ProductDTO] and [Product] domain entity.
 */
object ProductMapper {

    /**
     * Maps a [ProductDTO] from the backend to a [Product] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if any field cannot be parsed.
     */
    fun toDomain(dto: ProductDTO): Product = Product.create(
        id = dto.id,
        sku = dto.sku,
        barcode = dto.barcode,
        name = dto.name,
        description = dto.description,
        categoryId = dto.categoryId,
        categoryName = dto.categoryName,
        costPrice = dto.costPrice,
        costCurrencyCode = dto.costCurrencyCode ?: "USD",
        sellingPrice = dto.sellingPrice,
        currentStock = dto.currentStock,
        minStockLevel = dto.minStockLevel,
        unit = dto.unit,
        imageUrl = dto.imageUrl,
        isActive = dto.isActive,
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    /**
     * Maps a [Product] domain entity to a [ProductDTO] for the backend.
     *
     * Note: The categoryName is not included in the DTO when sending to the backend,
     * as it will be fetched via JOIN when reading from the database.
     *
     * @param product The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(product: Product): ProductDTO = ProductDTO(
        id = product.id,
        sku = product.sku,
        barcode = product.barcode,
        name = product.name,
        description = product.description,
        categoryId = product.categoryId,
        categories = null, // Not sent to backend, only received
        costPrice = product.costPrice,
        costCurrencyCode = product.costCurrencyCode,
        sellingPrice = product.sellingPrice,
        currentStock = product.currentStock,
        minStockLevel = product.minStockLevel,
        unit = product.unit,
        imageUrl = product.imageUrl,
        isActive = product.isActive,
        createdAt = product.createdAt.toString(),
        updatedAt = product.updatedAt.toString(),
    )
}
