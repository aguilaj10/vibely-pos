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

class UpdateUserUseCaseTest {

    private class MockUserRepository : UserRepository {
        var updateResult: Result<User>? = null

        override suspend fun getAll(role: UserRole?, status: UserStatus?, page: Int, pageSize: Int): Result<List<User>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<User> = Result.Error("Not implemented")

        override suspend fun getByEmail(email: String): Result<User> = Result.Error("Not implemented")

        override suspend fun create(email: String, password: String, fullName: String, role: UserRole): Result<User> = Result.Error("Not implemented")

        override suspend fun update(user: User): Result<User> = updateResult ?: Result.Error("Not configured")

        override suspend fun updateStatus(id: String, status: UserStatus): Result<User> = Result.Error("Not implemented")

        override suspend fun assignRole(id: String, role: UserRole): Result<User> = Result.Error("Not implemented")

        override suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun resetPassword(id: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<User>> = Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when user is updated`() = runTest {
        val mockRepo = MockUserRepository()
        val now = Clock.System.now()
        val email = Email.create("john.doe@example.com")
        val user = User.create(
            id = "user-001",
            email = email,
            fullName = "John Doe Updated",
            role = UserRole.CASHIER,
            status = UserStatus.ACTIVE,
            createdAt = now,
        )
        mockRepo.updateResult = Result.Success(user)

        val useCase = UpdateUserUseCase(mockRepo)

        val result = useCase(user)

        assertIs<Result.Success<User>>(result)
        assertEquals("John Doe Updated", result.data.fullName)
    }

    @Test
    fun `invoke should return error when full name is blank`() = runTest {
        val mockRepo = MockUserRepository()
        val now = Clock.System.now()
        val email = Email.create("john.doe@example.com")
        val user = User(
            id = "user-001",
            email = email,
            fullName = "",
            role = UserRole.CASHIER,
            status = UserStatus.ACTIVE,
            createdAt = now,
        )

        val useCase = UpdateUserUseCase(mockRepo)

        val result = useCase(user)

        assertIs<Result.Error>(result)
        assertEquals("INVALID_NAME", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
