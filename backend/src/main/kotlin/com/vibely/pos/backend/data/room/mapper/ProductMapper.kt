package com.vibely.pos.backend.data.room.mapper

import com.vibely.pos.backend.data.room.entity.ProductEntity
import com.vibely.pos.backend.dto.request.CreateProductRequest
import com.vibely.pos.shared.data.sales.dto.CategoryNameDTO
import com.vibely.pos.shared.data.sales.dto.ProductDTO
import java.util.UUID
import kotlin.time.Clock

private const val DEFAULT_COST_PRICE = 0.0
private const val DEFAULT_MIN_STOCK_LEVEL = 0
private const val DEFAULT_UNIT = "unit"

/**
 * Maps a [ProductEntity] to its shared [ProductDTO] representation.
 *
 * The [ProductDTO.categories] field is reconstructed from [ProductEntity.categoryName]
 * so that callers get the expected embedded-category structure.
 *
 * @return Populated [ProductDTO]
 */
fun ProductEntity.toDto(): ProductDTO =
    ProductDTO(
        id = id,
        sku = sku,
        barcode = barcode,
        name = name,
        description = description,
        categoryId = categoryId,
        categories = categoryName?.let { CategoryNameDTO(name = it) },
        costPrice = costPrice,
        costCurrencyCode = costCurrencyCode,
        sellingPrice = sellingPrice,
        currentStock = currentStock,
        minStockLevel = minStockLevel,
        unit = unit,
        imageUrl = imageUrl,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Maps a [CreateProductRequest] to a new [ProductEntity] ready for Room insertion.
 *
 * Generates a new random UUID for [ProductEntity.id] and sets both timestamps to the
 * current instant.
 *
 * @param userId Owning user ID (stored for reference but not enforced locally)
 * @return New [ProductEntity] with a generated ID and current timestamps
 */
fun CreateProductRequest.toEntity(userId: String): ProductEntity {
    val now = Clock.System.now().toString()
    return ProductEntity(
        id = UUID.randomUUID().toString(),
        sku = sku ?: "",
        barcode = barcode,
        name = name,
        description = description,
        categoryId = categoryId,
        categoryName = null,
        costPrice = costPrice ?: DEFAULT_COST_PRICE,
        costCurrencyCode = costCurrencyCode,
        sellingPrice = unitPrice,
        currentStock = currentStock,
        minStockLevel = minStockLevel ?: DEFAULT_MIN_STOCK_LEVEL,
        unit = unitOfMeasure ?: DEFAULT_UNIT,
        imageUrl = null,
        isActive = isActive,
        createdAt = now,
        updatedAt = now,
    )
}
