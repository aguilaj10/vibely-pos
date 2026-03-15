package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertIs

class DeleteUserUseCaseTest {

    private class MockUserRepository : UserRepository {
        var deleteResult: Result<Unit>? = null

        override suspend fun getAll(role: UserRole?, status: UserStatus?, page: Int, pageSize: Int): Result<List<User>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<User> = Result.Error("Not implemented")

        override suspend fun getByEmail(email: String): Result<User> = Result.Error("Not implemented")

        override suspend fun create(email: String, password: String, fullName: String, role: UserRole): Result<User> = Result.Error("Not implemented")

        override suspend fun update(user: User): Result<User> = Result.Error("Not implemented")

        override suspend fun updateStatus(id: String, status: UserStatus): Result<User> = Result.Error("Not implemented")

        override suspend fun assignRole(id: String, role: UserRole): Result<User> = Result.Error("Not implemented")

        override suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun resetPassword(id: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = deleteResult ?: Result.Error("Not configured")

        override suspend fun search(query: String): Result<List<User>> = Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when user is deleted`() = runTest {
        val mockRepo = MockUserRepository()
        mockRepo.deleteResult = Result.Success(Unit)

        val useCase = DeleteUserUseCase(mockRepo)

        val result = useCase("user-001")

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `invoke should return error when user not found`() = runTest {
        val mockRepo = MockUserRepository()
        mockRepo.deleteResult = Result.Error("User not found", "NOT_FOUND")

        val useCase = DeleteUserUseCase(mockRepo)

        val result = useCase("user-999")

        assertIs<Result.Error>(result)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
