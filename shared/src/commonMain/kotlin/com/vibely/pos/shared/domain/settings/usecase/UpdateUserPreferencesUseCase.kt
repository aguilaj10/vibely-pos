package com.vibely.pos.shared.domain.settings.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.settings.entity.UserPreferences
import com.vibely.pos.shared.domain.settings.repository.SettingsRepository

/**
 * Use case for updating user preferences.
 *
 * @param settingsRepository The settings data repository.
 */
class UpdateUserPreferencesUseCase(private val settingsRepository: SettingsRepository) {
    /**
     * Executes the use case to update user preferences.
     *
     * @param preferences New user preferences to persist.
     * @return [Result.Success] with Unit if update succeeds,
     *         [Result.Error] if validation or persistence fails.
     */
    suspend operator fun invoke(preferences: UserPreferences): Result<Unit> = settingsRepository.updateUserPreferences(preferences)
}
