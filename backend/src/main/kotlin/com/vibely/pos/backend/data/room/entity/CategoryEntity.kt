package com.vibely.pos.backend.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a product category stored in the local SQLite database.
 */
@Suppress("UndocumentedPublicProperty")
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "parent_id")
    val parentId: String?,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    @ColumnInfo(name = "display_order")
    val displayOrder: Int,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
