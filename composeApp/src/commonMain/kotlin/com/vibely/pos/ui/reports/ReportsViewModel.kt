package com.vibely.pos.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.reports.ReportPeriod
import com.vibely.pos.shared.domain.reports.entity.CategoryBreakdown
import com.vibely.pos.shared.domain.reports.entity.CustomerAnalytics
import com.vibely.pos.shared.domain.reports.entity.ProductPerformance
import com.vibely.pos.shared.domain.reports.entity.SalesReport
import com.vibely.pos.shared.domain.reports.entity.SalesTrend
import com.vibely.pos.shared.domain.reports.usecase.GetCategoryBreakdownUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetCustomerAnalyticsUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetSalesReportUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetSalesTrendUseCase
import com.vibely.pos.shared.domain.reports.usecase.GetTopProductsUseCase
import com.vibely.pos.shared.domain.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

class ReportsViewModel(
    private val getSalesReportUseCase: GetSalesReportUseCase,
    private val getSalesTrendUseCase: GetSalesTrendUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    private val getTopProductsUseCase: GetTopProductsUseCase,
    private val getCustomerAnalyticsUseCase: GetCustomerAnalyticsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    init {
        loadReports(ReportPeriod.TODAY, null, null)
    }

    fun onPeriodSelected(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?) {
        _state.update { it.copy(selectedPeriod = period, customStartDate = customStartDate, customEndDate = customEndDate) }
        loadReports(period, customStartDate, customEndDate)
    }

    fun refreshReports() {
        val currentState = _state.value
        loadReports(currentState.selectedPeriod, currentState.customStartDate, currentState.customEndDate)
    }

    private fun loadReports(period: ReportPeriod, customStartDate: Instant?, customEndDate: Instant?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val salesReportDeferred = async { getSalesReportUseCase(period, customStartDate, customEndDate) }
            val salesTrendDeferred = async { getSalesTrendUseCase(period, customStartDate, customEndDate) }
            val categoryBreakdownDeferred = async { getCategoryBreakdownUseCase(period, customStartDate, customEndDate) }
            val topProductsDeferred = async { getTopProductsUseCase(period, customStartDate, customEndDate) }
            val customerAnalyticsDeferred = async { getCustomerAnalyticsUseCase(period, customStartDate, customEndDate) }

            val salesReportResult = salesReportDeferred.await()
            val salesTrendResult = salesTrendDeferred.await()
            val categoryBreakdownResult = categoryBreakdownDeferred.await()
            val topProductsResult = topProductsDeferred.await()
            val customerAnalyticsResult = customerAnalyticsDeferred.await()

            when {
                salesReportResult is Result.Success &&
                    salesTrendResult is Result.Success &&
                    categoryBreakdownResult is Result.Success &&
                    topProductsResult is Result.Success &&
                    customerAnalyticsResult is Result.Success -> {
                    _state.update {
                        it.copy(
                            salesReport = salesReportResult.data,
                            salesTrend = salesTrendResult.data,
                            categoryBreakdown = categoryBreakdownResult.data,
                            topProducts = topProductsResult.data,
                            topCustomers = customerAnalyticsResult.data,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                }
                else -> {
                    val errorMessage = listOfNotNull(
                        (salesReportResult as? Result.Error)?.message,
                        (salesTrendResult as? Result.Error)?.message,
                        (categoryBreakdownResult as? Result.Error)?.message,
                        (topProductsResult as? Result.Error)?.message,
                        (customerAnalyticsResult as? Result.Error)?.message,
                    ).firstOrNull() ?: "Failed to load reports"

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }
}

data class ReportsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val selectedPeriod: ReportPeriod = ReportPeriod.TODAY,
    val customStartDate: Instant? = null,
    val customEndDate: Instant? = null,
    val salesReport: SalesReport? = null,
    val salesTrend: List<SalesTrend> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val topProducts: List<ProductPerformance> = emptyList(),
    val topCustomers: List<CustomerAnalytics> = emptyList(),
) {
    val hasData: Boolean
        get() = salesReport != null ||
            salesTrend.isNotEmpty() ||
            categoryBreakdown.isNotEmpty() ||
            topProducts.isNotEmpty() ||
            topCustomers.isNotEmpty()
}
