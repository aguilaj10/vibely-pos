package com.vibely.pos.shared.domain.supplier.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

class CreateSupplierUseCaseTest {

    private class MockSupplierRepository : SupplierRepository {
        var createResult: Result<Supplier>? = null
        var lastCreatedSupplier: Supplier? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Supplier>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun create(supplier: Supplier): Result<Supplier> {
            lastCreatedSupplier = supplier
            return createResult ?: Result.Error("Not configured")
        }

        override suspend fun update(supplier: Supplier): Result<Supplier> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Supplier>> = Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when supplier is created`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-001",
            code = "S001",
            name = "Acme Corp",
            contactPerson = "John Smith",
            email = "john@acme.com",
            phone = "+1234567890",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Success(supplier)

        val useCase = CreateSupplierUseCase(mockRepo)

        // When
        val result = useCase(supplier)

        // Then
        assertIs<Result.Success<Supplier>>(result)
        assertEquals(supplier.id, result.data.id)
        assertEquals(supplier.code, result.data.code)
        assertEquals(supplier.name, result.data.name)
        assertEquals(supplier, mockRepo.lastCreatedSupplier)
    }

    @Test
    fun `invoke should return error when creation fails`() = runTest {
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
        mockRepo.createResult = Result.Error(
            message = "Supplier code already exists",
            code = "DUPLICATE_CODE",
        )

        val useCase = CreateSupplierUseCase(mockRepo)

        // When
        val result = useCase(supplier)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Supplier code already exists", result.message)
        assertEquals("DUPLICATE_CODE", result.code)
    }

    @Test
    fun `invoke should pass supplier with contact person to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-002",
            code = "S002",
            name = "Tech Supplies",
            contactPerson = "Jane Doe",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Success(supplier)

        val useCase = CreateSupplierUseCase(mockRepo)

        // When
        useCase(supplier)

        // Then
        assertEquals("Jane Doe", mockRepo.lastCreatedSupplier?.contactPerson)
    }

    @Test
    fun `invoke should pass supplier with address to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-003",
            code = "S003",
            name = "Global Goods",
            address = "123 Main St, City",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Success(supplier)

        val useCase = CreateSupplierUseCase(mockRepo)

        // When
        useCase(supplier)

        // Then
        assertEquals("123 Main St, City", mockRepo.lastCreatedSupplier?.address)
    }

    @Test
    fun `invoke should pass supplier with email to repository`() = runTest {
        // Given
        val mockRepo = MockSupplierRepository()
        val now = Clock.System.now()
        val supplier = Supplier.create(
            id = "sup-004",
            code = "S004",
            name = "Quick Parts",
            email = "orders@quickparts.com",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Success(supplier)

        val useCase = CreateSupplierUseCase(mockRepo)

        // When
        useCase(supplier)

        // Then
        assertEquals("orders@quickparts.com", mockRepo.lastCreatedSupplier?.email)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
