package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

class GetAllCustomersUseCaseTest {

    private class MockCustomerRepository : CustomerRepository {
        var getAllResult: Result<List<Customer>>? = null
        var lastIsActive: Boolean? = null
        var lastPage: Int? = null
        var lastPageSize: Int? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Customer>> {
            lastIsActive = isActive
            lastPage = page
            lastPageSize = pageSize
            return getAllResult ?: Result.Success(emptyList())
        }

        override suspend fun getById(id: String): Result<Customer> = Result.Error("Not implemented")

        override suspend fun create(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun update(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer> = Result.Error("Not implemented")

        override suspend fun getPurchaseHistory(customerId: String, page: Int, pageSize: Int): Result<List<Map<String, Any>>> =
            Result.Success(emptyList())
    }

    @Test
    fun `invoke should return all customers when no filters provided`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val customers = listOf(
            createCustomer("cust-001", "C001", "John", "Doe", now),
            createCustomer("cust-002", "C002", "Jane", "Smith", now),
        )
        mockRepo.getAllResult = Result.Success(customers)

        val useCase = GetAllCustomersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<Customer>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("John", result.data[0].firstName)
        assertEquals("Jane", result.data[1].firstName)
    }

    @Test
    fun `invoke should filter by active status when isActive provided`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val now = Clock.System.now()
        val activeCustomers = listOf(
            createCustomer("cust-001", "C001", "John", "Doe", now, isActive = true),
        )
        mockRepo.getAllResult = Result.Success(activeCustomers)

        val useCase = GetAllCustomersUseCase(mockRepo)

        // When
        useCase(isActive = true)

        // Then
        assertEquals(true, mockRepo.lastIsActive)
    }

    @Test
    fun `invoke should pass pagination parameters to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllCustomersUseCase(mockRepo)

        // When
        useCase(page = 2, pageSize = 25)

        // Then
        assertEquals(2, mockRepo.lastPage)
        assertEquals(25, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should use default pagination values`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllCustomersUseCase(mockRepo)

        // When
        useCase()

        // Then
        assertEquals(1, mockRepo.lastPage)
        assertEquals(50, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getAllResult = Result.Error(
            message = "Database connection failed",
            code = "DB_ERROR",
        )

        val useCase = GetAllCustomersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Database connection failed", result.message)
    }

    @Test
    fun `invoke should return empty list when no customers exist`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllCustomersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<Customer>>>(result)
        assertTrue(result.data.isEmpty())
    }

    private fun createCustomer(id: String, code: String, firstName: String, lastName: String, now: kotlin.time.Instant, isActive: Boolean = true) =
        Customer.create(
            id = id,
            code = code,
            firstName = firstName,
            lastName = lastName,
            isActive = isActive,
            createdAt = now,
            updatedAt = now,
        )

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
