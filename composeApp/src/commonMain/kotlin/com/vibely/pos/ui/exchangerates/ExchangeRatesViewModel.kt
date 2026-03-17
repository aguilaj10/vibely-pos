package com.vibely.pos.ui.exchangerates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.data.currency.dto.CurrencyDTO
import com.vibely.pos.shared.domain.currency.entity.CurrencyExchangeRate
import com.vibely.pos.shared.domain.currency.usecase.CreateExchangeRateUseCase
import com.vibely.pos.shared.domain.currency.usecase.DeleteExchangeRateUseCase
import com.vibely.pos.shared.domain.currency.usecase.GetActiveCurrenciesUseCase
import com.vibely.pos.shared.domain.currency.usecase.GetAllExchangeRatesUseCase
import com.vibely.pos.shared.domain.currency.usecase.UpdateExchangeRateUseCase
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.ui.dialogs.ExchangeRateFormData
import com.vibely.pos.ui.util.randomUuidString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExchangeRatesState(
    val exchangeRates: List<CurrencyExchangeRate> = emptyList(),
    val currencies: List<CurrencyDTO> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showExchangeRateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val editingExchangeRate: CurrencyExchangeRate? = null,
    val deletingExchangeRateId: String? = null,
)

class ExchangeRatesViewModel(
    private val getAllExchangeRatesUseCase: GetAllExchangeRatesUseCase,
    private val createExchangeRateUseCase: CreateExchangeRateUseCase,
    private val updateExchangeRateUseCase: UpdateExchangeRateUseCase,
    private val deleteExchangeRateUseCase: DeleteExchangeRateUseCase,
    private val getActiveCurrenciesUseCase: GetActiveCurrenciesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ExchangeRatesState())
    val state: StateFlow<ExchangeRatesState> = _state.asStateFlow()

    init {
        loadExchangeRates()
        loadCurrencies()
    }

    fun loadExchangeRates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAllExchangeRatesUseCase()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            exchangeRates = result.data,
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

    private fun loadCurrencies() {
        viewModelScope.launch {
            when (val result = getActiveCurrenciesUseCase()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(currencies = result.data)
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onAddExchangeRate() {
        _state.update { it.copy(showExchangeRateDialog = true, editingExchangeRate = null) }
    }

    fun onEditExchangeRate(exchangeRateId: String) {
        val exchangeRate = _state.value.exchangeRates.find { it.id == exchangeRateId }
        _state.update { it.copy(showExchangeRateDialog = true, editingExchangeRate = exchangeRate) }
    }

    fun onDeleteExchangeRate(exchangeRateId: String) {
        _state.update { it.copy(showDeleteDialog = true, deletingExchangeRateId = exchangeRateId) }
    }

    fun onDismissExchangeRateDialog() {
        _state.update { it.copy(showExchangeRateDialog = false, editingExchangeRate = null) }
    }

    fun onDismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false, deletingExchangeRateId = null) }
    }

    fun onSaveExchangeRate(formData: ExchangeRateFormData) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val exchangeRate = CurrencyExchangeRate(
                id = formData.id.ifBlank { randomUuidString() },
                currencyCodeFrom = formData.currencyFrom,
                currencyCodeTo = formData.currencyTo,
                rate = formData.rate,
                effectiveDate = formData.effectiveDate,
            )

            val result = if (formData.id.isBlank()) {
                createExchangeRateUseCase(exchangeRate)
            } else {
                updateExchangeRateUseCase(exchangeRate)
            }

            when (result) {
                is Result.Success -> {
                    val message = if (formData.id.isBlank()) {
                        "Exchange rate created successfully"
                    } else {
                        "Exchange rate updated successfully"
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showExchangeRateDialog = false,
                            editingExchangeRate = null,
                            successMessage = message,
                        )
                    }
                    loadExchangeRates()
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

    fun onConfirmDelete() {
        val exchangeRateId = _state.value.deletingExchangeRateId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteDialog = false) }

            when (val result = deleteExchangeRateUseCase(exchangeRateId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingExchangeRateId = null,
                            successMessage = "Exchange rate deleted successfully",
                        )
                    }
                    loadExchangeRates()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingExchangeRateId = null,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismiss() {
        _state.update { it.copy(successMessage = null) }
    }
}
