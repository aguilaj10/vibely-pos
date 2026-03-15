package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class CreateUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(email: String, password: String, fullName: String, role: UserRole): Result<User> {
        val validationError = validateInput(email, password, fullName)
        if (validationError != null) {
            return validationError
        }

        return userRepository.create(email, password, fullName, role)
    }

    private fun validateInput(email: String, password: String, fullName: String): Result.Error? {
        if (email.isBlank()) {
            return Result.Error(
                message = "Email is required",
                code = "INVALID_EMAIL",
            )
        }

        if (!isValidEmail(email)) {
            return Result.Error(
                message = "Invalid email format",
                code = "INVALID_EMAIL_FORMAT",
            )
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            return Result.Error(
                message = "Password must be at least $MIN_PASSWORD_LENGTH characters",
                code = "PASSWORD_TOO_SHORT",
            )
        }

        if (!hasRequiredPasswordComplexity(password)) {
            return Result.Error(
                message = "Password must contain uppercase, lowercase, and a number",
                code = "PASSWORD_COMPLEXITY",
            )
        }

        if (fullName.isBlank()) {
            return Result.Error(
                message = "Full name is required",
                code = "INVALID_NAME",
            )
        }

        if (fullName.length < MIN_NAME_LENGTH || fullName.length > MAX_NAME_LENGTH) {
            return Result.Error(
                message = "Full name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters",
                code = "INVALID_NAME_LENGTH",
            )
        }

        return null
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    private fun hasRequiredPasswordComplexity(password: String): Boolean {
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUppercase && hasLowercase && hasDigit
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MIN_NAME_LENGTH = 2
        private const val MAX_NAME_LENGTH = 100
    }
}
