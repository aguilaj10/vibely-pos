package com.vibely.pos.shared.domain.reports.entity

/**
 * Domain entity representing a sales report summary.
 *
 * @param totalRevenue Total revenue in cents.
 * @param totalCost Total cost of goods sold in cents.
 * @param totalProfit Total profit in cents.
 * @param transactionCount Total number of transactions.
 * @param averageTransactionValue Average transaction value in cents.
 */
data class SalesReport(
    val totalRevenue: Long,
    val totalCost: Long,
    val totalProfit: Long,
    val transactionCount: Int,
    val averageTransactionValue: Long,
)
