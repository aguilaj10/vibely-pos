package com.vibely.pos.ui.sales

import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
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
 */
data class SalesListState(
    val sales: List<Sale> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val statusFilter: SaleStatus? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val selectedSale: Sale? = null,
) {
    val hasSales: Boolean get() = sales.isNotEmpty()
    val hasFilters: Boolean get() = statusFilter != null || startDate != null || endDate != null
}
