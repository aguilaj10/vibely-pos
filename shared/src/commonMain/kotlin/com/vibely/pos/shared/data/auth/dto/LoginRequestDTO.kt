package com.vibely.pos.shared.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for login requests.
 *
 * Sent to the backend /auth/login endpoint.
 */
@Serializable
data class LoginRequestDTO(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String,
)
