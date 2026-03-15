@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.vibely.pos.ui.shifts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.usecase.GetShiftHistoryUseCase
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
    val openShiftsCount: Int = 0,
    val todaysSales: Double = 0.0,
    val totalDiscrepancy: Double = 0.0,
)

class ShiftsViewModel(private val getShiftHistoryUseCase: GetShiftHistoryUseCase) : ViewModel() {

    private val _state = MutableStateFlow(ShiftsState())
    val state: StateFlow<ShiftsState> = _state.asStateFlow()

    init {
        loadShifts()
    }

    fun loadShifts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getShiftHistoryUseCase()) {
                is Result.Success -> {
                    val shifts = result.data

                    _state.update {
                        it.copy(
                            shifts = shifts,
                            isLoading = false,
                            openShiftsCount = shifts.count { shift -> shift.isOpen },
                            todaysSales = shifts.sumOf { shift -> shift.totalSales },
                            totalDiscrepancy = shifts.mapNotNull { shift -> shift.discrepancy }.sum(),
                            currentShift = shifts.firstOrNull { shift -> shift.isOpen },
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
        _state.update {
            it.copy(errorMessage = "Open shift functionality not yet implemented")
        }
    }

    fun onCloseShift(shiftId: String) {
        _state.update {
            it.copy(errorMessage = "Close shift functionality not yet implemented for: $shiftId")
        }
    }

    fun onViewShiftDetails(shiftId: String) {
        _state.update {
            it.copy(errorMessage = "View shift details not yet implemented for: $shiftId")
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }
}
