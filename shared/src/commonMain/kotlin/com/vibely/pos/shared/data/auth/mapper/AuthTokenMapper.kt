package com.vibely.pos.shared.data.auth.mapper

import com.vibely.pos.shared.data.auth.dto.AuthResponseDTO
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken

/**
 * Mapper for converting authentication response DTOs to domain objects.
 */
object AuthTokenMapper {

    /**
     * Maps an [AuthResponseDTO] from the backend to an [AuthToken] domain value object.
     *
     * @param dto The DTO from the backend.
     * @return The domain value object.
     */
    fun toDomain(dto: AuthResponseDTO): AuthToken = AuthToken.create(
        accessToken = dto.accessToken,
        refreshToken = dto.refreshToken,
        expiresInSeconds = dto.expiresIn,
    )
}
