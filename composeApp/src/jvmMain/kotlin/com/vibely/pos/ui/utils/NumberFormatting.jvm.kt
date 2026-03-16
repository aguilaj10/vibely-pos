package com.vibely.pos.ui.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * JVM implementation of currency formatting using java.text.NumberFormat.
 */
actual fun Double.formatCurrency(currencyCode: String): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    formatter.currency = Currency.getInstance(currencyCode)
    return formatter.format(this)
}

/**
 * JVM implementation of percentage formatting.
 */
actual fun Double.formatPercentage(decimalPlaces: Int): String = String.format(Locale.US, "%.${decimalPlaces}f%%", this)

/**
 * JVM implementation of decimal formatting.
 */
actual fun Double.formatDecimal(decimalPlaces: Int): String = String.format(Locale.US, "%.${decimalPlaces}f", this)
