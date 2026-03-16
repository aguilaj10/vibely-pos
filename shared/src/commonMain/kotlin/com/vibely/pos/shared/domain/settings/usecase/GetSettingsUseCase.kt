package com.vibely.pos.shared.domain.settings.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.settings.entity.ReceiptSettings
import com.vibely.pos.shared.domain.settings.entity.StoreSettings
import com.vibely.pos.shared.domain.settings.entity.TaxSettings
import com.vibely.pos.shared.domain.settings.entity.UserPreferences
import com.vibely.pos.shared.domain.settings.repository.SettingsRepository

/**
 * Use case for retrieving all system settings.
 *
 * Aggregates store, receipt, tax, and user preference settings into
 * a unified data structure for display in the settings screen.
 *
 * @param settingsRepository The settings data repository.
 */
class GetSettingsUseCase(private val settingsRepository: SettingsRepository) {
    /**
     * Executes the use case to retrieve all settings.
     *
     * @return [Result.Success] with [Settings] if successful,
     *         [Result.Error] if any retrieval fails.
     */
    suspend operator fun invoke(): Result<Settings> {
        val storeResult = settingsRepository.getStoreSettings()
        val receiptResult = settingsRepository.getReceiptSettings()
        val taxResult = settingsRepository.getTaxSettings()
        val preferencesResult = settingsRepository.getUserPreferences()

        return when {
            storeResult is Result.Error -> storeResult
            receiptResult is Result.Error -> receiptResult
            taxResult is Result.Error -> taxResult
            preferencesResult is Result.Error -> preferencesResult
            else -> Result.Success(
                Settings(
                    store = (storeResult as Result.Success).data,
                    receipt = (receiptResult as Result.Success).data,
                    tax = (taxResult as Result.Success).data,
                    preferences = (preferencesResult as Result.Success).data,
                ),
            )
        }
    }

    /**
     * Aggregated settings data structure.
     */
    data class Settings(val store: StoreSettings, val receipt: ReceiptSettings, val tax: TaxSettings, val preferences: UserPreferences)
}
