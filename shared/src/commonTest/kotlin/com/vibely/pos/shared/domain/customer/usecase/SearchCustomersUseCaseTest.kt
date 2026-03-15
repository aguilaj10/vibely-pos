package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

class SearchCustomersUseCaseTest {

    private class MockCustomerRepository : CustomerRepository {
        var searchResult: Result<List<Customer>>? = null
        var lastSearchQuery: String? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Customer> = Result.Error("Not implemented")

        override suspend fun create(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun update(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Customer>> {
            lastSearchQuery = query
            return searchResult ?: Result.Error("Not configured")
        }

        override suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer> = Result.Error("Not implemented")

        override suspend fun getPurchaseHistory(customerId: String, page: Int, pageSize: Int): Result<List<Map<String, Any>>> =
            Result.Success(emptyList())
    }

    private fun createTestCustomer(id: String, code: String, firstName: String, lastName: String, email: String? = null): Customer {
        val now = Clock.System.now()
        return Customer.create(
            id = id,
            code = code,
            firstName = firstName,
            lastName = lastName,
            email = email,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `invoke should return matching customers`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val customers = listOf(
            createTestCustomer("cust-001", "C001", "John", "Doe", "john@example.com"),
            createTestCustomer("cust-002", "C002", "John", "Smith", "john.smith@example.com"),
        )
        mockRepo.searchResult = Result.Success(customers)

        val useCase = SearchCustomersUseCase(mockRepo)

        // When
        val result = useCase("John")

        // Then
        assertIs<Result.Success<List<Customer>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("John", result.data[0].firstName)
        assertEquals("John", result.data[1].firstName)
    }

    @Test
    fun `invoke should return empty list when no matches found`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.searchResult = Result.Success(emptyList())

        val useCase = SearchCustomersUseCase(mockRepo)

        // When
        val result = useCase("NonExistent")

        // Then
        assertIs<Result.Success<List<Customer>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should return error when search fails`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.searchResult = Result.Error(
            message = "Search index unavailable",
            code = "SEARCH_ERROR",
        )

        val useCase = SearchCustomersUseCase(mockRepo)

        // When
        val result = useCase("John")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Search index unavailable", result.message)
        assertEquals("SEARCH_ERROR", result.code)
    }

    @Test
    fun `invoke should pass search query to repository`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.searchResult = Result.Success(emptyList())

        val useCase = SearchCustomersUseCase(mockRepo)

        // When
        useCase("jane.doe@example.com")

        // Then
        assertEquals("jane.doe@example.com", mockRepo.lastSearchQuery)
    }

    @Test
    fun `invoke should search by customer code`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val customers = listOf(
            createTestCustomer("cust-001", "C001", "John", "Doe"),
        )
        mockRepo.searchResult = Result.Success(customers)

        val useCase = SearchCustomersUseCase(mockRepo)

        // When
        val result = useCase("C001")

        // Then
        assertIs<Result.Success<List<Customer>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("C001", result.data[0].code)
        assertEquals("C001", mockRepo.lastSearchQuery)
    }

    @Test
    fun `invoke should search by email`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val customers = listOf(
            createTestCustomer("cust-001", "C001", "Jane", "Doe", "jane@example.com"),
        )
        mockRepo.searchResult = Result.Success(customers)

        val useCase = SearchCustomersUseCase(mockRepo)

        // When
        val result = useCase("jane@example.com")

        // Then
        assertIs<Result.Success<List<Customer>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("jane@example.com", result.data[0].email)
    }

    @Test
    fun `invoke should handle partial name matches`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val customers = listOf(
            createTestCustomer("cust-001", "C001", "Jonathan", "Doe"),
            createTestCustomer("cust-002", "C002", "John", "Johnson"),
        )
        mockRepo.searchResult = Result.Success(customers)

        val useCase = SearchCustomersUseCase(mockRepo)

        // When
        val result = useCase("John")

        // Then
        assertIs<Result.Success<List<Customer>>>(result)
        assertEquals(2, result.data.size)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
