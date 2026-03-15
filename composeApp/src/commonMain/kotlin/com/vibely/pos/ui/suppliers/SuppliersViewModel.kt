package com.vibely.pos.ui.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.usecase.GetAllSuppliersUseCase
import com.vibely.pos.shared.domain.supplier.usecase.SearchSuppliersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SuppliersState(
    val suppliers: List<Supplier> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalSuppliers: Int = 0,
    val activeSuppliers: Int = 0,
)

class SuppliersViewModel(private val getAllSuppliersUseCase: GetAllSuppliersUseCase, private val searchSuppliersUseCase: SearchSuppliersUseCase) :
    ViewModel() {

    private val _state = MutableStateFlow(SuppliersState())
    val state: StateFlow<SuppliersState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSuppliers()
    }

    fun loadSuppliers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAllSuppliersUseCase()) {
                is Result.Success -> {
                    val suppliers = result.data

                    _state.update {
                        it.copy(
                            suppliers = suppliers,
                            isLoading = false,
                            totalSuppliers = suppliers.size,
                            activeSuppliers = suppliers.count { s -> s.isActive },
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
            loadSuppliers()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            searchSuppliers(query)
        }
    }

    private suspend fun searchSuppliers(query: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        when (val result = searchSuppliersUseCase(query)) {
            is Result.Success -> {
                val suppliers = result.data

                _state.update {
                    it.copy(
                        suppliers = suppliers,
                        isLoading = false,
                        totalSuppliers = suppliers.size,
                        activeSuppliers = suppliers.count { s -> s.isActive },
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
        loadSuppliers()
    }

    fun onDeleteSupplier(supplierId: String) {
        _state.update {
            it.copy(errorMessage = "Delete functionality not yet implemented for: $supplierId")
        }
    }

    fun onEditSupplier(supplierId: String) {
        _state.update {
            it.copy(errorMessage = "Edit navigation not yet implemented for: $supplierId")
        }
    }

    fun onAddSupplier() {
        _state.update {
            it.copy(errorMessage = "Add supplier navigation not yet implemented")
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }
}
