package com.vibely.pos.shared.data.inventory.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDTO(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("color_hex")
    val color: String? = null,
    @SerialName("icon_name")
    val icon: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("product_count")
    val productCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)
