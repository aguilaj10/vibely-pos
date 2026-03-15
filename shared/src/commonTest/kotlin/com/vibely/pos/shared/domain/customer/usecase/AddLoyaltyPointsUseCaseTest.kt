package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

class AddLoyaltyPointsUseCaseTest {

    private class MockCustomerRepository : CustomerRepository {
        var addPointsResult: Result<Customer>? = null
        var lastCustomerId: String? = null
        var lastPoints: Int? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Customer> = Result.Error("Not implemented")

        override suspend fun create(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun update(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer> {
            lastCustomerId = customerId
            lastPoints = points
            return addPointsResult ?: Result.Error("Not configured")
        }

        override suspend fun getPurchaseHistory(customerId: String, page: Int, pageSize: Int): Result<List<Map<String, Any>>> =
            Result.Success(emptyList())
    }

    @Test
    fun `invoke should return updated customer when points added successfully`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val updatedCustomer = Customer.create(
            id = "cust-001",
            code = "C001",
            firstName = "John",
            lastName = "Doe",
            loyaltyPoints = 150,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.addPointsResult = Result.Success(updatedCustomer)

        val useCase = AddLoyaltyPointsUseCase(mockRepo)

        // When
        val result = useCase("cust-001", 50)

        // Then
        assertIs<Result.Success<Customer>>(result)
        assertEquals(150, result.data.loyaltyPoints)
        assertEquals("cust-001", mockRepo.lastCustomerId)
        assertEquals(50, mockRepo.lastPoints)
    }

    @Test
    fun `invoke should return error when customer not found`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.addPointsResult = Result.Error(
            message = "Customer not found",
            code = "CUSTOMER_NOT_FOUND",
        )

        val useCase = AddLoyaltyPointsUseCase(mockRepo)

        // When
        val result = useCase("invalid-id", 50)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Customer not found", result.message)
        assertEquals("CUSTOMER_NOT_FOUND", result.code)
    }

    @Test
    fun `invoke should pass correct customer ID and points to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customer = Customer.create(
            id = "cust-123",
            code = "C123",
            firstName = "Test",
            lastName = "User",
            loyaltyPoints = 200,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.addPointsResult = Result.Success(customer)

        val useCase = AddLoyaltyPointsUseCase(mockRepo)

        // When
        useCase("cust-123", 100)

        // Then
        assertEquals("cust-123", mockRepo.lastCustomerId)
        assertEquals(100, mockRepo.lastPoints)
    }

    @Test
    fun `invoke should handle tier upgrade from Bronze to Silver`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val upgradedCustomer = Customer.create(
            id = "cust-001",
            code = "C001",
            firstName = "John",
            lastName = "Doe",
            loyaltyPoints = 500,
            loyaltyTier = "Silver",
            totalPurchases = 500.0,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.addPointsResult = Result.Success(upgradedCustomer)

        val useCase = AddLoyaltyPointsUseCase(mockRepo)

        // When
        val result = useCase("cust-001", 500)

        // Then
        assertIs<Result.Success<Customer>>(result)
        assertEquals("Silver", result.data.loyaltyTier)
    }

    @Test
    fun `invoke should return error when adding negative points`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.addPointsResult = Result.Error(
            message = "Points must be positive",
            code = "INVALID_POINTS",
        )

        val useCase = AddLoyaltyPointsUseCase(mockRepo)

        // When
        val result = useCase("cust-001", -10)

        // Then
        assertIs<Result.Error>(result)
    }

    @Test
    fun `invoke should return error when customer is inactive`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.addPointsResult = Result.Error(
            message = "Cannot add points to inactive customer",
            code = "CUSTOMER_INACTIVE",
        )

        val useCase = AddLoyaltyPointsUseCase(mockRepo)

        // When
        val result = useCase("inactive-customer", 50)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Cannot add points to inactive customer", result.message)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
