package com.vibely.pos.backend.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a sale header stored in the local SQLite database.
 */
@Suppress("UndocumentedPublicProperty")
@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String,
    @ColumnInfo(name = "customer_id")
    val customerId: String?,
    @ColumnInfo(name = "cashier_id")
    val cashierId: String,
    @ColumnInfo(name = "subtotal")
    val subtotal: Double,
    @ColumnInfo(name = "tax_amount")
    val taxAmount: Double,
    @ColumnInfo(name = "discount_amount")
    val discountAmount: Double,
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "payment_status")
    val paymentStatus: String,
    @ColumnInfo(name = "notes")
    val notes: String?,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "sale_date")
    val saleDate: String,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    /** ISO-8601 instant stored as String. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
