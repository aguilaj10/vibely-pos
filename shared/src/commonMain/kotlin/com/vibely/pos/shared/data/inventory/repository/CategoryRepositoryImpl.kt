package com.vibely.pos.shared.data.inventory.repository

import com.vibely.pos.shared.data.inventory.datasource.RemoteCategoryDataSource
import com.vibely.pos.shared.data.inventory.mapper.CategoryMapper
import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.inventory.repository.CategoryRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.map

class CategoryRepositoryImpl(private val remoteDataSource: RemoteCategoryDataSource) : CategoryRepository {

    override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Category>> =
        remoteDataSource.getAll(isActive, page, pageSize)
            .map { dtos -> dtos.map { CategoryMapper.toDomain(it) } }

    override suspend fun getById(id: String): Result<Category> = remoteDataSource.getById(id)
        .map { CategoryMapper.toDomain(it) }

    override suspend fun create(category: Category): Result<Category> {
        TODO("Not implemented - will be added in inventory management phase")
    }

    override suspend fun update(category: Category): Result<Category> {
        TODO("Not implemented - will be added in inventory management phase")
    }

    override suspend fun delete(id: String): Result<Unit> = remoteDataSource.delete(id)

    override suspend fun search(query: String): Result<List<Category>> {
        TODO("Not implemented - will be added in inventory management phase")
    }
}
