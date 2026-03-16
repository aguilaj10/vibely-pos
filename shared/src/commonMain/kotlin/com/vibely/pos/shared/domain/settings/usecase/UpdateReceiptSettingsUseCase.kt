package com.vibely.pos.shared.domain.settings.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.settings.entity.ReceiptSettings
import com.vibely.pos.shared.domain.settings.repository.SettingsRepository

/**
 * Use case for updating receipt formatting settings.
 *
 * @param settingsRepository The settings data repository.
 */
class UpdateReceiptSettingsUseCase(private val settingsRepository: SettingsRepository) {
    /**
     * Executes the use case to update receipt settings.
     *
     * @param settings New receipt settings to persist.
     * @return [Result.Success] with Unit if update succeeds,
     *         [Result.Error] if validation or persistence fails.
     */
    suspend operator fun invoke(settings: ReceiptSettings): Result<Unit> = settingsRepository.updateReceiptSettings(settings)
}
