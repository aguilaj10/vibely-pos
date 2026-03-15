package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

class UpdateSupplierUseCaseTest {

    private class MockSupplierRepository : SupplierRepository {
        var updateResult: Result<Supplier>? = null
        var lastUpdatedSupplier: Supplier? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Supplier>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun create(supplier: Supplier): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun update(supplier: Supplier): Result<Supplier> {
            lastUpdatedSupplier = supplier
            return updateResult ?: Result.Error("Not configured")
        }

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Supplier>> = Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when supplier is updated`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-001",
            code = "S001",
            name = "Acme Corp Updated",
            contactPerson = "Jane Smith",
            email = "jane@acme.com",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(supplier)

        val useCase = UpdateSupplierUseCase(mockRepo)

        // When
        val result = useCase(supplier)

        // Then
        assertIs<Result.Success<Supplier>>(result)
        assertEquals(supplier.id, result.data.id)
        assertEquals("Acme Corp Updated", result.data.name)
        assertEquals(supplier, mockRepo.lastUpdatedSupplier)
    }

    @Test
    fun `invoke should return error when update fails`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-001",
            code = "S001",
            name = "Acme Corp",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Error(
            message = "Supplier not found",
            code = "NOT_FOUND",
        )

        val useCase = UpdateSupplierUseCase(mockRepo)

        // When
        val result = useCase(supplier)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Supplier not found", result.message)
        assertEquals("NOT_FOUND", result.code)
    }

    @Test
    fun `invoke should pass updated contact person to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-002",
            code = "S002",
            name = "Tech Supplies",
            contactPerson = "Bob Wilson",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(supplier)

        val useCase = UpdateSupplierUseCase(mockRepo)

        // When
        useCase(supplier)

        // Then
        assertEquals("Bob Wilson", mockRepo.lastUpdatedSupplier?.contactPerson)
    }

    @Test
    fun `invoke should pass updated email to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-003",
            code = "S003",
            name = "Global Goods",
            email = "new.email@globalgoods.com",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(supplier)

        val useCase = UpdateSupplierUseCase(mockRepo)

        // When
        useCase(supplier)

        // Then
        assertEquals("new.email@globalgoods.com", mockRepo.lastUpdatedSupplier?.email)
    }

    @Test
    fun `invoke should pass inactive status to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-004",
            code = "S004",
            name = "Inactive Supplier",
            isActive = false,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(supplier)

        val useCase = UpdateSupplierUseCase(mockRepo)

        // When
        useCase(supplier)

        // Then
        assertEquals(false, mockRepo.lastUpdatedSupplier?.isActive)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
