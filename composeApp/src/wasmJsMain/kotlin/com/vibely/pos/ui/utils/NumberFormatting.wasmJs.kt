@file:OptIn(ExperimentalWasmJsInterop::class)

package com.vibely.pos.ui.utils

/**
 * JS interop: Call Number.toFixed() method.
 * Must be top-level single-expression for Kotlin/Wasm js() restrictions.
 */
private fun jsToFixed(value: Double, digits: Int): String = js("value.toFixed(digits)")

/**
 * JS interop: Call Intl.NumberFormat with currency options.
 * Must be top-level single-expression for Kotlin/Wasm js() restrictions.
 */
private fun jsFormatCurrency(value: Double, currencyCode: String): String =
    js("new Intl.NumberFormat('en-US', { style: 'currency', currency: currencyCode }).format(value)")

/**
 * WasmJS implementation of currency formatting using Intl.NumberFormat API.
 */
actual fun Double.formatCurrency(currencyCode: String): String = try {
    jsFormatCurrency(this, currencyCode)
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
 * WasmJS implementation of percentage formatting.
 */
actual fun Double.formatPercentage(decimalPlaces: Int): String {
    val formatted = jsToFixed(this, decimalPlaces)
    return "$formatted%"
}

/**
 * WasmJS implementation of decimal formatting.
 */
actual fun Double.formatDecimal(decimalPlaces: Int): String = jsToFixed(this, decimalPlaces)
