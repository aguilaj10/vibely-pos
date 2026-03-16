package com.vibely.pos.shared.data.inventory.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.inventory.datasource.RemoteCategoryDataSource
import com.vibely.pos.shared.data.inventory.mapper.CategoryMapper
import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.inventory.repository.CategoryRepository
import com.vibely.pos.shared.domain.result.Result

class CategoryRepositoryImpl(private val remoteDataSource: RemoteCategoryDataSource) :
    BaseRepository(),
    CategoryRepository {

    override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Category>> =
        mapList(remoteDataSource.getAll(isActive, page, pageSize), CategoryMapper::toDomain)

    override suspend fun getById(id: String): Result<Category> = mapSingle(remoteDataSource.getById(id), CategoryMapper::toDomain)

    override suspend fun create(category: Category): Result<Category> = mapSingle(
        remoteDataSource.create(CategoryMapper.toDTO(category)),
        CategoryMapper::toDomain,
    )

    override suspend fun update(category: Category): Result<Category> = mapSingle(
        remoteDataSource.update(category.id, CategoryMapper.toDTO(category)),
        CategoryMapper::toDomain,
    )

    override suspend fun delete(id: String): Result<Unit> = remoteDataSource.delete(id)

    override suspend fun search(query: String): Result<List<Category>> = mapList(
        remoteDataSource.search(query),
        CategoryMapper::toDomain,
    )
}
