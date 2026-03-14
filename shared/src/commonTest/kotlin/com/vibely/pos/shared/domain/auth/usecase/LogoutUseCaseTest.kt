package com.vibely.pos.shared.domain.auth.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.auth.valueobject.Credentials
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LogoutUseCaseTest {

    // Mock repository for testing
    private class MockAuthRepository : AuthRepository {
        var logoutResult: Result<Unit> = Result.Success(Unit)
        var logoutCalled = false

        override suspend fun login(credentials: Credentials): Result<AuthToken> = Result.Error("Not implemented")

        override suspend fun logout(): Result<Unit> {
            logoutCalled = true
            return logoutResult
        }

        override suspend fun getCurrentUser(): Result<User?> = Result.Success(null)

        override suspend fun refreshToken(): Result<AuthToken> = Result.Error("Not implemented")

        override suspend fun getStoredToken(): Result<AuthToken?> = Result.Success(null)

        override suspend fun storeToken(token: AuthToken): Result<Unit> = Result.Success(Unit)

        override suspend fun clearStoredToken(): Result<Unit> = Result.Success(Unit)
    }

    @Test
    fun `invoke should call repository logout`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        val useCase = LogoutUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertTrue(mockRepo.logoutCalled)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `invoke should return success when logout succeeds`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        mockRepo.logoutResult = Result.Success(Unit)

        val useCase = LogoutUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `invoke should return error when logout fails`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        mockRepo.logoutResult = Result.Error(
            message = "Network error",
            code = "NETWORK_ERROR",
        )

        val useCase = LogoutUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Network error", result.message)
        assertEquals("NETWORK_ERROR", result.code)
    }

    // Helper function for running suspending tests
    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
