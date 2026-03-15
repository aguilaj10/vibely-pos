package com.vibely.pos.backend.services

import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val TABLE_CATEGORIES = "categories"
private const val COLUMN_NAME = "name"
private const val COLUMN_DESCRIPTION = "description"
private const val COLUMN_COLOR = "color"
private const val COLUMN_ICON = "icon"
private const val COLUMN_IS_ACTIVE = "is_active"
private const val COLUMN_USER_ID = "user_id"
private const val COLUMN_ID = "id"
private const val ERROR_FETCH_FAILED = "Failed to fetch categories"
private const val ERROR_CATEGORY_NOT_FOUND = "Category not found"
private const val ERROR_CREATE_FAILED = "Failed to create category"
private const val ERROR_UPDATE_FAILED = "Failed to update category"
private const val ERROR_DELETE_FAILED = "Failed to delete category"

/**
 * Service for managing category operations.
 */
class CategoryService(
    private val supabaseClient: SupabaseClient,
) {
    /**
     * Request parameters for creating a category.
     *
     * @property name Category name
     * @property description Optional description
     * @property color Optional color hex code
     * @property icon Optional icon name
     * @property isActive Whether category is active
     */
    data class CreateCategoryRequest(
        val name: String,
        val description: String?,
        val color: String?,
        val icon: String?,
        val isActive: Boolean
    )

    /**
     * Request parameters for updating a category.
     *
     * @property name Optional new name
     * @property description Optional new description
     * @property color Optional new color
     * @property icon Optional new icon
     * @property isActive Optional new active status
     */
    data class UpdateCategoryRequest(
        val name: String?,
        val description: String?,
        val color: String?,
        val icon: String?,
        val isActive: Boolean?
    )

    /**
     * Retrieves all categories with optional filtering and pagination.
     *
     * @param userId ID of the user
     * @param isActive Optional active status filter
     * @param page Page number (1-indexed)
     * @param pageSize Number of items per page
     * @return Result containing list of categories
     */
    suspend fun getAllCategories(
        userId: String,
        isActive: Boolean?,
        page: Int,
        pageSize: Int
    ): Result<List<JsonObject>> {
        return try {
            val categories = supabaseClient.from(TABLE_CATEGORIES)
                .select {
                    filter {
                        eq(COLUMN_USER_ID, userId)
                        isActive?.let { eq(COLUMN_IS_ACTIVE, it) }
                    }
                    order(COLUMN_NAME, Order.ASCENDING)
                    range(
                        from = ((page - 1) * pageSize).toLong(),
                        to = (page * pageSize - 1).toLong()
                    )
                }
                .decodeList<JsonObject>()

            Result.Success(categories)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_FETCH_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_FETCH_FAILED: ${e.message}", cause = e)
        }
    }

    /**
     * Retrieves a category by its ID.
     *
     * @param userId ID of the user
     * @param categoryId Category ID
     * @return Result containing the category or error
     */
    suspend fun getCategoryById(userId: String, categoryId: String): Result<JsonObject> {
        return try {
            val category = supabaseClient.from(TABLE_CATEGORIES)
                .select {
                    filter {
                        eq(COLUMN_ID, categoryId)
                        eq(COLUMN_USER_ID, userId)
                    }
                }
                .decodeSingle<JsonObject>()

            Result.Success(category)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error(ERROR_CATEGORY_NOT_FOUND, cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error(ERROR_CATEGORY_NOT_FOUND, cause = e)
        }
    }

    /**
     * Creates a new category.
     *
     * @param userId ID of the user creating the category
     * @param request Category creation parameters
     * @return Result containing created category or error
     */
    suspend fun createCategory(
        userId: String,
        request: CreateCategoryRequest
    ): Result<JsonObject> {
        return try {
            val data = buildJsonObject {
                put(COLUMN_USER_ID, userId)
                put(COLUMN_NAME, request.name)
                request.description?.let { put(COLUMN_DESCRIPTION, it) }
                request.color?.let { put(COLUMN_COLOR, it) }
                request.icon?.let { put(COLUMN_ICON, it) }
                put(COLUMN_IS_ACTIVE, request.isActive)
            }

            val category = supabaseClient.from(TABLE_CATEGORIES)
                .insert(data) {
                    select()
                }
                .decodeSingle<JsonObject>()

            Result.Success(category)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_CREATE_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_CREATE_FAILED: ${e.message}", cause = e)
        }
    }

    /**
     * Updates an existing category.
     *
     * @param userId ID of the user updating the category
     * @param categoryId ID of the category to update
     * @param request Category update parameters
     * @return Result containing updated category or error
     */
    suspend fun updateCategory(
        userId: String,
        categoryId: String,
        request: UpdateCategoryRequest
    ): Result<JsonObject> {
        return try {
            val data = buildJsonObject {
                request.name?.let { put(COLUMN_NAME, it) }
                request.description?.let { put(COLUMN_DESCRIPTION, it) }
                request.color?.let { put(COLUMN_COLOR, it) }
                request.icon?.let { put(COLUMN_ICON, it) }
                request.isActive?.let { put(COLUMN_IS_ACTIVE, it) }
            }

            val category = supabaseClient.from(TABLE_CATEGORIES)
                .update(data) {
                    filter {
                        eq(COLUMN_ID, categoryId)
                        eq(COLUMN_USER_ID, userId)
                    }
                    select()
                }
                .decodeSingle<JsonObject>()

            Result.Success(category)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_UPDATE_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_UPDATE_FAILED: ${e.message}", cause = e)
        }
    }

    /**
     * Deletes a category.
     *
     * @param userId ID of the user deleting the category
     * @param categoryId ID of the category to delete
     * @return Result indicating success or error
     */
    suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> {
        return try {
            supabaseClient.from(TABLE_CATEGORIES)
                .delete() {
                    filter {
                        eq(COLUMN_ID, categoryId)
                        eq(COLUMN_USER_ID, userId)
                    }
                }

            Result.Success(Unit)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Result.Error("$ERROR_DELETE_FAILED: ${e.message}", cause = e)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.Error("$ERROR_DELETE_FAILED: ${e.message}", cause = e)
        }
    }
}
