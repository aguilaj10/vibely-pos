package com.vibely.pos.shared.data.dashboard.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for dashboard summary from the backend.
 *
 * Contains aggregated metrics for today's sales, transactions, stock alerts,
 * and active shift information.
 */
@Serializable
data class DashboardSummaryDTO(
    @SerialName("today_sales_cents")
    val todaySalesCents: Long,

    @SerialName("today_transaction_count")
    val todayTransactionCount: Int,

    @SerialName("low_stock_count")
    val lowStockCount: Int,

    @SerialName("active_shift")
    val activeShift: ActiveShiftInfoDTO?,

    @SerialName("generated_at")
    val generatedAt: String,
)

/**
 * Data Transfer Object for active shift information.
 *
 * Represents the currently open cash shift.
 */
@Serializable
data class ActiveShiftInfoDTO(
    @SerialName("shift_id")
    val shiftId: String,

    @SerialName("cashier_id")
    val cashierId: String,

    @SerialName("cashier_name")
    val cashierName: String,

    @SerialName("opened_at")
    val openedAt: String,

    @SerialName("opening_balance_cents")
    val openingBalanceCents: Long,
)
