package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class GetAllUsersUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(role: UserRole? = null, status: UserStatus? = null, page: Int = 1, pageSize: Int = 50): Result<List<User>> =
        userRepository.getAll(role, status, page, pageSize)
}
