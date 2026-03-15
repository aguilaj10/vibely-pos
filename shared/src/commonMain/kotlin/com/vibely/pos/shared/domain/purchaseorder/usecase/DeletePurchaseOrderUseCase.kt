package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.result.Result

class DeletePurchaseOrderUseCase(private val purchaseOrderRepository: PurchaseOrderRepository) {

    suspend operator fun invoke(id: String): Result<Unit> {
        val existingResult = purchaseOrderRepository.getById(id)

        return when (existingResult) {
            is Result.Error -> existingResult
            is Result.Success -> {
                val existing = existingResult.data

                if (!existing.canModify()) {
                    Result.Error(
                        message = "Purchase order cannot be deleted in status: ${existing.status}",
                        code = "CANNOT_DELETE",
                    )
                } else {
                    purchaseOrderRepository.delete(id)
                }
            }
        }
    }
}
