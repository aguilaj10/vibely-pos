package com.vibely.pos.shared.data.sales.datasource

import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RemoteSaleDataSource(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun createSale(request: CreateSaleRequest): Result<SaleDTO> = Result.runCatching {
        httpClient.post("$baseUrl/api/sales") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<SaleDTO>()
    }

    suspend fun getAllSales(
        startDate: String? = null,
        endDate: String? = null,
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<SaleDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/sales") {
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
            status?.let { parameter("status", it) }
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body<List<SaleDTO>>()
    }

    suspend fun getSaleById(id: String): Result<SaleDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/sales/$id").body<SaleDTO>()
    }

    suspend fun getSaleItems(saleId: String): Result<List<SaleItemDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/sales/$saleId/items").body<List<SaleItemDTO>>()
    }

    suspend fun updateSaleStatus(saleId: String, status: String): Result<SaleDTO> = Result.runCatching {
        httpClient.put("$baseUrl/api/sales/$saleId/status") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status))
        }.body<SaleDTO>()
    }
}
