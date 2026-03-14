package com.vibely.pos.shared.util

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Utility object for time-related operations.
 *
 * Provides a centralized way to access the current time, making it easier to:
 * - Mock time in tests
 * - Handle time zone conversions
 * - Ensure consistent time usage across the application
 */
object TimeUtil {

    /**
     * Returns the current system time as an [Instant].
     *
     * Uses [Clock.System.now()] which provides the current time from the system clock.
     * This is the recommended way to get the current time in Kotlin Multiplatform.
     *
     * @return The current time as an [Instant].
     */
    fun now(): Instant = Clock.System.now()
}
