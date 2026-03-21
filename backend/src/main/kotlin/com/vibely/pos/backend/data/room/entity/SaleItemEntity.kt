package com.vibely.pos.backend.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a line item within a sale stored in the local SQLite database.
 */
@Suppress("UndocumentedPublicProperty")
@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "sale_id")
    val saleId: String,
    @ColumnInfo(name = "product_id")
    val productId: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,
    @ColumnInfo(name = "discount_amount")
    val discountAmount: Double,
    @ColumnInfo(name = "subtotal")
    val subtotal: Double,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "created_at")
    val createdAt: String,
)
