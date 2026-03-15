package com.vibely.pos.shared.data.sales.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(
    @SerialName("id")
    val id: String,
    @SerialName("sku")
    val sku: String,
    @SerialName("barcode")
    val barcode: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("cost_price")
    val costPrice: Double,
    @SerialName("selling_price")
    val sellingPrice: Double,
    @SerialName("current_stock")
    val currentStock: Int,
    @SerialName("min_stock_level")
    val minStockLevel: Int,
    @SerialName("unit")
    val unit: String = "unit",
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)
