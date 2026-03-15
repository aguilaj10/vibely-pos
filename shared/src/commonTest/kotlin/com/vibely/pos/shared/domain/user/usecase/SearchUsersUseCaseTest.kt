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
import kotlin.test.assertTrue
import kotlin.time.Clock

class SearchUsersUseCaseTest {

    private class MockUserRepository : UserRepository {
        var searchResult: Result<List<User>>? = null
        var lastSearchQuery: String? = null

        override suspend fun search(query: String): Result<List<User>> {
            lastSearchQuery = query
            return searchResult ?: Result.Success(emptyList())
        }

        override suspend fun getAll(role: UserRole?, status: UserStatus?, page: Int, pageSize: Int): Result<List<User>> = Result.Success(emptyList())
        override suspend fun getById(id: String): Result<User> = Result.Error("Not implemented")
        override suspend fun getByEmail(email: String): Result<User> = Result.Error("Not implemented")
        override suspend fun create(email: String, password: String, fullName: String, role: UserRole): Result<User> = Result.Error("Not implemented")
        override suspend fun update(user: User): Result<User> = Result.Error("Not implemented")
        override suspend fun updateStatus(id: String, status: UserStatus): Result<User> = Result.Error("Not implemented")
        override suspend fun assignRole(id: String, role: UserRole): Result<User> = Result.Error("Not implemented")
        override suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")
        override suspend fun resetPassword(id: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")
        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")
    }

    private fun createTestUser(
        id: String,
        email: String,
        fullName: String,
        role: UserRole = UserRole.CASHIER,
        status: UserStatus = UserStatus.ACTIVE,
    ): User {
        val now = Clock.System.now()
        return User.create(
            id = id,
            email = Email.create(email),
            fullName = fullName,
            role = role,
            status = status,
            createdAt = now,
        )
    }

    @Test
    fun `invoke should return matching users`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        val users = listOf(
            createTestUser("user-001", "john@test.com", "John Doe"),
        )
        mockRepo.searchResult = Result.Success(users)

        val useCase = SearchUsersUseCase(mockRepo)

        // When
        val result = useCase("john")

        // Then
        assertIs<Result.Success<List<User>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("John Doe", result.data[0].fullName)
    }

    @Test
    fun `invoke should pass query to repository`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.searchResult = Result.Success(emptyList())

        val useCase = SearchUsersUseCase(mockRepo)

        // When
        useCase("admin@company.com")

        // Then
        assertEquals("admin@company.com", mockRepo.lastSearchQuery)
    }

    @Test
    fun `invoke should return empty list when no matches found`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.searchResult = Result.Success(emptyList())

        val useCase = SearchUsersUseCase(mockRepo)

        // When
        val result = useCase("nonexistent")

        // Then
        assertIs<Result.Success<List<User>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should return error when search fails`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.searchResult = Result.Error(
            message = "Search index unavailable",
            code = "SEARCH_ERROR",
        )

        val useCase = SearchUsersUseCase(mockRepo)

        // When
        val result = useCase("test")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Search index unavailable", result.message)
        assertEquals("SEARCH_ERROR", result.code)
    }

    @Test
    fun `invoke should return multiple matching users`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        val users = listOf(
            createTestUser("user-001", "john.smith@test.com", "John Smith"),
            createTestUser("user-002", "johnny@test.com", "Johnny Bravo"),
            createTestUser("user-003", "john.doe@test.com", "John Doe"),
        )
        mockRepo.searchResult = Result.Success(users)

        val useCase = SearchUsersUseCase(mockRepo)

        // When
        val result = useCase("john")

        // Then
        assertIs<Result.Success<List<User>>>(result)
        assertEquals(3, result.data.size)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
