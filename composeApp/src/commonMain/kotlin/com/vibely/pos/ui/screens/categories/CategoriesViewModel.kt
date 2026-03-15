package com.vibely.pos.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.inventory.entity.Category
import com.vibely.pos.shared.domain.inventory.usecase.GetCategoriesUseCase
import com.vibely.pos.shared.domain.result.Result
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
    val totalCategories: Int = 0,
    val totalProducts: Int = 0,
    val avgPerCategory: Int = 0,
    val largestCategory: String = "",
)

class CategoriesViewModel(private val getCategoriesUseCase: GetCategoriesUseCase) : ViewModel() {

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
        // For now, filter locally since search is not implemented in repository
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

    fun onDeleteCategory(categoryId: String) {
        _state.update {
            it.copy(errorMessage = "Delete functionality not yet implemented for: $categoryId")
        }
    }

    fun onEditCategory(categoryId: String) {
        _state.update {
            it.copy(errorMessage = "Edit navigation not yet implemented for: $categoryId")
        }
    }

    fun onAddCategory() {
        _state.update {
            it.copy(errorMessage = "Add category navigation not yet implemented")
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }
}
