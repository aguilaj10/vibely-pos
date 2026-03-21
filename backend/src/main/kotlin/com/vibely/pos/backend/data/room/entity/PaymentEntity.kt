package com.vibely.pos.backend.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a payment record stored in the local SQLite database.
 */
@Suppress("UndocumentedPublicProperty")
@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "sale_id")
    val saleId: String,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "reference_number")
    val referenceNumber: String?,
    @ColumnInfo(name = "status")
    val status: String,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "processed_at")
    val processedAt: String,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "created_at")
    val createdAt: String,
)
