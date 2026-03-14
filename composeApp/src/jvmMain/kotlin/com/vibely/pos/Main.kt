package com.vibely.pos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.vibely.pos.config.DebugConfig

fun main(args: Array<String>) {
    // Check for debug flags
    if (args.contains("--skip-auth") || args.contains("-d")) {
        DebugConfig.enableDebugMode()
    }

    // Check environment variable
    val debugEnv = System.getenv("DEBUG_MODE")
    if (debugEnv?.toBoolean() == true) {
        DebugConfig.enableDebugMode()
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Vibely POS",
        ) {
            App()
        }
    }
}
