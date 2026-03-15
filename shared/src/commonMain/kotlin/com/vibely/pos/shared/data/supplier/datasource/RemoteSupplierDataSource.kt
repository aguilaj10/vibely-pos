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
import kotlinx.serialization.json.JsonObject

class RemoteSupplierDataSource(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getAllSuppliers(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<SupplierDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/suppliers") {
            isActive?.let { parameter("is_active", it) }
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body<List<SupplierDTO>>()
    }

    suspend fun getSupplierById(id: String): Result<SupplierDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/suppliers/$id").body<SupplierDTO>()
    }

    suspend fun createSupplier(supplier: JsonObject): Result<SupplierDTO> = Result.runCatching {
        httpClient.post("$baseUrl/api/suppliers") {
            setBody(supplier)
        }.body<SupplierDTO>()
    }

    suspend fun updateSupplier(id: String, supplier: JsonObject): Result<SupplierDTO> = Result.runCatching {
        httpClient.put("$baseUrl/api/suppliers/$id") {
            setBody(supplier)
        }.body<SupplierDTO>()
    }

    suspend fun deleteSupplier(id: String): Result<Unit> = Result.runCatching {
        httpClient.delete("$baseUrl/api/suppliers/$id")
    }

    suspend fun searchSuppliers(query: String): Result<List<SupplierDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/suppliers") {
            parameter("search", query)
        }.body<List<SupplierDTO>>()
    }
}
