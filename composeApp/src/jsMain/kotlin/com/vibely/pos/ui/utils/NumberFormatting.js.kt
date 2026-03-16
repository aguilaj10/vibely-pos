package com.vibely.pos.ui.utils

/**
 * JavaScript implementation of currency formatting using Intl.NumberFormat API.
 */
actual fun Double.formatCurrency(currencyCode: String): String = try {
    js("new Intl.NumberFormat('en-US', { style: 'currency', currency: currencyCode }).format(this)") as String
} catch (e: Exception) {
    // Fallback for unsupported currencies
    val symbol = when (currencyCode) {
        "USD" -> "$"
        "EUR" -> "€"
        "MXN" -> "$"
        else -> currencyCode
    }
    "$symbol${this.formatDecimal(2)}"
}

/**
 * JavaScript implementation of percentage formatting.
 */
actual fun Double.formatPercentage(decimalPlaces: Int): String {
    val formatted = this.asDynamic().toFixed(decimalPlaces) as String
    return "$formatted%"
}

/**
 * JavaScript implementation of decimal formatting.
 */
actual fun Double.formatDecimal(decimalPlaces: Int): String = this.asDynamic().toFixed(decimalPlaces) as String
