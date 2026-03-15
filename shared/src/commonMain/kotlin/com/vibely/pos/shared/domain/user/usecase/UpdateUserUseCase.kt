package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class UpdateUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(user: User): Result<User> {
        if (user.fullName.isBlank()) {
            return Result.Error(
                message = "Full name is required",
                code = "INVALID_NAME",
            )
        }

        return userRepository.update(user)
    }
}
