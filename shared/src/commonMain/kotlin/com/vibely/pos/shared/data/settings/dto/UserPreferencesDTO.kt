package com.vibely.pos.shared.data.settings.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for user preferences from the backend.
 *
 * Contains user-specific application settings including language, theme,
 * notification preferences, and security settings.
 *
 * @param id Unique identifier (UUID from database).
 * @param language ISO 639-1 language code (e.g., "en", "es", "fr").
 * @param theme Application theme preference ("light", "dark", "system").
 * @param enableNotifications Whether to show system notifications.
 * @param autoLogoutTimeoutMinutes Duration of inactivity in minutes before automatic logout.
 * @param createdAt When settings were created (epoch milliseconds).
 * @param updatedAt When settings were last updated (epoch milliseconds).
 */
@Serializable
data class UserPreferencesDTO(
    @SerialName("id")
    val id: String,

    @SerialName("language")
    val language: String,

    @SerialName("theme")
    val theme: String,

    @SerialName("enable_notifications")
    val enableNotifications: Boolean,

    @SerialName("auto_logout_timeout_minutes")
    val autoLogoutTimeoutMinutes: Int,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("updated_at")
    val updatedAt: Long,
)
