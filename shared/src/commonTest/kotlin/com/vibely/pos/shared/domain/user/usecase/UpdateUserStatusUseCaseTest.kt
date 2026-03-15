package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository
import com.vibely.pos.shared.domain.valueobject.Email
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

class UpdateUserStatusUseCaseTest {

    private class MockUserRepository : UserRepository {
        var updateStatusResult: Result<User>? = null

        override suspend fun getAll(role: UserRole?, status: UserStatus?, page: Int, pageSize: Int): Result<List<User>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<User> = Result.Error("Not implemented")

        override suspend fun getByEmail(email: String): Result<User> = Result.Error("Not implemented")

        override suspend fun create(email: String, password: String, fullName: String, role: UserRole): Result<User> = Result.Error("Not implemented")

        override suspend fun update(user: User): Result<User> = Result.Error("Not implemented")

        override suspend fun updateStatus(id: String, status: UserStatus): Result<User> = updateStatusResult ?: Result.Error("Not configured")

        override suspend fun assignRole(id: String, role: UserRole): Result<User> = Result.Error("Not implemented")

        override suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun resetPassword(id: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<User>> = Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when user status is updated`() = runTest {
        val mockRepo = MockUserRepository()
        val now = Clock.System.now()
        val email = Email.create("john.doe@example.com")
        val user = User.create(
            id = "user-001",
            email = email,
            fullName = "John Doe",
            role = UserRole.CASHIER,
            status = UserStatus.INACTIVE,
            createdAt = now,
        )
        mockRepo.updateStatusResult = Result.Success(user)

        val useCase = UpdateUserStatusUseCase(mockRepo)

        val result = useCase("user-001", UserStatus.INACTIVE)

        assertIs<Result.Success<User>>(result)
        assertEquals(UserStatus.INACTIVE, result.data.status)
    }

    @Test
    fun `invoke should return error when user not found`() = runTest {
        val mockRepo = MockUserRepository()
        mockRepo.updateStatusResult = Result.Error("User not found", "NOT_FOUND")

        val useCase = UpdateUserStatusUseCase(mockRepo)

        val result = useCase("user-999", UserStatus.INACTIVE)

        assertIs<Result.Error>(result)
        assertEquals("NOT_FOUND", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
