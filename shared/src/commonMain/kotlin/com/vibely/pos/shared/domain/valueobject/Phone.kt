package com.vibely.pos.shared.domain.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException

/**
 * Value object representing a validated phone number.
 *
 * Stores the phone number in a normalized digits-only format (with optional leading '+').
 * Supports international phone numbers with country codes.
 *
 * @param value The validated, normalized phone number.
 */
@ConsistentCopyVisibility
data class Phone private constructor(val value: String) {
    /**
     * Returns `true` if this phone number has an international prefix (starts with '+').
     */
    val isInternational: Boolean
        get() = value.startsWith('+')

    override fun toString(): String = value

    companion object {
        private const val MIN_DIGITS = 7
        private const val MAX_DIGITS = 15

        /**
         * Creates a validated [Phone] from the given [value].
         *
         * The value is normalized by removing spaces, parentheses, hyphens, and dots.
         * An optional leading '+' for international format is preserved.
         *
         * Valid examples: "+1234567890", "(555) 123-4567", "+52 55 1234 5678"
         *
         * @param value The phone number string to validate.
         * @throws ValidationException if the value is blank, too short/long, or has invalid characters.
         */
        fun create(value: String): Phone {
            val trimmed = value.trim()

            if (trimmed.isBlank()) {
                throw ValidationException(
                    field = "phone",
                    message = "Phone number cannot be blank",
                )
            }

            // Normalize: remove common formatting characters
            val normalized = normalizePhoneNumber(trimmed)

            // Extract digits only (excluding leading '+')
            val digitsOnly = normalized.removePrefix("+")

            if (!digitsOnly.all { it.isDigit() }) {
                throw ValidationException(
                    field = "phone",
                    message = "Phone number contains invalid characters. " +
                        "Only digits, spaces, parentheses, hyphens, dots, and leading '+' are allowed.",
                )
            }

            if (digitsOnly.length < MIN_DIGITS) {
                throw ValidationException(
                    field = "phone",
                    message = "Phone number must have at least $MIN_DIGITS digits, got ${digitsOnly.length}",
                )
            }

            if (digitsOnly.length > MAX_DIGITS) {
                throw ValidationException(
                    field = "phone",
                    message = "Phone number must have at most $MAX_DIGITS digits, got ${digitsOnly.length}",
                )
            }

            return Phone(normalized)
        }

        private fun normalizePhoneNumber(input: String): String {
            val hasPlus = input.startsWith('+')
            val digits = input.filter { it.isDigit() }
            return if (hasPlus) "+$digits" else digits
        }
    }
}
