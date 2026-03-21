package com.vibely.pos.backend.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vibely.pos.backend.data.room.entity.ProductEntity

/**
 * Room DAO for product persistence operations.
 */
@Dao
interface ProductDao {
    /**
     * Retrieves a paginated list of products with optional filters.
     *
     * @param categoryId Optional category filter; null means no filter
     * @param isActive Optional active status filter; null means no filter
     * @param limit Maximum number of rows to return
     * @param offset Number of rows to skip (for pagination)
     * @return List of matching [ProductEntity] rows
     */
    @Query(
        """
        SELECT * FROM products
        WHERE (:categoryId IS NULL OR category_id = :categoryId)
          AND (:isActive IS NULL OR is_active = :isActive)
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getAll(
        categoryId: String?,
        isActive: Boolean?,
        limit: Int,
        offset: Int,
    ): List<ProductEntity>

    /**
     * Retrieves a single product by ID.
     *
     * @param id Product primary key
     * @return Matching [ProductEntity] or null if not found
     */
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProductEntity?

    /**
     * Searches products whose name, SKU, or barcode matches [pattern].
     *
     * @param pattern SQL LIKE pattern (e.g. "%query%")
     * @return Up to 50 matching [ProductEntity] rows
     */
    @Query(
        """
        SELECT * FROM products
        WHERE name LIKE :pattern OR sku LIKE :pattern OR barcode LIKE :pattern
        LIMIT 50
        """
    )
    suspend fun search(pattern: String): List<ProductEntity>

    /**
     * Inserts or replaces a product.
     *
     * @param entity Product to persist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProductEntity)

    /**
     * Updates an existing product.
     *
     * @param entity Product with updated fields
     */
    @Update
    suspend fun update(entity: ProductEntity)

    /**
     * Deletes a product by ID.
     *
     * @param id Product primary key to delete
     */
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun delete(id: String)
}
