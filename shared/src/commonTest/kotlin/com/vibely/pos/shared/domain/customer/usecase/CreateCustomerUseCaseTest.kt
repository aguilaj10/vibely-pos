package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

/**
 * Unit tests for [CreateCustomerUseCase].
 */
class CreateCustomerUseCaseTest {

    private class MockCustomerRepository : CustomerRepository {
        var createResult: Result<Customer>? = null
        var lastCreatedCustomer: Customer? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Customer> = Result.Error("Not implemented")

        override suspend fun create(customer: Customer): Result<Customer> {
            lastCreatedCustomer = customer
            return createResult ?: Result.Error("Not configured")
        }

        override suspend fun update(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer> = Result.Error("Not implemented")

        override suspend fun getPurchaseHistory(customerId: String, page: Int, pageSize: Int): Result<List<Map<String, Any>>> =
            Result.Success(emptyList())
    }

    @Test
    fun `invoke should return success when customer is created`() = runTest {
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
        mockRepo.createResult = Result.Success(customer)

        val useCase = CreateCustomerUseCase(mockRepo)

        // When
        val result = useCase(customer)

        // Then
        assertIs<Result.Success<Customer>>(result)
        assertEquals(customer.id, result.data.id)
        assertEquals(customer.code, result.data.code)
        assertEquals(customer.firstName, result.data.firstName)
        assertEquals(customer.lastName, result.data.lastName)
        assertEquals(customer, mockRepo.lastCreatedCustomer)
    }

    @Test
    fun `invoke should return error when creation fails`() = runTest {
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
        mockRepo.createResult = Result.Error(
            message = "Customer code already exists",
            code = "DUPLICATE_CODE",
        )

        val useCase = CreateCustomerUseCase(mockRepo)

        // When
        val result = useCase(customer)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Customer code already exists", result.message)
        assertEquals("DUPLICATE_CODE", result.code)
    }

    @Test
    fun `invoke should pass customer with email to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-002",
            code = "C002",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Success(customer)

        val useCase = CreateCustomerUseCase(mockRepo)

        // When
        useCase(customer)

        // Then
        assertEquals("jane.smith@example.com", mockRepo.lastCreatedCustomer?.email)
    }

    @Test
    fun `invoke should pass customer with phone to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-003",
            code = "C003",
            firstName = "Bob",
            lastName = "Wilson",
            phone = "+0987654321",
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Success(customer)

        val useCase = CreateCustomerUseCase(mockRepo)

        // When
        useCase(customer)

        // Then
        assertEquals("+0987654321", mockRepo.lastCreatedCustomer?.phone)
    }

    @Test
    fun `invoke should pass customer with initial loyalty points to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-004",
            code = "C004",
            firstName = "Alice",
            lastName = "Brown",
            loyaltyPoints = 100,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Success(customer)

        val useCase = CreateCustomerUseCase(mockRepo)

        // When
        useCase(customer)

        // Then
        assertEquals(100, mockRepo.lastCreatedCustomer?.loyaltyPoints)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
