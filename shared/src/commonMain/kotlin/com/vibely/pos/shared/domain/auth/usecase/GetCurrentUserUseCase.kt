package com.vibely.pos.shared.domain.auth.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.result.Result

/**
 * Use case for retrieving the currently authenticated user.
 *
 * Fetches user details from the backend using the stored authentication token.
 * Returns null if no user is currently authenticated.
 *
 * Business Flow:
 * 1. Check if authentication token is stored
 * 2. If token exists, fetch user details from backend
 * 3. Return user or null
 *
 * @param authRepository The authentication repository.
 */
class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    /**
     * Executes the get current user operation.
     *
     * @return [Result.Success] with [User] if authenticated,
     *         [Result.Success] with null if not authenticated,
     *         [Result.Error] if an error occurs fetching user data.
     *
     * Possible error codes:
     * - "INVALID_TOKEN": Access token is invalid or expired
     * - "NETWORK_ERROR": Network request failed
     */
    suspend operator fun invoke(): Result<User?> = authRepository.getCurrentUser()
}
