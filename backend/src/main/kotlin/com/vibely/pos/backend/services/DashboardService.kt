package com.vibely.pos.backend.services

import com.vibely.pos.shared.data.dashboard.dto.ActiveShiftInfoDTO
import com.vibely.pos.shared.data.dashboard.dto.DashboardSummaryDTO
import com.vibely.pos.shared.data.dashboard.dto.LowStockProductDTO
import com.vibely.pos.shared.data.dashboard.dto.RecentTransactionDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlinx.serialization.SerializationException

// Database table names
private const val TABLE_SALES = "sales"
private const val TABLE_PRODUCTS = "products"
private const val TABLE_CASH_SHIFTS = "cash_shifts"

// Database column names
private const val COLUMN_STATUS = "status"
private const val COLUMN_SALE_DATE = "sale_date"
private const val COLUMN_CURRENT_STOCK = "current_stock"
private const val COLUMN_MIN_STOCK_LEVEL = "min_stock_level"
private const val COLUMN_CLOSED_AT = "closed_at"

// Status values
private const val STATUS_COMPLETED = "completed"
private const val CURRENT_DATE = "CURRENT_DATE"
private const val DEFAULT_TRANSACTIONS_LIMIT = 10

/**
 * Service for handling dashboard data operations.
 *
 * Responsibilities:
 * - Aggregate today's sales metrics
 * - Fetch recent transactions
 * - Retrieve low stock products
 * - Get active shift information
 */
