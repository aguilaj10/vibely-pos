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

class LoginUseCaseTest {

    // Mock repository for testing
    private class MockAuthRepository : AuthRepository {
        var loginResult: Result<AuthToken>? = null
        var storeTokenResult: Result<Unit> = Result.Success(Unit)
        var lastStoredToken: AuthToken? = null

        override suspend fun login(credentials: Credentials): Result<AuthToken> = loginResult ?: Result.Error("Not configured")

        override suspend fun logout(): Result<Unit> = Result.Success(Unit)

        override suspend fun getCurrentUser(): Result<User?> = Result.Success(null)

        override suspend fun refreshToken(): Result<AuthToken> = Result.Error("Not implemented")

        override suspend fun getStoredToken(): Result<AuthToken?> = Result.Success(null)

        override suspend fun storeToken(token: AuthToken): Result<Unit> {
            lastStoredToken = token
            return storeTokenResult
        }

        override suspend fun clearStoredToken(): Result<Unit> = Result.Success(Unit)
    }

    @Test
    fun `invoke should return success when login succeeds`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        val expectedToken = AuthToken.create(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresInSeconds = 3600,
        )
        mockRepo.loginResult = Result.Success(expectedToken)

        val useCase = LoginUseCase(mockRepo)
        val credentials = Credentials.create("test@example.com", "Password123!")

        // When
        val result = useCase(credentials)

        // Then
        assertIs<Result.Success<AuthToken>>(result)
        assertEquals(expectedToken.accessToken, result.data.accessToken)
        assertEquals(expectedToken.refreshToken, result.data.refreshToken)
        assertEquals(expectedToken, mockRepo.lastStoredToken)
    }

    @Test
    fun `invoke should return error when login fails`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        mockRepo.loginResult = Result.Error(
            message = "Invalid credentials",
            code = "INVALID_CREDENTIALS",
        )

        val useCase = LoginUseCase(mockRepo)
        val credentials = Credentials.create("test@example.com", "Password123!")

        // When
        val result = useCase(credentials)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Invalid credentials", result.message)
        assertEquals("INVALID_CREDENTIALS", result.code)
    }

    @Test
    fun `invoke should store token after successful login`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        val token = AuthToken.create(
            accessToken = "access",
            refreshToken = "refresh",
            expiresInSeconds = 3600,
        )
        mockRepo.loginResult = Result.Success(token)

        val useCase = LoginUseCase(mockRepo)
        val credentials = Credentials.create("user@test.com", "ValidPass1!")

        // When
        val result = useCase(credentials)

        // Then
        assertIs<Result.Success<AuthToken>>(result)
        assertEquals(token, mockRepo.lastStoredToken)
    }

    @Test
    fun `invoke should return error if token storage fails`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        val token = AuthToken.create(
            accessToken = "access",
            refreshToken = "refresh",
            expiresInSeconds = 3600,
        )
        mockRepo.loginResult = Result.Success(token)
        mockRepo.storeTokenResult = Result.Error("Storage failed")

        val useCase = LoginUseCase(mockRepo)
        val credentials = Credentials.create("user@test.com", "ValidPass1!")

        // When
        val result = useCase(credentials)

        // Then
        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Storage failed"))
    }

    // Helper function for running suspending tests
    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
