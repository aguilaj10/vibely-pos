package com.vibely.pos.backend.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vibely.pos.backend.data.room.entity.CategoryEntity

/**
 * Room DAO for category persistence operations.
 */
@Dao
interface CategoryDao {
    /**
     * Retrieves a paginated list of categories with an optional active status filter.
     *
     * @param isActive Optional active status filter; null means no filter
     * @param limit Maximum number of rows to return
     * @param offset Number of rows to skip (for pagination)
     * @return List of matching [CategoryEntity] rows
     */
    @Query(
        """
        SELECT * FROM categories
        WHERE (:isActive IS NULL OR is_active = :isActive)
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getAll(isActive: Boolean?, limit: Int, offset: Int): List<CategoryEntity>

    /**
     * Retrieves a single category by ID.
     *
     * @param id Category primary key
     * @return Matching [CategoryEntity] or null if not found
     */
    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    /**
     * Searches categories whose name or description matches [pattern].
     *
     * @param pattern SQL LIKE pattern (e.g. "%query%")
     * @return All matching [CategoryEntity] rows ordered by name
     */
    @Query(
        """
        SELECT * FROM categories
        WHERE name LIKE :pattern OR description LIKE :pattern
        ORDER BY name ASC
        """
    )
    suspend fun search(pattern: String): List<CategoryEntity>

    /**
     * Inserts or replaces a category.
     *
     * @param entity Category to persist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CategoryEntity)

    /**
     * Updates an existing category.
     *
     * @param entity Category with updated fields
     */
    @Update
    suspend fun update(entity: CategoryEntity)

    /**
     * Deletes a category by ID.
     *
     * @param id Category primary key to delete
     */
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)
}
