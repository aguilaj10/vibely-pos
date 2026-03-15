package com.vibely.pos.shared.data.sales.datasource

import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class RemoteProductDataSource(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun searchProducts(query: String): Result<List<ProductDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/products/search") {
            parameter("q", query)
        }.body<List<ProductDTO>>()
    }

    suspend fun getProductById(id: String): Result<ProductDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/products/$id").body<ProductDTO>()
    }

    suspend fun getAllProducts(
        categoryId: String? = null,
        isActive: Boolean? = null,
        lowStockOnly: Boolean = false,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<ProductDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/products") {
            categoryId?.let { parameter("category_id", it) }
            isActive?.let { parameter("is_active", it) }
            parameter("low_stock", lowStockOnly)
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body<List<ProductDTO>>()
    }

    suspend fun checkStock(productId: String, quantity: Int): Result<Boolean> = Result.runCatching {
        val product = httpClient.get("$baseUrl/api/products/$productId").body<ProductDTO>()
        product.currentStock >= quantity
    }
}
