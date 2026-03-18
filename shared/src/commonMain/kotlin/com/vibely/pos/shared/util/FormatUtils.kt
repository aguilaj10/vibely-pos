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
     * @param instant that has the date to format
     * @return The formatted date string
     */
    fun formatDate(instant: Instant): String {
        val (year, month, day) = epochToDate(instant.toEpochMilliseconds())
        return "${MONTH_NAMES[month]} $day, $year"
    }

    /**
     * Formats Instant into readable date and time
     *
     * @param instant The instant to format
     * @return The formatted date-time string (e.g., "Jan 3, 2026 14:30")
     */
    fun formatDateTime(instant: Instant): String {
        val epochMillis = instant.toEpochMilliseconds()
        val (year, month, day) = epochToDate(epochMillis)
        val (hours, minutes) = epochToTime(epochMillis)
        return "${MONTH_NAMES[month]} $day, $year ${hours.toString().padStart(
            2,
            '0',
        )}:${minutes.toString().padStart(2, '0')}"
    }

    /**
     * Formats Instant into short date for charts
     *
     * @param instant The instant to format
     * @return The formatted short date (e.g., "01/03")
     */
    fun formatShortDate(instant: Instant): String {
        val (_, month, day) = epochToDate(instant.toEpochMilliseconds())
        return "${(month + 1).toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}"
    }

    /**
     * Converts epoch milliseconds to date components
     * @return Triple of (year, month, day)
     */
    private fun epochToDate(epochMillis: Long): Triple<Int, Int, Int> {
        val seconds = epochMillis / 1000
        var days = seconds / 86400

        var year = 1970
        while (true) {
            val leap = if ((year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)) 1 else 0
            val daysInYear = 365 + leap
            if (days < daysInYear) break
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
        return Triple(year, month, day.toInt())
    }

    /**
     * Converts epoch milliseconds to time components
     * @return Pair of (hours, minutes)
     */
    private fun epochToTime(epochMillis: Long): Pair<Int, Int> {
        val seconds = epochMillis / 1000
        val hours = ((seconds % 86400) / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        return Pair(hours, minutes)
    }

    private val MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
}
