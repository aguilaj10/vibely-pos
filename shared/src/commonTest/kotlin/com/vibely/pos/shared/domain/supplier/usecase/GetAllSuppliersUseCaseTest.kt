package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

class GetAllSuppliersUseCaseTest {

    private class MockSupplierRepository : SupplierRepository {
        var getAllResult: Result<List<Supplier>>? = null
        var lastIsActive: Boolean? = null
        var lastPage: Int? = null
        var lastPageSize: Int? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Supplier>> {
            lastIsActive = isActive
            lastPage = page
            lastPageSize = pageSize
            return getAllResult ?: Result.Error("Not configured")
        }

        override suspend fun getById(id: String): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun create(supplier: Supplier): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun update(supplier: Supplier): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Supplier>> = Result.Success(emptyList())
    }

    private fun createTestSupplier(id: String, code: String, name: String, isActive: Boolean = true): Supplier {
        val now = Clock.System.now()
        return Supplier.create(
            id = id,
            code = code,
            name = name,
            isActive = isActive,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `invoke should return all suppliers`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val suppliers = listOf(
            createTestSupplier("sup-001", "S001", "Acme Corp"),
            createTestSupplier("sup-002", "S002", "Tech Supplies"),
        )
        mockRepo.getAllResult = Result.Success(suppliers)

        val useCase = GetAllSuppliersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<Supplier>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("Acme Corp", result.data[0].name)
        assertEquals("Tech Supplies", result.data[1].name)
    }

    @Test
    fun `invoke should return empty list when no suppliers exist`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllSuppliersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<Supplier>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should return error when fetch fails`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.getAllResult = Result.Error(
            message = "Database connection failed",
            code = "DB_ERROR",
        )

        val useCase = GetAllSuppliersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Database connection failed", result.message)
        assertEquals("DB_ERROR", result.code)
    }

    @Test
    fun `invoke should pass active filter to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllSuppliersUseCase(mockRepo)

        // When
        useCase(isActive = true)

        // Then
        assertEquals(true, mockRepo.lastIsActive)
    }

    @Test
    fun `invoke should pass default pagination parameters`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllSuppliersUseCase(mockRepo)

        // When
        useCase()

        // Then
        assertEquals(null, mockRepo.lastIsActive)
        assertEquals(1, mockRepo.lastPage)
        assertEquals(50, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should pass custom pagination parameters`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllSuppliersUseCase(mockRepo)

        // When
        useCase(isActive = false, page = 3, pageSize = 25)

        // Then
        assertEquals(false, mockRepo.lastIsActive)
        assertEquals(3, mockRepo.lastPage)
        assertEquals(25, mockRepo.lastPageSize)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
