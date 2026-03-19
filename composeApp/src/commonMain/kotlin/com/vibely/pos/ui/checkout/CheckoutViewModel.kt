package com.vibely.pos.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.auth.usecase.GetCurrentUserUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.sales.usecase.AddToCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.CompleteSaleUseCase
import com.vibely.pos.shared.domain.sales.usecase.RecordPaymentsUseCase
import com.vibely.pos.shared.domain.sales.usecase.RemoveFromCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.SearchProductsUseCase
import com.vibely.pos.shared.domain.sales.usecase.UpdateCartUseCase
import com.vibely.pos.shared.domain.sales.valueobject.PaymentInfo
import com.vibely.pos.shared.domain.sales.valueobject.PaymentStatus
import com.vibely.pos.shared.domain.sales.valueobject.PaymentType
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateCartUseCase: UpdateCartUseCase,
    private val completeSaleUseCase: CompleteSaleUseCase,
    private val recordPaymentsUseCase: RecordPaymentsUseCase,
    private val saleRepository: SaleRepository? = null,
) : ViewModel() {
    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun loadSale(saleId: String) {
        val repo = saleRepository ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repo.getById(saleId)) {
                is Result.Success -> {
                    val sale = result.data
                    when (val itemsResult = repo.getItems(saleId)) {
                        is Result.Success -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    saleId = saleId,
                                )
                            }
                        }

                        is Result.Error -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = itemsResult.message,
                                )
                            }
                        }
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
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob =
            viewModelScope.launch {
                delay(300)
                if (query.length >= 3) {
                    performSearch(query)
                }
            }
    }

    private suspend fun performSearch(query: String) {
        _state.update { it.copy(isSearching = true, errorMessage = null) }

        when (val result = searchProductsUseCase(query)) {
            is Result.Success -> {
                _state.update {
                    it.copy(
                        searchResults = result.data,
                        isSearching = false,
                    )
                }
            }

            is Result.Error -> {
                _state.update {
                    it.copy(
                        isSearching = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    fun onProductSelected(product: Product) {
        onAddToCart(product, 1)
    }

    fun onCustomerSelected(customerId: String?) {
        _state.update { it.copy(customerId = customerId) }
    }

    fun onAddToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = addToCartUseCase(_state.value.cart, product.id, quantity)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            cart = result.data,
                            isLoading = false,
                            searchQuery = "",
                            searchResults = emptyList(),
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

    fun onRemoveFromCart(productId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = removeFromCartUseCase(_state.value.cart, productId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            cart = result.data,
                            isLoading = false,
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

    fun onUpdateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            onRemoveFromCart(productId)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = updateCartUseCase(_state.value.cart, productId, quantity)
            _state.update {
                it.copy(
                    cart = result,
                    isLoading = false,
                )
            }
        }
    }

    fun onCheckout() {
        _state.update { it.copy(showPaymentDialog = true, paymentTenders = emptyList()) }
    }

    fun onPaymentDialogDismiss() {
        _state.update { it.copy(showPaymentDialog = false) }
    }

    fun addPaymentTender(type: PaymentType, amount: Double) {
        if (amount <= 0) return
        _state.update { currentState ->
            currentState.copy(
                paymentTenders = currentState.paymentTenders + PaymentTender(type, amount),
            )
        }
    }

    fun removePaymentTender(index: Int) {
        _state.update { currentState ->
            currentState.copy(
                paymentTenders = currentState.paymentTenders.filterIndexed { i, _ -> i != index },
            )
        }
    }

    fun recordPayments() {
        viewModelScope.launch {
            _state.update { it.copy(isProcessingPayment = true, errorMessage = null) }

            val currentState = _state.value
            val userResult = getCurrentUserUseCase()
            val cashierId =
                when (userResult) {
                    is Result.Success -> userResult.data?.id ?: "unknown"
                    is Result.Error -> "unknown"
                }

            val totalAmount = currentState.cart.totalAmount
            val totalPaid = currentState.paymentTenders.sumOf { it.amount }
            val saleStatus = calculateSaleStatus(totalAmount, totalPaid)
            val paymentStatus = calculatePaymentStatus(totalAmount, totalPaid)

            val saleResult =
                completeSaleUseCase(
                    cart = currentState.cart,
                    cashierId = cashierId,
                    customerId = currentState.customerId,
                    status = saleStatus,
                    paymentStatus = paymentStatus,
                )

            when (saleResult) {
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isProcessingPayment = false,
                            errorMessage = saleResult.message,
                        )
                    }
                }

                is Result.Success -> {
                    val saleId = saleResult.data.id
                    val paymentInfos =
                        currentState.paymentTenders.map { tender ->
                            PaymentInfo(
                                type = tender.type,
                                amount = tender.amount,
                                reference = "",
                            )
                        }

                    when (val paymentsResult = recordPaymentsUseCase(saleId, paymentInfos)) {
                        is Result.Error -> {
                            _state.update {
                                it.copy(
                                    isProcessingPayment = false,
                                    errorMessage = paymentsResult.message,
                                )
                            }
                        }

                        is Result.Success -> {
                            _state.update {
                                it.copy(
                                    cart = it.cart.clear(),
                                    isProcessingPayment = false,
                                    checkoutSuccess = true,
                                    completedSaleId = saleId,
                                    showPaymentDialog = false,
                                    paymentTenders = emptyList(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onCheckoutSuccessDismiss() {
        _state.update {
            it.copy(
                checkoutSuccess = false,
                completedSaleId = null,
            )
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onClearCart() {
        _state.update { it.copy(cart = it.cart.clear()) }
    }

    private fun calculateSaleStatus(totalAmount: Double, totalPaid: Double): SaleStatus = if (totalPaid >=
        totalAmount
    ) {
        SaleStatus.COMPLETED
    } else {
        SaleStatus.DRAFT
    }

    private fun calculatePaymentStatus(totalAmount: Double, totalPaid: Double): PaymentStatus = if (totalPaid >=
        totalAmount
    ) {
        PaymentStatus.COMPLETED
    } else {
        PaymentStatus.PENDING
    }
}
