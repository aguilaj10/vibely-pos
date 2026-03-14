package com.vibely.pos.shared.data.dashboard.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for low stock product from the backend.
 *
 * Represents a product with current stock below minimum threshold.
 */
@Serializable
data class LowStockProductDTO(
    @SerialName("id")
    val id: String,

    @SerialName("sku")
    val sku: String,

    @SerialName("name")
    val name: String,

    @SerialName("current_stock")
    val currentStock: Int,

    @SerialName("min_stock_level")
    val minStockLevel: Int,

    @SerialName("selling_price_cents")
    val sellingPriceCents: Long,

    @SerialName("category_name")
    val categoryName: String?,
)
