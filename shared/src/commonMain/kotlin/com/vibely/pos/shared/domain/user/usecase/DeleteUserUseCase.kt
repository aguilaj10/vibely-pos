package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class DeleteUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(userId: String): Result<Unit> = userRepository.delete(userId)
}
