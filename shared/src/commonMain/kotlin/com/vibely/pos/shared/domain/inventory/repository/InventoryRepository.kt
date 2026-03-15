package com.vibely.pos.shared.domain.inventory.repository

import com.vibely.pos.shared.domain.inventory.entity.InventoryTransaction
import com.vibely.pos.shared.domain.inventory.entity.TransactionType
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Instant

interface InventoryRepository {
    suspend fun getAll(
        productId: String? = null,
        transactionType: TransactionType? = null,
        startDate: Instant? = null,
        endDate: Instant? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<InventoryTransaction>>

    suspend fun getById(id: String): Result<InventoryTransaction>

    suspend fun create(transaction: InventoryTransaction): Result<InventoryTransaction>

    suspend fun getByProduct(productId: String, page: Int = 1, pageSize: Int = 50): Result<List<InventoryTransaction>>
}
