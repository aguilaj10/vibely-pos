package com.vibely.pos.shared.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for authentication responses from the backend.
 *
 * Contains JWT tokens and user information returned after successful login.
 */
@Serializable
data class AuthResponseDTO(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("expires_in")
    val expiresIn: Long,

    @SerialName("user")
    val user: UserDTO,
)
