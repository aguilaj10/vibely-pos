package com.vibely.pos.shared.domain.inventory.entity

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Domain entity representing a product category.
 *
 * Categories provide organization for products with visual attributes
 * like color and icon for UI presentation.
 *
 * Non-hierarchical for MVP - categories are flat (no parent-child relationships).
 *
 * @param id Unique identifier (UUID from database).
 * @param name Category name (displayed in UI).
 * @param description Optional description of the category.
 * @param color Hex color code for UI representation (e.g., "#FF5733").
 * @param icon Icon identifier for UI representation (e.g., "electronics", "food").
 * @param isActive Whether the category is active and visible.
 * @param productCount Number of products in this category (computed/denormalized).
 * @param createdAt When the category was created.
 * @param updatedAt When the category was last updated.
 */
data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val icon: String?,
    val isActive: Boolean = true,
    val productCount: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(id.isNotBlank()) { "Category ID cannot be blank" }
        require(name.isNotBlank()) { "Category name cannot be blank" }
        require(productCount >= 0) { "Product count cannot be negative" }
        color?.let {
            require(it.startsWith("#") && (it.length == 7 || it.length == 9)) {
                "Color must be a valid hex code (e.g., #FF5733 or #FF5733FF)"
            }
        }
    }

    /**
     * Returns true if the category has products.
     */
    val hasProducts: Boolean
        get() = productCount > 0

    /**
     * Returns a copy of this category with updated name.
     *
     * @param newName The new category name.
     */
    fun withName(newName: String): Category {
        require(newName.isNotBlank()) { "Category name cannot be blank" }
        return copy(
            name = newName,
            updatedAt = Clock.System.now(),
        )
    }

    /**
     * Returns a copy of this category with updated description.
     *
     * @param newDescription The new description.
     */
    fun withDescription(newDescription: String?): Category = copy(
        description = newDescription,
        updatedAt = Clock.System.now(),
    )

    /**
     * Returns a copy of this category with updated color.
     *
     * @param newColor The new hex color code.
     */
    fun withColor(newColor: String?): Category {
        newColor?.let {
            require(it.startsWith("#") && (it.length == 7 || it.length == 9)) {
                "Color must be a valid hex code (e.g., #FF5733 or #FF5733FF)"
            }
        }
        return copy(
            color = newColor,
            updatedAt = Clock.System.now(),
        )
    }

    /**
     * Returns a copy of this category with updated icon.
     *
     * @param newIcon The new icon identifier.
     */
    fun withIcon(newIcon: String?): Category = copy(
        icon = newIcon,
        updatedAt = Clock.System.now(),
    )

    /**
     * Returns a copy of this category as active/inactive.
     *
     * @param active Whether the category should be active.
     */
    fun withActive(active: Boolean): Category = copy(
        isActive = active,
        updatedAt = Clock.System.now(),
    )

    /**
     * Returns a copy of this category with updated product count.
     *
     * @param count The new product count.
     */
    fun withProductCount(count: Int): Category {
        require(count >= 0) { "Product count cannot be negative" }
        return copy(
            productCount = count,
            updatedAt = Clock.System.now(),
        )
    }

    companion object {
        /**
         * Creates a new Category instance with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            id: String,
            name: String,
            description: String? = null,
            color: String? = null,
            icon: String? = null,
            isActive: Boolean = true,
            productCount: Int = 0,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): Category = Category(
            id = id,
            name = name,
            description = description,
            color = color,
            icon = icon,
            isActive = isActive,
            productCount = productCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
