package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request data classes for Category operations.
 */

/**
 * Request body for creating a category.
 *
 * @property name Category name (required)
 * @property description Optional description
 * @property color Optional color hex code
 * @property icon Optional icon name
 * @property isActive Whether category is active (default: true)
 */
@Serializable
data class CreateCategoryRequest(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("color")
    val color: String? = null,
    @SerialName("icon")
    val icon: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

/**
 * Request body for updating a category.
 *
 * @property name Optional new name
 * @property description Optional new description
 * @property color Optional new color hex code
 * @property icon Optional new icon name
 * @property isActive Optional new active status
 */
@Serializable
data class UpdateCategoryRequest(
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("color")
    val color: String? = null,
    @SerialName("icon")
    val icon: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null
)
