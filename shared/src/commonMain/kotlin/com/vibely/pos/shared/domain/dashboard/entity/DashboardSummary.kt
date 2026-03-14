package com.vibely.pos.shared.domain.dashboard.entity

import com.vibely.pos.shared.domain.valueobject.Money
import kotlin.time.Instant

/**
 * Domain entity representing a summary of today's dashboard metrics.
 *
 * Provides an aggregated view of key business metrics for the current day,
 * including sales performance, transaction counts, inventory alerts, and shift status.
 *
 * @param todaySales Total sales amount for today (all completed sales).
 * @param todayTransactionCount Number of completed transactions today.
 * @param lowStockCount Number of products below their reorder level.
 * @param activeShift Currently open cash shift (null if no shift is open).
 * @param generatedAt Timestamp when this summary was generated.
 */
data class DashboardSummary(
    val todaySales: Money,
    val todayTransactionCount: Int,
    val lowStockCount: Int,
    val activeShift: ActiveShiftInfo?,
    val generatedAt: Instant,
) {
    /**
     * Returns true if there is an active (open) cash shift.
     */
    fun hasActiveShift(): Boolean = activeShift != null

    /**
     * Returns true if there are products requiring stock attention.
     */
    fun hasLowStockAlerts(): Boolean = lowStockCount > 0

    companion object {
        /**
         * Creates a DashboardSummary with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            todaySales: Money,
            todayTransactionCount: Int,
            lowStockCount: Int,
            activeShift: ActiveShiftInfo?,
            generatedAt: Instant,
        ): DashboardSummary {
            require(todayTransactionCount >= 0) { "Transaction count cannot be negative" }
            require(lowStockCount >= 0) { "Low stock count cannot be negative" }

            return DashboardSummary(
                todaySales = todaySales,
                todayTransactionCount = todayTransactionCount,
                lowStockCount = lowStockCount,
                activeShift = activeShift,
                generatedAt = generatedAt,
            )
        }
    }
}

/**
 * Information about the currently active cash shift.
 *
 * @param shiftId Unique identifier of the shift.
 * @param cashierId ID of the cashier operating the shift.
 * @param cashierName Name of the cashier for display.
 * @param openedAt When the shift was opened.
 * @param openingBalance Starting cash balance for the shift.
 */
data class ActiveShiftInfo(val shiftId: String, val cashierId: String, val cashierName: String, val openedAt: Instant, val openingBalance: Money)
