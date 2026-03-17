package com.vibely.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vibely.pos.config.DebugConfig
import com.vibely.pos.di.androidModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val debugMode = try {
            System.getProperty("DEBUG_MODE")?.toBoolean() == true ||
                System.getenv("DEBUG_MODE")?.toBoolean() == true
        } catch (e: Exception) {
            false
        }

        if (debugMode) {
            DebugConfig.enableDebugMode()
        }

        setContent {
            App(platformModules = listOf(androidModule(applicationContext)))
        }
    }
}
