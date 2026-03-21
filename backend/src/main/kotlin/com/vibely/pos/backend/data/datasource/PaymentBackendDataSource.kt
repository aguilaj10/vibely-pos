package com.vibely.pos.backend.data.datasource

import com.vibely.pos.shared.data.sales.dto.CreatePaymentRequest
import com.vibely.pos.shared.data.sales.dto.PaymentDTO
import com.vibely.pos.shared.domain.result.Result

/**
 * Data source abstraction for payment persistence operations.
 *
 * Implementations provide either Supabase (remote) or Room/SQLite (local) backends.
 */
interface PaymentBackendDataSource {
    /**
     * Records a payment for a sale.
     *
     * @param request Payment request details
     * @return Result containing the created payment or error
     */
    suspend fun recordPayment(request: CreatePaymentRequest): Result<PaymentDTO>

    /**
     * Calculates the total amount paid for a sale.
     *
     * @param saleId ID of the sale
     * @return Total amount paid across all completed payments
     */
    suspend fun calculateTotalPaid(saleId: String): Double

    /**
     * Gets all payments for a sale.
     *
     * @param saleId ID of the sale
     * @return Result containing list of payments or error
     */
    suspend fun getPaymentsBySale(saleId: String): Result<List<PaymentDTO>>

    /**
     * Updates the sale's payment status based on total payments received.
     *
     * If total paid >= sale total amount, sets payment_status to 'completed'.
     *
     * @param saleId ID of the sale
     * @return Result indicating success or error
     */
    suspend fun updateSalePaymentStatus(saleId: String): Result<Unit>
}
