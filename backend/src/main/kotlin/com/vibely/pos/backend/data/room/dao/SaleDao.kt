package com.vibely.pos.backend.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vibely.pos.backend.data.room.entity.SaleEntity
import com.vibely.pos.backend.data.room.entity.SaleItemEntity

/**
 * Room DAO for sale and sale item persistence operations.
 */
@Dao
interface SaleDao {
    /**
     * Retrieves a paginated list of sales with optional filters.
     *
     * @param startDate Optional lower bound for sale_date (inclusive); null means no lower bound
     * @param endDate Optional upper bound for sale_date (inclusive); null means no upper bound
     * @param status Optional status filter; null means no filter
     * @param limit Maximum number of rows to return
     * @param offset Number of rows to skip (for pagination)
     * @return List of matching [SaleEntity] rows ordered by sale date descending
     */
    @Suppress("LongParameterList") // Room @Query methods cannot use default values or wrapper objects
    @Query(
        """
        SELECT * FROM sales
        WHERE (:startDate IS NULL OR sale_date >= :startDate)
          AND (:endDate IS NULL OR sale_date <= :endDate)
          AND (:status IS NULL OR status = :status)
        ORDER BY sale_date DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getAll(
        startDate: String?,
        endDate: String?,
        status: String?,
        limit: Int,
        offset: Int,
    ): List<SaleEntity>

    /**
     * Retrieves a single sale by ID.
     *
     * @param id Sale primary key
     * @return Matching [SaleEntity] or null if not found
     */
    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SaleEntity?

    /**
     * Retrieves the total count of sales rows (used for invoice number generation).
     *
     * @return Total number of sale records
     */
    @Query("SELECT COUNT(*) FROM sales")
    suspend fun count(): Int

    /**
     * Inserts or replaces a sale.
     *
     * @param entity Sale to persist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(entity: SaleEntity)

    /**
     * Updates an existing sale.
     *
     * @param entity Sale with updated fields
     */
    @Update
    suspend fun updateSale(entity: SaleEntity)

    /**
     * Retrieves all line items for a given sale.
     *
     * @param saleId Parent sale primary key
     * @return List of [SaleItemEntity] belonging to the sale
     */
    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId")
    suspend fun getItemsBySaleId(saleId: String): List<SaleItemEntity>

    /**
     * Inserts or replaces a sale line item.
     *
     * @param entity Sale item to persist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(entity: SaleItemEntity)
}
