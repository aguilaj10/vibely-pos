package com.vibely.pos.backend.dto.request

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
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
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
    val name: String? = null,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val isActive: Boolean? = null
)
