package com.vibely.pos.shared.data.customer.datasource

import com.vibely.pos.shared.data.customer.dto.CustomerDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject

class RemoteCustomerDataSource(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getAllCustomers(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<CustomerDTO>> = Result.runCatching {
        httpClient
            .get("$baseUrl/api/customers") {
                isActive?.let { parameter("is_active", it) }
                parameter("page", page)
                parameter("page_size", pageSize)
            }.body<List<CustomerDTO>>()
    }

    suspend fun getCustomerById(id: String): Result<CustomerDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/customers/$id").body<CustomerDTO>()
    }

    suspend fun createCustomer(customer: JsonObject): Result<CustomerDTO> = Result.runCatching {
        httpClient
            .post("$baseUrl/api/customers") {
                contentType(ContentType.Application.Json)
                setBody(customer)
            }.body<CustomerDTO>()
    }

    suspend fun updateCustomer(id: String, customer: JsonObject): Result<CustomerDTO> = Result.runCatching {
        httpClient
            .put("$baseUrl/api/customers/$id") {
                contentType(ContentType.Application.Json)
                setBody(customer)
            }.body<CustomerDTO>()
    }

    suspend fun deleteCustomer(id: String): Result<Unit> = Result.runCatching {
        httpClient.delete("$baseUrl/api/customers/$id")
    }

    suspend fun searchCustomers(query: String): Result<List<CustomerDTO>> = Result.runCatching {
        httpClient
            .get("$baseUrl/api/customers") {
                parameter("search", query)
            }.body<List<CustomerDTO>>()
    }

    suspend fun addLoyaltyPoints(id: String, points: Int): Result<CustomerDTO> = Result.runCatching {
        httpClient
            .post("$baseUrl/api/customers/$id/loyalty-points") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("points" to points))
            }.body<CustomerDTO>()
    }

    suspend fun getPurchaseHistory(customerId: String, page: Int = 1, pageSize: Int = 50): Result<List<Map<String, Any>>> = Result.runCatching {
        httpClient
            .get("$baseUrl/api/customers/$customerId/purchase-history") {
                parameter("page", page)
                parameter("page_size", pageSize)
            }.body<List<Map<String, Any>>>()
    }
}
