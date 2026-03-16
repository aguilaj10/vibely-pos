package com.vibely.pos.shared.domain.settings.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.settings.entity.StoreSettings
import com.vibely.pos.shared.domain.settings.repository.SettingsRepository

/**
 * Use case for updating store information settings.
 *
 * Validates and persists changes to core business details like
 * store name, address, phone, and email.
 *
 * @param settingsRepository The settings data repository.
 */
class UpdateStoreInfoUseCase(private val settingsRepository: SettingsRepository) {
    /**
     * Executes the use case to update store settings.
     *
     * @param settings New store settings to persist.
     * @return [Result.Success] with Unit if update succeeds,
     *         [Result.Error] if validation or persistence fails.
     */
    suspend operator fun invoke(settings: StoreSettings): Result<Unit> = settingsRepository.updateStoreSettings(settings)
}
