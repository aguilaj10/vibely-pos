package com.vibely.pos.ui.checkout

import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.valueobject.Cart
import com.vibely.pos.shared.domain.sales.valueobject.PaymentType

data class CheckoutState(
    val cart: Cart = Cart(),
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showPaymentDialog: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val completedSaleId: String? = null,
    val paymentTenders: List<PaymentTender> = emptyList(),
    val saleId: String? = null,
    val customerId: String? = null,
) {
    val hasItems: Boolean get() = cart.items.isNotEmpty()
    val totalItems: Int get() = cart.totalItems
    val totalAmount: Double get() = cart.totalAmount
    val canCheckout: Boolean get() = hasItems && !isProcessingPayment
    val shouldExpand: Boolean get() = searchQuery.length >= 3 && (searchResults.isNotEmpty() || isSearching)
    val totalPaid: Double get() = paymentTenders.sumOf { it.amount }
    val remainingAmount: Double get() = (totalAmount - totalPaid).coerceAtLeast(0.0)
    val canCompleteSale: Boolean get() = true
}

data class PaymentTender(val type: PaymentType, val amount: Double)
