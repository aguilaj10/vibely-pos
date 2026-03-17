package com.vibely.pos.shared.util

internal expect object PlatformNetworkMonitorFactory {
    fun create(): NetworkMonitor
}
