package com.vibely.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vibely.pos.config.DebugConfig
import com.vibely.pos.shared.data.auth.storage.initAndroidAuthStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAndroidAuthStorage(applicationContext)

        // Check for debug mode from BuildConfig or system properties
        // In debug builds, check for DEBUG_MODE property
        val debugMode = try {
            // Check system property (can be set via adb or local.properties)
            System.getProperty("DEBUG_MODE")?.toBoolean() == true ||
                System.getenv("DEBUG_MODE")?.toBoolean() == true
        } catch (e: Exception) {
            false
        }

        if (debugMode) {
            DebugConfig.enableDebugMode()
        }

        setContent {
            App()
        }
    }
}
