package com.vibely.pos.shared.domain.settings.entity

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Domain entity representing user-specific application preferences.
 *
 * Controls UI behavior, localization, and security settings for individual users.
 *
 * @param id Unique identifier (UUID from database).
 * @param language ISO 639-1 language code (e.g., "en", "es", "fr").
 * @param theme Application theme preference ("light", "dark", "system").
 * @param enableNotifications Whether to show system notifications.
 * @param autoLogoutTimeout Duration of inactivity before automatic logout.
 * @param createdAt When settings were created.
 * @param updatedAt When settings were last updated.
 */
data class UserPreferences(
    val id: String,
    val language: String,
    val theme: String,
    val enableNotifications: Boolean,
    val autoLogoutTimeout: Duration,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(language.length == 2) { "Language must be a 2-letter ISO 639-1 code" }
        require(theme in VALID_THEMES) { "Theme must be one of: ${VALID_THEMES.joinToString()}" }
        require(autoLogoutTimeout >= MIN_TIMEOUT && autoLogoutTimeout <= MAX_TIMEOUT) {
            "Auto-logout timeout must be between $MIN_TIMEOUT and $MAX_TIMEOUT"
        }
    }

    companion object {
        private val VALID_THEMES = setOf("light", "dark", "system")
        private val MIN_TIMEOUT = 5.minutes
        private val MAX_TIMEOUT = 120.minutes
        val DEFAULT_TIMEOUT = 30.minutes

        /**
         * Creates a new UserPreferences instance with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            id: String,
            language: String = "en",
            theme: String = "system",
            enableNotifications: Boolean = true,
            autoLogoutTimeout: Duration = DEFAULT_TIMEOUT,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): UserPreferences {
            val normalizedLanguage = language.trim().lowercase()
            val normalizedTheme = theme.trim().lowercase()

            return UserPreferences(
                id = id,
                language = normalizedLanguage,
                theme = normalizedTheme,
                enableNotifications = enableNotifications,
                autoLogoutTimeout = autoLogoutTimeout,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }
}
