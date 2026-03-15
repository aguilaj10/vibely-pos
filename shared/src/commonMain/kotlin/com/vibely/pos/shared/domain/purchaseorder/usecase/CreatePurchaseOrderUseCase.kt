package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.result.Result

class CreatePurchaseOrderUseCase(private val purchaseOrderRepository: PurchaseOrderRepository) {

    suspend operator fun invoke(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> {
        if (purchaseOrder.items.isEmpty()) {
            return Result.Error(
                message = "Purchase order must have at least one item",
                code = "EMPTY_ITEMS",
            )
        }

        if (purchaseOrder.totalAmount <= 0) {
            return Result.Error(
                message = "Purchase order total amount must be positive",
                code = "INVALID_TOTAL",
            )
        }

        return purchaseOrderRepository.create(purchaseOrder)
    }
}
