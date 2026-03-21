package com.vibely.pos.backend.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a product stored in the local SQLite database.
 */
@Suppress("UndocumentedPublicProperty")
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "sku")
    val sku: String,
    @ColumnInfo(name = "barcode")
    val barcode: String?,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "category_id")
    val categoryId: String?,
    @ColumnInfo(name = "category_name")
    val categoryName: String?,
    @ColumnInfo(name = "cost_price")
    val costPrice: Double,
    @ColumnInfo(name = "cost_currency_code")
    val costCurrencyCode: String?,
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double,
    @ColumnInfo(name = "current_stock")
    val currentStock: Int,
    @ColumnInfo(name = "min_stock_level")
    val minStockLevel: Int,
    @ColumnInfo(name = "unit")
    val unit: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
