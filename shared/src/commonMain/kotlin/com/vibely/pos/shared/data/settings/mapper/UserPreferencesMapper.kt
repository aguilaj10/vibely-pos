package com.vibely.pos.shared.data.settings.mapper

import com.vibely.pos.shared.data.settings.dto.UserPreferencesDTO
import com.vibely.pos.shared.domain.settings.entity.UserPreferences
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Mapper for converting between [UserPreferencesDTO] and [UserPreferences] domain entity.
 */
object UserPreferencesMapper {

    /**
     * Maps a [UserPreferencesDTO] from the backend to a [UserPreferences] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     */
    fun toDomain(dto: UserPreferencesDTO): UserPreferences = UserPreferences(
        id = dto.id,
        language = dto.language,
        theme = dto.theme,
        enableNotifications = dto.enableNotifications,
        autoLogoutTimeout = dto.autoLogoutTimeoutMinutes.minutes,
        createdAt = Instant.fromEpochMilliseconds(dto.createdAt),
        updatedAt = Instant.fromEpochMilliseconds(dto.updatedAt),
    )

    /**
     * Maps a [UserPreferences] domain entity to a [UserPreferencesDTO] for the backend.
     *
     * @param preferences The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(preferences: UserPreferences): UserPreferencesDTO = UserPreferencesDTO(
        id = preferences.id,
        language = preferences.language,
        theme = preferences.theme,
        enableNotifications = preferences.enableNotifications,
        autoLogoutTimeoutMinutes = preferences.autoLogoutTimeout.inWholeMinutes.toInt(),
        createdAt = preferences.createdAt.toEpochMilliseconds(),
        updatedAt = preferences.updatedAt.toEpochMilliseconds(),
    )
}
