package com.vibely.pos.shared.util

import kotlinx.coroutines.flow.StateFlow

/**
 * Network connectivity monitor that tracks whether the device has an active internet connection.
 *
 * This class uses platform-specific APIs to detect connectivity changes and exposes
 * the current state via a StateFlow. Use [isOnline] to observe connectivity state.
 *
 * ## Usage
 *
 * ```kotlin
 * val networkMonitor: NetworkMonitor = koinInject()
 *
 * // Observe connectivity state
 * networkMonitor.isOnline.collect { isOnline ->
 *     if (!isOnline) {
 *         showOfflineBanner()
 *     }
 * }
 * ```
 */
expect class NetworkMonitor() {

    /**
     * A Flow that emits the current network connectivity state.
     *
     * - `true` when the device has an active internet connection
     * - `false` when the device is offline
     */
    val isOnline: StateFlow<Boolean>

    /**
     * Starts monitoring network connectivity.
     *
     * Call this method when you want to begin listening for connectivity changes.
     * The [isOnline] Flow will start emitting values after this is called.
     */
    fun startMonitoring()

    /**
     * Stops monitoring network connectivity.
     *
     * Call this method to release resources when connectivity monitoring
     * is no longer needed (e.g., when the app is being destroyed).
     */
    fun stopMonitoring()
}
