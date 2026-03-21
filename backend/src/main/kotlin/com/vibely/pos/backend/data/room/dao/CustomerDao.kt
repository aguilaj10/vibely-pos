package com.vibely.pos.backend.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vibely.pos.backend.data.room.entity.CustomerEntity

/**
 * Room DAO for customer persistence operations.
 */
@Dao
interface CustomerDao {
    /**
     * Retrieves a paginated list of customers with optional filters.
     *
     * When [searchPattern] is null, no text search is applied.
     *
     * @param isActive Optional active status filter; null means no filter
     * @param searchPattern Optional SQL LIKE pattern applied to name, email, phone, and code
     * @param limit Maximum number of rows to return
     * @param offset Number of rows to skip (for pagination)
     * @return List of matching [CustomerEntity] rows
     */
    @Query(
        """
        SELECT * FROM customers
        WHERE (:isActive IS NULL OR is_active = :isActive)
          AND (:searchPattern IS NULL
               OR full_name LIKE :searchPattern
               OR email LIKE :searchPattern
               OR phone LIKE :searchPattern
               OR customer_code LIKE :searchPattern)
        ORDER BY full_name ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getAll(
        isActive: Boolean?,
        searchPattern: String?,
        limit: Int,
        offset: Int,
    ): List<CustomerEntity>

    /**
     * Retrieves a single customer by ID.
     *
     * @param id Customer primary key
     * @return Matching [CustomerEntity] or null if not found
     */
    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CustomerEntity?

    /**
     * Inserts or replaces a customer.
     *
     * @param entity Customer to persist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CustomerEntity)

    /**
     * Updates an existing customer.
     *
     * @param entity Customer with updated fields
     */
    @Update
    suspend fun update(entity: CustomerEntity)

    /**
     * Deletes a customer by ID.
     *
     * @param id Customer primary key to delete
     */
    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun delete(id: String)
}
