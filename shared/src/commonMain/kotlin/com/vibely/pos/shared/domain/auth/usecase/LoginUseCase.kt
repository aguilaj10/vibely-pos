package com.vibely.pos.shared.domain.auth.usecase

import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.auth.valueobject.Credentials
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap

/**
 * Use case for authenticating a user with email and password.
 *
 * Validates credentials and delegates to the repository for authentication.
 * Stores the returned authentication token for subsequent requests.
 *
 * Business Flow:
 * 1. Validate credentials format
 * 2. Authenticate with backend via repository
 * 3. Store authentication token locally
 * 4. Return success/failure result
 *
 * @param authRepository The authentication repository.
 */
class LoginUseCase(private val authRepository: AuthRepository) {
    /**
     * Executes the login operation.
     *
     * @param credentials The user's email and password.
     * @return [Result.Success] with [AuthToken] if login succeeds,
     *         [Result.Error] if login fails.
     *
     * Possible error codes:
     * - "INVALID_CREDENTIALS": Email or password is incorrect
     * - "USER_SUSPENDED": User account is suspended
     * - "USER_INACTIVE": User account is inactive
     * - "VALIDATION_ERROR": Credentials format is invalid
     * - "NETWORK_ERROR": Network request failed
     */
    suspend operator fun invoke(credentials: Credentials): Result<AuthToken> = authRepository.login(credentials)
        .flatMap { token ->
            // Store the token after successful login
            authRepository.storeToken(token)
                .flatMap { Result.Success(token) }
        }
}
