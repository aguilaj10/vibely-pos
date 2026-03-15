package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.inventory.repository.CategoryRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import kotlin.time.Clock

class CreateCategoryUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(
        id: String,
        name: String,
        description: String? = null,
        color: String? = null,
        icon: String? = null,
    ): Result<Category> {
        // Validate name is not blank
        if (name.isBlank()) {
            return Result.Error("Category name cannot be blank")
        }

        // Validate name length (2-100 characters)
        if (name.length < 2 || name.length > 100) {
            return Result.Error("Category name must be between 2 and 100 characters")
        }

        // Validate color is valid hex format if provided
        color?.let {
            if (it.isNotBlank()) {
                val isValidHex = it.startsWith("#") &&
                    (it.length == 7 || it.length == 9) &&
                    it.drop(1).all { char -> char.isDigit() || char in 'a'..'f' || char in 'A'..'F' }

                if (!isValidHex) {
                    return Result.Error("Color must be a valid hex format (#RRGGBB or #RRGGBBAA)")
                }
            }
        }

        // Validate icon is not blank if provided
        icon?.let {
            if (it.isBlank()) {
                return Result.Error("Icon cannot be blank if provided")
            }
        }

        // Check name uniqueness via repository.search
        return categoryRepository.search(name.trim()).flatMap { categories ->
            val existingCategory = categories.find {
                it.name.equals(name.trim(), ignoreCase = true)
            }

            if (existingCategory != null) {
                return@flatMap Result.Error("A category with name '$name' already exists")
            }

            // Create Category using Category.create() with Clock.System.now()
            val now = Clock.System.now()
            val category = Category.create(
                id = id,
                name = name.trim(),
                description = description,
                color = color,
                icon = icon,
                isActive = true,
                productCount = 0,
                createdAt = now,
                updatedAt = now,
            )

            // Call repository.create(category)
            categoryRepository.create(category)
        }
    }
}
