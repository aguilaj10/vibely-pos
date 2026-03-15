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

class RemoteCategoryDataSource(private val httpClient: HttpClient, private val baseUrl: String = "http://localhost:8080") {

    suspend fun getAll(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<CategoryDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/categories") {
            isActive?.let { parameter("is_active", it) }
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body<List<CategoryDTO>>()
    }

    suspend fun getById(id: String): Result<CategoryDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/categories/$id").body<CategoryDTO>()
    }

    suspend fun create(dto: CategoryDTO): Result<CategoryDTO> = Result.runCatching {
        httpClient.post("$baseUrl/api/categories") {
            setBody(dto)
        }.body<CategoryDTO>()
    }

    suspend fun update(id: String, dto: CategoryDTO): Result<CategoryDTO> = Result.runCatching {
        httpClient.put("$baseUrl/api/categories/$id") {
            setBody(dto)
        }.body<CategoryDTO>()
    }

    suspend fun delete(id: String): Result<Unit> = Result.runCatching {
        httpClient.delete("$baseUrl/api/categories/$id").body<Unit>()
    }
}
