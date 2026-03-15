package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.dto.request.CreateCategoryRequest
import com.vibely.pos.backend.dto.request.UpdateCategoryRequest
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val TABLE_CATEGORIES = "categories"
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
                        eq(DatabaseColumns.USER_ID, userId)
                        isActive?.let { eq(DatabaseColumns.IS_ACTIVE, it) }
                    }
                    order(DatabaseColumns.NAME, Order.ASCENDING)
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
                        eq(DatabaseColumns.ID, categoryId)
                        eq(DatabaseColumns.USER_ID, userId)
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
                put(DatabaseColumns.USER_ID, userId)
                put(DatabaseColumns.NAME, request.name)
                request.description?.let { put(DatabaseColumns.DESCRIPTION, it) }
                request.color?.let { put(DatabaseColumns.COLOR, it) }
                request.icon?.let { put(DatabaseColumns.ICON, it) }
                put(DatabaseColumns.IS_ACTIVE, request.isActive)
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
                request.name?.let { put(DatabaseColumns.NAME, it) }
                request.description?.let { put(DatabaseColumns.DESCRIPTION, it) }
                request.color?.let { put(DatabaseColumns.COLOR, it) }
                request.icon?.let { put(DatabaseColumns.ICON, it) }
                request.isActive?.let { put(DatabaseColumns.IS_ACTIVE, it) }
            }

            val category = supabaseClient.from(TABLE_CATEGORIES)
                .update(data) {
                    filter {
                        eq(DatabaseColumns.ID, categoryId)
                        eq(DatabaseColumns.USER_ID, userId)
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
                        eq(DatabaseColumns.ID, categoryId)
                        eq(DatabaseColumns.USER_ID, userId)
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
