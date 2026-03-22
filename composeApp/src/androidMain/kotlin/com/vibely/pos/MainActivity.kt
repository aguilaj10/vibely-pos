package com.vibely.pos

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vibely.pos.config.DebugConfig
import com.vibely.pos.di.androidModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            DebugConfig.enableDebugMode()
        }

        setContent {
            App(platformModules = listOf(androidModule(applicationContext)))
        }
    }
}
