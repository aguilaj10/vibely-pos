package com.vibely.pos.shared.domain.purchaseorder.entity

import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import kotlin.time.Clock
import kotlin.time.Instant

data class PurchaseOrder(
    val id: String,
    val poNumber: String,
    val supplierId: String,
    val supplierName: String?,
    val createdById: String,
    val totalAmount: Double,
    val status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFT,
    val orderDate: Instant,
    val expectedDeliveryDate: Instant?,
    val receivedDate: Instant?,
    val notes: String?,
    val items: List<PurchaseOrderItem> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "Purchase order ID cannot be blank" }
        require(poNumber.isNotBlank()) { "PO number cannot be blank" }
        require(supplierId.isNotBlank()) { "Supplier ID cannot be blank" }
        require(createdById.isNotBlank()) { "Created by ID cannot be blank" }
        require(totalAmount >= 0) { "Total amount cannot be negative" }
    }

    fun canModify(): Boolean = status.canModify()

    fun canCancel(): Boolean = status.canCancel()

    fun canApprove(): Boolean = status.canApprove()

    fun canReceive(): Boolean = status.canReceive()

    fun withStatus(newStatus: PurchaseOrderStatus): PurchaseOrder = copy(
        status = newStatus,
        updatedAt = Clock.System.now(),
    )

    fun markAsReceived(receivedAt: Instant = Clock.System.now()): PurchaseOrder = copy(
        status = PurchaseOrderStatus.RECEIVED,
        receivedDate = receivedAt,
        updatedAt = Clock.System.now(),
    )

    fun calculateTotalFromItems(): Double = items.sumOf { it.subtotal }

    companion object {
        fun create(
            id: String,
            poNumber: String,
            supplierId: String,
            createdById: String,
            totalAmount: Double,
            supplierName: String? = null,
            status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFT,
            orderDate: Instant = Clock.System.now(),
            expectedDeliveryDate: Instant? = null,
            receivedDate: Instant? = null,
            notes: String? = null,
            items: List<PurchaseOrderItem> = emptyList(),
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): PurchaseOrder = PurchaseOrder(
            id = id,
            poNumber = poNumber,
            supplierId = supplierId,
            supplierName = supplierName,
            createdById = createdById,
            totalAmount = totalAmount,
            status = status,
            orderDate = orderDate,
            expectedDeliveryDate = expectedDeliveryDate,
            receivedDate = receivedDate,
            notes = notes,
            items = items,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
