@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "LongParameterList",
    "StringLiteralDuplication",
    "MaxLineLength"
)

package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.backend.common.ErrorMessages
import com.vibely.pos.backend.dto.request.CreatePurchaseOrderRequest
import com.vibely.pos.backend.dto.request.ReceivePurchaseOrderItemRequest
import com.vibely.pos.backend.dto.request.ReceivePurchaseOrderRequest
import com.vibely.pos.backend.dto.request.UpdatePurchaseOrderRequest
import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderDTO
import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderItemDTO
import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderWithItemsDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val PO_NUMBER_PADDING = 4
private const val ERROR_FETCH_FAILED = "Failed to fetch purchase orders"
private const val ERROR_CREATE_FAILED = "Failed to create purchase order"
private const val ERROR_UPDATE_FAILED = "Failed to update purchase order"
private const val ERROR_DELETE_FAILED = "Failed to delete purchase order"
private const val ERROR_RECEIVE_FAILED = "Failed to receive purchase order"
private const val ERROR_STATUS_UPDATE_FAILED = "Failed to update purchase order status"
private const val ERROR_GENERATE_PO_FAILED = "Failed to generate PO number"

class PurchaseOrderService(
    private val supabaseClient: SupabaseClient,
    private val currencyService: CurrencyService,
) : BaseService() {

    suspend fun getAllPurchaseOrders(
        userId: String,
        supplierId: String?,
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<PurchaseOrderDTO>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient.from(TableNames.PURCHASE_ORDERS)
                .select {
                    filter {
                        eq(DatabaseColumns.CREATED_BY, userId)
                        supplierId?.let { eq(DatabaseColumns.SUPPLIER_ID, it) }
                        status?.let { eq(DatabaseColumns.STATUS, it) }
                    }
                    order(DatabaseColumns.ORDER_DATE, Order.DESCENDING)
                    range(from, to)
                }
                .decodeList<PurchaseOrderDTO>()
        }
    }

    suspend fun getPurchaseOrderById(userId: String, purchaseOrderId: String): Result<PurchaseOrderWithItemsDTO> {
        return executeQuery(ErrorMessages.PURCHASE_ORDER_NOT_FOUND) {
            supabaseClient.from(TableNames.PURCHASE_ORDERS)
                .select(
                    Columns.raw(
                        """*, purchase_order_items(*)""".trimIndent()
                    )
                ) {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq(DatabaseColumns.CREATED_BY, userId)
                    }
                }
                .decodeSingle<PurchaseOrderWithItemsDTO>()
        }
    }

    suspend fun createPurchaseOrder(userId: String, request: CreatePurchaseOrderRequest): Result<PurchaseOrderDTO> {
        return executeQuery(ERROR_CREATE_FAILED) {
            val poNumber = generateNextPoNumber(userId)

            val totalAmount = request.items.sumOf { it.quantity * it.unitCost }

            val data = buildJsonObject {
                put(DatabaseColumns.PO_NUMBER, poNumber)
                put(DatabaseColumns.SUPPLIER_ID, request.supplierId)
                put("created_by", userId)
                put(DatabaseColumns.TOTAL_AMOUNT, totalAmount)
                put(DatabaseColumns.STATUS, "draft")
                put(DatabaseColumns.ORDER_DATE, java.time.Instant.now().toString())
                request.expectedDeliveryDate?.let { put(DatabaseColumns.EXPECTED_DELIVERY_DATE, it) }
                request.notes?.let { put(DatabaseColumns.NOTES, it) }
            }

            val purchaseOrder = supabaseClient.from(TableNames.PURCHASE_ORDERS)
                .insert(data) { select() }
                .decodeSingle<PurchaseOrderDTO>()

            if (request.items.isNotEmpty()) {
                createPurchaseOrderItems(purchaseOrder.id, request)
            }

            purchaseOrder
        }
    }

    private suspend fun createPurchaseOrderItems(purchaseOrderId: String, request: CreatePurchaseOrderRequest) {
        val itemsData = request.items.map { item ->
            buildJsonObject {
                put(DatabaseColumns.PURCHASE_ORDER_ID, purchaseOrderId)
                put(DatabaseColumns.PRODUCT_ID, item.productId)
                put(DatabaseColumns.QUANTITY, item.quantity)
                put(DatabaseColumns.UNIT_COST, item.unitCost)
                put(DatabaseColumns.COST_CURRENCY_CODE, item.costCurrencyCode)
                put(DatabaseColumns.SUBTOTAL, item.quantity * item.unitCost)
                put(DatabaseColumns.RECEIVED_QUANTITY, 0)
            }
        }

        supabaseClient.from(TableNames.PURCHASE_ORDER_ITEMS)
            .insert(itemsData)
    }

    suspend fun updatePurchaseOrder(
        userId: String,
        purchaseOrderId: String,
        request: UpdatePurchaseOrderRequest,
    ): Result<PurchaseOrderDTO> {
        return executeQuery(ERROR_UPDATE_FAILED) {
            val data = buildJsonObject {
                request.supplierId?.let { put(DatabaseColumns.SUPPLIER_ID, it) }
                request.expectedDeliveryDate?.let { put(DatabaseColumns.EXPECTED_DELIVERY_DATE, it) }
                request.notes?.let { put(DatabaseColumns.NOTES, it) }
            }

            supabaseClient.from(TableNames.PURCHASE_ORDERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq(DatabaseColumns.CREATED_BY, userId)
                    }
                    select()
                }
                .decodeSingle<PurchaseOrderDTO>()
        }
    }

    suspend fun updatePurchaseOrderStatus(
        userId: String,
        purchaseOrderId: String,
        newStatus: String,
    ): Result<PurchaseOrderDTO> {
        return executeQuery(ERROR_STATUS_UPDATE_FAILED) {
            val data = buildJsonObject {
                put(DatabaseColumns.STATUS, newStatus)
            }

            supabaseClient.from(TableNames.PURCHASE_ORDERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq(DatabaseColumns.CREATED_BY, userId)
                    }
                    select()
                }
                .decodeSingle<PurchaseOrderDTO>()
        }
    }

    suspend fun deletePurchaseOrder(userId: String, purchaseOrderId: String): Result<Unit> {
        return executeQuery(ERROR_DELETE_FAILED) {
            supabaseClient.from(TableNames.PURCHASE_ORDER_ITEMS)
                .delete {
                    filter { eq(DatabaseColumns.PURCHASE_ORDER_ID, purchaseOrderId) }
                }

            supabaseClient.from(TableNames.PURCHASE_ORDERS)
                .delete {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq(DatabaseColumns.CREATED_BY, userId)
                    }
                }
        }
    }

    /**
     * Receives a purchase order by updating received quantities and marking as received.
     *
     * This function performs multiple operations in sequence:
     * 1. Validates purchase order ownership
     * 2. Fetches all items from the order
     * 3. Updates received quantities for each item
     * 4. Converts non-MXN costs to MXN using latest exchange rates
     * 5. Marks the order as received
     */
    suspend fun receivePurchaseOrder(
        userId: String,
        purchaseOrderId: String,
        request: ReceivePurchaseOrderRequest,
    ): Result<PurchaseOrderDTO> {
        return executeQuery(ERROR_RECEIVE_FAILED) {
            supabaseClient.from(TableNames.PURCHASE_ORDERS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq(DatabaseColumns.CREATED_BY, userId)
                    }
                }
                .decodeSingle<PurchaseOrderDTO>()

            val orderItems = supabaseClient.from(TableNames.PURCHASE_ORDER_ITEMS)
                .select {
                    filter {
                        eq(DatabaseColumns.PURCHASE_ORDER_ID, purchaseOrderId)
                    }
                }
                .decodeList<PurchaseOrderItemDTO>()

            updateReceivedQuantities(purchaseOrderId, request.items)
            convertProductCostsToMXN(orderItems)

            val statusData = buildJsonObject {
                put(DatabaseColumns.STATUS, "received")
                put(DatabaseColumns.RECEIVED_DATE, java.time.Instant.now().toString())
            }

            supabaseClient.from(TableNames.PURCHASE_ORDERS)
                .update(statusData) {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq(DatabaseColumns.CREATED_BY, userId)
                    }
                    select()
                }
                .decodeSingle<PurchaseOrderDTO>()
        }
    }

    private suspend fun updateReceivedQuantities(
        purchaseOrderId: String,
        items: List<ReceivePurchaseOrderItemRequest>
    ) {
        items.forEach { itemUpdate ->
            val updateData = buildJsonObject {
                put(DatabaseColumns.RECEIVED_QUANTITY, itemUpdate.receivedQuantity)
            }
            supabaseClient.from(TableNames.PURCHASE_ORDER_ITEMS)
                .update(updateData) {
                    filter {
                        eq(DatabaseColumns.ID, itemUpdate.itemId)
                        eq(DatabaseColumns.PURCHASE_ORDER_ID, purchaseOrderId)
                    }
                }
        }
    }

    private suspend fun convertProductCostsToMXN(orderItems: List<PurchaseOrderItemDTO>) {
        orderItems.filter { it.costCurrencyCode != "MXN" }
            .forEach { item ->
                currencyService.convertAmount(
                    amount = item.unitCost,
                    fromCurrency = item.costCurrencyCode,
                    toCurrency = "MXN"
                )?.let { convertedCost ->
                    val productData = buildJsonObject {
                        put("cost_price", convertedCost)
                        put(DatabaseColumns.COST_CURRENCY_CODE, "MXN")
                    }
                    supabaseClient.from(TableNames.PRODUCTS)
                        .update(productData) {
                            filter {
                                eq(DatabaseColumns.ID, item.productId)
                            }
                        }
                }
            }
    }

    suspend fun generatePoNumber(userId: String): Result<String> {
        return executeQuery(ERROR_GENERATE_PO_FAILED) {
            generateNextPoNumber(userId)
        }
    }

    private suspend fun generateNextPoNumber(userId: String): String {
        val today = java.time.LocalDate.now()
        val prefix = "PO-${today.year}${today.monthValue.toString().padStart(2, '0')}"

        val existingOrders = supabaseClient.from(TableNames.PURCHASE_ORDERS)
            .select {
                filter {
                    eq(DatabaseColumns.CREATED_BY, userId)
                    ilike(DatabaseColumns.PO_NUMBER, "$prefix%")
                }
            }
            .decodeList<PurchaseOrderDTO>()

        val nextNumber = existingOrders.size + 1
        return "$prefix-${nextNumber.toString().padStart(PO_NUMBER_PADDING, '0')}"
    }
}
