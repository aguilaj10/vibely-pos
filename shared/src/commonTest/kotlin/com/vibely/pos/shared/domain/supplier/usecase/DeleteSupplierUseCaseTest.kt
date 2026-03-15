package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeleteSupplierUseCaseTest {

    private class MockSupplierRepository : SupplierRepository {
        var deleteResult: Result<Unit>? = null
        var lastDeletedId: String? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Supplier>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun create(supplier: Supplier): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun update(supplier: Supplier): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> {
            lastDeletedId = id
            return deleteResult ?: Result.Error("Not configured")
        }

        override suspend fun search(query: String): Result<List<Supplier>> = Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when supplier is deleted`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.deleteResult = Result.Success(Unit)

        val useCase = DeleteSupplierUseCase(mockRepo)

        // When
        val result = useCase("sup-001")

        // Then
        assertIs<Result.Success<Unit>>(result)
        assertEquals("sup-001", mockRepo.lastDeletedId)
    }

    @Test
    fun `invoke should return error when deletion fails`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.deleteResult = Result.Error(
            message = "Supplier not found",
            code = "NOT_FOUND",
        )

        val useCase = DeleteSupplierUseCase(mockRepo)

        // When
        val result = useCase("sup-999")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Supplier not found", result.message)
        assertEquals("NOT_FOUND", result.code)
    }

    @Test
    fun `invoke should return error when supplier has pending orders`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.deleteResult = Result.Error(
            message = "Cannot delete supplier with pending purchase orders",
            code = "HAS_PENDING_ORDERS",
        )

        val useCase = DeleteSupplierUseCase(mockRepo)

        // When
        val result = useCase("sup-002")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Cannot delete supplier with pending purchase orders", result.message)
        assertEquals("HAS_PENDING_ORDERS", result.code)
    }

    @Test
    fun `invoke should pass correct supplier id to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.deleteResult = Result.Success(Unit)

        val useCase = DeleteSupplierUseCase(mockRepo)

        // When
        useCase("supplier-uuid-12345")

        // Then
        assertEquals("supplier-uuid-12345", mockRepo.lastDeletedId)
    }

    @Test
    fun `invoke should return error for database connection issues`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        mockRepo.deleteResult = Result.Error(
            message = "Database connection failed",
            code = "DB_ERROR",
        )

        val useCase = DeleteSupplierUseCase(mockRepo)

        // When
        val result = useCase("sup-003")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Database connection failed", result.message)
        assertEquals("DB_ERROR", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
