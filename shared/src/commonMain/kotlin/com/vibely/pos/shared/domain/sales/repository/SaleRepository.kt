package com.vibely.pos.shared.domain.sales.repository

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.entity.SaleItem
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlin.time.Instant

interface SaleRepository {
    suspend fun create(sale: Sale, items: List<SaleItem>): Result<Sale>

    suspend fun getAll(
        startDate: Instant? = null,
        endDate: Instant? = null,
        status: SaleStatus? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<Sale>>

    suspend fun getById(id: String): Result<Sale>

    suspend fun getItems(saleId: String): Result<List<SaleItem>>

    suspend fun updateStatus(saleId: String, status: SaleStatus): Result<Sale>
}
