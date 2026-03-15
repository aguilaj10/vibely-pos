package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

class UpdateCustomerUseCaseTest {

    private class MockCustomerRepository : CustomerRepository {
        var updateResult: Result<Customer>? = null
        var lastUpdatedCustomer: Customer? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Customer> = Result.Error("Not implemented")

        override suspend fun create(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun update(customer: Customer): Result<Customer> {
            lastUpdatedCustomer = customer
            return updateResult ?: Result.Error("Not configured")
        }

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer> = Result.Error("Not implemented")

        override suspend fun getPurchaseHistory(customerId: String, page: Int, pageSize: Int): Result<List<Map<String, Any>>> =
            Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when customer is updated`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-001",
            code = "C001",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+1234567890",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(customer)

        val useCase = UpdateCustomerUseCase(mockRepo)

        // When
        val result = useCase(customer)

        // Then
        assertIs<Result.Success<Customer>>(result)
        assertEquals(customer.id, result.data.id)
        assertEquals(customer.firstName, result.data.firstName)
        assertEquals(customer, mockRepo.lastUpdatedCustomer)
    }

    @Test
    fun `invoke should return error when update fails`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-001",
            code = "C001",
            firstName = "John",
            lastName = "Doe",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Error(
            message = "Customer not found",
            code = "NOT_FOUND",
        )

        val useCase = UpdateCustomerUseCase(mockRepo)

        // When
        val result = useCase(customer)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Customer not found", result.message)
        assertEquals("NOT_FOUND", result.code)
    }

    @Test
    fun `invoke should pass updated email to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-002",
            code = "C002",
            firstName = "Jane",
            lastName = "Smith",
            email = "new.email@example.com",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(customer)

        val useCase = UpdateCustomerUseCase(mockRepo)

        // When
        useCase(customer)

        // Then
        assertEquals("new.email@example.com", mockRepo.lastUpdatedCustomer?.email)
    }

    @Test
    fun `invoke should pass updated phone to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-003",
            code = "C003",
            firstName = "Bob",
            lastName = "Wilson",
            phone = "+9876543210",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(customer)

        val useCase = UpdateCustomerUseCase(mockRepo)

        // When
        useCase(customer)

        // Then
        assertEquals("+9876543210", mockRepo.lastUpdatedCustomer?.phone)
    }

    @Test
    fun `invoke should pass updated loyalty points to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-004",
            code = "C004",
            firstName = "Alice",
            lastName = "Brown",
            loyaltyPoints = 500,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(customer)

        val useCase = UpdateCustomerUseCase(mockRepo)

        // When
        useCase(customer)

        // Then
        assertEquals(500, mockRepo.lastUpdatedCustomer?.loyaltyPoints)
    }

    @Test
    fun `invoke should pass inactive status to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-005",
            code = "C005",
            firstName = "Charlie",
            lastName = "Davis",
            isActive = false,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(customer)

        val useCase = UpdateCustomerUseCase(mockRepo)

        // When
        useCase(customer)

        // Then
        assertEquals(false, mockRepo.lastUpdatedCustomer?.isActive)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
