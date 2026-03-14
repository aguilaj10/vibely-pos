package com.vibely.pos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.dashboard.usecase.GetDashboardSummaryUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetLowStockProductsUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetRecentTransactionsUseCase
import com.vibely.pos.shared.domain.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen.
 *
 * Manages dashboard state, data loading, and user interactions.
 * Loads all dashboard data in parallel for optimal performance.
 *
 * @param getDashboardSummaryUseCase Use case for fetching dashboard summary metrics.
 * @param getRecentTransactionsUseCase Use case for fetching recent transactions.
 * @param getLowStockProductsUseCase Use case for fetching low stock alerts.
 */
class DashboardViewModel(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase,
    private val getLowStockProductsUseCase: GetLowStockProductsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    /**
     * Loads all dashboard data in parallel.
     *
     * Fetches summary metrics, recent transactions, and low stock alerts
     * concurrently for optimal performance. Updates state with results.
     */
    fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Load all data in parallel using async
                val summaryDeferred = async { getDashboardSummaryUseCase() }
                val transactionsDeferred = async { getRecentTransactionsUseCase(limit = 10) }
                val lowStockDeferred = async { getLowStockProductsUseCase() }

                // Await all results
                val summaryResult = summaryDeferred.await()
                val transactionsResult = transactionsDeferred.await()
                val lowStockResult = lowStockDeferred.await()

                // Handle results
                when {
                    // All successful - update state with data
                    summaryResult is Result.Success &&
                        transactionsResult is Result.Success &&
                        lowStockResult is Result.Success -> {
                        _state.update {
                            it.copy(
                                summary = summaryResult.data,
                                recentTransactions = transactionsResult.data,
                                lowStockProducts = lowStockResult.data,
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }
                    // At least one error - show error message
                    else -> {
                        val errorMessage = listOfNotNull(
                            (summaryResult as? Result.Error)?.message,
                            (transactionsResult as? Result.Error)?.message,
                            (lowStockResult as? Result.Error)?.message,
                        ).firstOrNull() ?: "Failed to load dashboard data"

                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = errorMessage,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An unexpected error occurred",
                    )
                }
            }
        }
    }

    /**
     * Handles pull-to-refresh action.
     *
     * Reloads all dashboard data while keeping existing data visible.
     * Sets isRefreshing flag for UI feedback.
     */
    fun onRefresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }

            try {
                // Load all data in parallel
                val summaryDeferred = async { getDashboardSummaryUseCase() }
                val transactionsDeferred = async { getRecentTransactionsUseCase(limit = 10) }
                val lowStockDeferred = async { getLowStockProductsUseCase() }

                // Await results
                val summaryResult = summaryDeferred.await()
                val transactionsResult = transactionsDeferred.await()
                val lowStockResult = lowStockDeferred.await()

                // Update state based on results
                when {
                    summaryResult is Result.Success &&
                        transactionsResult is Result.Success &&
                        lowStockResult is Result.Success -> {
                        _state.update {
                            it.copy(
                                summary = summaryResult.data,
                                recentTransactions = transactionsResult.data,
                                lowStockProducts = lowStockResult.data,
                                isRefreshing = false,
                                errorMessage = null,
                            )
                        }
                    }
                    else -> {
                        val errorMessage = listOfNotNull(
                            (summaryResult as? Result.Error)?.message,
                            (transactionsResult as? Result.Error)?.message,
                            (lowStockResult as? Result.Error)?.message,
                        ).firstOrNull() ?: "Failed to refresh dashboard"

                        _state.update {
                            it.copy(
                                isRefreshing = false,
                                errorMessage = errorMessage,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = e.message ?: "Refresh failed",
                    )
                }
            }
        }
    }

    /**
     * Dismisses the error message toast.
     */
    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }
}

/**
 * Quick action buttons available on dashboard.
 */
enum class QuickAction {
    NEW_SALE,
    INVENTORY,
    REPORTS,
}
