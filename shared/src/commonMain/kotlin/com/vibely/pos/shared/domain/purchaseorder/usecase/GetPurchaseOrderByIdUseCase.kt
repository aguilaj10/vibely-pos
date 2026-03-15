package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.result.Result

class GetPurchaseOrderByIdUseCase(private val purchaseOrderRepository: PurchaseOrderRepository) {

    suspend operator fun invoke(id: String): Result<PurchaseOrder> = purchaseOrderRepository.getById(id)
}
