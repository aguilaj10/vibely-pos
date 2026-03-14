package com.vibely.pos.shared.data.auth.datasource

import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.result.Result

/**
 * In-memory implementation of [LocalAuthDataSource].
 *
 * Stores authentication tokens in memory (lost on app restart).
 * Suitable for development and testing. Production apps should use
 * encrypted storage or secure preferences.
 */
class InMemoryAuthDataSource : LocalAuthDataSource {

    private var storedToken: AuthToken? = null

    override suspend fun storeToken(token: AuthToken): Result<Unit> {
        storedToken = token
        return Result.Success(Unit)
    }

    override suspend fun getToken(): Result<AuthToken?> = Result.Success(storedToken)

    override suspend fun clearToken(): Result<Unit> {
        storedToken = null
        return Result.Success(Unit)
    }
}
