package com.vibely.pos.shared.data.auth.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClientConfig
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Instant

private const val AUTH_DATASTORE_FILE_NAME = "auth.preferences_pb"

private val accessTokenKey = stringPreferencesKey("access_token")
private val refreshTokenKey = stringPreferencesKey("refresh_token")
private val expiresAtEpochMsKey = longPreferencesKey("expires_at_epoch_ms")

internal actual object PlatformAuthStorageFactory : KoinComponent {
    actual fun createLocalAuthDataSource(): LocalAuthDataSource {
        val context: Context by inject()
        val dataStore =
            PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile(AUTH_DATASTORE_FILE_NAME) },
            )
        return AndroidDataStoreAuthDataSource(dataStore)
    }
}

internal actual fun HttpClientConfig<*>.configurePlatformHttpClient() = Unit

private class AndroidDataStoreAuthDataSource(private val dataStore: DataStore<Preferences>) : LocalAuthDataSource {
    override suspend fun storeToken(token: AuthToken): Result<Unit> = Result.runCatching {
        dataStore.edit { prefs ->
            prefs[accessTokenKey] = token.accessToken
            prefs[refreshTokenKey] = token.refreshToken
            prefs[expiresAtEpochMsKey] = token.expiresAt.toEpochMilliseconds()
        }
    }

    override suspend fun getToken(): Result<AuthToken?> = Result.runCatching {
        val prefs = dataStore.data.first()
        val accessToken = prefs[accessTokenKey] ?: return@runCatching null
        val refreshToken = prefs[refreshTokenKey] ?: return@runCatching null
        val expiresAtEpochMs = prefs[expiresAtEpochMsKey] ?: return@runCatching null
        AuthToken.create(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = Instant.fromEpochMilliseconds(expiresAtEpochMs),
        )
    }

    override suspend fun clearToken(): Result<Unit> = Result.runCatching {
        dataStore.edit { prefs ->
            prefs.remove(accessTokenKey)
            prefs.remove(refreshTokenKey)
            prefs.remove(expiresAtEpochMsKey)
        }
    }
}
