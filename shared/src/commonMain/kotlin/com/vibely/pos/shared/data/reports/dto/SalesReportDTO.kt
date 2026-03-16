package com.vibely.pos.shared.data.reports.dto

import kotlinx.serialization.Serializable

/**
 * Data transfer object for sales report.
 *
 * Represents aggregated sales data for a specific period.
 *
 * @property totalRevenue Total revenue in cents.
 * @property totalCost Total cost of goods sold in cents.
 * @property totalProfit Total profit in cents.
 * @property transactionCount Total number of transactions.
 * @property averageTransactionValue Average transaction value in cents.
 */
@Serializable
data class SalesReportDTO(
    val totalRevenue: Long,
    val totalCost: Long,
    val totalProfit: Long,
    val transactionCount: Int,
    val averageTransactionValue: Long,
)
