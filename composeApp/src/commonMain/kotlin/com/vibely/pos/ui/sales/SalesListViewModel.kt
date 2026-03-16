package com.vibely.pos.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.inventory.usecase.AdjustStockUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.sales.usecase.GetSalesUseCase
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

class SalesListViewModel(
    private val getSalesUseCase: GetSalesUseCase,
    private val saleRepository: SaleRepository,
    private val adjustStockUseCase: AdjustStockUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SalesListState())
    val state: StateFlow<SalesListState> = _state.asStateFlow()

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (
                val result = getSalesUseCase(
                    startDate = _state.value.startDate,
                    endDate = _state.value.endDate,
                    status = _state.value.statusFilter,
                )
            ) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            sales = result.data,
                            isLoading = false,
                            errorMessage = null,
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

    fun onRefresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }

            when (
                val result = getSalesUseCase(
                    startDate = _state.value.startDate,
                    endDate = _state.value.endDate,
                    status = _state.value.statusFilter,
                )
            ) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            sales = result.data,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterSales()
    }

    fun onStatusFilterChange(status: SaleStatus?) {
        _state.update { it.copy(statusFilter = status) }
        loadSales()
    }

    fun onDateRangeChange(startDate: Instant?, endDate: Instant?) {
        _state.update { it.copy(startDate = startDate, endDate = endDate) }
        loadSales()
    }

    fun onSaleSelected(sale: Sale) {
        _state.update { it.copy(selectedSale = sale) }
    }

    fun onSaleDetailDismiss() {
        _state.update { it.copy(selectedSale = null) }
    }

    fun onClearFilters() {
        _state.update {
            it.copy(
                statusFilter = null,
                startDate = null,
                endDate = null,
                searchQuery = "",
            )
        }
        loadSales()
    }

    fun onRefundSale(sale: Sale) {
        viewModelScope.launch {
            // Get items count for the sale
            when (val itemsResult = saleRepository.getItems(sale.id)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            confirmRefundSaleId = sale.id,
                            refundItemsCount = itemsResult.data.size,
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            confirmRefundSaleId = sale.id,
                            refundItemsCount = 0,
                        )
                    }
                }
            }
        }
    }

    fun onConfirmRefund(reason: String) {
        val saleId = _state.value.confirmRefundSaleId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, confirmRefundSaleId = null) }

            // First get the sale items to restock
            when (val itemsResult = saleRepository.getItems(saleId)) {
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to get sale items: ${itemsResult.message}",
                        )
                    }
                    return@launch
                }
                is Result.Success -> {
                    val items = itemsResult.data

                    // Restock each product
                    for (item in items) {
                        adjustStockUseCase(
                            productId = item.productId,
                            quantity = item.quantity,
                            reason = "Refund: $reason",
                            performedBy = "system", // Would normally be current user
                            notes = "Refund for sale $saleId",
                        )
                    }
                }
            }

            // Then update the sale status
            when (val statusResult = saleRepository.updateStatus(saleId, SaleStatus.REFUNDED)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Sale refunded successfully. Products restocked.",
                        )
                    }
                    loadSales()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = statusResult.message,
                        )
                    }
                }
            }
        }
    }

    fun onDismissRefundConfirmation() {
        _state.update { it.copy(confirmRefundSaleId = null, refundItemsCount = 0) }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismiss() {
        _state.update { it.copy(successMessage = null) }
    }

    private fun filterSales() {
        val query = _state.value.searchQuery.lowercase()
        if (query.isBlank()) {
            loadSales()
            return
        }

        val currentSales = _state.value.sales
        val filtered = currentSales.filter { sale ->
            sale.invoiceNumber.lowercase().contains(query) ||
                sale.id.lowercase().contains(query)
        }
        _state.update { it.copy(sales = filtered) }
    }
}
