package com.vibely.pos.shared.data.currency.datasource

import com.vibely.pos.shared.data.currency.dto.CurrencyDTO
import com.vibely.pos.shared.data.currency.dto.CurrencyExchangeRateDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject

class RemoteCurrencyDataSource(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getAllExchangeRates(): Result<List<CurrencyExchangeRateDTO>> = Result.runCatching {
        val response: HttpResponse = httpClient.get("$baseUrl/api/currencies/exchange-rates")
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<List<CurrencyExchangeRateDTO>>()
    }

    suspend fun getExchangeRateById(id: String): Result<CurrencyExchangeRateDTO> = Result.runCatching {
        val response: HttpResponse = httpClient.get("$baseUrl/api/currencies/exchange-rates/$id")
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<CurrencyExchangeRateDTO>()
    }

    suspend fun createExchangeRate(exchangeRate: JsonObject): Result<CurrencyExchangeRateDTO> = Result.runCatching {
        val response: HttpResponse = httpClient.post("$baseUrl/api/currencies/exchange-rates") {
            setBody(exchangeRate)
        }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<CurrencyExchangeRateDTO>()
    }

    suspend fun updateExchangeRate(id: String, exchangeRate: JsonObject): Result<CurrencyExchangeRateDTO> = Result.runCatching {
        val response: HttpResponse = httpClient.put("$baseUrl/api/currencies/exchange-rates/$id") {
            setBody(exchangeRate)
        }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<CurrencyExchangeRateDTO>()
    }

    suspend fun deleteExchangeRate(id: String): Result<Unit> = Result.runCatching {
        val response: HttpResponse = httpClient.delete("$baseUrl/api/currencies/exchange-rates/$id")
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
    }

    suspend fun getActiveCurrencies(): Result<List<CurrencyDTO>> = Result.runCatching {
        val response: HttpResponse = httpClient.get("$baseUrl/api/currencies/active")
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<List<CurrencyDTO>>()
    }
}
