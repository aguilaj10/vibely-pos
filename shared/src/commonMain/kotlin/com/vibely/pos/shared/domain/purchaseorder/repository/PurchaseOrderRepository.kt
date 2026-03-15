package com.vibely.pos.shared.domain.purchaseorder.repository

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.domain.result.Result

interface PurchaseOrderRepository {

    suspend fun getAll(
        supplierId: String? = null,
        status: PurchaseOrderStatus? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<PurchaseOrder>>

    suspend fun getById(id: String): Result<PurchaseOrder>

    suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder>

    suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder>

    suspend fun updateStatus(id: String, status: PurchaseOrderStatus): Result<PurchaseOrder>

    suspend fun delete(id: String): Result<Unit>

    suspend fun receive(id: String): Result<PurchaseOrder>

    suspend fun generatePoNumber(): Result<String>
}
