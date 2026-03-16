package com.vibely.pos.shared.data.settings.mapper

import com.vibely.pos.shared.data.settings.dto.StoreSettingsDTO
import com.vibely.pos.shared.domain.settings.entity.StoreSettings
import kotlin.time.Instant

/**
 * Mapper for converting between [StoreSettingsDTO] and [StoreSettings] domain entity.
 */
object StoreSettingsMapper {

    /**
     * Maps a [StoreSettingsDTO] from the backend to a [StoreSettings] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     */
    fun toDomain(dto: StoreSettingsDTO): StoreSettings = StoreSettings(
        id = dto.id,
        storeName = dto.storeName,
        address = dto.address,
        phone = dto.phone,
        email = dto.email,
        createdAt = Instant.fromEpochMilliseconds(dto.createdAt),
        updatedAt = Instant.fromEpochMilliseconds(dto.updatedAt),
    )

    /**
     * Maps a [StoreSettings] domain entity to a [StoreSettingsDTO] for the backend.
     *
     * @param settings The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(settings: StoreSettings): StoreSettingsDTO = StoreSettingsDTO(
        id = settings.id,
        storeName = settings.storeName,
        address = settings.address,
        phone = settings.phone,
        email = settings.email,
        createdAt = settings.createdAt.toEpochMilliseconds(),
        updatedAt = settings.updatedAt.toEpochMilliseconds(),
    )
}
