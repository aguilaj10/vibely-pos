package com.vibely.pos.shared.data.supplier.datasource

import com.vibely.pos.shared.data.supplier.dto.SupplierDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject

class RemoteSupplierDataSource(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getAllSuppliers(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<SupplierDTO>> = Result.runCatching {
        val response: HttpResponse =
            httpClient.get("$baseUrl/api/suppliers") {
                isActive?.let { parameter("is_active", it) }
                parameter("page", page)
                parameter("page_size", pageSize)
            }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<List<SupplierDTO>>()
    }

    suspend fun getSupplierById(id: String): Result<SupplierDTO> = Result.runCatching {
        val response: HttpResponse = httpClient.get("$baseUrl/api/suppliers/$id")
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<SupplierDTO>()
    }

    suspend fun createSupplier(supplier: JsonObject): Result<SupplierDTO> = Result.runCatching {
        val response: HttpResponse =
            httpClient.post("$baseUrl/api/suppliers") {
                contentType(ContentType.Application.Json)
                setBody(supplier)
            }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<SupplierDTO>()
    }

    suspend fun updateSupplier(id: String, supplier: JsonObject): Result<SupplierDTO> = Result.runCatching {
        val response: HttpResponse =
            httpClient.put("$baseUrl/api/suppliers/$id") {
                contentType(ContentType.Application.Json)
                setBody(supplier)
            }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<SupplierDTO>()
    }

    suspend fun deleteSupplier(id: String): Result<Unit> = Result.runCatching {
        val response: HttpResponse = httpClient.delete("$baseUrl/api/suppliers/$id")
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
    }

    suspend fun searchSuppliers(query: String): Result<List<SupplierDTO>> = Result.runCatching {
        val response: HttpResponse =
            httpClient.get("$baseUrl/api/suppliers") {
                parameter("search", query)
            }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<List<SupplierDTO>>()
    }
}
