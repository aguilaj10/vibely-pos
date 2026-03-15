package com.vibely.pos.shared.domain.sales.repository

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product

interface ProductRepository {
    suspend fun search(query: String): Result<List<Product>>

    suspend fun getById(id: String): Result<Product>

    suspend fun checkStock(productId: String, quantity: Int): Result<Boolean>

    suspend fun getAll(
        categoryId: String? = null,
        isActive: Boolean? = null,
        lowStockOnly: Boolean = false,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<List<Product>>

    suspend fun create(product: Product): Result<Product>

    suspend fun update(product: Product): Result<Product>

    suspend fun delete(id: String): Result<Unit>
}
