package com.vibely.pos.backend.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a customer stored in the local SQLite database.
 */
@Suppress("UndocumentedPublicProperty")
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "customer_code")
    val code: String,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    @ColumnInfo(name = "email")
    val email: String?,
    @ColumnInfo(name = "phone")
    val phone: String?,
    @ColumnInfo(name = "loyalty_points")
    val loyaltyPoints: Int,
    @ColumnInfo(name = "loyalty_tier")
    val loyaltyTier: String?,
    @ColumnInfo(name = "total_purchases")
    val totalPurchases: Double,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
