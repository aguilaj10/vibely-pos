package com.vibely.pos.ui.utils

import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale
import kotlin.math.pow
import kotlin.math.roundToLong

private val currencyFormatter = CurrencyFormatter(KurrencyLocale.systemLocale())

/**
 * Formats a double value as currency with the specified currency code.
 *
 * @param currencyCode ISO 4217 currency code (e.g., "USD", "EUR", "MXN")
 * @return Formatted currency string
 */
fun Double.formatCurrency(currencyCode: String = "USD"): String = currencyFormatter
    .formatCurrencyStyleResult(
        this.toString(),
        currencyCode,
    ).getOrNull() ?: "$0.00"

/**
 * Formats a long value (in cents) as currency with the specified currency code.
 *
 * @param currencyCode ISO 4217 currency code (e.g., "USD", "EUR", "MXN")
 * @return Formatted currency string
 */
fun Long.formatCurrency(currencyCode: String = "USD"): String = (this / 100.0).formatCurrency(currencyCode)

/**
 * Formats a double value as a percentage with the specified number of decimal places.
 *
 * @param decimalPlaces Number of decimal places (default: 1)
 * @return Formatted percentage string (e.g., "25.5%")
 */
fun Double.formatPercentage(decimalPlaces: Int = 1): String = "${this.formatDecimal(decimalPlaces)}%"

/**
 * Formats a double value with the specified number of decimal places.
 *
 * @param decimalPlaces Number of decimal places (default: 2)
 * @return Formatted number string
 */
fun Double.formatDecimal(decimalPlaces: Int = 2): String {
    val factor = 10.0.pow(decimalPlaces)
    val rounded = (this * factor).roundToLong() / factor

    return if (decimalPlaces == 0) {
        rounded.toLong().toString()
    } else {
        val integerPart = rounded.toLong()
        val fractionalPart = ((rounded - integerPart) * factor).roundToLong()
        "$integerPart.${fractionalPart.toString().padStart(decimalPlaces, '0')}"
    }
}
