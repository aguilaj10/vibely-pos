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

class GetAllUsersUseCaseTest {

    private class MockUserRepository : UserRepository {
        var getAllResult: Result<List<User>>? = null
        var lastRole: UserRole? = null
        var lastStatus: UserStatus? = null
        var lastPage: Int? = null
        var lastPageSize: Int? = null

        override suspend fun getAll(role: UserRole?, status: UserStatus?, page: Int, pageSize: Int): Result<List<User>> {
            lastRole = role
            lastStatus = status
            lastPage = page
            lastPageSize = pageSize
            return getAllResult ?: Result.Success(emptyList())
        }

        override suspend fun getById(id: String): Result<User> = Result.Error("Not implemented")
        override suspend fun getByEmail(email: String): Result<User> = Result.Error("Not implemented")
        override suspend fun create(email: String, password: String, fullName: String, role: UserRole): Result<User> = Result.Error("Not implemented")
        override suspend fun update(user: User): Result<User> = Result.Error("Not implemented")
        override suspend fun updateStatus(id: String, status: UserStatus): Result<User> = Result.Error("Not implemented")
        override suspend fun assignRole(id: String, role: UserRole): Result<User> = Result.Error("Not implemented")
        override suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")
        override suspend fun resetPassword(id: String, newPassword: String): Result<Unit> = Result.Error("Not implemented")
        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")
        override suspend fun search(query: String): Result<List<User>> = Result.Success(emptyList())
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
    fun `invoke should return all users when no filters provided`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        val users = listOf(
            createTestUser("user-001", "john@test.com", "John Doe"),
            createTestUser("user-002", "jane@test.com", "Jane Smith"),
        )
        mockRepo.getAllResult = Result.Success(users)

        val useCase = GetAllUsersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<User>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("John Doe", result.data[0].fullName)
        assertEquals("Jane Smith", result.data[1].fullName)
    }

    @Test
    fun `invoke should filter by role when role provided`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        val adminUsers = listOf(
            createTestUser("user-001", "admin@test.com", "Admin User", role = UserRole.ADMIN),
        )
        mockRepo.getAllResult = Result.Success(adminUsers)

        val useCase = GetAllUsersUseCase(mockRepo)

        // When
        useCase(role = UserRole.ADMIN)

        // Then
        assertEquals(UserRole.ADMIN, mockRepo.lastRole)
    }

    @Test
    fun `invoke should filter by status when status provided`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllUsersUseCase(mockRepo)

        // When
        useCase(status = UserStatus.ACTIVE)

        // Then
        assertEquals(UserStatus.ACTIVE, mockRepo.lastStatus)
    }

    @Test
    fun `invoke should pass pagination parameters to repository`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllUsersUseCase(mockRepo)

        // When
        useCase(page = 2, pageSize = 25)

        // Then
        assertEquals(2, mockRepo.lastPage)
        assertEquals(25, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should use default pagination values`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllUsersUseCase(mockRepo)

        // When
        useCase()

        // Then
        assertEquals(1, mockRepo.lastPage)
        assertEquals(50, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.getAllResult = Result.Error(
            message = "Database connection failed",
            code = "DB_ERROR",
        )

        val useCase = GetAllUsersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Database connection failed", result.message)
        assertEquals("DB_ERROR", result.code)
    }

    @Test
    fun `invoke should return empty list when no users exist`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllUsersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<User>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should pass both role and status filters`() = runTest {
        // Given
        val mockRepo = MockUserRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllUsersUseCase(mockRepo)

        // When
        useCase(role = UserRole.MANAGER, status = UserStatus.SUSPENDED)

        // Then
        assertEquals(UserRole.MANAGER, mockRepo.lastRole)
        assertEquals(UserStatus.SUSPENDED, mockRepo.lastStatus)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
