package com.vibely.pos.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.settings.entity.ReceiptSettings
import com.vibely.pos.shared.domain.settings.entity.StoreSettings
import com.vibely.pos.shared.domain.settings.entity.TaxSettings
import com.vibely.pos.shared.domain.settings.entity.UserPreferences
import com.vibely.pos.shared.domain.settings.usecase.GetSettingsUseCase
import com.vibely.pos.shared.domain.settings.usecase.UpdateReceiptSettingsUseCase
import com.vibely.pos.shared.domain.settings.usecase.UpdateStoreInfoUseCase
import com.vibely.pos.shared.domain.settings.usecase.UpdateTaxSettingsUseCase
import com.vibely.pos.shared.domain.settings.usecase.UpdateUserPreferencesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateStoreInfoUseCase: UpdateStoreInfoUseCase,
    private val updateReceiptSettingsUseCase: UpdateReceiptSettingsUseCase,
    private val updateTaxSettingsUseCase: UpdateTaxSettingsUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<SettingsUiState>(SettingsUiState.Success(isLoading = true))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _state.update {
                when (it) {
                    is SettingsUiState.Success -> it.copy(isLoading = true, errorMessage = null)
                    else -> SettingsUiState.Success(isLoading = true)
                }
            }

            val settingsDeferred = async { getSettingsUseCase() }
            val settingsResult = settingsDeferred.await()

            when (settingsResult) {
                is Result.Success -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isLoading = false,
                                errorMessage = null,
                                storeSettings = settingsResult.data.store,
                                receiptSettings = settingsResult.data.receipt,
                                taxSettings = settingsResult.data.tax,
                                userPreferences = settingsResult.data.preferences,
                            )
                            else -> SettingsUiState.Success(
                                isLoading = false,
                                errorMessage = null,
                                storeSettings = settingsResult.data.store,
                                receiptSettings = settingsResult.data.receipt,
                                taxSettings = settingsResult.data.tax,
                                userPreferences = settingsResult.data.preferences,
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isLoading = false,
                                errorMessage = settingsResult.message,
                            )
                            else -> SettingsUiState.Error(settingsResult.message)
                        }
                    }
                }
            }
        }
    }

    fun updateStoreInfo(storeName: String, address: String, phone: String, email: String) {
        val currentState = _state.value as? SettingsUiState.Success ?: return
        val currentStore = currentState.storeSettings ?: return

        viewModelScope.launch {
            _state.update {
                when (it) {
                    is SettingsUiState.Success -> it.copy(isSaving = true, saveSuccess = false, saveError = null)
                    else -> it
                }
            }

            val newStoreSettings = StoreSettings(
                id = currentStore.id,
                storeName = storeName,
                address = address,
                phone = phone,
                email = email,
                createdAt = currentStore.createdAt,
                updatedAt = currentStore.updatedAt,
            )

            val result = updateStoreInfoUseCase(newStoreSettings)

            when (result) {
                is Result.Success -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isSaving = false,
                                saveSuccess = true,
                                saveError = null,
                                storeSettings = newStoreSettings,
                            )
                            else -> it
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isSaving = false,
                                saveSuccess = false,
                                saveError = result.message,
                            )
                            else -> it
                        }
                    }
                }
            }
        }
    }

    fun updateReceiptSettings(header: String, footer: String, logoUrl: String?, showTax: Boolean) {
        val currentState = _state.value as? SettingsUiState.Success ?: return
        val currentReceipt = currentState.receiptSettings ?: return

        viewModelScope.launch {
            _state.update {
                when (it) {
                    is SettingsUiState.Success -> it.copy(isSaving = true, saveSuccess = false, saveError = null)
                    else -> it
                }
            }

            val newReceiptSettings = ReceiptSettings(
                id = currentReceipt.id,
                header = header,
                footer = footer,
                logoUrl = logoUrl,
                showTax = showTax,
                createdAt = currentReceipt.createdAt,
                updatedAt = currentReceipt.updatedAt,
            )

            val result = updateReceiptSettingsUseCase(newReceiptSettings)

            when (result) {
                is Result.Success -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isSaving = false,
                                saveSuccess = true,
                                saveError = null,
                                receiptSettings = newReceiptSettings,
                            )
                            else -> it
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isSaving = false,
                                saveSuccess = false,
                                saveError = result.message,
                            )
                            else -> it
                        }
                    }
                }
            }
        }
    }

    fun updateTaxSettings(taxRate: Double, currency: String) {
        val currentState = _state.value as? SettingsUiState.Success ?: return
        val currentTax = currentState.taxSettings ?: return

        viewModelScope.launch {
            _state.update {
                when (it) {
                    is SettingsUiState.Success -> it.copy(isSaving = true, saveSuccess = false, saveError = null)
                    else -> it
                }
            }

            val newTaxSettings = TaxSettings(
                id = currentTax.id,
                taxRate = taxRate,
                currency = currency,
                createdAt = currentTax.createdAt,
                updatedAt = currentTax.updatedAt,
            )

            val result = updateTaxSettingsUseCase(newTaxSettings)

            when (result) {
                is Result.Success -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isSaving = false,
                                saveSuccess = true,
                                saveError = null,
                                taxSettings = newTaxSettings,
                            )
                            else -> it
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isSaving = false,
                                saveSuccess = false,
                                saveError = result.message,
                            )
                            else -> it
                        }
                    }
                }
            }
        }
    }

    fun updateUserPreferences(language: String, theme: String, enableNotifications: Boolean, autoLogoutTimeout: Duration) {
        val currentState = _state.value as? SettingsUiState.Success ?: return
        val currentPrefs = currentState.userPreferences ?: return

        viewModelScope.launch {
            _state.update {
                when (it) {
                    is SettingsUiState.Success -> it.copy(isSaving = true, saveSuccess = false, saveError = null)
                    else -> it
                }
            }

            val newPreferences = UserPreferences(
                id = currentPrefs.id,
                language = language,
                theme = theme,
                enableNotifications = enableNotifications,
                autoLogoutTimeout = autoLogoutTimeout,
                createdAt = currentPrefs.createdAt,
                updatedAt = currentPrefs.updatedAt,
            )

            val result = updateUserPreferencesUseCase(newPreferences)

            when (result) {
                is Result.Success -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isSaving = false,
                                saveSuccess = true,
                                saveError = null,
                                userPreferences = newPreferences,
                            )
                            else -> it
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        when (it) {
                            is SettingsUiState.Success -> it.copy(
                                isSaving = false,
                                saveSuccess = false,
                                saveError = result.message,
                            )
                            else -> it
                        }
                    }
                }
            }
        }
    }

    fun clearSaveStatus() {
        _state.update {
            when (it) {
                is SettingsUiState.Success -> it.copy(saveSuccess = false, saveError = null)
                else -> it
            }
        }
    }
}

sealed class SettingsUiState {
    data object Loading : SettingsUiState()

    data class Success(
        val storeSettings: StoreSettings? = null,
        val receiptSettings: ReceiptSettings? = null,
        val taxSettings: TaxSettings? = null,
        val userPreferences: UserPreferences? = null,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
        val saveSuccess: Boolean = false,
        val saveError: String? = null,
    ) : SettingsUiState()

    data class Error(val message: String) : SettingsUiState()
}
