package com.vibely.pos.ui.checkout

import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.valueobject.Cart

/**
 * State for the Checkout screen.
 *
 * Manages the complete state of the checkout process including product search,
 * shopping cart, and payment processing.
 *
 * @param cart Current shopping cart with items.
 * @param searchQuery Current search query string.
 * @param searchResults List of products matching the search query.
 * @param isSearching True while search is in progress.
 * @param isLoading True during any loading operation (search, cart operations, checkout).
 * @param errorMessage Error message to display (null if no error).
 * @param showPaymentDialog True if payment dialog should be displayed.
 * @param isProcessingPayment True while payment is being processed.
 * @param checkoutSuccess True if checkout was completed successfully.
 * @param completedSaleId ID of the completed sale (for display/navigation).
 */
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
) {
    /**
     * Returns true if the cart has items.
     */
    val hasItems: Boolean get() = cart.items.isNotEmpty()

    /**
     * Returns the total number of items in the cart.
     */
    val totalItems: Int get() = cart.totalItems

    /**
     * Returns the total amount to pay.
     */
    val totalAmount: Double get() = cart.totalAmount

    /**
     * Returns true if checkout button should be enabled.
     */
    val canCheckout: Boolean get() = hasItems && !isProcessingPayment

    val shouldExpand: Boolean get() = searchQuery.length >= 3 && (searchResults.isNotEmpty() || isSearching)
}
