package com.vibely.pos.shared.domain.inventory.repository

import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.result.Result

interface CategoryRepository {
    suspend fun getAll(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<Category>>

    suspend fun getById(id: String): Result<Category>

    suspend fun create(category: Category): Result<Category>

    suspend fun update(category: Category): Result<Category>

    suspend fun delete(id: String): Result<Unit>

    suspend fun search(query: String): Result<List<Category>>
}
