package com.vibely.pos.shared.data.purchaseorder.mapper

import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderDTO
import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderWithItemsDTO
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import kotlin.time.Instant

object PurchaseOrderMapper {
    fun toDomain(dto: PurchaseOrderDTO): PurchaseOrder = PurchaseOrder.create(
        id = dto.id,
        poNumber = dto.poNumber,
        supplierId = dto.supplierId,
        supplierName = dto.supplierName,
        createdById = dto.createdBy,
        totalAmount = dto.totalAmount,
        status = PurchaseOrderStatus.fromString(dto.status),
        orderDate = Instant.parse(dto.orderDate),
        expectedDeliveryDate = dto.expectedDeliveryDate?.let { Instant.parse(it) },
        receivedDate = dto.receivedDate?.let { Instant.parse(it) },
        notes = dto.notes,
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    fun toDomain(dto: PurchaseOrderWithItemsDTO): PurchaseOrder = PurchaseOrder.create(
        id = dto.id,
        poNumber = dto.poNumber,
        supplierId = dto.supplierId,
        supplierName = dto.supplierName,
        createdById = dto.createdBy,
        totalAmount = dto.totalAmount,
        status = PurchaseOrderStatus.fromString(dto.status),
        orderDate = Instant.parse(dto.orderDate),
        expectedDeliveryDate = dto.expectedDeliveryDate?.let { Instant.parse(it) },
        receivedDate = dto.receivedDate?.let { Instant.parse(it) },
        notes = dto.notes,
        items = dto.items.map { PurchaseOrderItemMapper.toDomain(it) },
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    fun toDTO(purchaseOrder: PurchaseOrder): PurchaseOrderDTO = PurchaseOrderDTO(
        id = purchaseOrder.id,
        poNumber = purchaseOrder.poNumber,
        supplierId = purchaseOrder.supplierId,
        supplierName = purchaseOrder.supplierName,
        createdBy = purchaseOrder.createdById,
        totalAmount = purchaseOrder.totalAmount,
        status = purchaseOrder.status.name.lowercase(),
        orderDate = purchaseOrder.orderDate.toString(),
        expectedDeliveryDate = purchaseOrder.expectedDeliveryDate?.toString(),
        receivedDate = purchaseOrder.receivedDate?.toString(),
        notes = purchaseOrder.notes,
        createdAt = purchaseOrder.createdAt.toString(),
        updatedAt = purchaseOrder.updatedAt.toString(),
    )
}
