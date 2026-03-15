package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.inventory.repository.CategoryRepository
import com.vibely.pos.shared.domain.result.Result

class GetCategoriesUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<Category>> =
        categoryRepository.getAll(isActive, page, pageSize)
}
