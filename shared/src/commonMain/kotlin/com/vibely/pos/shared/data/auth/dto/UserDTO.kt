package com.vibely.pos.shared.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for user data from the backend.
 *
 * Maps to the backend User response structure.
 */
@Serializable
data class UserDTO(
    @SerialName("id")
    val id: String,

    @SerialName("email")
    val email: String,

    @SerialName("full_name")
    val fullName: String,

    @SerialName("role")
    val role: String,

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("last_login_at")
    val lastLoginAt: String? = null,
)
