package com.vibely.pos.shared.data.auth.storage

import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import io.ktor.client.HttpClientConfig

internal expect object PlatformAuthStorageFactory {
    fun createLocalAuthDataSource(): LocalAuthDataSource
}

internal expect fun HttpClientConfig<*>.configurePlatformHttpClient()
