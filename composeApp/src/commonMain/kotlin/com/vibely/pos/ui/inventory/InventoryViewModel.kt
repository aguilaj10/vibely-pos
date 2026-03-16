package com.vibely.pos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.inventory.usecase.CreateProductUseCase
import com.vibely.pos.shared.domain.inventory.usecase.DeleteProductUseCase
import com.vibely.pos.shared.domain.inventory.usecase.GetAllProductsUseCase
import com.vibely.pos.shared.domain.inventory.usecase.GetCategoriesUseCase
import com.vibely.pos.shared.domain.inventory.usecase.UpdateProductUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.usecase.SearchProductsUseCase
import com.vibely.pos.ui.dialogs.CategoryOption
import com.vibely.pos.ui.dialogs.ProductFormData
import com.vibely.pos.ui.util.randomUuidString
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
    val successMessage: String? = null,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val totalValue: Double = 0.0,
    val categoriesCount: Int = 0,
    val categories: List<CategoryOption> = emptyList(),
    val showProductForm: Boolean = false,
    val editingProductId: String? = null,
    val confirmDeleteProductId: String? = null,
)

class InventoryViewModel(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadProducts()
        loadCategories()
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

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is Result.Success -> {
                    val categories = result.data
                        .filter { it.isActive }
                        .map { CategoryOption(it.id, it.name) }
                    _state.update { it.copy(categories = categories) }
                }
                is Result.Error -> {
                    // Silently fail for categories - not critical
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

    fun onAddProduct() {
        _state.update { it.copy(showProductForm = true, editingProductId = null) }
    }

    fun onEditProduct(productId: String) {
        _state.update { it.copy(showProductForm = true, editingProductId = productId) }
    }

    fun onDeleteProduct(productId: String) {
        _state.update { it.copy(confirmDeleteProductId = productId) }
    }

    fun onDismissProductForm() {
        _state.update { it.copy(showProductForm = false, editingProductId = null) }
    }

    fun onConfirmDeleteProduct() {
        val productId = _state.value.confirmDeleteProductId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, confirmDeleteProductId = null) }

            when (val result = deleteProductUseCase(productId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Product deleted successfully",
                        )
                    }
                    loadProducts()
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
        _state.update { it.copy(confirmDeleteProductId = null) }
    }

    fun onSaveProduct(formData: ProductFormData) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val isEdit = _state.value.editingProductId != null

            val result = if (isEdit) {
                updateProductUseCase(
                    id = formData.id,
                    sku = formData.sku,
                    name = formData.name,
                    costPrice = formData.costPrice.toDoubleOrNull() ?: 0.0,
                    sellingPrice = formData.sellingPrice.toDoubleOrNull() ?: 0.0,
                    currentStock = formData.currentStock.toIntOrNull() ?: 0,
                    minStockLevel = formData.minStockLevel.toIntOrNull() ?: 10,
                    barcode = formData.barcode.ifBlank { null },
                    description = formData.description.ifBlank { null },
                    categoryId = formData.categoryId,
                    unit = formData.unit.ifBlank { "unit" },
                    imageUrl = formData.imageUrl.ifBlank { null },
                    isActive = formData.isActive,
                )
            } else {
                createProductUseCase(
                    id = randomUuidString(),
                    sku = formData.sku,
                    name = formData.name,
                    costPrice = formData.costPrice.toDoubleOrNull() ?: 0.0,
                    sellingPrice = formData.sellingPrice.toDoubleOrNull() ?: 0.0,
                    currentStock = formData.currentStock.toIntOrNull() ?: 0,
                    minStockLevel = formData.minStockLevel.toIntOrNull() ?: 10,
                    barcode = formData.barcode.ifBlank { null },
                    description = formData.description.ifBlank { null },
                    categoryId = formData.categoryId,
                    unit = formData.unit.ifBlank { "unit" },
                    imageUrl = formData.imageUrl.ifBlank { null },
                )
            }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showProductForm = false,
                            editingProductId = null,
                            successMessage = if (isEdit) "Product updated successfully" else "Product created successfully",
                        )
                    }
                    loadProducts()
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

    fun getEditingProduct(): ProductFormData? {
        val productId = _state.value.editingProductId ?: return null
        val product = _state.value.products.find { it.id == productId } ?: return null

        return ProductFormData(
            id = product.id,
            sku = product.sku,
            name = product.name,
            description = product.description ?: "",
            categoryId = product.categoryId,
            costPrice = product.costPrice.toString(),
            sellingPrice = product.sellingPrice.toString(),
            currentStock = product.currentStock.toString(),
            minStockLevel = product.minStockLevel.toString(),
            unit = product.unit,
            barcode = product.barcode ?: "",
            imageUrl = product.imageUrl ?: "",
            isActive = product.isActive,
        )
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismiss() {
        _state.update { it.copy(successMessage = null) }
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
