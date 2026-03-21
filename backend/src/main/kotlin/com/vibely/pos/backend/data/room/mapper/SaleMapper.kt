package com.vibely.pos.backend.data.room.mapper

import com.vibely.pos.backend.data.room.entity.SaleEntity
import com.vibely.pos.backend.data.room.entity.SaleItemEntity
import com.vibely.pos.shared.data.sales.dto.CreateSaleItemRequest
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import java.util.UUID
import kotlin.time.Clock

private const val INVOICE_NUMBER_PADDING = 5

/**
 * Maps a [SaleEntity] to its shared [SaleDTO] representation.
 *
 * @return Populated [SaleDTO]
 */
fun SaleEntity.toDto(): SaleDTO =
    SaleDTO(
        id = id,
        invoiceNumber = invoiceNumber,
        customerId = customerId,
        cashierId = cashierId,
        subtotal = subtotal,
        taxAmount = taxAmount,
        discountAmount = discountAmount,
        totalAmount = totalAmount,
        status = status,
        paymentStatus = paymentStatus,
        notes = notes,
        saleDate = saleDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Maps a [SaleItemEntity] to its shared [SaleItemDTO] representation.
 *
 * @return Populated [SaleItemDTO]
 */
fun SaleItemEntity.toDto(): SaleItemDTO =
    SaleItemDTO(
        id = id,
        saleId = saleId,
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
        discountAmount = discountAmount,
        subtotal = subtotal,
        createdAt = createdAt,
    )

/**
 * Creates a new [SaleEntity] from a [CreateSaleRequest].
 *
 * Generates a new random UUID and an invoice number using [nextSaleCount].
 *
 * @param cashierId ID of the cashier creating the sale
 * @param nextSaleCount Total existing sales count used to derive the invoice number sequence
 * @return New [SaleEntity] with generated ID, invoice number, and current timestamps
 */
fun CreateSaleRequest.toEntity(cashierId: String, nextSaleCount: Int): SaleEntity {
    val now = Clock.System.now().toString()
    val year = now.take(YEAR_LENGTH)
    val invoiceNumber = "SAL-$year-${(nextSaleCount + 1).toString().padStart(INVOICE_NUMBER_PADDING, '0')}"
    return SaleEntity(
        id = UUID.randomUUID().toString(),
        invoiceNumber = invoiceNumber,
        customerId = customerId,
        cashierId = cashierId,
        subtotal = subtotal,
        taxAmount = taxAmount,
        discountAmount = discountAmount,
        totalAmount = totalAmount,
        status = status,
        paymentStatus = paymentStatus,
        notes = notes,
        saleDate = now,
        createdAt = now,
        updatedAt = now,
    )
}

private const val YEAR_LENGTH = 4

/**
 * Creates a new [SaleItemEntity] from a [CreateSaleItemRequest].
 *
 * @param saleId Parent sale ID
 * @param productName Display name of the product at time of sale
 * @return New [SaleItemEntity] with a generated ID and current timestamp
 */
fun CreateSaleItemRequest.toEntity(saleId: String, productName: String): SaleItemEntity {
    val now = Clock.System.now().toString()
    val itemSubtotal = unitPrice * quantity - discountAmount
    return SaleItemEntity(
        id = UUID.randomUUID().toString(),
        saleId = saleId,
        productId = productId,
        productName = productName,
        quantity = quantity,
        unitPrice = unitPrice,
        discountAmount = discountAmount,
        subtotal = itemSubtotal,
        createdAt = now,
    )
}
