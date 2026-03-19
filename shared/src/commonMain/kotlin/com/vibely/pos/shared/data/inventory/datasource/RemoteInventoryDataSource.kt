package com.vibely.pos.shared.data.inventory.datasource

import com.vibely.pos.shared.data.inventory.dto.InventoryTransactionDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RemoteInventoryDataSource(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getTransactions(
        productId: String? = null,
        type: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<InventoryTransactionDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/inventory/transactions") {
            productId?.let { parameter("product_id", it) }
            type?.let { parameter("type", it) }
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body<List<InventoryTransactionDTO>>()
    }

    suspend fun getTransactionById(id: String): Result<InventoryTransactionDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/inventory/transactions/$id")
            .body<InventoryTransactionDTO>()
    }

    suspend fun createTransaction(dto: InventoryTransactionDTO): Result<InventoryTransactionDTO> = Result.runCatching {
        httpClient.post("$baseUrl/api/inventory/transactions") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body<InventoryTransactionDTO>()
    }
}
