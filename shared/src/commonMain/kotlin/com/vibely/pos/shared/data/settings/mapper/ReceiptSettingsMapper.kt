package com.vibely.pos.shared.data.settings.mapper

import com.vibely.pos.shared.data.settings.dto.ReceiptSettingsDTO
import com.vibely.pos.shared.domain.settings.entity.ReceiptSettings
import kotlin.time.Instant

/**
 * Mapper for converting between [ReceiptSettingsDTO] and [ReceiptSettings] domain entity.
 */
object ReceiptSettingsMapper {

    /**
     * Maps a [ReceiptSettingsDTO] from the backend to a [ReceiptSettings] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     */
    fun toDomain(dto: ReceiptSettingsDTO): ReceiptSettings = ReceiptSettings(
        id = dto.id,
        header = dto.header,
        footer = dto.footer,
        logoUrl = dto.logoUrl,
        showTax = dto.showTax,
        createdAt = Instant.fromEpochMilliseconds(dto.createdAt),
        updatedAt = Instant.fromEpochMilliseconds(dto.updatedAt),
    )

    /**
     * Maps a [ReceiptSettings] domain entity to a [ReceiptSettingsDTO] for the backend.
     *
     * @param settings The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(settings: ReceiptSettings): ReceiptSettingsDTO = ReceiptSettingsDTO(
        id = settings.id,
        header = settings.header,
        footer = settings.footer,
        logoUrl = settings.logoUrl,
        showTax = settings.showTax,
        createdAt = settings.createdAt.toEpochMilliseconds(),
        updatedAt = settings.updatedAt.toEpochMilliseconds(),
    )
}
