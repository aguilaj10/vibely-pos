package com.vibely.pos.shared.domain.auth.usecase

import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap

/**
 * Use case for refreshing an expired or expiring access token.
 *
 * Uses the stored refresh token to obtain a new access token from the backend.
 * Updates the stored tokens with the new values.
 *
 * Business Flow:
 * 1. Retrieve stored refresh token
 * 2. Exchange refresh token for new access token via backend
 * 3. Store new tokens locally
 * 4. Return new authentication token
 *
 * @param authRepository The authentication repository.
 */
class RefreshTokenUseCase(private val authRepository: AuthRepository) {
    /**
     * Executes the token refresh operation.
     *
     * @return [Result.Success] with new [AuthToken] if refresh succeeds,
     *         [Result.Error] if refresh fails.
     *
     * Possible error codes:
     * - "INVALID_REFRESH_TOKEN": Refresh token is invalid or expired
     * - "NO_REFRESH_TOKEN": No refresh token is stored
     * - "NETWORK_ERROR": Network request failed
     */
    suspend operator fun invoke(): Result<AuthToken> = authRepository.refreshToken()
        .flatMap { token ->
            // Store the new token after successful refresh
            authRepository.storeToken(token)
                .flatMap { Result.Success(token) }
        }
}
