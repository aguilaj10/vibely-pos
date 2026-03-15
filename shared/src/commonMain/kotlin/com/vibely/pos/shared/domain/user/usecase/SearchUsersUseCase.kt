package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class SearchUsersUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(query: String): Result<List<User>> = userRepository.search(query)
}
