package com.vibely.pos.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.usecase.GetAllCustomersUseCase
import com.vibely.pos.shared.domain.customer.usecase.SearchCustomersUseCase
import com.vibely.pos.shared.domain.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomersState(
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val totalLoyaltyPoints: Int = 0,
)

class CustomersViewModel(private val getAllCustomersUseCase: GetAllCustomersUseCase, private val searchCustomersUseCase: SearchCustomersUseCase) :
    ViewModel() {

    private val _state = MutableStateFlow(CustomersState())
    val state: StateFlow<CustomersState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAllCustomersUseCase()) {
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
        loadCustomers()
    }

    fun onDeleteCustomer(customerId: String) {
        _state.update {
            it.copy(errorMessage = "Delete functionality not yet implemented for: $customerId")
        }
    }

    fun onEditCustomer(customerId: String) {
        _state.update {
            it.copy(errorMessage = "Edit navigation not yet implemented for: $customerId")
        }
    }

    fun onAddCustomer() {
        _state.update {
            it.copy(errorMessage = "Add customer navigation not yet implemented")
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }
}
