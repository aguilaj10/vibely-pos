package com.vibely.pos.backend.data.room.datasource

import com.vibely.pos.backend.data.datasource.CategoryBackendDataSource
import com.vibely.pos.backend.data.room.dao.CategoryDao
import com.vibely.pos.backend.data.room.mapper.toEntity
import com.vibely.pos.backend.data.room.mapper.toJsonObject
import com.vibely.pos.backend.dto.request.CreateCategoryRequest
import com.vibely.pos.backend.dto.request.UpdateCategoryRequest
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.JsonObject

private const val ERROR_NOT_FOUND = "Category not found"

/**
 * Room/SQLite-backed implementation of [CategoryBackendDataSource].
 *
 * User-scoped filtering is not enforced in local mode — categories are treated as shared
 * resources across all operators on the local network.
 */
class RoomCategoryDataSource(
    private val categoryDao: CategoryDao,
) : CategoryBackendDataSource {
    /**
     * Retrieves all categories with optional filtering and pagination.
     *
     * @param userId ID of the user (not used for filtering in local mode)
     * @param isActive Optional active status filter
     * @param page Page number (1-indexed)
     * @param pageSize Number of items per page
     * @return Result containing list of categories
     */
    override suspend fun getAllCategories(
        userId: String,
        isActive: Boolean?,
        page: Int,
        pageSize: Int,
    ): Result<List<JsonObject>> =
        runCatchingSuspend {
            val offset = (page - 1) * pageSize
            categoryDao.getAll(isActive = isActive, limit = pageSize, offset = offset)
                .map { it.toJsonObject() }
        }

    /**
     * Retrieves a category by its ID.
     *
     * @param userId ID of the user (not used for filtering in local mode)
     * @param categoryId Category ID
     * @return Result containing the category or error
     */
    override suspend fun getCategoryById(userId: String, categoryId: String): Result<JsonObject> =
        runCatchingSuspend {
            categoryDao.getById(categoryId)?.toJsonObject() ?: error(ERROR_NOT_FOUND)
        }

    /**
     * Creates a new category.
     *
     * @param userId ID of the user creating the category (not enforced locally)
     * @param request Category creation parameters
     * @return Result containing created category data or error
     */
    override suspend fun createCategory(
        userId: String,
        request: CreateCategoryRequest,
    ): Result<JsonObject> =
        runCatchingSuspend {
            val entity = request.toEntity()
            categoryDao.insert(entity)
            entity.toJsonObject()
        }

    /**
     * Updates an existing category.
     *
     * @param userId ID of the user updating the category (not enforced locally)
     * @param categoryId ID of the category to update
     * @param request Category update parameters
     * @return Result containing updated category data or error
     */
    override suspend fun updateCategory(
        userId: String,
        categoryId: String,
        request: UpdateCategoryRequest,
    ): Result<JsonObject> =
        runCatchingSuspend {
            val existing = categoryDao.getById(categoryId) ?: error(ERROR_NOT_FOUND)
            val updated = existing.copy(
                name = request.name ?: existing.name,
                description = request.description ?: existing.description,
                isActive = request.isActive ?: existing.isActive,
            )
            categoryDao.update(updated)
            updated.toJsonObject()
        }

    /**
     * Deletes a category.
     *
     * @param userId ID of the user deleting the category (not enforced locally)
     * @param categoryId ID of the category to delete
     * @return Result indicating success or error
     */
    override suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> =
        runCatchingSuspend { categoryDao.delete(categoryId) }

    /**
     * Searches categories by name or description.
     *
     * @param userId ID of the user (not used in local mode)
     * @param query Search query string
     * @return Result containing list of matching categories
     */
    override suspend fun searchCategories(userId: String, query: String): Result<List<JsonObject>> =
        runCatchingSuspend { categoryDao.search("%$query%").map { it.toJsonObject() } }
}
