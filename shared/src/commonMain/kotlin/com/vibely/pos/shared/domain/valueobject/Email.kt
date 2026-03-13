package com.vibely.pos.shared.domain.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException

/**
 * Value object representing a validated email address.
 *
 * Performs basic structural validation (presence of '@', domain with dot, etc.).
 * Does not attempt full RFC 5322 compliance, as that requires significantly more
 * complexity with marginal practical benefit.
 *
 * @param value The validated, lowercased email address.
 */
@ConsistentCopyVisibility
data class Email private constructor(val value: String) {
    /**
     * The local part of the email address (before the '@').
     */
    val localPart: String
        get() = value.substringBefore('@')

    /**
     * The domain part of the email address (after the '@').
     */
    val domain: String
        get() = value.substringAfter('@')

    override fun toString(): String = value

    companion object {
        private const val MAX_LENGTH = 254

        /**
         * Basic email pattern: non-empty local part, '@', domain with at least one dot.
         * Allows common characters in the local part including dots, hyphens, underscores, and plus.
         */
        private val EMAIL_REGEX =
            Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")

        /**
         * Creates a validated [Email] from the given [value].
         *
         * The value is trimmed and lowercased before validation.
         *
         * @param value The email address string to validate.
         * @throws ValidationException if the value is blank, too long, or has invalid format.
         */
        fun create(value: String): Email {
            val normalized = value.trim().lowercase()

            if (normalized.isBlank()) {
                throw ValidationException(
                    field = "email",
                    message = "Email cannot be blank",
                )
            }

            if (normalized.length > MAX_LENGTH) {
                throw ValidationException(
                    field = "email",
                    message = "Email must be at most $MAX_LENGTH characters, got ${normalized.length}",
                )
            }

            if (!EMAIL_REGEX.matches(normalized)) {
                throw ValidationException(
                    field = "email",
                    message = "Invalid email format: $normalized",
                )
            }

            return Email(normalized)
        }
    }
}
