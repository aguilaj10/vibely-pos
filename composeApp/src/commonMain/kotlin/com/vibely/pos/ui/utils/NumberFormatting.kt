package com.vibely.pos.ui.utils

/**
 * Formats a double value as currency with the specified currency code.
 *
 * @param currencyCode ISO 4217 currency code (e.g., "USD", "EUR", "MXN")
 * @return Formatted currency string
 */
expect fun Double.formatCurrency(currencyCode: String = "USD"): String

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
expect fun Double.formatPercentage(decimalPlaces: Int = 1): String

/**
 * Formats a double value with the specified number of decimal places.
 *
 * @param decimalPlaces Number of decimal places (default: 2)
 * @return Formatted number string
 */
expect fun Double.formatDecimal(decimalPlaces: Int = 2): String
