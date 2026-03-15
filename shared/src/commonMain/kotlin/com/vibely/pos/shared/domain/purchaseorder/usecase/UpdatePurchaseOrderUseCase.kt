package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.result.Result

class UpdatePurchaseOrderUseCase(private val purchaseOrderRepository: PurchaseOrderRepository) {

    suspend operator fun invoke(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> {
        if (!purchaseOrder.canModify()) {
            return Result.Error(
                message = "Purchase order cannot be modified in status: ${purchaseOrder.status}",
                code = "CANNOT_MODIFY",
            )
        }

        if (purchaseOrder.items.isEmpty()) {
            return Result.Error(
                message = "Purchase order must have at least one item",
                code = "EMPTY_ITEMS",
            )
        }

        return purchaseOrderRepository.update(purchaseOrder)
    }
}
