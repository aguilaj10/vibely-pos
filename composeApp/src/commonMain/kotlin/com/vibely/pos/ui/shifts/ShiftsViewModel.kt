@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.vibely.pos.ui.shifts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.usecase.CloseShiftUseCase
import com.vibely.pos.shared.domain.shift.usecase.GetShiftHistoryUseCase
import com.vibely.pos.shared.domain.shift.usecase.OpenShiftUseCase
import com.vibely.pos.ui.common.PaginatedResult
import com.vibely.pos.ui.common.PaginationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShiftsState(
    val shifts: List<Shift> = emptyList(),
    val currentShift: Shift? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val openShiftsCount: Int = 0,
    val todaysSales: Double = 0.0,
    val totalDiscrepancy: Double = 0.0,
    val showOpenShiftDialog: Boolean = false,
    val showCloseShiftDialog: Boolean = false,
    val closingShift: Shift? = null,
    val pagination: PaginationState = PaginationState(),
)

class ShiftsViewModel(
    private val getShiftHistoryUseCase: GetShiftHistoryUseCase,
    private val openShiftUseCase: OpenShiftUseCase,
    private val closeShiftUseCase: CloseShiftUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ShiftsState())
    val state: StateFlow<ShiftsState> = _state.asStateFlow()

    init {
        loadShifts()
    }

    fun loadShifts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val currentPagination = _state.value.pagination
            when (
                val result = getShiftHistoryUseCase(
                    page = currentPagination.currentPage,
                    pageSize = currentPagination.pageSize,
                )
            ) {
                is Result.Success -> {
                    val shifts = result.data
                    val paginatedResult = PaginatedResult.from(shifts, currentPagination.pageSize)

                    _state.update {
                        it.copy(
                            shifts = shifts,
                            isLoading = false,
                            openShiftsCount = shifts.count { shift -> shift.isOpen },
                            todaysSales = shifts.sumOf { shift -> shift.totalSales },
                            totalDiscrepancy = shifts.mapNotNull { shift -> shift.discrepancy }.sum(),
                            currentShift = shifts.firstOrNull { shift -> shift.isOpen },
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

    fun onOpenShift() {
        _state.update { it.copy(showOpenShiftDialog = true) }
    }

    fun onDismissOpenShiftDialog() {
        _state.update { it.copy(showOpenShiftDialog = false) }
    }

    fun onSaveOpenShift(openingBalance: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showOpenShiftDialog = false) }

            val cashierId = "current-user-id"

            when (val result = openShiftUseCase(cashierId, openingBalance)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Shift opened successfully",
                        )
                    }
                    loadShifts()
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

    fun onCloseShift(shiftId: String) {
        val shift = _state.value.shifts.find { it.id == shiftId }
        _state.update { it.copy(showCloseShiftDialog = true, closingShift = shift) }
    }

    fun onDismissCloseShiftDialog() {
        _state.update { it.copy(showCloseShiftDialog = false, closingShift = null) }
    }

    fun onSaveCloseShift(closingBalance: Double, notes: String?) {
        val shiftId = _state.value.closingShift?.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showCloseShiftDialog = false) }

            when (val result = closeShiftUseCase(shiftId, closingBalance, notes)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            closingShift = null,
                            successMessage = "Shift closed successfully",
                        )
                    }
                    loadShifts()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            closingShift = null,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onViewShiftDetails(shiftId: String) {
        // Could show a details dialog in the future
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismiss() {
        _state.update { it.copy(successMessage = null) }
    }

    fun onNextPage() {
        _state.update { it.copy(pagination = it.pagination.nextPage()) }
        loadShifts()
    }

    fun onPreviousPage() {
        _state.update { it.copy(pagination = it.pagination.previousPage()) }
        loadShifts()
    }
}
