package com.vibely.pos.shared.domain.auth.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.auth.valueobject.Credentials
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.valueobject.Email
import com.vibely.pos.shared.util.TimeUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GetCurrentUserUseCaseTest {

    // Mock repository for testing
    private class MockAuthRepository : AuthRepository {
        var getCurrentUserResult: Result<User?>? = null

        override suspend fun login(credentials: Credentials): Result<AuthToken> = Result.Error("Not implemented")

        override suspend fun logout(): Result<Unit> = Result.Success(Unit)

        override suspend fun getCurrentUser(): Result<User?> = getCurrentUserResult ?: Result.Success(null)

        override suspend fun refreshToken(): Result<AuthToken> = Result.Error("Not implemented")

        override suspend fun getStoredToken(): Result<AuthToken?> = Result.Success(null)

        override suspend fun storeToken(token: AuthToken): Result<Unit> = Result.Success(Unit)

        override suspend fun clearStoredToken(): Result<Unit> = Result.Success(Unit)
    }

    @Test
    fun `invoke should return user when authenticated`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        val expectedUser = User.create(
            id = "user-123",
            email = Email.create("test@example.com"),
            fullName = "Test User",
            role = UserRole.ADMIN,
            status = UserStatus.ACTIVE,
            createdAt = TimeUtil.now(),
        )
        mockRepo.getCurrentUserResult = Result.Success(expectedUser)

        val useCase = GetCurrentUserUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<User?>>(result)
        assertEquals(expectedUser, result.data)
    }

    @Test
    fun `invoke should return null when not authenticated`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        mockRepo.getCurrentUserResult = Result.Success(null)

        val useCase = GetCurrentUserUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<User?>>(result)
        assertNull(result.data)
    }

    @Test
    fun `invoke should return error when fetch fails`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        mockRepo.getCurrentUserResult = Result.Error(
            message = "Invalid token",
            code = "INVALID_TOKEN",
        )

        val useCase = GetCurrentUserUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Invalid token", result.message)
        assertEquals("INVALID_TOKEN", result.code)
    }

    // Helper function for running suspending tests
    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
