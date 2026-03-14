package com.vibely.pos.shared.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for token refresh requests.
 *
 * Sent to the backend /auth/refresh endpoint.
 */
@Serializable
data class RefreshTokenRequestDTO(
    @SerialName("refresh_token")
    val refreshToken: String,
)
