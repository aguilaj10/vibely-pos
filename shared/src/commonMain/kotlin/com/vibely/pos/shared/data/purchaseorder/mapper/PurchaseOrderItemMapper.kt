package com.vibely.pos.shared.data.purchaseorder.mapper

import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderItemDTO
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrderItem
import kotlin.time.Instant

object PurchaseOrderItemMapper {
    fun toDomain(dto: PurchaseOrderItemDTO): PurchaseOrderItem = PurchaseOrderItem.create(
        id = dto.id,
        purchaseOrderId = dto.purchaseOrderId,
        productId = dto.productId,
        productName = dto.productName,
        productSku = dto.productSku,
        quantity = dto.quantity,
        unitCost = dto.unitCost,
        costCurrencyCode = dto.costCurrencyCode,
        receivedQuantity = dto.receivedQuantity,
        createdAt = Instant.parse(dto.createdAt),
    )

    fun toDTO(item: PurchaseOrderItem): PurchaseOrderItemDTO = PurchaseOrderItemDTO(
        id = item.id,
        purchaseOrderId = item.purchaseOrderId,
        productId = item.productId,
        productName = item.productName,
        productSku = item.productSku,
        quantity = item.quantity,
        unitCost = item.unitCost,
        costCurrencyCode = item.costCurrencyCode,
        subtotal = item.subtotal,
        receivedQuantity = item.receivedQuantity,
        createdAt = item.createdAt.toString(),
    )
}
