package com.vibely.pos.shared.data.inventory.mapper

import com.vibely.pos.shared.data.inventory.dto.CategoryDTO
import com.vibely.pos.shared.domain.inventory.entity.Category
import kotlin.time.Instant

/**
 * Mapper for converting between [CategoryDTO] and [Category] domain entity.
 */
object CategoryMapper {

    /**
     * Maps a [CategoryDTO] from the backend to a [Category] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if any field cannot be parsed.
     */
    fun toDomain(dto: CategoryDTO): Category = Category.create(
        id = dto.id,
        name = dto.name,
        description = dto.description,
        color = dto.color,
        icon = dto.icon,
        isActive = dto.isActive,
        productCount = dto.productCount,
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    /**
     * Maps a [Category] domain entity to a [CategoryDTO] for the backend.
     *
     * @param category The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(category: Category): CategoryDTO = CategoryDTO(
        id = category.id,
        name = category.name,
        description = category.description,
        color = category.color,
        icon = category.icon,
        isActive = category.isActive,
        productCount = category.productCount,
        createdAt = category.createdAt.toString(),
        updatedAt = category.updatedAt.toString(),
    )
}
