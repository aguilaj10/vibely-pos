package com.vibely.pos.shared.data.auth.datasource

import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.result.Result

/**
 * Interface for local authentication data source.
 *
 * Handles local storage and retrieval of authentication tokens.
 * Implementations may use in-memory storage, encrypted preferences, or secure storage.
 */
interface LocalAuthDataSource {

    /**
     * Stores an authentication token locally.
     *
     * @param token The token to store.
     * @return [Result.Success] if storage succeeds, [Result.Error] otherwise.
     */
    suspend fun storeToken(token: AuthToken): Result<Unit>

    /**
     * Retrieves the stored authentication token.
     *
     * @return [Result.Success] with the token if stored,
     *         [Result.Success] with null if no token is stored,
     *         [Result.Error] if an error occurs.
     */
    suspend fun getToken(): Result<AuthToken?>

    /**
     * Clears the stored authentication token.
     *
     * @return [Result.Success] if cleared successfully, [Result.Error] otherwise.
     */
    suspend fun clearToken(): Result<Unit>
}
