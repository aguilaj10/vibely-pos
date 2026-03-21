package com.vibely.pos.backend.data.datasource

import com.vibely.pos.backend.dto.request.CreateCategoryRequest
import com.vibely.pos.backend.dto.request.UpdateCategoryRequest
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.JsonObject

/**
 * Data source abstraction for category persistence operations.
 *
 * Implementations provide either Supabase (remote) or Room/SQLite (local) backends.
 */
interface CategoryBackendDataSource {
    /**
     * Retrieves all categories with optional filtering and pagination.
     *
     * Categories are global resources shared across all users.
     *
     * @param userId ID of the user (kept for API compatibility but not used for filtering)
     * @param isActive Optional active status filter
     * @param page Page number (1-indexed)
     * @param pageSize Number of items per page
     * @return Result containing list of categories
     */
    suspend fun getAllCategories(
        userId: String,
        isActive: Boolean?,
        page: Int,
        pageSize: Int,
    ): Result<List<JsonObject>>

    /**
     * Retrieves a category by its ID.
     *
     * @param userId ID of the user (kept for API compatibility)
     * @param categoryId Category ID
     * @return Result containing the category or error
     */
    suspend fun getCategoryById(userId: String, categoryId: String): Result<JsonObject>

    /**
     * Creates a new category.
     *
     * @param userId ID of the user creating the category
     * @param request Category creation parameters
     * @return Result containing created category data or error
     */
    suspend fun createCategory(userId: String, request: CreateCategoryRequest): Result<JsonObject>

    /**
     * Updates an existing category.
     *
     * @param userId ID of the user updating the category
     * @param categoryId ID of the category to update
     * @param request Category update parameters
     * @return Result containing updated category data or error
     */
    suspend fun updateCategory(
        userId: String,
        categoryId: String,
        request: UpdateCategoryRequest,
    ): Result<JsonObject>

    /**
     * Deletes a category.
     *
     * @param userId ID of the user deleting the category
     * @param categoryId ID of the category to delete
     * @return Result indicating success or error
     */
    suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit>

    /**
     * Searches categories by name or description.
     *
     * @param userId ID of the user (kept for API compatibility)
     * @param query Search query string
     * @return Result containing list of matching categories
     */
    suspend fun searchCategories(userId: String, query: String): Result<List<JsonObject>>
}
