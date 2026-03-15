package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.domain.result.Result

class UpdatePurchaseOrderStatusUseCase(private val purchaseOrderRepository: PurchaseOrderRepository) {

    suspend operator fun invoke(id: String, newStatus: PurchaseOrderStatus): Result<PurchaseOrder> {
        val existingResult = purchaseOrderRepository.getById(id)

        return when (existingResult) {
            is Result.Error -> existingResult
            is Result.Success -> {
                val existing = existingResult.data
                val validationError = validateStatusTransition(existing, newStatus)

                if (validationError != null) {
                    validationError
                } else {
                    purchaseOrderRepository.updateStatus(id, newStatus)
                }
            }
        }
    }

    private fun validateStatusTransition(purchaseOrder: PurchaseOrder, newStatus: PurchaseOrderStatus): Result.Error? = when (newStatus) {
        PurchaseOrderStatus.APPROVED -> {
            if (!purchaseOrder.canApprove()) {
                Result.Error(
                    message = "Purchase order cannot be approved from status: ${purchaseOrder.status}",
                    code = "INVALID_STATUS_TRANSITION",
                )
            } else {
                null
            }
        }
        PurchaseOrderStatus.RECEIVED -> {
            if (!purchaseOrder.canReceive()) {
                Result.Error(
                    message = "Purchase order cannot be received from status: ${purchaseOrder.status}",
                    code = "INVALID_STATUS_TRANSITION",
                )
            } else {
                null
            }
        }
        PurchaseOrderStatus.CANCELLED -> {
            if (!purchaseOrder.canCancel()) {
                Result.Error(
                    message = "Purchase order cannot be cancelled from status: ${purchaseOrder.status}",
                    code = "INVALID_STATUS_TRANSITION",
                )
            } else {
                null
            }
        }
        else -> null
    }
}
