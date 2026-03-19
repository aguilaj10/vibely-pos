package com.vibely.pos.ui.sales

import com.vibely.pos.shared.domain.sales.entity.Payment
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import com.vibely.pos.ui.checkout.PaymentTender
import com.vibely.pos.ui.common.PaginationState
import kotlin.time.Instant

/**
 * UI state for the Sales List screen.
 *
 * @property sales List of sales to display.
 * @property isLoading Whether data is being loaded.
 * @property isRefreshing Whether data is being refreshed (pull-to-refresh).
 * @property errorMessage Error message to display if loading fails.
 * @property searchQuery Current search query.
 * @property statusFilter Current status filter (null = all).
 * @property startDate Start date filter (null = no start date).
 * @property endDate End date filter (null = no end date).
 * @property selectedSale Currently selected sale for detail view.
 * @property pagination Pagination state for navigating through sales.
 * @property showPaymentDialog Whether to show the payment dialog.
 * @property selectedSaleForPayment The sale currently being paid.
 * @property paymentTenders List of payment tenders to apply.
 * @property existingPayments Existing payments for the selected sale.
 * @property isProcessingPayment Whether payment is being processed.
 */
data class SalesListState(
    val sales: List<Sale> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val statusFilter: SaleStatus? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val selectedSale: Sale? = null,
    val confirmRefundSaleId: String? = null,
    val refundItemsCount: Int = 0,
    val pagination: PaginationState = PaginationState(),
    val showPaymentDialog: Boolean = false,
    val selectedSaleForPayment: Sale? = null,
    val paymentTenders: List<PaymentTender> = emptyList(),
    val existingPayments: List<Payment> = emptyList(),
    val isProcessingPayment: Boolean = false,
) {
    val hasSales: Boolean get() = sales.isNotEmpty()
    val hasFilters: Boolean get() = statusFilter != null || startDate != null || endDate != null
    val alreadyPaid: Double get() = existingPayments.sumOf { it.amount }
    val remainingAmount: Double get() =
        (selectedSaleForPayment?.totalAmount ?: 0.0) - alreadyPaid -
            paymentTenders.sumOf { it.amount }
    val canCompletePayment: Boolean get() = remainingAmount <= 0.0
}