class DashboardService(
    private val supabaseClient: SupabaseClient,
) {
    /**
     * Gets the dashboard summary containing today's sales, transaction count,
     * low stock count, and active shift information.
     *
     * @return [DashboardSummaryDTO] with aggregated metrics.
     */
    suspend fun getDashboardSummary(): DashboardSummaryDTO {
        val now = Clock.System.now().toString()
        val todaySalesCents = fetchTodaySalesTotal()
        val todayTransactionCount = fetchTodayTransactionCount()
        val lowStockCount = fetchLowStockCount()
        val activeShift = fetchActiveShift()

        return DashboardSummaryDTO(
            todaySalesCents = todaySalesCents,
            todayTransactionCount = todayTransactionCount,
            lowStockCount = lowStockCount,
            activeShift = activeShift,
            generatedAt = now,
        )
    }

    /**
     * Fetches the total sales amount for today.
     */
    private suspend fun fetchTodaySalesTotal(): Long {
        val salesResult = supabaseClient.from(TABLE_SALES)
            .select(columns = Columns.list("total")) {
                filter {
                    eq(COLUMN_STATUS, STATUS_COMPLETED)
                    gte(COLUMN_SALE_DATE, CURRENT_DATE)
                }
            }
            .decodeList<SaleTotalRow>()

        return salesResult.sumOf { it.total }
    }

    /**
     * Fetches the count of completed transactions for today.
     */
    private suspend fun fetchTodayTransactionCount(): Int {
        val transactions = supabaseClient.from(TABLE_SALES)
            .select(columns = Columns.list("id")) {
                filter {
                    eq(COLUMN_STATUS, STATUS_COMPLETED)
                    gte(COLUMN_SALE_DATE, CURRENT_DATE)
                }
            }
            .decodeList<SaleTotalRow>()

        return transactions.size
    }

    /**
     * Fetches the count of products below minimum stock level.
     */
    private suspend fun fetchLowStockCount(): Int {
        val products = supabaseClient.from(TABLE_PRODUCTS)
            .select(columns = Columns.list("id")) {
                filter {
                    lt(COLUMN_CURRENT_STOCK, COLUMN_MIN_STOCK_LEVEL)
                }
            }
            .decodeList<SaleTotalRow>()

        return products.size
    }

    /**
     * Fetches the currently active shift information.
     * Returns null if no shift is currently open.
     */
    private suspend fun fetchActiveShift(): ActiveShiftInfoDTO? {
        val activeShiftColumns = Columns.list(
            "shift_id",
            "cashier_id",
            "cashier_name",
            "opened_at",
            "opening_balance_cents",
        )

        // Query for active shift (WHERE closed_at IS NULL)
        val activeShiftResult = try {
            supabaseClient.from(TABLE_CASH_SHIFTS)
                .select(columns = activeShiftColumns) {
                    filter {
                        // Check for NULL closed_at using Postgrest filter syntax
                        exact(COLUMN_CLOSED_AT, null)
                    }
                    limit(1)
                }
                .decodeSingle<ActiveShiftRow>()
        } catch (_: RestException) {
            return null
        } catch (_: SerializationException) {
            return null
        }

        return ActiveShiftInfoDTO(
            shiftId = activeShiftResult.shiftId,
            cashierId = activeShiftResult.cashierId,
            cashierName = activeShiftResult.cashierName,
            openedAt = activeShiftResult.openedAt,
            openingBalanceCents = activeShiftResult.openingBalanceCents,
        )
    }

    /**
     * Gets recent transactions ordered by sale date descending.
     *
     * @param limit Maximum number of transactions to retrieve (default: 10).
     * @return List of [RecentTransactionDTO].
     */
    suspend fun getRecentTransactions(limit: Int = DEFAULT_TRANSACTIONS_LIMIT): List<RecentTransactionDTO> {
        val transactions = supabaseClient.from(TABLE_SALES)
            .select(
                columns = Columns.list(
                    "id",
                    "invoice_number",
                    "total",
                    "status",
                    "sale_date",
                    "customer_name",
                )
            ) {
                order(column = COLUMN_SALE_DATE, order = Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<RecentTransactionRow>()

        return transactions.map { row ->
            RecentTransactionDTO(
                id = row.id,
                invoiceNumber = row.invoiceNumber,
                totalCents = row.total,
                status = row.status,
                saleDate = row.saleDate,
                customerName = row.customerName,
            )
        }
    }

    /**
     * Gets products with stock levels below their minimum threshold.
     *
     * @return List of [LowStockProductDTO].
     */
    suspend fun getLowStockProducts(): List<LowStockProductDTO> {
        val products = supabaseClient.from(TABLE_PRODUCTS)
            .select(
                columns = Columns.list(
                    "id",
                    "sku",
                    "name",
                    "current_stock",
                    "min_stock_level",
                    "selling_price_cents",
                    "category_name",
                )
            ) {
                filter {
                    lt(COLUMN_CURRENT_STOCK, COLUMN_MIN_STOCK_LEVEL)
                }
                order(column = COLUMN_CURRENT_STOCK, order = Order.ASCENDING)
            }
            .decodeList<LowStockProductRow>()

        return products.map { row ->
            LowStockProductDTO(
                id = row.id,
                sku = row.sku,
                name = row.name,
                currentStock = row.currentStock,
                minStockLevel = row.minStockLevel,
                sellingPriceCents = row.sellingPriceCents,
                categoryName = row.categoryName,
            )
        }
    }

    // Internal data classes for database rows

    @Serializable
    private data class SaleTotalRow(
        @SerialName("total") val total: Long,
    )

    @Serializable
    private data class ActiveShiftRow(
        @SerialName("shift_id") val shiftId: String,
        @SerialName("cashier_id") val cashierId: String,
        @SerialName("cashier_name") val cashierName: String,
        @SerialName("opened_at") val openedAt: String,
        @SerialName("opening_balance_cents") val openingBalanceCents: Long,
    )

    @Serializable
    private data class RecentTransactionRow(
        @SerialName("id") val id: String,
        @SerialName("invoice_number") val invoiceNumber: String,
        @SerialName("total") val total: Long,
        @SerialName("status") val status: String,
        @SerialName("sale_date") val saleDate: String,
        @SerialName("customer_name") val customerName: String?,
    )

    @Serializable
    private data class LowStockProductRow(
        @SerialName("id") val id: String,
        @SerialName("sku") val sku: String,
        @SerialName("name") val name: String,
        @SerialName("current_stock") val currentStock: Int,
        @SerialName("min_stock_level") val minStockLevel: Int,
        @SerialName("selling_price_cents") val sellingPriceCents: Long,
        @SerialName("category_name") val categoryName: String?,
    )
}
