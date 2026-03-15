package com.vibely.pos.shared.data.purchaseorder.datasource

import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderDTO
import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderWithItemsDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.json.JsonObject

class RemotePurchaseOrderDataSource(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getAllPurchaseOrders(
        supplierId: String? = null,
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<PurchaseOrderDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/purchase-orders") {
            supplierId?.let { parameter("supplier_id", it) }
            status?.let { parameter("status", it) }
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body<List<PurchaseOrderDTO>>()
    }

    suspend fun getPurchaseOrderById(id: String): Result<PurchaseOrderWithItemsDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/purchase-orders/$id").body<PurchaseOrderWithItemsDTO>()
    }

    suspend fun createPurchaseOrder(purchaseOrder: JsonObject): Result<PurchaseOrderDTO> = Result.runCatching {
        httpClient.post("$baseUrl/api/purchase-orders") {
            setBody(purchaseOrder)
        }.body<PurchaseOrderDTO>()
    }

    suspend fun updatePurchaseOrder(id: String, purchaseOrder: JsonObject): Result<PurchaseOrderDTO> = Result.runCatching {
        httpClient.put("$baseUrl/api/purchase-orders/$id") {
            setBody(purchaseOrder)
        }.body<PurchaseOrderDTO>()
    }

    suspend fun updatePurchaseOrderStatus(id: String, status: String): Result<PurchaseOrderDTO> = Result.runCatching {
        httpClient.patch("$baseUrl/api/purchase-orders/$id/status") {
            setBody(mapOf("status" to status))
        }.body<PurchaseOrderDTO>()
    }

    suspend fun deletePurchaseOrder(id: String): Result<Unit> = Result.runCatching {
        httpClient.delete("$baseUrl/api/purchase-orders/$id")
    }

    suspend fun receivePurchaseOrder(id: String): Result<PurchaseOrderDTO> = Result.runCatching {
        httpClient.post("$baseUrl/api/purchase-orders/$id/receive").body<PurchaseOrderDTO>()
    }

    suspend fun generatePoNumber(): Result<String> = Result.runCatching {
        httpClient.get("$baseUrl/api/purchase-orders/generate-po-number").body<Map<String, String>>()["po_number"]
            ?: throw IllegalStateException("PO number not returned")
    }
}
