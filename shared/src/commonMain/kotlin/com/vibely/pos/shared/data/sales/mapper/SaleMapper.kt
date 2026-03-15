package com.vibely.pos.shared.data.sales.mapper

import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.valueobject.PaymentStatus
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlin.time.Instant

/**
 * Mapper for converting between [SaleDTO] and [Sale] domain entity.
 */
object SaleMapper {

    /**
     * Maps a [SaleDTO] from the backend to a [Sale] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if any field cannot be parsed.
     */
    fun toDomain(dto: SaleDTO): Sale = Sale.create(
        id = dto.id,
        invoiceNumber = dto.invoiceNumber,
        customerId = dto.customerId,
        cashierId = dto.cashierId,
        subtotal = dto.subtotal,
        taxAmount = dto.taxAmount,
        discountAmount = dto.discountAmount,
        totalAmount = dto.totalAmount,
        status = SaleStatus.valueOf(dto.status.uppercase()),
        paymentStatus = PaymentStatus.valueOf(dto.paymentStatus.uppercase()),
        notes = dto.notes,
        saleDate = Instant.parse(dto.saleDate),
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    /**
     * Maps a [Sale] domain entity to a [SaleDTO] for the backend.
     *
     * @param sale The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(sale: Sale): SaleDTO = SaleDTO(
        id = sale.id,
        invoiceNumber = sale.invoiceNumber,
        customerId = sale.customerId,
        cashierId = sale.cashierId,
        subtotal = sale.subtotal,
        taxAmount = sale.taxAmount,
        discountAmount = sale.discountAmount,
        totalAmount = sale.totalAmount,
        status = sale.status.name.lowercase(),
        paymentStatus = sale.paymentStatus.name.lowercase(),
        notes = sale.notes,
        saleDate = sale.saleDate.toString(),
        createdAt = sale.createdAt.toString(),
        updatedAt = sale.updatedAt.toString(),
    )
}
