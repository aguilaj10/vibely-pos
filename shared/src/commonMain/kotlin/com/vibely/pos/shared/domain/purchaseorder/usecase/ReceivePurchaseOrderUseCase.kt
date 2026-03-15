package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.result.Result

class ReceivePurchaseOrderUseCase(private val purchaseOrderRepository: PurchaseOrderRepository) {

    suspend operator fun invoke(id: String): Result<PurchaseOrder> {
        val existingResult = purchaseOrderRepository.getById(id)

        return when (existingResult) {
            is Result.Error -> existingResult
            is Result.Success -> {
                val existing = existingResult.data

                if (!existing.canReceive()) {
                    Result.Error(
                        message = "Purchase order cannot be received from status: ${existing.status}",
                        code = "INVALID_STATUS_TRANSITION",
                    )
                } else {
                    purchaseOrderRepository.receive(id)
                }
            }
        }
    }
}
