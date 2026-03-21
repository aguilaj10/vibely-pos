package com.vibely.pos.backend.services

import com.vibely.pos.backend.data.datasource.PaymentBackendDataSource
import com.vibely.pos.shared.data.sales.dto.CreatePaymentRequest
import com.vibely.pos.shared.data.sales.dto.PaymentDTO
import com.vibely.pos.shared.domain.result.Result

/**
 * Service for managing payment operations.
 *
 * Delegates all persistence to [PaymentBackendDataSource], which is either Supabase or
 * Room/SQLite depending on the active [com.vibely.pos.backend.data.DatabaseStrategy].
 */
class PaymentService(
    private val dataSource: PaymentBackendDataSource,
) : BaseService() {
    /**
     * Records a payment for a sale.
     *
     * @param request Payment request details
     * @return Result containing the created payment or error
     */
    suspend fun recordPayment(request: CreatePaymentRequest): Result<PaymentDTO> =
        dataSource.recordPayment(request)

    /**
     * Calculates the total amount paid for a sale.
     *
     * @param saleId ID of the sale
     * @return Total amount paid
     */
    suspend fun calculateTotalPaid(saleId: String): Double = dataSource.calculateTotalPaid(saleId)

    /**
     * Gets all payments for a sale.
     *
     * @param saleId ID of the sale
     * @return Result containing list of payments or error
     */
    suspend fun getPaymentsBySale(saleId: String): Result<List<PaymentDTO>> =
        dataSource.getPaymentsBySale(saleId)

    /**
     * Updates the sale's payment status based on total payments.
     * If total paid >= sale total amount, sets payment_status to 'completed'.
     *
     * @param saleId ID of the sale
     * @return Result containing the updated sale or error
     */
    suspend fun updateSalePaymentStatus(saleId: String): Result<Unit> =
        dataSource.updateSalePaymentStatus(saleId)
}
