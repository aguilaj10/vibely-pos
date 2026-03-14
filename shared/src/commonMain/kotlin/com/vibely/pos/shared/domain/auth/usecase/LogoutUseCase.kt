package com.vibely.pos.shared.domain.auth.usecase

import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.result.Result

/**
 * Use case for logging out the current user.
 *
 * Clears stored authentication tokens and optionally notifies the backend.
 *
 * Business Flow:
 * 1. Clear local authentication token
 * 2. Notify backend to invalidate session (via repository)
 * 3. Return success/failure result
 *
 * @param authRepository The authentication repository.
 */
class LogoutUseCase(private val authRepository: AuthRepository) {
    /**
     * Executes the logout operation.
     *
     * @return [Result.Success] with Unit if logout succeeds,
     *         [Result.Error] if an error occurs.
     *
     * Note: Logout should succeed even if backend notification fails,
     * as long as local token is cleared.
     */
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}
