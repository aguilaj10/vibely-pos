@file:OptIn(ExperimentalWasmJsInterop::class)

package com.vibely.pos.shared.data.auth.storage

import com.vibely.pos.shared.data.auth.datasource.InMemoryAuthDataSource
import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.exception.ValidationException
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClientConfig
import io.ktor.client.fetchOptions
import io.ktor.client.request.HttpRequestPipeline
import org.w3c.fetch.INCLUDE
import org.w3c.fetch.RequestCredentials
import kotlin.time.Instant

private const val KEY_ACCESS_TOKEN = "vibely_access_token"
private const val KEY_REFRESH_TOKEN = "vibely_refresh_token"
private const val KEY_EXPIRES_AT = "vibely_expires_at"

@JsFun("(key) => localStorage.getItem(key)")
private external fun jsLocalStorageGet(key: String): JsString?

@JsFun("(key, value) => localStorage.setItem(key, value)")
private external fun jsLocalStorageSet(key: String, value: String)

@JsFun("(key) => localStorage.removeItem(key)")
private external fun jsLocalStorageRemove(key: String)

@JsFun(
    "() => { try { localStorage.setItem('__vibely_ls_test__', '1'); localStorage.removeItem('__vibely_ls_test__'); return 1; } catch(e) { return 0; } }",
)
private external fun jsIsLocalStorageAvailable(): Int

/**
 * Returns true if browser localStorage is available and functional.
 *
 * Some environments (Node.js test runners, headless WASM runtimes) may not expose
 * a usable localStorage. A lightweight write/remove probe is used to verify
 * availability before committing to this storage backend.
 */
private fun isLocalStorageAvailable(): Boolean = jsIsLocalStorageAvailable() != 0

/**
 * Browser localStorage implementation of [LocalAuthDataSource] for Kotlin/WasmJs targets.
 *
 * Persists authentication tokens in the browser's localStorage via JS interop so
 * they survive page reloads within the same origin. Tokens are stored as plain
 * strings keyed by well-known constants and cleared on logout or corruption detection.
 */
internal class WasmLocalStorageAuthDataSource : LocalAuthDataSource {

    override suspend fun storeToken(token: AuthToken): Result<Unit> {
        jsLocalStorageSet(KEY_ACCESS_TOKEN, token.accessToken)
        jsLocalStorageSet(KEY_REFRESH_TOKEN, token.refreshToken)
        jsLocalStorageSet(KEY_EXPIRES_AT, token.expiresAt.toEpochMilliseconds().toString())
        return Result.Success(Unit)
    }

    override suspend fun getToken(): Result<AuthToken?> {
        val accessToken = jsLocalStorageGet(KEY_ACCESS_TOKEN)?.toString()
            ?: return Result.Success(null)
        val refreshToken = jsLocalStorageGet(KEY_REFRESH_TOKEN)?.toString()
            ?: return Result.Success(null)
        val expiresAtMs = jsLocalStorageGet(KEY_EXPIRES_AT)?.toString()?.toLongOrNull()
            ?: return Result.Success(null)

        return try {
            val expiresAt = Instant.fromEpochMilliseconds(expiresAtMs)
            Result.Success(AuthToken.create(accessToken, refreshToken, expiresAt))
        } catch (e: ValidationException) {
            // Stored data is corrupted — clear it and treat as unauthenticated
            clearToken()
            Result.Success(null)
        }
    }

    override suspend fun clearToken(): Result<Unit> {
        jsLocalStorageRemove(KEY_ACCESS_TOKEN)
        jsLocalStorageRemove(KEY_REFRESH_TOKEN)
        jsLocalStorageRemove(KEY_EXPIRES_AT)
        return Result.Success(Unit)
    }
}

internal actual object PlatformAuthStorageFactory {
    actual fun createLocalAuthDataSource(): LocalAuthDataSource =
        if (isLocalStorageAvailable()) WasmLocalStorageAuthDataSource() else InMemoryAuthDataSource()
}

internal actual fun HttpClientConfig<*>.configurePlatformHttpClient() {
    install("JsFetchOptions") {
        requestPipeline.intercept(HttpRequestPipeline.State) {
            context.fetchOptions {
                credentials = RequestCredentials.INCLUDE
            }
        }
    }
}
