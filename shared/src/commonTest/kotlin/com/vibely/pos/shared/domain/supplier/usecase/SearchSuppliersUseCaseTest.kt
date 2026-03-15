package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

class SearchSuppliersUseCaseTest {

    private class MockSupplierRepository : SupplierRepository {
        var searchResult: Result<List<Supplier>>? = null
        var lastSearchQuery: String? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Supplier>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun create(supplier: Supplier): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun update(supplier: Supplier): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Supplier>> {
            lastSearchQuery = query
            return searchResult ?: Result.Error("Not configured")
        }
    }

    private fun createTestSupplier(id: String, code: String, name: String, email: String? = null): Supplier {
        val now = Clock.System.now()
        return Supplier.create(
            id = id,
            code = code,
            name = name,
            email = email,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `invoke should return matching suppliers`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val suppliers = listOf(
            createTestSupplier("sup-001", "S001", "Acme Corp", "contact@acme.com"),
            createTestSupplier("sup-002", "S002", "Acme Industries", "sales@acme-ind.com"),
        )
        mockRepo.searchResult = Result.Success(suppliers)

        val useCase = SearchSuppliersUseCase(mockRepo)

        // When
        val result = useCase("Acme")

        // Then
        assertIs<Result.Success<List<Supplier>>>(result)
        assertEquals(2, result.data.size)
        assertTrue(result.data.all { it.name.contains("Acme") })
    }

    @Test
    fun `invoke should return empty list when no matches found`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.searchResult = Result.Success(emptyList())

        val useCase = SearchSuppliersUseCase(mockRepo)

        // When
        val result = useCase("NonExistent")

        // Then
        assertIs<Result.Success<List<Supplier>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should return error when search fails`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.searchResult = Result.Error(
            message = "Search index unavailable",
            code = "SEARCH_ERROR",
        )

        val useCase = SearchSuppliersUseCase(mockRepo)

        // When
        val result = useCase("Acme")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Search index unavailable", result.message)
        assertEquals("SEARCH_ERROR", result.code)
    }

    @Test
    fun `invoke should pass search query to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.searchResult = Result.Success(emptyList())

        val useCase = SearchSuppliersUseCase(mockRepo)

        // When
        useCase("contact@supplier.com")

        // Then
        assertEquals("contact@supplier.com", mockRepo.lastSearchQuery)
    }

    @Test
    fun `invoke should search by supplier code`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val suppliers = listOf(
            createTestSupplier("sup-001", "S001", "Acme Corp"),
        )
        mockRepo.searchResult = Result.Success(suppliers)

        val useCase = SearchSuppliersUseCase(mockRepo)

        // When
        val result = useCase("S001")

        // Then
        assertIs<Result.Success<List<Supplier>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("S001", result.data[0].code)
        assertEquals("S001", mockRepo.lastSearchQuery)
    }

    @Test
    fun `invoke should search by email`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val suppliers = listOf(
            createTestSupplier("sup-001", "S001", "Tech Parts", "orders@techparts.com"),
        )
        mockRepo.searchResult = Result.Success(suppliers)

        val useCase = SearchSuppliersUseCase(mockRepo)

        // When
        val result = useCase("orders@techparts.com")

        // Then
        assertIs<Result.Success<List<Supplier>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("orders@techparts.com", result.data[0].email)
    }

    @Test
    fun `invoke should handle partial name matches`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val suppliers = listOf(
            createTestSupplier("sup-001", "S001", "Global Tech Solutions"),
            createTestSupplier("sup-002", "S002", "Tech Parts Inc"),
        )
        mockRepo.searchResult = Result.Success(suppliers)

        val useCase = SearchSuppliersUseCase(mockRepo)

        // When
        val result = useCase("Tech")

        // Then
        assertIs<Result.Success<List<Supplier>>>(result)
        assertEquals(2, result.data.size)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
