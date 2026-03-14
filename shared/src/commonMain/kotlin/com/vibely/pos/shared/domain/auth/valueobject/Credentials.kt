package com.vibely.pos.shared.domain.auth.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException
import com.vibely.pos.shared.domain.valueobject.Email

/**
 * Value object representing user credentials for authentication.
 *
 * Encapsulates email and password with validation rules:
 * - Email: Must be valid email format (reuses [Email] value object)
 * - Password: Must be at least 8 characters, contain uppercase, lowercase, digit, and special char
 *
 * @param email The validated email address.
 * @param password The password (stored in plain text for transmission; hashing done at data layer).
 */
@ConsistentCopyVisibility
data class Credentials private constructor(val email: Email, val password: String) {
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_LENGTH = 128

        /**
         * Password must contain:
         * - At least one uppercase letter
         * - At least one lowercase letter
         * - At least one digit
         * - At least one special character from: !@#$%^&*()_+-=[]{}|;:,.<>?
         */
        private val PASSWORD_REGEX = Regex(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).+$",
        )

        /**
         * Creates validated [Credentials] from email and password strings.
         *
         * @param email The email address (will be validated using Email.create).
         * @param password The password string to validate.
         * @throws ValidationException if email or password validation fails.
         */
        fun create(email: String, password: String): Credentials {
            val validatedEmail = Email.create(email)
            validatePassword(password)
            return Credentials(validatedEmail, password)
        }

        /**
         * Creates [Credentials] from an already validated [Email] instance.
         *
         * @param email The validated email address.
         * @param password The password string to validate.
         * @throws ValidationException if password validation fails.
         */
        fun create(email: Email, password: String): Credentials {
            validatePassword(password)
            return Credentials(email, password)
        }

        /**
         * Validates password according to security requirements.
         *
         * @throws ValidationException if password does not meet requirements.
         */
        private fun validatePassword(password: String) {
            if (password.isBlank()) {
                throw ValidationException(
                    field = "password",
                    message = "Password cannot be blank",
                )
            }

            if (password.length < MIN_PASSWORD_LENGTH) {
                throw ValidationException(
                    field = "password",
                    message = "Password must be at least $MIN_PASSWORD_LENGTH characters",
                )
            }

            if (password.length > MAX_PASSWORD_LENGTH) {
                throw ValidationException(
                    field = "password",
                    message = "Password must be at most $MAX_PASSWORD_LENGTH characters",
                )
            }

            if (!PASSWORD_REGEX.matches(password)) {
                throw ValidationException(
                    field = "password",
                    message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character",
                )
            }
        }
    }
}
