package com.vibely.pos.config

/**
 * Debug configuration for development mode.
 * Provides functionality to bypass authentication and enable development features.
 */
object DebugConfig {
    var isDebugMode: Boolean = false
        private set

    /**
     * Enables debug mode with auto-login and skip authentication.
     * Logs a warning when activated.
     */
    fun enableDebugMode() {
        isDebugMode = true
        println("⚠️ DEBUG MODE ENABLED - Auto-login active, skip authentication")
    }

    /**
     * Checks if the current build is a production build.
     * @return true if production build, false if debug mode is enabled
     */
    fun isProductionBuild(): Boolean = !isDebugMode
}
