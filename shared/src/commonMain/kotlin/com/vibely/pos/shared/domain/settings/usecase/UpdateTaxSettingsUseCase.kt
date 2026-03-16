package com.vibely.pos.shared.domain.settings.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.settings.entity.TaxSettings
import com.vibely.pos.shared.domain.settings.repository.SettingsRepository

/**
 * Use case for updating tax and currency settings.
 *
 * @param settingsRepository The settings data repository.
 */
class UpdateTaxSettingsUseCase(private val settingsRepository: SettingsRepository) {
    /**
     * Executes the use case to update tax settings.
     *
     * @param settings New tax settings to persist.
     * @return [Result.Success] with Unit if update succeeds,
     *         [Result.Error] if validation or persistence fails.
     */
    suspend operator fun invoke(settings: TaxSettings): Result<Unit> = settingsRepository.updateTaxSettings(settings)
}
