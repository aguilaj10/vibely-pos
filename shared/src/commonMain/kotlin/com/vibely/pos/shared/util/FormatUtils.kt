package com.vibely.pos.shared.util

import kotlin.time.Instant

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

    /**
     * Formats Instant into readable dates
     *
     * @param Instant that has the date to format
     * @return The formatted date string
     */
    fun formatDate(instant: Instant): String {
        val epochMillis = instant.toEpochMilliseconds()
        val daySeconds = 86400L
        val seconds = epochMillis / 1000
        var days = seconds / daySeconds

        var year = 1970
        while (true) {
            val leap = if ((year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)) 1 else 0
            val daysInYear = 365 + leap
            if (days < daysInYear)break
            days -= daysInYear
            year++
        }

        val isLeap = (year % 4 == 0 && year % 100 != 0 || year % 400 == 0)
        val monthDays = intArrayOf(31, if (isLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var month = 0
        while (days >= monthDays[month]) {
            days -= monthDays[month]
            month++
        }

        val day = days + 1

        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return "${monthNames[month]} $day, $year"
    }
}
