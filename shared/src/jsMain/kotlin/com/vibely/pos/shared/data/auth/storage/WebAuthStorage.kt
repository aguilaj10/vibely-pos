package com.vibely.pos.shared.data.auth.storage

import com.vibely.pos.shared.data.auth.datasource.InMemoryAuthDataSource
import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.exception.ValidationException
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClientConfig
import io.ktor.client.fetchOptions
import io.ktor.client.request.HttpRequestPipeline
import kotlinx.browser.localStorage
import org.w3c.fetch.INCLUDE
import org.w3c.fetch.RequestCredentials
import kotlin.time.Instant

private const val KEY_ACCESS_TOKEN = "vibely_access_token"
private const val KEY_REFRESH_TOKEN = "vibely_refresh_token"
private const val KEY_EXPIRES_AT = "vibely_expires_at"

/**
 * Returns true if browser localStorage is available and functional.
 *
 * Some environments (Node.js test runners, private browsing with storage disabled,
 * sandboxed iframes) may not expose a usable localStorage. The check is implemented
 * as an inline JavaScript IIFE so the try/catch lives entirely in JS — avoiding
 * Kotlin-level exception handling for a platform API availability probe.
 */
private fun isLocalStorageAvailable(): Boolean = js(
    """(function() {
            try {
                localStorage.setItem('__vibely_ls_test__', '1');
                localStorage.removeItem('__vibely_ls_test__');
                return true;
            } catch(e) {
                return false;
            }
        })()""",
)

/**
 * Browser localStorage implementation of [LocalAuthDataSource] for Kotlin/JS targets.
 *
 * Persists authentication tokens in the browser's localStorage so they survive
 * page reloads within the same origin. Tokens are stored as plain strings keyed
 * by well-known constants and cleared on logout or corruption detection.
 */
internal class LocalStorageAuthDataSource : LocalAuthDataSource {

    override suspend fun storeToken(token: AuthToken): Result<Unit> {
        localStorage.setItem(KEY_ACCESS_TOKEN, token.accessToken)
        localStorage.setItem(KEY_REFRESH_TOKEN, token.refreshToken)
        localStorage.setItem(KEY_EXPIRES_AT, token.expiresAt.toEpochMilliseconds().toString())
        return Result.Success(Unit)
    }

    override suspend fun getToken(): Result<AuthToken?> {
        val accessToken = localStorage.getItem(KEY_ACCESS_TOKEN) ?: return Result.Success(null)
        val refreshToken = localStorage.getItem(KEY_REFRESH_TOKEN) ?: return Result.Success(null)
        val expiresAtMs = localStorage.getItem(KEY_EXPIRES_AT)?.toLongOrNull()
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
        localStorage.removeItem(KEY_ACCESS_TOKEN)
        localStorage.removeItem(KEY_REFRESH_TOKEN)
        localStorage.removeItem(KEY_EXPIRES_AT)
        return Result.Success(Unit)
    }
}

internal actual object PlatformAuthStorageFactory {
    actual fun createLocalAuthDataSource(): LocalAuthDataSource =
        if (isLocalStorageAvailable()) LocalStorageAuthDataSource() else InMemoryAuthDataSource()
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
