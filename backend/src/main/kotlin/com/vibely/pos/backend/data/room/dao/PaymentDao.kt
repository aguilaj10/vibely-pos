package com.vibely.pos.backend.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vibely.pos.backend.data.room.entity.PaymentEntity

/**
 * Room DAO for payment persistence operations.
 */
@Dao
interface PaymentDao {
    /**
     * Retrieves all payments for a given sale, ordered by creation time.
     *
     * @param saleId Parent sale primary key
     * @return List of [PaymentEntity] belonging to the sale
     */
    @Query("SELECT * FROM payments WHERE sale_id = :saleId ORDER BY created_at ASC")
    suspend fun getBySaleId(saleId: String): List<PaymentEntity>

    /**
     * Retrieves the sum of completed payment amounts for a sale.
     *
     * @param saleId Parent sale primary key
     * @param completedStatus The status value that represents a completed payment
     * @return Sum of payment amounts, or 0.0 if no completed payments exist
     */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0) FROM payments
        WHERE sale_id = :saleId AND status = :completedStatus
        """
    )
    suspend fun sumCompletedPayments(saleId: String, completedStatus: String): Double

    /**
     * Inserts or replaces a payment.
     *
     * @param entity Payment to persist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentEntity)
}
