package com.vibely.pos.ui.utils

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.localeWithLocaleIdentifier

/**
 * iOS implementation of currency formatting using NSNumberFormatter.
 */
actual fun Double.formatCurrency(currencyCode: String): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    formatter.locale = NSLocale.localeWithLocaleIdentifier("en_US")
    formatter.currencyCode = currencyCode
    return formatter.stringFromNumber(NSNumber(this)) ?: "$currencyCode ${this.formatDecimal(2)}"
}

/**
 * iOS implementation of percentage formatting.
 */
actual fun Double.formatPercentage(decimalPlaces: Int): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    formatter.minimumFractionDigits = decimalPlaces.toULong()
    formatter.maximumFractionDigits = decimalPlaces.toULong()
    val formatted = formatter.stringFromNumber(NSNumber(this)) ?: this.toString()
    return "$formatted%"
}

/**
 * iOS implementation of decimal formatting.
 */
actual fun Double.formatDecimal(decimalPlaces: Int): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    formatter.minimumFractionDigits = decimalPlaces.toULong()
    formatter.maximumFractionDigits = decimalPlaces.toULong()
    return formatter.stringFromNumber(NSNumber(this)) ?: this.toString()
}
