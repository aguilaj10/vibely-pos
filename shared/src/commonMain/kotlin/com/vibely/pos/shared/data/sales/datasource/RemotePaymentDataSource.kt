package com.vibely.pos.shared.data.sales.datasource

import com.vibely.pos.shared.data.sales.dto.CreatePaymentRequest
import com.vibely.pos.shared.data.sales.dto.PaymentDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RemotePaymentDataSource(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun recordPayment(request: CreatePaymentRequest): Result<PaymentDTO> = Result.runCatching {
        httpClient
            .post("$baseUrl/api/payments") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<PaymentDTO>()
    }

    suspend fun getPaymentsBySale(saleId: String): Result<List<PaymentDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/sales/$saleId/payments").body<List<PaymentDTO>>()
    }
}
