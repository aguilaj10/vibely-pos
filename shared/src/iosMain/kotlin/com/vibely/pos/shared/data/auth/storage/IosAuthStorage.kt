package com.vibely.pos.shared.data.auth.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClientConfig
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.first
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import kotlin.time.Instant

private const val AUTH_DATASTORE_FILE_NAME = "auth.preferences_pb"

private val accessTokenKey = stringPreferencesKey("access_token")
private val refreshTokenKey = stringPreferencesKey("refresh_token")
private val expiresAtEpochMsKey = longPreferencesKey("expires_at_epoch_ms")

internal actual object PlatformAuthStorageFactory {
    actual fun createLocalAuthDataSource(): LocalAuthDataSource {
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { dataStorePath() },
        )
        return IosDataStoreAuthDataSource(dataStore)
    }
}

internal actual fun HttpClientConfig<*>.configurePlatformHttpClient() = Unit

@OptIn(ExperimentalForeignApi::class)
private fun dataStorePath(): Path {
    val fileManager = NSFileManager.defaultManager
    val documentsUrl =
        requireNotNull(
            fileManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            ),
        )
    val appDirUrl = documentsUrl.URLByAppendingPathComponent(pathComponent = "vibely-pos")
    val basePath = requireNotNull(appDirUrl?.path)
    val directoryPath = basePath.toPath()
    FileSystem.SYSTEM.createDirectories(directoryPath)
    return ("$basePath/$AUTH_DATASTORE_FILE_NAME").toPath()
}

private class IosDataStoreAuthDataSource(private val dataStore: DataStore<Preferences>) : LocalAuthDataSource {
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
