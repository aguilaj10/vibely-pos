package com.vibely.pos.shared.domain.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException

/**
 * Value object representing a Stock Keeping Unit (SKU).
 *
 * A SKU is a unique identifier for a product, typically composed of
 * uppercase alphanumeric characters and hyphens.
 *
 * @param value The validated SKU string.
 */
@ConsistentCopyVisibility
data class SKU private constructor(val value: String) {
    override fun toString(): String = value

    companion object {
        /**
         * SKU format: 3-20 characters, uppercase alphanumeric and hyphens.
         * Must start and end with an alphanumeric character.
         * Examples: "ABC-123", "PROD001", "SKU-XL-BLU"
         */
        private val SKU_REGEX = Regex("^[A-Z0-9][A-Z0-9\\-]{1,18}[A-Z0-9]$")

        private const val MIN_LENGTH = 3
        private const val MAX_LENGTH = 20

        /**
         * Creates a validated [SKU] from the given [value].
         *
         * The value is trimmed and uppercased before validation.
         *
         * @param value The SKU string to validate.
         * @throws ValidationException if the value is blank, too short/long, or has invalid characters.
         */
        fun create(value: String): SKU {
            val normalized = value.trim().uppercase()

            if (normalized.isBlank()) {
                throw ValidationException(
                    field = "sku",
                    message = "SKU cannot be blank",
                )
            }

            if (normalized.length < MIN_LENGTH) {
                throw ValidationException(
                    field = "sku",
                    message = "SKU must be at least $MIN_LENGTH characters, got ${normalized.length}",
                )
            }

            if (normalized.length > MAX_LENGTH) {
                throw ValidationException(
                    field = "sku",
                    message = "SKU must be at most $MAX_LENGTH characters, got ${normalized.length}",
                )
            }

            if (!SKU_REGEX.matches(normalized)) {
                throw ValidationException(
                    field = "sku",
                    message = "SKU must contain only uppercase alphanumeric characters and hyphens, " +
                        "and must start and end with an alphanumeric character. Got: $normalized",
                )
            }

            return SKU(normalized)
        }
    }
}
