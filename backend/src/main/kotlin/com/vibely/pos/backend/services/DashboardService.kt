package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.shared.data.dashboard.dto.ActiveShiftInfoDTO
import com.vibely.pos.shared.data.dashboard.dto.DashboardSummaryDTO
import com.vibely.pos.shared.data.dashboard.dto.LowStockProductDTO
import com.vibely.pos.shared.data.dashboard.dto.RecentTransactionDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlinx.serialization.SerializationException
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// Database table names
private const val TABLE_SALES = "sales"
private const val TABLE_CASH_SHIFTS = "cash_shifts"
private const val VIEW_LOW_STOCK_PRODUCTS = "low_stock_products"

// Status values
private const val STATUS_COMPLETED = "completed"
private const val DEFAULT_TRANSACTIONS_LIMIT = 10
private const val CENTS_PER_DOLLAR = 100

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
) : BaseService() {
    /**
     * Gets the dashboard summary containing today's sales, transaction count,
     * low stock count, and active shift information.
     *
     * @return Result containing [DashboardSummaryDTO] with aggregated metrics or error.
     */
    suspend fun getDashboardSummary(): Result<DashboardSummaryDTO> {
        return executeQuery("Failed to fetch dashboard summary") {
            val now = Clock.System.now().toString()
            val todaySalesCents = fetchTodaySalesTotal()
            val todayTransactionCount = fetchTodayTransactionCount()
            val lowStockCount = fetchLowStockCount()
            val activeShift = fetchActiveShift()

            DashboardSummaryDTO(
                todaySalesCents = todaySalesCents,
                todayTransactionCount = todayTransactionCount,
                lowStockCount = lowStockCount,
                activeShift = activeShift,
                generatedAt = now,
            )
        }
    }

    /**
     * Fetches the total sales amount for today.
     */
    private suspend fun fetchTodaySalesTotal(): Long {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val salesResult = supabaseClient.from(TABLE_SALES)
            .select(columns = Columns.list("total_amount")) {
                filter {
                    eq(DatabaseColumns.STATUS, STATUS_COMPLETED)
                    gte(DatabaseColumns.SALE_DATE, today)
                }
            }
            .decodeList<SaleTotalRow>()

        // Convert from dollars (numeric) to cents (Long)
        return salesResult.sumOf { (it.totalAmount * CENTS_PER_DOLLAR).toLong() }
    }

    /**
     * Fetches the count of completed transactions for today.
     */
    private suspend fun fetchTodayTransactionCount(): Int {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val transactions = supabaseClient.from(TABLE_SALES)
            .select(columns = Columns.list("id")) {
                filter {
                    eq(DatabaseColumns.STATUS, STATUS_COMPLETED)
                    gte(DatabaseColumns.SALE_DATE, today)
                }
            }
            .decodeList<SaleIdRow>()

        return transactions.size
    }

    /**
     * Fetches the count of products below minimum stock level.
     */
    private suspend fun fetchLowStockCount(): Int {
        val products = supabaseClient.from(VIEW_LOW_STOCK_PRODUCTS)
            .select(columns = Columns.list("id"))
            .decodeList<ProductIdRow>()

        return products.size
    }

    /**
     * Fetches the currently active shift information.
     * Returns null if no shift is currently open.
     */
    private suspend fun fetchActiveShift(): ActiveShiftInfoDTO? {
        val activeShiftColumns = Columns.list(
            "id",
            "shift_number",
            "cashier_id",
            "opened_at",
            "opening_balance",
            "users!inner(full_name)",
        )

        val activeShiftResult = try {
            supabaseClient.from(TABLE_CASH_SHIFTS)
                .select(columns = activeShiftColumns) {
                    filter {
                        exact(DatabaseColumns.CLOSED_AT, null)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<ActiveShiftRow>()
        } catch (_: RestException) {
            null
        } catch (_: SerializationException) {
            null
        }

        if (activeShiftResult == null) {
            return null
        }

        // Convert from dollars (numeric) to cents (Long)
        val openingBalanceCents = (activeShiftResult.openingBalance * CENTS_PER_DOLLAR).toLong()

        return ActiveShiftInfoDTO(
            shiftId = activeShiftResult.id,
            cashierId = activeShiftResult.cashierId,
            cashierName = activeShiftResult.users.fullName,
            openedAt = activeShiftResult.openedAt,
            openingBalanceCents = openingBalanceCents,
        )
    }

    /**
     * Gets recent transactions ordered by sale date descending.
     *
     * @param limit Maximum number of transactions to retrieve (default: 10).
     * @return Result containing list of [RecentTransactionDTO] or error.
     */
    suspend fun getRecentTransactions(limit: Int = DEFAULT_TRANSACTIONS_LIMIT): Result<List<RecentTransactionDTO>> {
        return executeQuery("Failed to fetch recent transactions") {
            val transactions = supabaseClient.from(TABLE_SALES)
                .select(
                    columns = Columns.list(
                        "id",
                        "invoice_number",
                        "total_amount",
                        "status",
                        "sale_date",
                        "customers(full_name)",
                    )
                ) {
                    order(column = DatabaseColumns.SALE_DATE, order = Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<RecentTransactionRow>()

            transactions.map { row ->
                val totalCents = (row.totalAmount * CENTS_PER_DOLLAR).toLong()

                RecentTransactionDTO(
                    id = row.id,
                    invoiceNumber = row.invoiceNumber,
                    totalCents = totalCents,
                    status = row.status,
                    saleDate = row.saleDate,
                    customerName = row.customers?.fullName,
                )
            }
        }
    }

    /**
     * Gets products with stock levels below their minimum threshold.
     *
     * @return Result containing list of [LowStockProductDTO] or error.
     */
    suspend fun getLowStockProducts(): Result<List<LowStockProductDTO>> {
        return executeQuery("Failed to fetch low stock products") {
            val products = supabaseClient.from(VIEW_LOW_STOCK_PRODUCTS)
                .select(
                    columns = Columns.list(
                        "id",
                        "sku",
                        "name",
                        "current_stock",
                        "min_stock_level",
                        "selling_price",
                        "categories(name)",
                    )
                ) {
                    order(column = DatabaseColumns.CURRENT_STOCK, order = Order.ASCENDING)
                }
                .decodeList<LowStockProductRow>()

            products.map { row ->
                val sellingPriceCents = (row.sellingPrice * CENTS_PER_DOLLAR).toLong()

                LowStockProductDTO(
                    id = row.id,
                    sku = row.sku,
                    name = row.name,
                    currentStock = row.currentStock,
                    minStockLevel = row.minStockLevel,
                    sellingPriceCents = sellingPriceCents,
                    categoryName = row.categories?.name,
                )
            }
        }
    }

    // Internal data classes for database rows

    @Serializable
    private data class SaleTotalRow(
        @SerialName("total_amount") val totalAmount: Double,
    )

    @Serializable
    private data class SaleIdRow(
        @SerialName("id") val id: String,
    )

    @Serializable
    private data class ProductIdRow(
        @SerialName("id") val id: String,
    )

    @Serializable
    private data class ActiveShiftRow(
        @SerialName("id") val id: String,
        @SerialName("shift_number") val shiftNumber: String,
        @SerialName("cashier_id") val cashierId: String,
        @SerialName("opened_at") val openedAt: String,
        @SerialName("opening_balance") val openingBalance: Double,
        @SerialName("users") val users: UserName,
    )

    @Serializable
    private data class UserName(
        @SerialName("full_name") val fullName: String,
    )

    @Serializable
    private data class RecentTransactionRow(
        @SerialName("id") val id: String,
        @SerialName("invoice_number") val invoiceNumber: String,
        @SerialName("total_amount") val totalAmount: Double,
        @SerialName("status") val status: String,
        @SerialName("sale_date") val saleDate: String,
        @SerialName("customers") val customers: CustomerName?,
    )

    @Serializable
    private data class CustomerName(
        @SerialName("full_name") val fullName: String,
    )

    @Serializable
    private data class LowStockProductRow(
        @SerialName("id") val id: String,
        @SerialName("sku") val sku: String,
        @SerialName("name") val name: String,
        @SerialName("current_stock") val currentStock: Int,
        @SerialName("min_stock_level") val minStockLevel: Int,
        @SerialName("selling_price") val sellingPrice: Double,
        @SerialName("categories") val categories: CategoryName?,
    )

    @Serializable
    private data class CategoryName(
        @SerialName("name") val name: String,
    )
}
