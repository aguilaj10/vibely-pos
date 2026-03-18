package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.shared.data.sales.dto.CreatePaymentRequest
import com.vibely.pos.shared.data.sales.dto.PaymentDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ERROR_RECORD_PAYMENT = "Failed to record payment"
private const val ERROR_GET_PAYMENTS = "Failed to fetch payments"
private const val ERROR_UPDATE_STATUS = "Failed to update sale payment status"
private const val PAYMENT_STATUS_COMPLETED = "completed"
private const val PAYMENT_STATUS_PENDING = "pending"

@Serializable
private data class SaleTotalRow(
    @SerialName("total_amount")
    val totalAmount: Double,
)

/**
 * Service for managing payment operations.
 */
class PaymentService(
    private val supabaseClient: SupabaseClient,
) : BaseService() {
    /**
     * Records a payment for a sale.
     *
     * @param request Payment request details
     * @return Result containing the created payment or error
     */
    suspend fun recordPayment(request: CreatePaymentRequest): Result<PaymentDTO> =
        executeQuery(ERROR_RECORD_PAYMENT) {
            val paymentData =
                buildJsonObject {
                    put(DatabaseColumns.SALE_ID, request.saleId)
                    put(DatabaseColumns.AMOUNT, request.amount)
                    put(DatabaseColumns.PAYMENT_TYPE, request.paymentType)
                    put(DatabaseColumns.STATUS, PAYMENT_STATUS_COMPLETED)
                    request.referenceNumber?.let { put(DatabaseColumns.REFERENCE_NUMBER, it) }
                    request.notes?.let { put(DatabaseColumns.NOTES, it) }
                }

            val result =
                supabaseClient
                    .from(TableNames.PAYMENTS)
                    .insert(paymentData) { select() }
                    .decodeSingle<PaymentDTO>()

            updateSalePaymentStatus(request.saleId)

            result
        }

    /**
     * Calculates the total amount paid for a sale.
     *
     * @param saleId ID of the sale
     * @return Total amount paid
     */
    suspend fun calculateTotalPaid(saleId: String): Double =
        try {
            val result =
                supabaseClient
                    .from(TableNames.PAYMENTS)
                    .select {
                        filter {
                            eq(DatabaseColumns.SALE_ID, saleId)
                            eq(DatabaseColumns.STATUS, PAYMENT_STATUS_COMPLETED)
                        }
                    }.decodeList<PaymentDTO>()

            result.sumOf { it.amount }
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            0.0
        } catch (e: kotlinx.serialization.SerializationException) {
            0.0
        }

    /**
     * Gets all payments for a sale.
     *
     * @param saleId ID of the sale
     * @return Result containing list of payments or error
     */
    suspend fun getPaymentsBySale(saleId: String): Result<List<PaymentDTO>> =
        executeQuery(ERROR_GET_PAYMENTS) {
            supabaseClient
                .from(TableNames.PAYMENTS)
                .select {
                    filter {
                        eq(DatabaseColumns.SALE_ID, saleId)
                    }
                    order(DatabaseColumns.CREATED_AT, Order.ASCENDING)
                }.decodeList<PaymentDTO>()
        }

    /**
     * Updates the sale's payment status based on total payments.
     * If total paid >= sale total amount, sets payment_status to 'completed'.
     *
     * @param saleId ID of the sale
     * @return Result containing the updated sale or error
     */
    suspend fun updateSalePaymentStatus(saleId: String): Result<Unit> =
        executeQuery(ERROR_UPDATE_STATUS) {
            val saleRow =
                supabaseClient
                    .from(TableNames.SALES)
                    .select(columns = Columns.list(DatabaseColumns.TOTAL_AMOUNT)) {
                        filter {
                            eq(DatabaseColumns.ID, saleId)
                        }
                    }.decodeSingle<SaleTotalRow>()

            val totalAmount = saleRow.totalAmount
            val totalPaid = calculateTotalPaid(saleId)

            val newPaymentStatus =
                if (totalPaid >= totalAmount) {
                    PAYMENT_STATUS_COMPLETED
                } else {
                    PAYMENT_STATUS_PENDING
                }

            supabaseClient
                .from(TableNames.SALES)
                .update(mapOf(DatabaseColumns.PAYMENT_STATUS to newPaymentStatus)) {
                    filter {
                        eq(DatabaseColumns.ID, saleId)
                    }
                }
        }
}
