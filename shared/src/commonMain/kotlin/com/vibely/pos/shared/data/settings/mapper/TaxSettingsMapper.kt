package com.vibely.pos.shared.data.settings.mapper

import com.vibely.pos.shared.data.settings.dto.TaxSettingsDTO
import com.vibely.pos.shared.domain.settings.entity.TaxSettings
import kotlin.time.Instant

/**
 * Mapper for converting between [TaxSettingsDTO] and [TaxSettings] domain entity.
 */
object TaxSettingsMapper {

    /**
     * Maps a [TaxSettingsDTO] from the backend to a [TaxSettings] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     */
    fun toDomain(dto: TaxSettingsDTO): TaxSettings = TaxSettings(
        id = dto.id,
        taxRate = dto.taxRate,
        currency = dto.currency,
        createdAt = Instant.fromEpochMilliseconds(dto.createdAt),
        updatedAt = Instant.fromEpochMilliseconds(dto.updatedAt),
    )

    /**
     * Maps a [TaxSettings] domain entity to a [TaxSettingsDTO] for the backend.
     *
     * @param settings The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(settings: TaxSettings): TaxSettingsDTO = TaxSettingsDTO(
        id = settings.id,
        taxRate = settings.taxRate,
        currency = settings.currency,
        createdAt = settings.createdAt.toEpochMilliseconds(),
        updatedAt = settings.updatedAt.toEpochMilliseconds(),
    )
}
