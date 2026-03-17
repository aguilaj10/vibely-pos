package com.vibely.pos.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.usecase.CreateCustomerUseCase
import com.vibely.pos.shared.domain.customer.usecase.DeleteCustomerUseCase
import com.vibely.pos.shared.domain.customer.usecase.GetAllCustomersUseCase
import com.vibely.pos.shared.domain.customer.usecase.SearchCustomersUseCase
import com.vibely.pos.shared.domain.customer.usecase.UpdateCustomerUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.ui.common.PaginatedResult
import com.vibely.pos.ui.common.PaginationState
import com.vibely.pos.ui.dialogs.CustomerFormData
import com.vibely.pos.ui.util.randomUuidString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class CustomersState(
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val totalLoyaltyPoints: Int = 0,
    val showCustomerDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val editingCustomer: Customer? = null,
    val deletingCustomerId: String? = null,
    val pagination: PaginationState = PaginationState(),
)

class CustomersViewModel(
    private val getAllCustomersUseCase: GetAllCustomersUseCase,
    private val searchCustomersUseCase: SearchCustomersUseCase,
    private val createCustomerUseCase: CreateCustomerUseCase,
    private val updateCustomerUseCase: UpdateCustomerUseCase,
    private val deleteCustomerUseCase: DeleteCustomerUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomersState())
    val state: StateFlow<CustomersState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val currentPagination = _state.value.pagination
            when (
                val result = getAllCustomersUseCase(
                    page = currentPagination.currentPage,
                    pageSize = currentPagination.pageSize,
                )
            ) {
                is Result.Success -> {
                    val customers = result.data
                    val paginatedResult = PaginatedResult.from(customers, currentPagination.pageSize)
                    val totalLoyaltyPoints = customers.sumOf { it.loyaltyPoints }

                    _state.update {
                        it.copy(
                            customers = customers,
                            isLoading = false,
                            totalCustomers = customers.size,
                            activeCustomers = customers.count { c -> c.isActive },
                            totalLoyaltyPoints = totalLoyaltyPoints,
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
            loadCustomers()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            searchCustomers(query)
        }
    }

    private suspend fun searchCustomers(query: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        when (val result = searchCustomersUseCase(query)) {
            is Result.Success -> {
                val customers = result.data
                val totalLoyaltyPoints = customers.sumOf { it.loyaltyPoints }

                _state.update {
                    it.copy(
                        customers = customers,
                        isLoading = false,
                        totalCustomers = customers.size,
                        activeCustomers = customers.count { c -> c.isActive },
                        totalLoyaltyPoints = totalLoyaltyPoints,
                        pagination = it.pagination.reset().withHasMore(false),
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
        loadCustomers()
    }

    fun onAddCustomer() {
        _state.update { it.copy(showCustomerDialog = true, editingCustomer = null) }
    }

    fun onEditCustomer(customerId: String) {
        val customer = _state.value.customers.find { it.id == customerId }
        _state.update { it.copy(showCustomerDialog = true, editingCustomer = customer) }
    }

    fun onDeleteCustomer(customerId: String) {
        _state.update { it.copy(showDeleteDialog = true, deletingCustomerId = customerId) }
    }

    fun onDismissCustomerDialog() {
        _state.update { it.copy(showCustomerDialog = false, editingCustomer = null) }
    }

    fun onDismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false, deletingCustomerId = null) }
    }

    fun onSaveCustomer(formData: CustomerFormData) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val customer = Customer(
                id = formData.id.ifBlank { randomUuidString() },
                code = generateCustomerCode(),
                firstName = formData.firstName,
                lastName = formData.lastName,
                email = formData.email.ifBlank { null },
                phone = formData.phone.ifBlank { null },
                loyaltyPoints = formData.loyaltyPoints,
                loyaltyTier = formData.loyaltyTier,
                totalPurchases = formData.totalPurchases,
                isActive = formData.isActive,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
            )

            val result = if (formData.id.isBlank()) {
                createCustomerUseCase(customer)
            } else {
                updateCustomerUseCase(customer)
            }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showCustomerDialog = false,
                            editingCustomer = null,
                            successMessage = if (formData.id.isBlank()) "Customer created successfully" else "Customer updated successfully",
                        )
                    }
                    loadCustomers()
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
        val customerId = _state.value.deletingCustomerId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteDialog = false) }

            when (val result = deleteCustomerUseCase(customerId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingCustomerId = null,
                            successMessage = "Customer deleted successfully",
                        )
                    }
                    loadCustomers()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingCustomerId = null,
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
        loadCustomers()
    }

    fun onPreviousPage() {
        _state.update { it.copy(pagination = it.pagination.previousPage()) }
        loadCustomers()
    }

    private fun generateCustomerCode(): String = "CUST-${Clock.System.now().toEpochMilliseconds().toString().takeLast(6)}"
}
