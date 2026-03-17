package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.math.BigDecimal
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val STATUS_COMPLETED = "completed"
private const val TRANSACTION_TYPE_SALE = "sale"
private const val REFERENCE_TYPE_SALE = "sale"
private const val INVOICE_NUMBER_PADDING = 5
private const val ERROR_INSUFFICIENT_STOCK = "Insufficient stock for"

@Suppress("LongParameterList")
private fun buildSaleData(
    invoiceNumber: String,
    request: CreateSaleRequest,
    cashierId: String,
    subtotal: BigDecimal,
    now: String
): JsonObject {
    return buildJsonObject {
        put("invoice_number", invoiceNumber)
        put("customer_id", request.customerId)
        put("cashier_id", cashierId)
        put("subtotal", subtotal.toDouble())
        put("tax_amount", 0.0)
        put("discount_amount", 0.0)
        put("total_amount", subtotal.toDouble())
        put(DatabaseColumns.STATUS, STATUS_COMPLETED)
        put("payment_status", STATUS_COMPLETED)
        put(DatabaseColumns.NOTES, request.notes)
        put("sale_date", now)
    }
}

@Suppress("LongParameterList")
private fun buildSaleItemData(
    saleId: String,
    productId: String,
    quantity: Int,
    unitPrice: Double,
    subtotal: Double,
    now: String
): JsonObject {
    return buildJsonObject {
        put(DatabaseColumns.SALE_ID, saleId)
        put(DatabaseColumns.PRODUCT_ID, productId)
        put(DatabaseColumns.QUANTITY, quantity)
        put("unit_price", unitPrice)
        put("discount_amount", 0.0)
        put("subtotal", subtotal)
        put(DatabaseColumns.CREATED_AT, now)
    }
}

@Suppress("LongParameterList")
private fun buildInventoryTransactionData(
    productId: String,
    quantity: Int,
    referenceId: String,
    performedBy: String,
    notes: String
): JsonObject {
    return buildJsonObject {
        put(DatabaseColumns.PRODUCT_ID, productId)
        put(DatabaseColumns.TRANSACTION_TYPE, TRANSACTION_TYPE_SALE)
        put(DatabaseColumns.QUANTITY, -quantity)
        put(DatabaseColumns.REFERENCE_ID, referenceId)
        put(DatabaseColumns.REFERENCE_TYPE, REFERENCE_TYPE_SALE)
        put("performed_by", performedBy)
        put(DatabaseColumns.NOTES, notes)
    }
}

internal class SaleCreationHelper(
    private val supabaseClient: SupabaseClient
) {
    suspend fun validateAndBuildSaleItems(
        request: CreateSaleRequest
    ): Result<Pair<List<Pair<String, Int>>, BigDecimal>> {
        return try {
            var subtotal = BigDecimal.ZERO
            val validatedItems = mutableListOf<Pair<String, Int>>()

            for (item in request.items) {
                val product = fetchProduct(item.productId)
                validateStock(product, item.quantity)

                val itemSubtotal = product.sellingPrice.toBigDecimal() * item.quantity.toBigDecimal()
                subtotal += itemSubtotal
                validatedItems.add(Pair(item.productId, item.quantity))
            }

            Result.Success(Pair(validatedItems, subtotal))
        } catch (e: IllegalStateException) {
            Result.Error(e.message ?: "Failed to validate sale items", cause = e)
        } catch (e: NoSuchElementException) {
            Result.Error(e.message ?: "Product not found", cause = e)
        }
    }

    private fun validateStock(product: ProductDTO, requestedQuantity: Int) {
        check(product.currentStock >= requestedQuantity) {
            "$ERROR_INSUFFICIENT_STOCK ${product.name}"
        }
    }

    private suspend fun fetchProduct(productId: String): ProductDTO {
        return supabaseClient.from(TableNames.PRODUCTS)
            .select(
                columns = Columns.list(
                    DatabaseColumns.ID,
                    DatabaseColumns.SKU,
                    DatabaseColumns.NAME,
                    DatabaseColumns.COST_PRICE,
                    DatabaseColumns.SELLING_PRICE,
                    DatabaseColumns.CURRENT_STOCK,
                    DatabaseColumns.MIN_STOCK_LEVEL,
                    DatabaseColumns.CREATED_AT,
                    DatabaseColumns.UPDATED_AT
                )
            ) {
                filter {
                    eq(DatabaseColumns.ID, productId)
                }
            }
            .decodeSingle<ProductDTO>()
    }

    suspend fun insertSale(
        request: CreateSaleRequest,
        cashierId: String,
        subtotal: BigDecimal
    ): JsonObject {
        val invoiceNumber = generateInvoiceNumber()
        val now = Clock.System.now().toString()
        val data = buildSaleData(invoiceNumber, request, cashierId, subtotal, now)

        return supabaseClient.from(TableNames.SALES)
            .insert(data) {
                select()
            }
            .decodeSingle<JsonObject>()
    }

    suspend fun insertSaleItems(
        saleId: String,
        items: List<Pair<String, Int>>
    ) {
        val now = Clock.System.now().toString()

        for ((productId, quantity) in items) {
            val product = fetchProduct(productId)
            val itemSubtotal = product.sellingPrice * quantity
            val data = buildSaleItemData(saleId, productId, quantity, product.sellingPrice, itemSubtotal, now)
            supabaseClient.from(TableNames.SALE_ITEMS).insert(data)
        }
    }

    suspend fun deductStockAndLogTransactions(
        items: List<Pair<String, Int>>,
        saleId: String,
        invoiceNumber: String,
        cashierId: String
    ) {
        for ((productId, quantity) in items) {
            val product = fetchProduct(productId)
            val newStock = product.currentStock - quantity

            supabaseClient.from(TableNames.PRODUCTS)
                .update(
                    buildJsonObject {
                        put(DatabaseColumns.CURRENT_STOCK, newStock)
                    }
                ) {
                    filter {
                        eq(DatabaseColumns.ID, productId)
                    }
                }

            val transactionData = buildInventoryTransactionData(
                productId,
                quantity,
                saleId,
                cashierId,
                "Sale $invoiceNumber"
            )
            supabaseClient.from(TableNames.INVENTORY_TRANSACTIONS).insert(transactionData)
        }
    }

    private suspend fun generateInvoiceNumber(): String {
        val sales = supabaseClient.from(TableNames.SALES)
            .select(Columns.list(DatabaseColumns.ID))
            .decodeList<JsonObject>()

        val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val year = localDateTime.year
        val nextNumber = sales.size + 1

        return "SAL-$year-${nextNumber.toString().padStart(INVOICE_NUMBER_PADDING, '0')}"
    }
}
