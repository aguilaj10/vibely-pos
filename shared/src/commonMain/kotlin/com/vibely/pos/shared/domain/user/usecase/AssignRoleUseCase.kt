package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class AssignRoleUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(userId: String, role: UserRole): Result<User> = userRepository.assignRole(userId, role)
}
