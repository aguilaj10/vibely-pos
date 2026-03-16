@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "LongParameterList",
    "StringLiteralDuplication",
    "MaxLineLength"
)

package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.dto.request.CreatePurchaseOrderRequest
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
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

private const val TABLE_PURCHASE_ORDERS = "purchase_orders"
private const val TABLE_PURCHASE_ORDER_ITEMS = "purchase_order_items"
private const val PO_NUMBER_PADDING = 4
private const val ERROR_FETCH_FAILED = "Failed to fetch purchase orders"
private const val ERROR_NOT_FOUND = "Purchase order not found"
private const val ERROR_CREATE_FAILED = "Failed to create purchase order"
private const val ERROR_UPDATE_FAILED = "Failed to update purchase order"
private const val ERROR_DELETE_FAILED = "Failed to delete purchase order"
private const val ERROR_RECEIVE_FAILED = "Failed to receive purchase order"
private const val ERROR_STATUS_UPDATE_FAILED = "Failed to update purchase order status"
private const val ERROR_GENERATE_PO_FAILED = "Failed to generate PO number"

class PurchaseOrderService(private val supabaseClient: SupabaseClient) : BaseService() {

    suspend fun getAllPurchaseOrders(
        userId: String,
        supplierId: String?,
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<PurchaseOrderDTO>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient.from(TABLE_PURCHASE_ORDERS)
                .select {
                    filter {
                        eq("created_by", userId)
                        supplierId?.let { eq(DatabaseColumns.SUPPLIER_ID, it) }
                        status?.let { eq(DatabaseColumns.STATUS, it) }
                    }
                    order("order_date", Order.DESCENDING)
                    range(from, to)
                }
                .decodeList<PurchaseOrderDTO>()
        }
    }

    suspend fun getPurchaseOrderById(userId: String, purchaseOrderId: String): Result<PurchaseOrderWithItemsDTO> {
        return executeQuery(ERROR_NOT_FOUND) {
            supabaseClient.from(TABLE_PURCHASE_ORDERS)
                .select(
                    Columns.raw(
                        """*, purchase_order_items(*)""".trimIndent()
                    )
                ) {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq("created_by", userId)
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
                put("po_number", poNumber)
                put(DatabaseColumns.SUPPLIER_ID, request.supplierId)
                put("created_by", userId)
                put("total_amount", totalAmount)
                put(DatabaseColumns.STATUS, "draft")
                put("order_date", java.time.Instant.now().toString())
                request.expectedDeliveryDate?.let { put("expected_delivery_date", it) }
                request.notes?.let { put(DatabaseColumns.NOTES, it) }
            }

            val purchaseOrder = supabaseClient.from(TABLE_PURCHASE_ORDERS)
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
                put("purchase_order_id", purchaseOrderId)
                put(DatabaseColumns.PRODUCT_ID, item.productId)
                put(DatabaseColumns.QUANTITY, item.quantity)
                put("unit_cost", item.unitCost)
                put("subtotal", item.quantity * item.unitCost)
                put("received_quantity", 0)
            }
        }

        supabaseClient.from(TABLE_PURCHASE_ORDER_ITEMS)
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
                request.expectedDeliveryDate?.let { put("expected_delivery_date", it) }
                request.notes?.let { put(DatabaseColumns.NOTES, it) }
            }

            supabaseClient.from(TABLE_PURCHASE_ORDERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq("created_by", userId)
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

            supabaseClient.from(TABLE_PURCHASE_ORDERS)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq("created_by", userId)
                    }
                    select()
                }
                .decodeSingle<PurchaseOrderDTO>()
        }
    }

    suspend fun deletePurchaseOrder(userId: String, purchaseOrderId: String): Result<Unit> {
        return executeQuery(ERROR_DELETE_FAILED) {
            supabaseClient.from(TABLE_PURCHASE_ORDER_ITEMS)
                .delete {
                    filter { eq("purchase_order_id", purchaseOrderId) }
                }

            supabaseClient.from(TABLE_PURCHASE_ORDERS)
                .delete {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq("created_by", userId)
                    }
                }
        }
    }

    suspend fun receivePurchaseOrder(
        userId: String,
        purchaseOrderId: String,
        request: ReceivePurchaseOrderRequest,
    ): Result<PurchaseOrderDTO> {
        return executeQuery(ERROR_RECEIVE_FAILED) {
            val purchaseOrder = supabaseClient.from(TABLE_PURCHASE_ORDERS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq("created_by", userId)
                    }
                }
                .decodeSingle<PurchaseOrderDTO>()

            request.items.forEach { itemUpdate ->
                val updateData = buildJsonObject {
                    put("received_quantity", itemUpdate.receivedQuantity)
                }
                supabaseClient.from(TABLE_PURCHASE_ORDER_ITEMS)
                    .update(updateData) {
                        filter {
                            eq(DatabaseColumns.ID, itemUpdate.itemId)
                            eq("purchase_order_id", purchaseOrderId)
                        }
                    }
            }

            val statusData = buildJsonObject {
                put(DatabaseColumns.STATUS, "received")
                put("received_date", java.time.Instant.now().toString())
            }

            supabaseClient.from(TABLE_PURCHASE_ORDERS)
                .update(statusData) {
                    filter {
                        eq(DatabaseColumns.ID, purchaseOrderId)
                        eq("created_by", userId)
                    }
                    select()
                }
                .decodeSingle<PurchaseOrderDTO>()
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

        val existingOrders = supabaseClient.from(TABLE_PURCHASE_ORDERS)
            .select {
                filter {
                    eq("created_by", userId)
                    ilike("po_number", "$prefix%")
                }
            }
            .decodeList<PurchaseOrderDTO>()

        val nextNumber = existingOrders.size + 1
        return "$prefix-${nextNumber.toString().padStart(PO_NUMBER_PADDING, '0')}"
    }
}
