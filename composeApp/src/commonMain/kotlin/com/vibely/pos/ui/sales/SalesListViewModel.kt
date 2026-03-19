package com.vibely.pos.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.inventory.usecase.AdjustStockUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.repository.PaymentRepository
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.sales.usecase.GetSalesUseCase
import com.vibely.pos.shared.domain.sales.usecase.RecordPaymentsUseCase
import com.vibely.pos.shared.domain.sales.valueobject.PaymentInfo
import com.vibely.pos.shared.domain.sales.valueobject.PaymentType
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import com.vibely.pos.ui.checkout.PaymentTender
import com.vibely.pos.ui.common.PaginatedResult
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
    private val paymentRepository: PaymentRepository,
    private val recordPaymentsUseCase: RecordPaymentsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SalesListState())
    val state: StateFlow<SalesListState> = _state.asStateFlow()

    private var onEditSaleCallback: ((Sale) -> Unit)? = null

    fun setOnEditSaleCallback(callback: (Sale) -> Unit) {
        onEditSaleCallback = callback
    }

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val currentPagination = _state.value.pagination
            when (
                val result =
                    getSalesUseCase(
                        startDate = _state.value.startDate,
                        endDate = _state.value.endDate,
                        status = _state.value.statusFilter,
                        page = currentPagination.currentPage,
                        pageSize = currentPagination.pageSize,
                    )
            ) {
                is Result.Success -> {
                    val sales = result.data
                    val paginatedResult = PaginatedResult.from(sales, currentPagination.pageSize)

                    _state.update {
                        it.copy(
                            sales = sales,
                            isLoading = false,
                            errorMessage = null,
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

    fun onRefresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }

            val currentPagination = _state.value.pagination
            when (
                val result =
                    getSalesUseCase(
                        startDate = _state.value.startDate,
                        endDate = _state.value.endDate,
                        status = _state.value.statusFilter,
                        page = currentPagination.currentPage,
                        pageSize = currentPagination.pageSize,
                    )
            ) {
                is Result.Success -> {
                    val sales = result.data
                    val paginatedResult = PaginatedResult.from(sales, currentPagination.pageSize)

                    _state.update {
                        it.copy(
                            sales = sales,
                            isRefreshing = false,
                            errorMessage = null,
                            pagination = currentPagination.withHasMore(paginatedResult.hasMore),
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
        _state.update { it.copy(searchQuery = query, pagination = it.pagination.reset()) }
        filterSales()
    }

    fun onStatusFilterChange(status: SaleStatus?) {
        _state.update { it.copy(statusFilter = status, pagination = it.pagination.reset()) }
        loadSales()
    }

    fun onDateRangeChange(startDate: Instant?, endDate: Instant?) {
        _state.update { it.copy(startDate = startDate, endDate = endDate, pagination = it.pagination.reset()) }
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
                pagination = it.pagination.reset(),
            )
        }
        loadSales()
    }

    fun onRefundSale(sale: Sale) {
        viewModelScope.launch {
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

    fun onEditSale(sale: Sale) {
        onEditSaleCallback?.invoke(sale)
    }

    fun onConfirmRefund(reason: String) {
        val saleId = _state.value.confirmRefundSaleId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, confirmRefundSaleId = null) }

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

                    for (item in items) {
                        adjustStockUseCase(
                            productId = item.productId,
                            quantity = item.quantity,
                            reason = "Refund: $reason",
                            performedBy = "system",
                            notes = "Refund for sale $saleId",
                        )
                    }
                }
            }

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

    fun onNextPage() {
        _state.update { it.copy(pagination = it.pagination.nextPage()) }
        loadSales()
    }

    fun onPreviousPage() {
        _state.update { it.copy(pagination = it.pagination.previousPage()) }
        loadSales()
    }

    fun onAddPayment(sale: Sale) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = paymentRepository.getPaymentsBySale(sale.id)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            showPaymentDialog = true,
                            selectedSaleForPayment = sale,
                            existingPayments = result.data,
                            paymentTenders = emptyList(),
                            isLoading = false,
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load existing payments: ${result.message}",
                        )
                    }
                }
            }
        }
    }

    fun addPaymentTender(type: PaymentType, amount: Double) {
        val newTender = PaymentTender(type = type, amount = amount)
        _state.update {
            it.copy(paymentTenders = it.paymentTenders + newTender)
        }
    }

    fun removePaymentTender(index: Int) {
        _state.update {
            it.copy(paymentTenders = it.paymentTenders.filterIndexed { i, _ -> i != index })
        }
    }

    fun recordPayments() {
        val saleId = _state.value.selectedSaleForPayment?.id ?: return
        val tenders = _state.value.paymentTenders
        if (tenders.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isProcessingPayment = true) }

            val paymentInfos =
                tenders.map { tender ->
                    PaymentInfo(type = tender.type, amount = tender.amount, reference = "")
                }

            when (val result = recordPaymentsUseCase(saleId, paymentInfos)) {
                is Result.Success -> {
                    val totalAmount = _state.value.selectedSaleForPayment?.totalAmount ?: 0.0
                    val totalPaid = _state.value.alreadyPaid + tenders.sumOf { it.amount }

                    if (totalPaid >= totalAmount) {
                        when (val statusResult = saleRepository.updateStatus(saleId, SaleStatus.COMPLETED)) {
                            is Result.Success -> {
                                _state.update {
                                    it.copy(
                                        isProcessingPayment = false,
                                        showPaymentDialog = false,
                                        selectedSaleForPayment = null,
                                        paymentTenders = emptyList(),
                                        existingPayments = emptyList(),
                                        successMessage = "Payment recorded and sale completed successfully",
                                    )
                                }
                                loadSales()
                            }

                            is Result.Error -> {
                                _state.update {
                                    it.copy(
                                        isProcessingPayment = false,
                                        errorMessage = "Payment recorded but failed to update sale status: ${statusResult.message}",
                                    )
                                }
                            }
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isProcessingPayment = false,
                                showPaymentDialog = false,
                                selectedSaleForPayment = null,
                                paymentTenders = emptyList(),
                                existingPayments = emptyList(),
                                successMessage = "Payment recorded successfully",
                            )
                        }
                        loadSales()
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isProcessingPayment = false,
                            errorMessage = "Failed to record payment: ${result.message}",
                        )
                    }
                }
            }
        }
    }

    fun onPaymentDialogDismiss() {
        _state.update {
            it.copy(
                showPaymentDialog = false,
                selectedSaleForPayment = null,
                paymentTenders = emptyList(),
                existingPayments = emptyList(),
            )
        }
    }

    private fun filterSales() {
        val query = _state.value.searchQuery.lowercase()
        if (query.isBlank()) {
            loadSales()
            return
        }

        val currentSales = _state.value.sales
        val filtered =
            currentSales.filter { sale ->
                sale.invoiceNumber.lowercase().contains(query) ||
                    sale.id.lowercase().contains(query)
            }
        _state.update { it.copy(sales = filtered) }
    }
}
