package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.shared.data.reports.dto.CategoryBreakdownDTO
import com.vibely.pos.shared.data.reports.dto.CustomerAnalyticsDTO
import com.vibely.pos.shared.data.reports.dto.ProductPerformanceDTO
import com.vibely.pos.shared.data.reports.dto.SalesReportDTO
import com.vibely.pos.shared.data.reports.dto.SalesTrendDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Database table names
private const val TABLE_SALES = "sales"
private const val TABLE_SALE_ITEMS = "sale_items"

// Status value for completed sales
private const val STATUS_COMPLETED = "completed"

// Conversion factor from dollars to cents
private const val CENTS_PER_DOLLAR = 100

// Default limits
private const val DEFAULT_LIMIT = 10
private const val MAX_LIMIT = 100

// Time granularity buckets
private const val TIME_BUCKET_DAILY = "daily"
private const val TIME_BUCKET_WEEKLY = "weekly"
private const val TIME_BUCKET_MONTHLY = "monthly"
private const val TIME_BUCKET_YEARLY = "yearly"

// Week prefix for weekly grouping
private const val WEEK_PREFIX = "WEEK_"

// Substring lengths for time parsing
private const val DATE_LENGTH = 10
private const val MONTH_LENGTH = 7
private const val YEAR_LENGTH = 4

/**
 * Service for generating complex sales reports and analytics.
 *
 * Responsibilities:
 * - Generate aggregated sales reports for time periods
 * - Calculate top performing products by revenue
 * - Provide category-wise sales breakdown
 * - Analyze customer purchasing behavior
 * - Generate sales trend data with various time granularities
 *
 * @param supabaseClient The Supabase client for database operations
 */
