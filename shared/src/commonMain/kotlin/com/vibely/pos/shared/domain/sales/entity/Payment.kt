package com.vibely.pos.shared.domain.sales.entity

import com.vibely.pos.shared.domain.sales.valueobject.PaymentStatus
import com.vibely.pos.shared.domain.sales.valueobject.PaymentType
import kotlin.time.Clock
import kotlin.time.Instant

data class Payment(
    val id: String,
    val saleId: String,
    val amount: Double,
    val paymentType: PaymentType,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val paymentDate: Instant = Clock.System.now(),
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
) {
    init {
        require(id.isNotBlank()) { "Payment ID cannot be blank" }
        require(saleId.isNotBlank()) { "Sale ID cannot be blank" }
        require(amount > 0) { "Amount must be positive" }
    }

    companion object {
        fun create(
            id: String,
            saleId: String,
            amount: Double,
            paymentType: PaymentType,
            status: PaymentStatus = PaymentStatus.PENDING,
            referenceNumber: String? = null,
            notes: String? = null,
            paymentDate: Instant = Clock.System.now(),
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): Payment = Payment(
            id = id,
            saleId = saleId,
            amount = amount,
            paymentType = paymentType,
            status = status,
            referenceNumber = referenceNumber,
            notes = notes,
            paymentDate = paymentDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
