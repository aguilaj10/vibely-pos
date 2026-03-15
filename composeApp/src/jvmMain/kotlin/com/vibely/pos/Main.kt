package com.vibely.pos

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
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
            state = rememberWindowState(width = 1280.dp, height = 800.dp),
        ) {
            App()
        }
    }
}
