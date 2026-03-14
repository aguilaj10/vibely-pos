package com.vibely.pos.shared.domain.auth.repository

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.auth.valueobject.Credentials
import com.vibely.pos.shared.domain.result.Result

/**
 * Repository interface for authentication operations.
 *
 * Defines the contract for authenticating users, managing sessions,
 * and retrieving authentication state. Implementations handle the
 * communication with backend services and local storage.
 */
interface AuthRepository {

    /**
     * Authenticates a user with the given credentials.
     *
     * Makes a network call to the backend authentication endpoint,
     * validates credentials, and returns authentication tokens.
     *
     * @param credentials The user's email and password.
     * @return [Result.Success] with [AuthToken] if authentication succeeds,
     *         [Result.Error] with appropriate message if it fails.
     *
     * Possible error codes:
     * - "INVALID_CREDENTIALS": Email or password is incorrect
     * - "USER_SUSPENDED": User account is suspended
     * - "USER_INACTIVE": User account is inactive
     * - "NETWORK_ERROR": Network request failed
     */
    suspend fun login(credentials: Credentials): Result<AuthToken>

    /**
     * Logs out the current user.
     *
     * Clears stored authentication tokens from local storage.
     * Optionally notifies the backend to invalidate the tokens.
     *
     * @return [Result.Success] with Unit if logout succeeds,
     *         [Result.Error] if an error occurs (e.g., network failure).
     */
    suspend fun logout(): Result<Unit>

    /**
     * Retrieves the currently authenticated user.
     *
     * Fetches user details from the backend using the stored access token.
     * Returns null if no user is authenticated (no valid token stored).
     *
     * @return [Result.Success] with [User] if authenticated,
     *         [Result.Success] with null if not authenticated,
     *         [Result.Error] if an error occurs fetching user data.
     *
     * Possible error codes:
     * - "INVALID_TOKEN": Access token is invalid or expired
     * - "NETWORK_ERROR": Network request failed
     */
    suspend fun getCurrentUser(): Result<User?>

    /**
     * Refreshes the access token using the refresh token.
     *
     * Exchanges the stored refresh token for a new access token.
     * Updates the stored tokens with the new values.
     *
     * @return [Result.Success] with new [AuthToken] if refresh succeeds,
     *         [Result.Error] if refresh fails.
     *
     * Possible error codes:
     * - "INVALID_REFRESH_TOKEN": Refresh token is invalid or expired
     * - "NO_REFRESH_TOKEN": No refresh token is stored
     * - "NETWORK_ERROR": Network request failed
     */
    suspend fun refreshToken(): Result<AuthToken>

    /**
     * Retrieves the stored authentication token.
     *
     * Returns the token from local storage, or null if not authenticated.
     *
     * @return [Result.Success] with [AuthToken] if stored,
     *         [Result.Success] with null if no token stored,
     *         [Result.Error] if an error occurs reading storage.
     */
    suspend fun getStoredToken(): Result<AuthToken?>

    /**
     * Stores the authentication token locally.
     *
     * Persists the token for subsequent requests and session management.
     *
     * @param token The authentication token to store.
     * @return [Result.Success] with Unit if storage succeeds,
     *         [Result.Error] if storage fails.
     */
    suspend fun storeToken(token: AuthToken): Result<Unit>

    /**
     * Clears the stored authentication token.
     *
     * Removes tokens from local storage (used during logout).
     *
     * @return [Result.Success] with Unit if cleared successfully,
     *         [Result.Error] if an error occurs.
     */
    suspend fun clearStoredToken(): Result<Unit>
}
