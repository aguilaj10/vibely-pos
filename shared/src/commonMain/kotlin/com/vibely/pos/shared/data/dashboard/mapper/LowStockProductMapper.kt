package com.vibely.pos.shared.data.dashboard.mapper

import com.vibely.pos.shared.data.dashboard.dto.LowStockProductDTO
import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.valueobject.Money
import com.vibely.pos.shared.domain.valueobject.SKU

/**
 * Mapper for converting between [LowStockProductDTO] and [LowStockProduct] domain entity.
 */
object LowStockProductMapper {

    /**
     * Maps a [LowStockProductDTO] from the backend to a [LowStockProduct] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if SKU is invalid.
     */
    fun toDomain(dto: LowStockProductDTO): LowStockProduct {
        val sku = SKU.create(dto.sku)
        val sellingPrice = Money.fromCents(dto.sellingPriceCents, "USD")

        return LowStockProduct.create(
            id = dto.id,
            sku = sku,
            name = dto.name,
            currentStock = dto.currentStock,
            minStockLevel = dto.minStockLevel,
            sellingPrice = sellingPrice,
            categoryName = dto.categoryName,
        )
    }

    /**
     * Maps a [LowStockProduct] domain entity to a [LowStockProductDTO] for the backend.
     *
     * @param product The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(product: LowStockProduct): LowStockProductDTO = LowStockProductDTO(
        id = product.id,
        sku = product.sku.value,
        name = product.name,
        currentStock = product.currentStock,
        minStockLevel = product.minStockLevel,
        sellingPriceCents = product.sellingPrice.amountInCents,
        categoryName = product.categoryName,
    )
}
