package com.vibely.pos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.inventory.usecase.GetAllProductsUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.usecase.SearchProductsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventoryState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val totalValue: Double = 0.0,
    val categoriesCount: Int = 0,
)

class InventoryViewModel(private val getAllProductsUseCase: GetAllProductsUseCase, private val searchProductsUseCase: SearchProductsUseCase) :
    ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAllProductsUseCase()) {
                is Result.Success -> {
                    val products = result.data
                    val lowStockCount = products.count { it.isLowStock }
                    val totalValue = products.sumOf { it.sellingPrice * it.currentStock }
                    val categoriesCount = products.mapNotNull { it.categoryId }.distinct().size

                    _state.update {
                        it.copy(
                            products = products,
                            isLoading = false,
                            totalProducts = products.size,
                            lowStockCount = lowStockCount,
                            totalValue = totalValue,
                            categoriesCount = categoriesCount,
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
            loadProducts()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            searchProducts(query)
        }
    }

    private suspend fun searchProducts(query: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        when (val result = searchProductsUseCase(query)) {
            is Result.Success -> {
                val products = result.data
                val lowStockCount = products.count { it.isLowStock }
                val totalValue = products.sumOf { it.sellingPrice * it.currentStock }
                val categoriesCount = products.mapNotNull { it.categoryId }.distinct().size

                _state.update {
                    it.copy(
                        products = products,
                        isLoading = false,
                        totalProducts = products.size,
                        lowStockCount = lowStockCount,
                        totalValue = totalValue,
                        categoriesCount = categoriesCount,
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

    fun onClearSearch() {
        _state.update { it.copy(searchQuery = "") }
        loadProducts()
    }

    fun onDeleteProduct(productId: String) {
        _state.update {
            it.copy(errorMessage = "Delete functionality not yet implemented")
        }
    }

    fun onEditProduct(productId: String) {
        _state.update {
            it.copy(errorMessage = "Edit navigation not yet implemented for: $productId")
        }
    }

    fun onAddProduct() {
        _state.update {
            it.copy(errorMessage = "Add product navigation not yet implemented")
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }
}

enum class StockStatus {
    Good,
    Medium,
    Low,
}

fun getStockStatus(product: Product): StockStatus = when {
    product.isLowStock -> StockStatus.Low
    product.currentStock <= product.minStockLevel * 2 -> StockStatus.Medium
    else -> StockStatus.Good
}
