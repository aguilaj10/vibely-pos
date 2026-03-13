package com.vibely.pos.shared.domain.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException
import kotlin.math.absoluteValue

/**
 * Value object representing a monetary amount with currency.
 *
 * Internally stores amounts as cents (Long) to avoid floating-point precision issues
 * and ensure cross-platform compatibility in Kotlin Multiplatform.
 *
 * @param amountInCents The monetary amount in the smallest currency unit (e.g., cents).
 * @param currency The ISO 4217 currency code (e.g., "USD", "EUR", "MXN").
 */
@ConsistentCopyVisibility
data class Money private constructor(val amountInCents: Long, val currency: String) {
    /**
     * The monetary amount as a Double. Use only for display purposes;
     * prefer [amountInCents] for calculations to maintain precision.
     */
    val amount: Double
        get() = amountInCents / CENTS_PER_UNIT

    /**
     * Returns `true` if the amount is zero.
     */
    val isZero: Boolean
        get() = amountInCents == 0L

    /**
     * Returns `true` if the amount is positive (greater than zero).
     */
    val isPositive: Boolean
        get() = amountInCents > 0L

    /**
     * Returns `true` if the amount is negative (less than zero).
     */
    val isNegative: Boolean
        get() = amountInCents < 0L

    /**
     * Adds this [Money] to [other]. Both must have the same currency.
     *
     * @throws ValidationException if currencies don't match.
     */
    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return Money(amountInCents + other.amountInCents, currency)
    }

    /**
     * Subtracts [other] from this [Money]. Both must have the same currency.
     *
     * @throws ValidationException if currencies don't match.
     */
    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return Money(amountInCents - other.amountInCents, currency)
    }

    /**
     * Multiplies this [Money] by a [factor].
     */
    operator fun times(factor: Int): Money = Money(amountInCents * factor, currency)

    /**
     * Multiplies this [Money] by a [factor].
     * The result is rounded to the nearest cent.
     */
    operator fun times(factor: Double): Money = Money((amountInCents * factor).toLong(), currency)

    /**
     * Returns the absolute value of this [Money].
     */
    fun abs(): Money = Money(amountInCents.absoluteValue, currency)

    /**
     * Returns a negated copy of this [Money].
     */
    operator fun unaryMinus(): Money = Money(-amountInCents, currency)

    /**
     * Compares this [Money] to [other]. Both must have the same currency.
     */
    operator fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return amountInCents.compareTo(other.amountInCents)
    }

    /**
     * Formats this monetary amount for display (e.g., "100.50 USD").
     */
    fun formatDisplay(): String {
        val whole = amountInCents / CENTS_DIVISOR
        val fraction = (amountInCents % CENTS_DIVISOR).absoluteValue
        val sign = if (amountInCents < 0) "-" else ""
        return "$sign${whole.absoluteValue}.${"$fraction".padStart(2, '0')} $currency"
    }

    override fun toString(): String = formatDisplay()

    private fun requireSameCurrency(other: Money) {
        if (currency != other.currency) {
            throw ValidationException(
                field = "currency",
                message = "Cannot operate on different currencies: $currency vs ${other.currency}",
            )
        }
    }

    companion object {
        private const val CENTS_PER_UNIT: Double = 100.0
        private const val CENTS_DIVISOR: Long = 100L

        private val VALID_CURRENCY_REGEX = Regex("^[A-Z]{3}$")

        /**
         * Creates a [Money] instance from an amount in cents.
         *
         * @param amountInCents The amount in the smallest currency unit.
         * @param currency The ISO 4217 currency code.
         * @throws ValidationException if the currency code is invalid.
         */
        fun fromCents(amountInCents: Long, currency: String): Money {
            validateCurrency(currency)
            return Money(amountInCents, currency.uppercase())
        }

        /**
         * Creates a [Money] instance from a fractional amount (e.g., 10.50).
         *
         * @param amount The monetary amount in major currency units.
         * @param currency The ISO 4217 currency code.
         * @throws ValidationException if the currency code is invalid.
         */
        fun fromAmount(amount: Double, currency: String): Money {
            validateCurrency(currency)
            return Money((amount * CENTS_PER_UNIT).toLong(), currency.uppercase())
        }

        /**
         * Creates a zero-value [Money] in the given [currency].
         */
        fun zero(currency: String): Money {
            validateCurrency(currency)
            return Money(0L, currency.uppercase())
        }

        private fun validateCurrency(currency: String) {
            if (!VALID_CURRENCY_REGEX.matches(currency.uppercase())) {
                throw ValidationException(
                    field = "currency",
                    message = "Invalid ISO 4217 currency code: $currency. Must be 3 uppercase letters.",
                )
            }
        }
    }
}
