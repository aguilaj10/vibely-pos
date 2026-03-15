@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val email: String,
    val password: String,
    @SerialName("full_name")
    val fullName: String,
    val role: String,
)

@Serializable
data class UpdateUserRequest(
    @SerialName("full_name")
    val fullName: String? = null,
    val role: String? = null,
)

@Serializable
data class UpdateUserStatusRequest(
    val status: String,
)

@Serializable
data class AssignRoleRequest(
    val role: String,
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password")
    val currentPassword: String,
    @SerialName("new_password")
    val newPassword: String,
)

@Serializable
data class ResetPasswordRequest(
    @SerialName("new_password")
    val newPassword: String,
)
