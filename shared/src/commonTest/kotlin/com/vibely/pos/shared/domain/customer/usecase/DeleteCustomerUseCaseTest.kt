package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeleteCustomerUseCaseTest {

    private class MockCustomerRepository : CustomerRepository {
        var deleteResult: Result<Unit>? = null
        var lastDeletedId: String? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Customer> = Result.Error("Not implemented")

        override suspend fun create(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun update(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> {
            lastDeletedId = id
            return deleteResult ?: Result.Error("Not configured")
        }

        override suspend fun search(query: String): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer> = Result.Error("Not implemented")

        override suspend fun getPurchaseHistory(customerId: String, page: Int, pageSize: Int): Result<List<Map<String, Any>>> =
            Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when customer is deleted`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.deleteResult = Result.Success(Unit)

        val useCase = DeleteCustomerUseCase(mockRepo)

        // When
        val result = useCase("cust-001")

        // Then
        assertIs<Result.Success<Unit>>(result)
        assertEquals("cust-001", mockRepo.lastDeletedId)
    }

    @Test
    fun `invoke should return error when deletion fails`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.deleteResult = Result.Error(
            message = "Customer not found",
            code = "NOT_FOUND",
        )

        val useCase = DeleteCustomerUseCase(mockRepo)

        // When
        val result = useCase("cust-999")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Customer not found", result.message)
        assertEquals("NOT_FOUND", result.code)
    }

    @Test
    fun `invoke should return error when customer has pending orders`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.deleteResult = Result.Error(
            message = "Cannot delete customer with pending orders",
            code = "HAS_PENDING_ORDERS",
        )

        val useCase = DeleteCustomerUseCase(mockRepo)

        // When
        val result = useCase("cust-002")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Cannot delete customer with pending orders", result.message)
        assertEquals("HAS_PENDING_ORDERS", result.code)
    }

    @Test
    fun `invoke should pass correct customer id to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.deleteResult = Result.Success(Unit)

        val useCase = DeleteCustomerUseCase(mockRepo)

        // When
        useCase("customer-uuid-12345")

        // Then
        assertEquals("customer-uuid-12345", mockRepo.lastDeletedId)
    }

    @Test
    fun `invoke should return error for database connection issues`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.deleteResult = Result.Error(
            message = "Database connection failed",
            code = "DB_ERROR",
        )

        val useCase = DeleteCustomerUseCase(mockRepo)

        // When
        val result = useCase("cust-003")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Database connection failed", result.message)
        assertEquals("DB_ERROR", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
