package com.vibely.pos.shared.util

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal actual object PlatformNetworkMonitorFactory : KoinComponent {
    actual fun create(): NetworkMonitor {
        val context: Context by inject()
        return NetworkMonitor(context)
    }
}
