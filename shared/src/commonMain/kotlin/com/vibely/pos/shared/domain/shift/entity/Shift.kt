package com.vibely.pos.shared.domain.shift.entity

import com.vibely.pos.shared.domain.shift.valueobject.ShiftStatus
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

data class Shift(
    val id: String,
    val shiftNumber: String,
    val cashierId: String,
    val cashierName: String?,
    val openingBalance: Double,
    val closingBalance: Double?,
    val expectedBalance: Double?,
    val discrepancy: Double?,
    val totalSales: Double,
    val totalCash: Double,
    val totalCard: Double,
    val totalOther: Double,
    val openedAt: Instant,
    val closedAt: Instant?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "Shift ID cannot be blank" }
        require(shiftNumber.isNotBlank()) { "Shift number cannot be blank" }
        require(cashierId.isNotBlank()) { "Cashier ID cannot be blank" }
        require(openingBalance >= 0) { "Opening balance cannot be negative" }
        require(totalSales >= 0) { "Total sales cannot be negative" }
        require(totalCash >= 0) { "Total cash cannot be negative" }
        require(totalCard >= 0) { "Total card cannot be negative" }
        require(totalOther >= 0) { "Total other cannot be negative" }
    }

    val status: ShiftStatus
        get() = if (closedAt == null) ShiftStatus.OPEN else ShiftStatus.CLOSED

    val isOpen: Boolean
        get() = status == ShiftStatus.OPEN

    val isClosed: Boolean
        get() = status == ShiftStatus.CLOSED

    val totalPayments: Double
        get() = totalCash + totalCard + totalOther

    val calculatedExpectedBalance: Double
        get() = openingBalance + totalCash

    fun hasDiscrepancy(): Boolean = discrepancy != null && abs(discrepancy) > DISCREPANCY_THRESHOLD

    fun close(actualClosingBalance: Double, closeNotes: String? = null): Shift {
        val expected = calculatedExpectedBalance
        val variance = actualClosingBalance - expected

        return copy(
            closingBalance = actualClosingBalance,
            expectedBalance = expected,
            discrepancy = variance,
            closedAt = Clock.System.now(),
            notes = closeNotes ?: notes,
            updatedAt = Clock.System.now(),
        )
    }

    companion object {
        private const val DISCREPANCY_THRESHOLD = 0.01

        fun create(
            id: String,
            shiftNumber: String,
            cashierId: String,
            openingBalance: Double,
            cashierName: String? = null,
            closingBalance: Double? = null,
            expectedBalance: Double? = null,
            discrepancy: Double? = null,
            totalSales: Double = 0.0,
            totalCash: Double = 0.0,
            totalCard: Double = 0.0,
            totalOther: Double = 0.0,
            openedAt: Instant = Clock.System.now(),
            closedAt: Instant? = null,
            notes: String? = null,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): Shift = Shift(
            id = id,
            shiftNumber = shiftNumber,
            cashierId = cashierId,
            cashierName = cashierName,
            openingBalance = openingBalance,
            closingBalance = closingBalance,
            expectedBalance = expectedBalance,
            discrepancy = discrepancy,
            totalSales = totalSales,
            totalCash = totalCash,
            totalCard = totalCard,
            totalOther = totalOther,
            openedAt = openedAt,
            closedAt = closedAt,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