@Suppress("LongMethod", "StringLiteralDuplication")
class ReportService(
    private val supabaseClient: SupabaseClient,
) : BaseService() {

    /**
     * Generates a comprehensive sales report for a given time period.
     *
     * Calculates total revenue, total cost (approximated from subtotals),
     * profit, transaction count, and average transaction value.
     *
     * @param startTime Start of the time period in ISO 8601 format
     * @param endTime End of the time period in ISO 8601 format
     * @return [SalesReportDTO] with aggregated sales metrics
     */
    suspend fun getSalesReport(startTime: String, endTime: String): SalesReportDTO {
        val salesData = try {
            supabaseClient.from(TABLE_SALES)
                .select(
                    columns = Columns.list(
                        "total_amount",
                        "subtotal",
                        "discount_amount"
                    )
                ) {
                    filter {
                        eq(DatabaseColumns.STATUS, STATUS_COMPLETED)
                        gte(DatabaseColumns.SALE_DATE, startTime)
                        lte(DatabaseColumns.SALE_DATE, endTime)
                    }
                }
                .decodeList<SalesReportRow>()
        } catch (e: RestException) {
            // Return empty report on error
            return SalesReportDTO(
                totalRevenue = 0,
                totalCost = 0,
                totalProfit = 0,
                transactionCount = 0,
                averageTransactionValue = 0
            )
        }

        if (salesData.isEmpty()) {
            return SalesReportDTO(
                totalRevenue = 0,
                totalCost = 0,
                totalProfit = 0,
                transactionCount = 0,
                averageTransactionValue = 0
            )
        }

        // Calculate aggregated values
        val totalRevenue = salesData.sumOf { it.totalAmount }
        val totalCost = salesData.sumOf { it.subtotal }
        val totalProfit = totalRevenue - totalCost
        val transactionCount = salesData.size
        val averageTransactionValue = if (transactionCount > 0) {
            totalRevenue / transactionCount
        } else {
            0.0
        }

        return SalesReportDTO(
            totalRevenue = (totalRevenue * CENTS_PER_DOLLAR).toLong(),
            totalCost = (totalCost * CENTS_PER_DOLLAR).toLong(),
            totalProfit = (totalProfit * CENTS_PER_DOLLAR).toLong(),
            transactionCount = transactionCount,
            averageTransactionValue = (averageTransactionValue * CENTS_PER_DOLLAR).toLong()
        )
    }

    /**
     * Retrieves the top performing products by revenue for a given time period.
     *
     * @param startTime Start of the time period in ISO 8601 format
     * @param endTime End of the time period in ISO 8601 format
     * @param limit Maximum number of products to return (default: 10, max: 100)
     * @return List of [ProductPerformanceDTO] sorted by revenue descending
     */
    @Suppress("ReturnCount")
    suspend fun getTopProducts(
        startTime: String,
        endTime: String,
        limit: Int = DEFAULT_LIMIT
    ): List<ProductPerformanceDTO> {
        val effectiveLimit = limit.coerceIn(1, MAX_LIMIT)

        // First, get the sale IDs in the date range
        val saleIds = try {
            supabaseClient.from(TABLE_SALES)
                .select(columns = Columns.list(DatabaseColumns.ID)) {
                    filter {
                        eq(DatabaseColumns.STATUS, STATUS_COMPLETED)
                        gte(DatabaseColumns.SALE_DATE, startTime)
                        lte(DatabaseColumns.SALE_DATE, endTime)
                    }
                }
                .decodeList<SaleIdRow>()
                .map { it.id }
        } catch (e: RestException) {
            return emptyList()
        }

        if (saleIds.isEmpty()) {
            return emptyList()
        }

        // Get sale items with product information
        val saleItems = try {
            supabaseClient.from(TABLE_SALE_ITEMS)
                .select(
                    columns = Columns.list(
                        "id",
                        "sale_id",
                        "product_id",
                        "quantity",
                        "unit_price",
                        "cost_price",
                        "line_total",
                        "products(id, name)"
                    )
                ) { }
                .decodeList<SaleItemWithSaleIdRow>()
                .filter { it.saleId in saleIds }
        } catch (e: RestException) {
            return emptyList()
        }

        // Group by product and aggregate
        val productAggregates = saleItems.groupBy { it.productId }.map { (productId, items) ->
            val productName = items.firstOrNull()?.products?.name ?: "Unknown"
            val quantitySold = items.sumOf { it.quantity.toInt() }
            val revenue = items.sumOf { it.lineTotal }
            val cost = items.sumOf { (it.costPrice ?: 0.0) * it.quantity }
            val profit = revenue - cost

            ProductPerformanceDTO(
                productId = productId,
                productName = productName,
                quantitySold = quantitySold,
                revenue = (revenue * CENTS_PER_DOLLAR).toLong(),
                cost = (cost * CENTS_PER_DOLLAR).toLong(),
                profit = (profit * CENTS_PER_DOLLAR).toLong()
            )
        }

        return productAggregates
            .sortedByDescending { it.revenue }
            .take(effectiveLimit)
    }

    /**
     * Generates category-wise sales breakdown for a given time period.
     *
     * @param startTime Start of the time period in ISO8601 format
     * @param endTime End of the time period in ISO8601 format
     * @return List of [CategoryBreakdownDTO] with category performance metrics
     */
    @Suppress("ReturnCount")
    suspend fun getCategoryBreakdown(startTime: String, endTime: String): List<CategoryBreakdownDTO> {
        // First, get the sale IDs in the date range
        val saleIds = try {
            supabaseClient.from(TABLE_SALES)
                .select(columns = Columns.list(DatabaseColumns.ID)) {
                    filter {
                        eq(DatabaseColumns.STATUS, STATUS_COMPLETED)
                        gte(DatabaseColumns.SALE_DATE, startTime)
                        lte(DatabaseColumns.SALE_DATE, endTime)
                    }
                }
                .decodeList<SaleIdRow>()
                .map { it.id }
        } catch (e: RestException) {
            return emptyList()
        }

        if (saleIds.isEmpty()) {
            return emptyList()
        }

        // Get sale items with category information
        val saleItems = try {
            supabaseClient.from(TABLE_SALE_ITEMS)
                .select(
                    columns = Columns.list(
                        "id",
                        "sale_id",
                        "product_id",
                        "line_total",
                        "products(id, name, category_id, categories(id, name))"
                    )
                ) { }
                .decodeList<SaleItemWithCategorySaleIdRow>()
                .filter { it.saleId in saleIds }
        } catch (e: RestException) {
            return emptyList()
        }

        // Group by category and aggregate
        val categoryAggregates = saleItems.groupBy { it.products?.categories?.id }.map { (categoryId, items) ->
            val categoryName = items.firstOrNull()?.products?.categories?.name ?: "Uncategorized"
            val revenue = items.sumOf { it.lineTotal }
            val transactionCount = items.size

            CategoryBreakdownDTO(
                categoryId = categoryId ?: "uncategorized",
                categoryName = categoryName,
                revenue = (revenue * CENTS_PER_DOLLAR).toLong(),
                transactionCount = transactionCount
            )
        }

        return categoryAggregates.sortedByDescending { it.revenue }
    }

    /**
     * Analyzes customer purchasing behavior for analytics.
     *
     * Groups sales by customer and calculates total spent, visit count,
     * and last visit timestamp.
     *
     * @param startTime Start of the time period in ISO 8601 format
     * @param endTime End of the time period in ISO 8601 format
     * @param limit Maximum number of customers to return (default: 10, max: 100)
     * @return List of [CustomerAnalyticsDTO] sorted by total spent descending
     */
    suspend fun getCustomerAnalytics(
        startTime: String,
        endTime: String,
        limit: Int = DEFAULT_LIMIT
    ): List<CustomerAnalyticsDTO> {
        val effectiveLimit = limit.coerceIn(1, MAX_LIMIT)

        val salesWithCustomers = try {
            supabaseClient.from(TABLE_SALES)
                .select(
                    columns = Columns.list(
                        "id",
                        "customer_id",
                        "total_amount",
                        "sale_date",
                        "customers(id, first_name, last_name, company_name)"
                    )
                ) {
                    filter {
                        eq(DatabaseColumns.STATUS, STATUS_COMPLETED)
                        gte(DatabaseColumns.SALE_DATE, startTime)
                        lte(DatabaseColumns.SALE_DATE, endTime)
                    }
                }
                .decodeList<SaleWithCustomerRow>()
        } catch (e: RestException) {
            return emptyList()
        }

        if (salesWithCustomers.isEmpty()) {
            return emptyList()
        }

        // Group by customer and aggregate
        val customerAggregates = salesWithCustomers.groupBy { it.customerId }.map { (customerId, sales) ->
            val customerName = sales.firstOrNull()?.let { sale ->
                sale.customers?.let { customer ->
                    when {
                        customer.firstName != null && customer.lastName != null ->
                            "${customer.firstName} ${customer.lastName}"
                        customer.companyName != null -> customer.companyName
                        else -> "Guest Customer"
                    }
                } ?: "Guest Customer"
            } ?: "Guest Customer"

            val totalSpent = sales.sumOf { it.totalAmount }
            val visitCount = sales.size
            val lastVisit = sales.maxOfOrNull { it.saleDate } ?: startTime

            CustomerAnalyticsDTO(
                customerId = customerId,
                customerName = customerName,
                totalSpent = (totalSpent * CENTS_PER_DOLLAR).toLong(),
                visitCount = visitCount,
                lastVisit = parseTimestampToEpoch(lastVisit)
            )
        }

        return customerAggregates
            .sortedByDescending { it.totalSpent }
            .take(effectiveLimit)
    }

    /**
     * Generates sales trend data with configurable time granularity.
     *
     * Aggregates sales data by time buckets (daily, weekly, monthly, yearly)
     * for trend analysis.
     *
     * @param startTime Start of the time period in ISO 8601 format
     * @param endTime End of the time period in ISO 8601 format
     * @param granularity Time bucket size: "daily", "weekly", "monthly", or "yearly"
     * @return List of [SalesTrendDTO] sorted by timestamp ascending
     */
    suspend fun getSalesTrend(
        startTime: String,
        endTime: String,
        granularity: String = "daily"
    ): List<SalesTrendDTO> {
        val salesData = try {
            supabaseClient.from(TABLE_SALES)
                .select(
                    columns = Columns.list(
                        "id",
                        "total_amount",
                        "sale_date"
                    )
                ) {
                    filter {
                        eq(DatabaseColumns.STATUS, STATUS_COMPLETED)
                        gte(DatabaseColumns.SALE_DATE, startTime)
                        lte(DatabaseColumns.SALE_DATE, endTime)
                    }
                    order(DatabaseColumns.SALE_DATE, io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<SaleForTrendRow>()
        } catch (e: RestException) {
            return emptyList()
        }

        if (salesData.isEmpty()) {
            return emptyList()
        }

        // Group by time bucket based on granularity
        val groupedData = when (granularity.lowercase()) {
            TIME_BUCKET_DAILY -> salesData.groupBy { sale ->
                sale.saleDate.substring(0, DATE_LENGTH)
            }
            TIME_BUCKET_WEEKLY -> salesData.groupBy { sale ->
                val datePart = sale.saleDate.substring(0, DATE_LENGTH)
                "$WEEK_PREFIX$datePart"
            }
            TIME_BUCKET_MONTHLY -> salesData.groupBy { sale ->
                sale.saleDate.substring(0, MONTH_LENGTH)
            }
            TIME_BUCKET_YEARLY -> salesData.groupBy { sale ->
                sale.saleDate.substring(0, YEAR_LENGTH)
            }
            else -> salesData.groupBy { it.saleDate.substring(0, DATE_LENGTH) }
        }

        return groupedData.map { (timeKey, sales) ->
            val timestamp = when (granularity.lowercase()) {
                TIME_BUCKET_DAILY -> parseDateToEpoch(timeKey)
                TIME_BUCKET_WEEKLY -> parseDateToEpoch(timeKey.removePrefix(WEEK_PREFIX))
                TIME_BUCKET_MONTHLY -> parseMonthToEpoch(timeKey)
                TIME_BUCKET_YEARLY -> parseYearToEpoch(timeKey)
                else -> parseDateToEpoch(timeKey)
            }
            val revenue = sales.sumOf { it.totalAmount }
            val transactionCount = sales.size

            SalesTrendDTO(
                timestamp = timestamp,
                revenue = (revenue * CENTS_PER_DOLLAR).toLong(),
                transactionCount = transactionCount
            )
        }.sortedBy { dto: SalesTrendDTO -> dto.timestamp }
    }

    // Helper function to parse timestamp to epoch milliseconds
    @Suppress("TooGenericExceptionCaught")
    private fun parseTimestampToEpoch(timestamp: String): Long {
        return try {
            // Simple parsing - assume ISO format
            // Extract milliseconds from ISO timestamp
            val cleanTimestamp = timestamp.replace("+00:00", "").replace("Z", "")
            if (cleanTimestamp.contains(".")) {
                val basePart = cleanTimestamp.substringBefore(".")
                val parsed = java.time.LocalDateTime.parse(basePart)
                java.time.Instant.from(parsed.atZone(java.time.ZoneOffset.UTC)).toEpochMilli()
            } else {
                val parsed = java.time.LocalDateTime.parse(cleanTimestamp)
                java.time.Instant.from(parsed.atZone(java.time.ZoneOffset.UTC)).toEpochMilli()
            }
        } catch (e: Exception) {
            // Return current time as fallback
            System.currentTimeMillis()
        }
    }

    // Helper function to parse date string to epoch milliseconds
    @Suppress("TooGenericExceptionCaught")
    private fun parseDateToEpoch(dateStr: String): Long {
        return try {
            val parsed = java.time.LocalDate.parse(dateStr)
            java.time.Instant.from(parsed.atStartOfDay(java.time.ZoneOffset.UTC)).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    // Helper function to parse month string to epoch milliseconds
    @Suppress("TooGenericExceptionCaught")
    private fun parseMonthToEpoch(monthStr: String): Long {
        return try {
            val parsed = java.time.YearMonth.parse(monthStr)
            java.time.Instant.from(parsed.atDay(1).atStartOfDay(java.time.ZoneOffset.UTC)).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    // Helper function to parse year string to epoch milliseconds
    @Suppress("TooGenericExceptionCaught")
    private fun parseYearToEpoch(yearStr: String): Long {
        return try {
            val year = yearStr.toInt()
            val date = java.time.LocalDate.of(year, 1, 1)
            java.time.Instant.from(date.atStartOfDay(java.time.ZoneOffset.UTC)).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    // Internal data classes for database rows

    @Serializable
    private data class SaleIdRow(
        @SerialName("id") val id: String,
    )

    @Serializable
    private data class SalesReportRow(
        @SerialName("total_amount") val totalAmount: Double,
        @SerialName("subtotal") val subtotal: Double,
        @SerialName("discount_amount") val discountAmount: Double,
    )

    @Serializable
    private data class ProductInfo(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
    )

    @Serializable
    private data class SaleItemWithSaleIdRow(
        @SerialName("id") val id: String,
        @SerialName("sale_id") val saleId: String,
        @SerialName("product_id") val productId: String,
        @SerialName("quantity") val quantity: Double,
        @SerialName("unit_price") val unitPrice: Double,
        @SerialName("cost_price") val costPrice: Double?,
        @SerialName("line_total") val lineTotal: Double,
        @SerialName("products") val products: ProductInfo?,
    )

    @Serializable
    private data class CategoryInfo(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
    )

    @Serializable
    private data class ProductWithCategory(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
        @SerialName("category_id") val categoryId: String?,
        @SerialName("categories") val categories: CategoryInfo?,
    )

    @Serializable
    private data class SaleItemWithCategorySaleIdRow(
        @SerialName("id") val id: String,
        @SerialName("sale_id") val saleId: String,
        @SerialName("product_id") val productId: String,
        @SerialName("line_total") val lineTotal: Double,
        @SerialName("products") val products: ProductWithCategory?,
    )

    @Serializable
    private data class CustomerInfo(
        @SerialName("id") val id: String,
        @SerialName("first_name") val firstName: String?,
        @SerialName("last_name") val lastName: String?,
        @SerialName("company_name") val companyName: String?,
    )

    @Serializable
    private data class SaleWithCustomerRow(
        @SerialName("id") val id: String,
        @SerialName("customer_id") val customerId: String?,
        @SerialName("total_amount") val totalAmount: Double,
        @SerialName("sale_date") val saleDate: String,
        @SerialName("customers") val customers: CustomerInfo?,
    )

    @Serializable
    private data class SaleForTrendRow(
        @SerialName("id") val id: String,
        @SerialName("total_amount") val totalAmount: Double,
        @SerialName("sale_date") val saleDate: String,
    )
}
