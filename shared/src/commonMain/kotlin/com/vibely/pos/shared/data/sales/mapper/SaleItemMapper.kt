package com.vibely.pos.shared.data.sales.mapper

import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import com.vibely.pos.shared.domain.sales.entity.SaleItem
import kotlin.time.Instant

/**
 * Mapper for converting between [SaleItemDTO] and [SaleItem] domain entity.
 */
object SaleItemMapper {

    /**
     * Maps a [SaleItemDTO] from the backend to a [SaleItem] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if any field cannot be parsed.
     */
    fun toDomain(dto: SaleItemDTO): SaleItem = SaleItem.create(
        id = dto.id,
        saleId = dto.saleId,
        productId = dto.productId,
        quantity = dto.quantity,
        unitPrice = dto.unitPrice,
        discountAmount = dto.discountAmount,
        createdAt = Instant.parse(dto.createdAt),
    )

    /**
     * Maps a [SaleItem] domain entity to a [SaleItemDTO] for the backend.
     *
     * @param saleItem The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(saleItem: SaleItem): SaleItemDTO = SaleItemDTO(
        id = saleItem.id,
        saleId = saleItem.saleId,
        productId = saleItem.productId,
        quantity = saleItem.quantity,
        unitPrice = saleItem.unitPrice,
        discountAmount = saleItem.discountAmount,
        subtotal = saleItem.subtotal,
        createdAt = saleItem.createdAt.toString(),
    )
}
