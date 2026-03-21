package com.vibely.pos.backend.data.room.datasource

import com.vibely.pos.backend.data.datasource.PaymentBackendDataSource
import com.vibely.pos.backend.data.room.dao.PaymentDao
import com.vibely.pos.backend.data.room.dao.SaleDao
import com.vibely.pos.backend.data.room.mapper.toDto
import com.vibely.pos.backend.data.room.mapper.toEntity
import com.vibely.pos.shared.data.sales.dto.CreatePaymentRequest
import com.vibely.pos.shared.data.sales.dto.PaymentDTO
import com.vibely.pos.shared.domain.result.Result

private const val PAYMENT_STATUS_COMPLETED = "completed"
private const val PAYMENT_STATUS_PENDING = "pending"
private const val ERROR_NOT_FOUND = "Sale not found"

/**
 * Room/SQLite-backed implementation of [PaymentBackendDataSource].
 *
 * Sale payment status is updated in the same local sales table after each payment is recorded.
 */
class RoomPaymentDataSource(
    private val paymentDao: PaymentDao,
    private val saleDao: SaleDao,
) : PaymentBackendDataSource {
    /**
     * Records a payment for a sale and updates the sale's payment status.
     *
     * @param request Payment request details
     * @return Result containing the created payment or error
     */
    override suspend fun recordPayment(request: CreatePaymentRequest): Result<PaymentDTO> =
        runCatchingSuspend {
            val entity = request.toEntity(PAYMENT_STATUS_COMPLETED)
            paymentDao.insert(entity)
            updateSalePaymentStatus(request.saleId)
            entity.toDto()
        }

    /**
     * Calculates the total amount paid for a sale from the local payments table.
     *
     * @param saleId ID of the sale
     * @return Total amount of completed payments, or 0.0 if none exist
     */
    override suspend fun calculateTotalPaid(saleId: String): Double =
        paymentDao.sumCompletedPayments(saleId, PAYMENT_STATUS_COMPLETED)

    /**
     * Gets all payments for a sale.
     *
     * @param saleId ID of the sale
     * @return Result containing list of payments or error
     */
    override suspend fun getPaymentsBySale(saleId: String): Result<List<PaymentDTO>> =
        runCatchingSuspend { paymentDao.getBySaleId(saleId).map { it.toDto() } }

    /**
     * Updates the sale's payment status based on total payments received.
     *
     * If total paid >= sale total amount, sets payment_status to 'completed'.
     *
     * @param saleId ID of the sale
     * @return Result indicating success or error
     */
    override suspend fun updateSalePaymentStatus(saleId: String): Result<Unit> =
        runCatchingSuspend {
            val sale = saleDao.getById(saleId) ?: error(ERROR_NOT_FOUND)
            val totalPaid = calculateTotalPaid(saleId)
            val newStatus = if (totalPaid >= sale.totalAmount) {
                PAYMENT_STATUS_COMPLETED
            } else {
                PAYMENT_STATUS_PENDING
            }
            saleDao.updateSale(sale.copy(paymentStatus = newStatus))
        }
}
