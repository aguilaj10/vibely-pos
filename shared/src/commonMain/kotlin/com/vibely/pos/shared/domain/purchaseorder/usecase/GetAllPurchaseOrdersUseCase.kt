package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.domain.result.Result

class GetAllPurchaseOrdersUseCase(private val purchaseOrderRepository: PurchaseOrderRepository) {

    suspend operator fun invoke(
        supplierId: String? = null,
        status: PurchaseOrderStatus? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<PurchaseOrder>> = purchaseOrderRepository.getAll(supplierId, status, page, pageSize)
}
