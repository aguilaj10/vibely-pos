package com.vibely.pos.shared.domain.sales.entity

import com.vibely.pos.shared.domain.sales.valueobject.PaymentStatus
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlin.time.Clock
import kotlin.time.Instant

data class Sale(
    val id: String,
    val invoiceNumber: String,
    val customerId: String?,
    val cashierId: String,
    val subtotal: Double,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val status: SaleStatus = SaleStatus.DRAFT,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val notes: String?,
    val saleDate: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "Sale ID cannot be blank" }
        require(cashierId.isNotBlank()) { "Cashier ID cannot be blank" }
        require(subtotal >= 0) { "Subtotal cannot be negative" }
        require(taxAmount >= 0) { "Tax amount cannot be negative" }
        require(discountAmount >= 0) { "Discount amount cannot be negative" }
        require(totalAmount >= 0) { "Total amount cannot be negative" }
    }

    fun canModify(): Boolean = status.canModify()

    fun canCancel(): Boolean = status.canCancel()

    fun canRefund(): Boolean = status.canRefund()

    fun withStatus(newStatus: SaleStatus): Sale = copy(
        status = newStatus,
        updatedAt = Clock.System.now(),
    )

    fun withPaymentStatus(newPaymentStatus: PaymentStatus): Sale = copy(
        paymentStatus = newPaymentStatus,
        updatedAt = Clock.System.now(),
    )

    companion object {
        fun create(
            id: String,
            invoiceNumber: String,
            cashierId: String,
            subtotal: Double,
            totalAmount: Double,
            customerId: String? = null,
            taxAmount: Double = 0.0,
            discountAmount: Double = 0.0,
            status: SaleStatus = SaleStatus.DRAFT,
            paymentStatus: PaymentStatus = PaymentStatus.PENDING,
            notes: String? = null,
            saleDate: Instant = Clock.System.now(),
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): Sale = Sale(
            id = id,
            invoiceNumber = invoiceNumber,
            customerId = customerId,
            cashierId = cashierId,
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            status = status,
            paymentStatus = paymentStatus,
            notes = notes,
            saleDate = saleDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
