package com.vibely.pos.shared.util

/**
 * Utility object for formatting values for display.
 *
 * Provides consistent formatting functions across all UI layers.
 */
object FormatUtils {

    /**
     * Formats a monetary amount as USD currency string.
     *
     * Note: This is a simplified implementation compatible with Kotlin Multiplatform.
     * Does NOT use String.format() which is not available in common code.
     *
     * Example:
     * ```
     * formatCurrency(123.45) // Returns "$123.45"
     * formatCurrency(99.99)  // Returns "$99.99"
     * formatCurrency(1000.0) // Returns "$1000.00"
     * ```
     *
     * @param amount The amount to format (e.g., 123.45)
     * @return Formatted currency string (e.g., "$123.45")
     */
    fun formatCurrency(amount: Double): String {
        val wholePart = amount.toInt()
        val decimalPart = ((amount - wholePart) * 100).toInt()
        return "$$wholePart.${decimalPart.toString().padStart(2, '0')}"
    }
}
