package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class ChangePasswordUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(userId: String, currentPassword: String, newPassword: String): Result<Unit> {
        if (currentPassword.isBlank()) {
            return Result.Error(
                message = "Current password is required",
                code = "CURRENT_PASSWORD_REQUIRED",
            )
        }

        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return Result.Error(
                message = "New password must be at least $MIN_PASSWORD_LENGTH characters",
                code = "PASSWORD_TOO_SHORT",
            )
        }

        if (!hasRequiredPasswordComplexity(newPassword)) {
            return Result.Error(
                message = "Password must contain uppercase, lowercase, and a number",
                code = "PASSWORD_COMPLEXITY",
            )
        }

        if (currentPassword == newPassword) {
            return Result.Error(
                message = "New password must be different from current password",
                code = "SAME_PASSWORD",
            )
        }

        return userRepository.changePassword(userId, currentPassword, newPassword)
    }

    private fun hasRequiredPasswordComplexity(password: String): Boolean {
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUppercase && hasLowercase && hasDigit
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }
}
