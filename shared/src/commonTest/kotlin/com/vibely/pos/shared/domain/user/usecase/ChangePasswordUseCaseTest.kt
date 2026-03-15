package com.vibely.pos.shared.domain.user.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChangePasswordUseCaseTest {

    private class MockUserRepository : UserRepository {
        var changePasswordResult: Result<Unit>? = null

        override suspend fun getAll(role: UserRole?, status: UserStatus?, page: Int, pageSize: Int): Result<List<User>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<User> = Result.Error("Not implemented")

        override suspend fun getByEmail(email: String): Result<User> = Result.Error("Not implemented")

        override suspend fun create(email: String, password: String, fullName: String, role: UserRole): Result<User> = Result.Error("Not implemented")

        override suspend fun update(user: User): Result<User> = Result.Error("Not implemented")

        override suspend fun updateStatus(id: String, status: UserStatus): Result<User> = Result.Error("Not implemented")

        override suspend fun assignRole(id: String, role: UserRole): Result<User> = Result.Error("Not implemented")

        override suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit> =
            changePasswordResult ?: Result.Error("Not configured")

        override suspend fun resetPassword(id: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<User>> = Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when password is changed`() = runTest {
        val mockRepo = MockUserRepository()
        mockRepo.changePasswordResult = Result.Success(Unit)

        val useCase = ChangePasswordUseCase(mockRepo)

        val result = useCase("user-001", "OldPassword123", "NewPassword456")

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `invoke should return error when current password is blank`() = runTest {
        val mockRepo = MockUserRepository()

        val useCase = ChangePasswordUseCase(mockRepo)

        val result = useCase("user-001", "", "NewPassword456")

        assertIs<Result.Error>(result)
        assertEquals("CURRENT_PASSWORD_REQUIRED", result.code)
    }

    @Test
    fun `invoke should return error when new password is too short`() = runTest {
        val mockRepo = MockUserRepository()

        val useCase = ChangePasswordUseCase(mockRepo)

        val result = useCase("user-001", "OldPassword123", "New1")

        assertIs<Result.Error>(result)
        assertEquals("PASSWORD_TOO_SHORT", result.code)
    }

    @Test
    fun `invoke should return error when new password lacks complexity`() = runTest {
        val mockRepo = MockUserRepository()

        val useCase = ChangePasswordUseCase(mockRepo)

        val result = useCase("user-001", "OldPassword123", "newpassword123")

        assertIs<Result.Error>(result)
        assertEquals("PASSWORD_COMPLEXITY", result.code)
    }

    @Test
    fun `invoke should return error when new password same as current`() = runTest {
        val mockRepo = MockUserRepository()

        val useCase = ChangePasswordUseCase(mockRepo)

        val result = useCase("user-001", "SamePassword123", "SamePassword123")

        assertIs<Result.Error>(result)
        assertEquals("SAME_PASSWORD", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
