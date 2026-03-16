package com.vibely.pos.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.inventory.usecase.CreateCategoryUseCase
import com.vibely.pos.shared.domain.inventory.usecase.GetCategoriesUseCase
import com.vibely.pos.shared.domain.inventory.usecase.UpdateCategoryUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.ui.dialogs.CategoryFormData
import com.vibely.pos.ui.util.randomUuidString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriesState(
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalCategories: Int = 0,
    val totalProducts: Int = 0,
    val avgPerCategory: Int = 0,
    val largestCategory: String = "",
    val showCategoryForm: Boolean = false,
    val editingCategoryId: String? = null,
    val confirmDeleteCategoryId: String? = null,
)

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getCategoriesUseCase()) {
                is Result.Success -> {
                    val categories = result.data
                    val totalProducts = categories.sumOf { it.productCount }
                    val avgPerCategory = if (categories.isNotEmpty()) totalProducts / categories.size else 0
                    val largestCategory = categories.maxByOrNull { it.productCount }?.name ?: ""

                    _state.update {
                        it.copy(
                            categories = categories,
                            isLoading = false,
                            totalCategories = categories.size,
                            totalProducts = totalProducts,
                            avgPerCategory = avgPerCategory,
                            largestCategory = largestCategory,
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        if (query.isBlank()) {
            loadCategories()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            searchCategories(query)
        }
    }

    private suspend fun searchCategories(query: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        val allCategories = getCategoriesUseCase()
        when (allCategories) {
            is Result.Success -> {
                val filtered = allCategories.data.filter { category ->
                    category.name.contains(query, ignoreCase = true) ||
                        category.description?.contains(query, ignoreCase = true) == true
                }
                val totalProducts = filtered.sumOf { it.productCount }
                val avgPerCategory = if (filtered.isNotEmpty()) totalProducts / filtered.size else 0
                val largestCategory = filtered.maxByOrNull { it.productCount }?.name ?: ""

                _state.update {
                    it.copy(
                        categories = filtered,
                        isLoading = false,
                        totalCategories = filtered.size,
                        totalProducts = totalProducts,
                        avgPerCategory = avgPerCategory,
                        largestCategory = largestCategory,
                    )
                }
            }
            is Result.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = allCategories.message,
                    )
                }
            }
        }
    }

    fun onClearSearch() {
        _state.update { it.copy(searchQuery = "") }
        loadCategories()
    }

    fun onAddCategory() {
        _state.update { it.copy(showCategoryForm = true, editingCategoryId = null) }
    }

    fun onEditCategory(categoryId: String) {
        _state.update { it.copy(showCategoryForm = true, editingCategoryId = categoryId) }
    }

    fun onDeleteCategory(categoryId: String) {
        _state.update { it.copy(confirmDeleteCategoryId = categoryId) }
    }

    fun onDismissCategoryForm() {
        _state.update { it.copy(showCategoryForm = false, editingCategoryId = null) }
    }

    fun onConfirmDeleteCategory() {
        val categoryId = _state.value.confirmDeleteCategoryId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, confirmDeleteCategoryId = null) }

            // Soft delete: set isActive to false
            val category = _state.value.categories.find { it.id == categoryId }
            if (category == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Category not found",
                    )
                }
                return@launch
            }

            val result = updateCategoryUseCase(
                id = categoryId,
                name = category.name,
                description = category.description,
                color = category.color,
                icon = category.icon,
                isActive = false,
            )

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Category deleted successfully",
                        )
                    }
                    loadCategories()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onDismissDeleteConfirmation() {
        _state.update { it.copy(confirmDeleteCategoryId = null) }
    }

    fun onSaveCategory(formData: CategoryFormData) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val isEdit = _state.value.editingCategoryId != null

            val result = if (isEdit) {
                val category = _state.value.categories.find { it.id == formData.id }
                updateCategoryUseCase(
                    id = formData.id,
                    name = formData.name,
                    description = formData.description.ifBlank { null },
                    color = formData.color.ifBlank { null },
                    icon = formData.icon.ifBlank { null },
                    isActive = formData.isActive,
                )
            } else {
                createCategoryUseCase(
                    id = randomUuidString(),
                    name = formData.name,
                    description = formData.description.ifBlank { null },
                    color = formData.color.ifBlank { null },
                    icon = formData.icon.ifBlank { null },
                )
            }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showCategoryForm = false,
                            editingCategoryId = null,
                            successMessage = if (isEdit) "Category updated successfully" else "Category created successfully",
                        )
                    }
                    loadCategories()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun getEditingCategory(): CategoryFormData? {
        val categoryId = _state.value.editingCategoryId ?: return null
        val category = _state.value.categories.find { it.id == categoryId } ?: return null

        return CategoryFormData(
            id = category.id,
            name = category.name,
            description = category.description ?: "",
            color = category.color ?: "",
            icon = category.icon ?: "",
            isActive = category.isActive,
        )
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismiss() {
        _state.update { it.copy(successMessage = null) }
    }
}
