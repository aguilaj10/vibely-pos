package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val TABLE_SALES = "sales"
private const val TABLE_SALE_ITEMS = "sale_items"
private const val TABLE_PRODUCTS = "products"
private const val TABLE_INVENTORY_TRANSACTIONS = "inventory_transactions"
private const val STATUS_COMPLETED = "completed"
private const val TRANSACTION_TYPE_SALE = "sale"
private const val REFERENCE_TYPE_SALE = "sale"
private const val INVOICE_NUMBER_PADDING = 5
private const val ERROR_INSUFFICIENT_STOCK = "Insufficient stock for"

internal class SaleCreationHelper(
    private val supabaseClient: SupabaseClient
) {
    suspend fun validateAndBuildSaleItems(
        request: CreateSaleRequest
    ): Result<Pair<List<SaleItemDTO>, BigDecimal>> {
        return try {
            val saleItems = buildSaleItemsList(request)
            val subtotal = calculateSubtotal(saleItems)
            Result.Success(Pair(saleItems, subtotal))
        } catch (e: IllegalStateException) {
            Result.Error(e.message ?: "Failed to validate sale items", cause = e)
        } catch (e: NoSuchElementException) {
            Result.Error(e.message ?: "Product not found", cause = e)
        }
    }

    private suspend fun buildSaleItemsList(request: CreateSaleRequest): List<SaleItemDTO> {
        val saleItems = mutableListOf<SaleItemDTO>()
        val now = Clock.System.now().toString()

        for (item in request.items) {
            val product = fetchProduct(item.productId)
            validateStock(product, item.quantity)

            val itemSubtotal = product.sellingPrice.toBigDecimal() * item.quantity.toBigDecimal()
            saleItems.add(
                SaleItemDTO(
                    id = UUID.randomUUID().toString(),
                    saleId = "",
                    productId = item.productId,
                    quantity = item.quantity,
                    unitPrice = product.sellingPrice,
                    discountAmount = 0.0,
                    subtotal = itemSubtotal.toDouble(),
                    createdAt = now
                )
            )
        }

        return saleItems
    }

    private fun validateStock(product: ProductDTO, requestedQuantity: Int) {
        check(product.currentStock >= requestedQuantity) {
            "$ERROR_INSUFFICIENT_STOCK ${product.name}"
        }
    }

    private fun calculateSubtotal(saleItems: List<SaleItemDTO>): BigDecimal =
        saleItems.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.subtotal.toBigDecimal()
        }

    suspend fun fetchProduct(productId: String): ProductDTO {
        return supabaseClient.from(TABLE_PRODUCTS)
            .select {
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
    ): SaleDTO {
        val invoiceNumber = generateInvoiceNumber()
        val now = Clock.System.now().toString()

        val saleData = mapOf(
            "invoice_number" to invoiceNumber,
            "customer_id" to request.customerId,
            "cashier_id" to cashierId,
            "subtotal" to subtotal.toString(),
            "tax_amount" to "0",
            "discount_amount" to "0",
            "total_amount" to subtotal.toString(),
            "status" to STATUS_COMPLETED,
            "payment_status" to STATUS_COMPLETED,
            "notes" to request.notes,
            "sale_date" to now
        )

        return supabaseClient.from(TABLE_SALES)
            .insert(saleData) {
                select()
            }
            .decodeSingle<SaleDTO>()
    }

    suspend fun insertSaleItems(saleItems: List<SaleItemDTO>, saleId: String) {
        val saleItemsWithSaleId = saleItems.map { it.copy(saleId = saleId) }
        supabaseClient.from(TABLE_SALE_ITEMS).insert(saleItemsWithSaleId)
    }

    suspend fun deductStockAndLogTransactions(
        request: CreateSaleRequest,
        sale: SaleDTO,
        cashierId: String
    ) {
        for (item in request.items) {
            val product = fetchProduct(item.productId)

            supabaseClient.from(TABLE_PRODUCTS)
                .update(mapOf(DatabaseColumns.CURRENT_STOCK to (product.currentStock - item.quantity))) {
                    filter {
                        eq(DatabaseColumns.ID, item.productId)
                    }
                }

            val transactionData = mapOf(
                "product_id" to item.productId,
                "transaction_type" to TRANSACTION_TYPE_SALE,
                "quantity" to -item.quantity,
                "reference_id" to sale.id,
                "reference_type" to REFERENCE_TYPE_SALE,
                "performed_by" to cashierId,
                "notes" to "Sale ${sale.invoiceNumber}"
            )

            supabaseClient.from(TABLE_INVENTORY_TRANSACTIONS).insert(transactionData)
        }
    }

    private suspend fun generateInvoiceNumber(): String {
        val sales = supabaseClient.from(TABLE_SALES)
            .select(Columns.list(DatabaseColumns.ID))
            .decodeList<Map<String, String>>()

        val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val year = localDateTime.year
        val nextNumber = sales.size + 1

        return "SAL-$year-${nextNumber.toString().padStart(INVOICE_NUMBER_PADDING, '0')}"
    }
}
