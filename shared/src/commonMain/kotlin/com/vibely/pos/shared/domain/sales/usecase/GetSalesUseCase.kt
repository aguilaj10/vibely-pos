package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlin.time.Instant

/**
 * Use case for retrieving sales with optional filters.
 *
 * Fetches sales from the repository with support for:
 * - Date range filtering (startDate, endDate)
 * - Status filtering
 * - Pagination
 *
 * @param saleRepository Repository for accessing sale data.
 */
class GetSalesUseCase(private val saleRepository: SaleRepository) {
    suspend operator fun invoke(
        startDate: Instant? = null,
        endDate: Instant? = null,
        status: SaleStatus? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<Sale>> = saleRepository.getAll(
        startDate = startDate,
        endDate = endDate,
        status = status,
        page = page,
        pageSize = pageSize,
    )
}
