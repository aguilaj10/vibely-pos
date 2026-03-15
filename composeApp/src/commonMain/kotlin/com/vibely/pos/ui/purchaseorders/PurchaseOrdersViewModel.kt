@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.vibely.pos.ui.purchaseorders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.usecase.GetAllPurchaseOrdersUseCase
import com.vibely.pos.shared.domain.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PurchaseOrdersState(
    val purchaseOrders: List<PurchaseOrder> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val totalAmount: Double = 0.0,
)

class PurchaseOrdersViewModel(private val getAllPurchaseOrdersUseCase: GetAllPurchaseOrdersUseCase) : ViewModel() {

    private val _state = MutableStateFlow(PurchaseOrdersState())
    val state: StateFlow<PurchaseOrdersState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPurchaseOrders()
    }

    fun loadPurchaseOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAllPurchaseOrdersUseCase()) {
                is Result.Success -> {
                    val orders = result.data

                    _state.update {
                        it.copy(
                            purchaseOrders = orders,
                            isLoading = false,
                            totalOrders = orders.size,
                            pendingOrders = orders.count { order -> order.status.name == "PENDING" },
                            totalAmount = orders.sumOf { order -> order.totalAmount },
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
            loadPurchaseOrders()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            searchPurchaseOrders(query)
        }
    }

    private fun searchPurchaseOrders(query: String) {
        val allOrders = _state.value.purchaseOrders
        val filteredOrders = allOrders.filter { order ->
            order.poNumber.contains(query, ignoreCase = true) ||
                order.supplierName?.contains(query, ignoreCase = true) == true ||
                order.status.name.contains(query, ignoreCase = true)
        }

        _state.update {
            it.copy(
                purchaseOrders = filteredOrders,
                totalOrders = filteredOrders.size,
                pendingOrders = filteredOrders.count { order -> order.status.name == "PENDING" },
                totalAmount = filteredOrders.sumOf { order -> order.totalAmount },
            )
        }
    }

    fun onClearSearch() {
        _state.update { it.copy(searchQuery = "") }
        loadPurchaseOrders()
    }

    fun onDeletePurchaseOrder(purchaseOrderId: String) {
        _state.update {
            it.copy(errorMessage = "Delete functionality not yet implemented for: $purchaseOrderId")
        }
    }

    fun onEditPurchaseOrder(purchaseOrderId: String) {
        _state.update {
            it.copy(errorMessage = "Edit navigation not yet implemented for: $purchaseOrderId")
        }
    }

    fun onViewPurchaseOrder(purchaseOrderId: String) {
        _state.update {
            it.copy(errorMessage = "View navigation not yet implemented for: $purchaseOrderId")
        }
    }

    fun onCreatePurchaseOrder() {
        _state.update {
            it.copy(errorMessage = "Create PO navigation not yet implemented")
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }
}
