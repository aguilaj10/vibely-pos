package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.inventory.repository.CategoryRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import kotlin.time.Clock

class UpdateCategoryUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(id: String, name: String, description: String?, color: String?, icon: String?, isActive: Boolean): Result<Category> {
        // Validate id is not blank
        if (id.isBlank()) {
            return Result.Error("Category ID cannot be blank")
        }

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

        // Fetch existing via repository.getById
        return categoryRepository.getById(id).flatMap { existingCategory ->
            // Check for name uniqueness (excluding current category)
            categoryRepository.search(name.trim()).flatMap { categories ->
                val duplicateName = categories.find {
                    it.id != id && it.name.equals(name.trim(), ignoreCase = true)
                }

                if (duplicateName != null) {
                    return@flatMap Result.Error("A category with name '$name' already exists")
                }

                // Update using .copy() with updatedAt = Clock.System.now()
                val updatedCategory = existingCategory.copy(
                    name = name.trim(),
                    description = description,
                    color = color,
                    icon = icon,
                    isActive = isActive,
                    updatedAt = Clock.System.now(),
                )

                // Call repository.update(category)
                categoryRepository.update(updatedCategory)
            }
        }
    }
}
