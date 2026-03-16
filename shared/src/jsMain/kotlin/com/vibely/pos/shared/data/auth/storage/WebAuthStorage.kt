package com.vibely.pos.shared.data.auth.storage

import com.vibely.pos.shared.data.auth.datasource.InMemoryAuthDataSource
import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import io.ktor.client.HttpClientConfig

internal actual object PlatformAuthStorageFactory {
    actual fun createLocalAuthDataSource(): LocalAuthDataSource = InMemoryAuthDataSource()
}

internal actual fun HttpClientConfig<*>.configurePlatformHttpClient() {
    // Skip platform-specific configuration in test environments
    // Browser Fetch API may not be fully available in headless test runners
}
