package com.vibely.pos.shared.data.inventory.datasource

import com.vibely.pos.shared.data.inventory.dto.CategoryDTO
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
import io.ktor.http.isSuccess

class RemoteCategoryDataSource(private val httpClient: HttpClient, private val baseUrl: String = "http://localhost:8080") {

    suspend fun getAll(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<CategoryDTO>> = Result.runCatching {
        val response: HttpResponse = httpClient.get("$baseUrl/api/categories") {
            isActive?.let { parameter("is_active", it) }
            parameter("page", page)
            parameter("page_size", pageSize)
        }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<List<CategoryDTO>>()
    }

    suspend fun getById(id: String): Result<CategoryDTO> = Result.runCatching {
        val response: HttpResponse = httpClient.get("$baseUrl/api/categories/$id")
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<CategoryDTO>()
    }

    suspend fun create(dto: CategoryDTO): Result<CategoryDTO> = Result.runCatching {
        val response: HttpResponse = httpClient.post("$baseUrl/api/categories") {
            setBody(dto)
        }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<CategoryDTO>()
    }

    suspend fun update(id: String, dto: CategoryDTO): Result<CategoryDTO> = Result.runCatching {
        val response: HttpResponse = httpClient.put("$baseUrl/api/categories/$id") {
            setBody(dto)
        }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<CategoryDTO>()
    }

    suspend fun delete(id: String): Result<Unit> = Result.runCatching {
        val response: HttpResponse = httpClient.delete("$baseUrl/api/categories/$id")
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }
        response.body<Unit>()
    }
}
