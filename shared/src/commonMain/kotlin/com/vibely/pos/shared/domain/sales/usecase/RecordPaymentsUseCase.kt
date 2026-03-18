package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Payment
import com.vibely.pos.shared.domain.sales.repository.PaymentRepository
import com.vibely.pos.shared.domain.sales.valueobject.PaymentInfo

class RecordPaymentsUseCase(private val paymentRepository: PaymentRepository) {
    suspend operator fun invoke(saleId: String, payments: List<PaymentInfo>): Result<List<Payment>> {
        if (saleId.isBlank()) {
            return Result.Error("Sale ID cannot be blank")
        }

        if (payments.isEmpty()) {
            return Result.Error("Payment list cannot be empty")
        }

        val recordedPayments = mutableListOf<Payment>()

        for (paymentInfo in payments) {
            val result =
                paymentRepository.recordPayment(
                    saleId = saleId,
                    amount = paymentInfo.amount,
                    paymentType = paymentInfo.type,
                    referenceNumber = paymentInfo.reference.ifBlank { null },
                )

            when (result) {
                is Result.Error -> return Result.Error(
                    "Failed to record ${paymentInfo.type}: ${result.message}",
                )

                is Result.Success -> recordedPayments.add(result.data)
            }
        }

        return Result.Success(recordedPayments)
    }
}
