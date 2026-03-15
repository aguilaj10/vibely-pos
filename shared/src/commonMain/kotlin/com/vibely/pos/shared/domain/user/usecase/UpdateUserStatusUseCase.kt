package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class UpdateUserStatusUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(userId: String, status: UserStatus): Result<User> = userRepository.updateStatus(userId, status)
}
