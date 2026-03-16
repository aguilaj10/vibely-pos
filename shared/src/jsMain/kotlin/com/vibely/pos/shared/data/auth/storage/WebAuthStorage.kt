package com.vibely.pos.shared.data.auth.storage

import com.vibely.pos.shared.data.auth.datasource.InMemoryAuthDataSource
import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import io.ktor.client.HttpClientConfig
import io.ktor.client.fetchOptions
import io.ktor.client.request.HttpRequestPipeline
import org.w3c.fetch.INCLUDE
import org.w3c.fetch.RequestCredentials

internal actual object PlatformAuthStorageFactory {
    actual fun createLocalAuthDataSource(): LocalAuthDataSource = InMemoryAuthDataSource()
}

internal actual fun HttpClientConfig<*>.configurePlatformHttpClient() {
    install("JsFetchOptions") {
        requestPipeline.intercept(HttpRequestPipeline.State) {
            // Aquí 'context' es de tipo HttpRequestBuilder
            context.fetchOptions {
                credentials = RequestCredentials.INCLUDE
            }
        }
    }
}
