package com.vibely.pos.ui.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.usecase.CreateSupplierUseCase
import com.vibely.pos.shared.domain.supplier.usecase.DeleteSupplierUseCase
import com.vibely.pos.shared.domain.supplier.usecase.GetAllSuppliersUseCase
import com.vibely.pos.shared.domain.supplier.usecase.SearchSuppliersUseCase
import com.vibely.pos.shared.domain.supplier.usecase.UpdateSupplierUseCase
import com.vibely.pos.ui.common.PaginatedResult
import com.vibely.pos.ui.common.PaginationState
import com.vibely.pos.ui.dialogs.SupplierFormData
import com.vibely.pos.ui.util.randomUuidString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class SuppliersState(
    val suppliers: List<Supplier> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalSuppliers: Int = 0,
    val activeSuppliers: Int = 0,
    val showSupplierDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val editingSupplier: Supplier? = null,
    val deletingSupplierId: String? = null,
    val pagination: PaginationState = PaginationState(),
)

class SuppliersViewModel(
    private val getAllSuppliersUseCase: GetAllSuppliersUseCase,
    private val searchSuppliersUseCase: SearchSuppliersUseCase,
    private val createSupplierUseCase: CreateSupplierUseCase,
    private val updateSupplierUseCase: UpdateSupplierUseCase,
    private val deleteSupplierUseCase: DeleteSupplierUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SuppliersState())
    val state: StateFlow<SuppliersState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSuppliers()
    }

    fun loadSuppliers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val currentPagination = _state.value.pagination
            when (
                val result = getAllSuppliersUseCase(
                    page = currentPagination.currentPage,
                    pageSize = currentPagination.pageSize,
                )
            ) {
                is Result.Success -> {
                    val suppliers = result.data
                    val paginatedResult = PaginatedResult.from(suppliers, currentPagination.pageSize)

                    _state.update {
                        it.copy(
                            suppliers = suppliers,
                            isLoading = false,
                            totalSuppliers = suppliers.size,
                            activeSuppliers = suppliers.count { s -> s.isActive },
                            pagination = currentPagination.withHasMore(paginatedResult.hasMore),
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
        _state.update { it.copy(searchQuery = query, pagination = it.pagination.reset()) }

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
        _state.update { it.copy(searchQuery = "", pagination = it.pagination.reset()) }
        loadSuppliers()
    }

    fun onAddSupplier() {
        _state.update { it.copy(showSupplierDialog = true, editingSupplier = null) }
    }

    fun onEditSupplier(supplierId: String) {
        val supplier = _state.value.suppliers.find { it.id == supplierId }
        _state.update { it.copy(showSupplierDialog = true, editingSupplier = supplier) }
    }

    fun onDeleteSupplier(supplierId: String) {
        _state.update { it.copy(showDeleteDialog = true, deletingSupplierId = supplierId) }
    }

    fun onDismissSupplierDialog() {
        _state.update { it.copy(showSupplierDialog = false, editingSupplier = null) }
    }

    fun onDismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false, deletingSupplierId = null) }
    }

    fun onSaveSupplier(formData: SupplierFormData) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val supplier = Supplier(
                id = formData.id.ifBlank { randomUuidString() },
                code = generateSupplierCode(),
                name = formData.name,
                contactPerson = formData.contactPerson.ifBlank { null },
                email = formData.email.ifBlank { null },
                phone = formData.phone.ifBlank { null },
                address = formData.address.ifBlank { null },
                isActive = formData.isActive,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
            )

            val result = if (formData.id.isBlank()) {
                createSupplierUseCase(supplier)
            } else {
                updateSupplierUseCase(supplier)
            }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showSupplierDialog = false,
                            editingSupplier = null,
                            successMessage = if (formData.id.isBlank()) "Supplier created successfully" else "Supplier updated successfully",
                        )
                    }
                    loadSuppliers()
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

    fun onConfirmDelete() {
        val supplierId = _state.value.deletingSupplierId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteDialog = false) }

            when (val result = deleteSupplierUseCase(supplierId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingSupplierId = null,
                            successMessage = "Supplier deleted successfully",
                        )
                    }
                    loadSuppliers()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingSupplierId = null,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismiss() {
        _state.update { it.copy(successMessage = null) }
    }

    fun onNextPage() {
        _state.update { it.copy(pagination = it.pagination.nextPage()) }
        loadSuppliers()
    }

    fun onPreviousPage() {
        _state.update { it.copy(pagination = it.pagination.previousPage()) }
        loadSuppliers()
    }

    private fun generateSupplierCode(): String = "SUP-${Clock.System.now().toEpochMilliseconds().toString().takeLast(6)}"
}
