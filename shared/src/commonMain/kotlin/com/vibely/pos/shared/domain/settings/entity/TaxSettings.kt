package com.vibely.pos.shared.domain.settings.entity

import com.vibely.pos.shared.domain.exception.ValidationException
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Domain entity representing tax and currency configuration.
 *
 * Controls how the system calculates and displays taxes and monetary values.
 * Tax rate is stored as a percentage (e.g., 16.0 for 16% tax).
 *
 * @param id Unique identifier (UUID from database).
 * @param taxRate Tax percentage rate (e.g., 16.0 for 16%).
 * @param currency ISO 4217 currency code (e.g., "USD", "MXN", "EUR").
 * @param createdAt When settings were created.
 * @param updatedAt When settings were last updated.
 */
data class TaxSettings(val id: String, val taxRate: Double, val currency: String, val createdAt: Instant, val updatedAt: Instant) {
    init {
        require(taxRate in 0.0..100.0) { "Tax rate must be between 0% and 100%" }
        require(currency.length == 3) { "Currency must be a 3-letter ISO 4217 code" }
        require(currency.all { it.isUpperCase() || it.isDigit() }) {
            "Currency code must be uppercase letters"
        }
    }

    companion object {
        private val VALID_CURRENCY_REGEX = Regex("^[A-Z]{3}$")

        /**
         * Creates a new TaxSettings instance with validation.
         *
         * @throws ValidationException if any field is invalid.
         */
        fun create(
            id: String,
            taxRate: Double,
            currency: String,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): TaxSettings {
            val normalizedCurrency = currency.trim().uppercase()

            if (!VALID_CURRENCY_REGEX.matches(normalizedCurrency)) {
                throw ValidationException(
                    field = "currency",
                    message = "Invalid ISO 4217 currency code: $currency. Must be 3 uppercase letters.",
                )
            }

            if (taxRate < 0.0 || taxRate > 100.0) {
                throw ValidationException(
                    field = "taxRate",
                    message = "Tax rate must be between 0% and 100%. Got: $taxRate",
                )
            }

            return TaxSettings(
                id = id,
                taxRate = taxRate,
                currency = normalizedCurrency,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }
}
