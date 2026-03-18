package com.vibely.pos.shared.domain.sales.repository

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Payment
import com.vibely.pos.shared.domain.sales.valueobject.PaymentType

interface PaymentRepository {
    suspend fun recordPayment(
        saleId: String,
        amount: Double,
        paymentType: PaymentType,
        referenceNumber: String? = null,
        notes: String? = null,
    ): Result<Payment>

    suspend fun getPaymentsBySale(saleId: String): Result<List<Payment>>
}
