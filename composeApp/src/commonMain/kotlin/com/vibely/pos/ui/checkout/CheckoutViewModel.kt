package com.vibely.pos.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.auth.usecase.GetCurrentUserUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.usecase.AddToCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.CompleteSaleUseCase
import com.vibely.pos.shared.domain.sales.usecase.RemoveFromCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.SearchProductsUseCase
import com.vibely.pos.shared.domain.sales.usecase.UpdateCartUseCase
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
    private val completeSaleUseCase: CompleteSaleUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateCartUseCase: UpdateCartUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
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
        _state.update { it.copy(showPaymentDialog = true) }
    }

    fun onPaymentDialogDismiss() {
        _state.update { it.copy(showPaymentDialog = false) }
    }

    fun onPaymentComplete(paymentMethod: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isProcessingPayment = true,
                    showPaymentDialog = false,
                    errorMessage = null,
                )
            }

            val userResult = getCurrentUserUseCase()
            val cashierId = when (userResult) {
                is Result.Success -> userResult.data?.id ?: "unknown"
                is Result.Error -> "unknown"
            }

            when (
                val result = completeSaleUseCase(
                    cart = _state.value.cart,
                    cashierId = cashierId,
                )
            ) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            cart = it.cart.clear(),
                            isProcessingPayment = false,
                            checkoutSuccess = true,
                            completedSaleId = result.data.id,
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isProcessingPayment = false,
                            errorMessage = result.message,
                        )
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
}
