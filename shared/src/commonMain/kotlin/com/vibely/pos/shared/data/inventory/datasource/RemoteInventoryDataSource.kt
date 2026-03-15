package com.vibely.pos.shared.data.inventory.datasource

import com.vibely.pos.shared.data.inventory.dto.InventoryTransactionDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class RemoteInventoryDataSource(private val httpClient: HttpClient, private val baseUrl: String = "http://localhost:8080") {

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
}
