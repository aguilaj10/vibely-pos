package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetCustomerPurchaseHistoryUseCaseTest {

    private class MockCustomerRepository : CustomerRepository {
        var getPurchaseHistoryResult: Result<List<Map<String, Any>>>? = null
        var lastCustomerId: String? = null
        var lastPage: Int? = null
        var lastPageSize: Int? = null

        override suspend fun getAll(isActive: Boolean?, page: Int, pageSize: Int): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Customer> = Result.Error("Not implemented")

        override suspend fun create(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun update(customer: Customer): Result<Customer> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun search(query: String): Result<List<Customer>> = Result.Success(emptyList())

        override suspend fun addLoyaltyPoints(customerId: String, points: Int): Result<Customer> = Result.Error("Not implemented")

        override suspend fun getPurchaseHistory(customerId: String, page: Int, pageSize: Int): Result<List<Map<String, Any>>> {
            lastCustomerId = customerId
            lastPage = page
            lastPageSize = pageSize
            return getPurchaseHistoryResult ?: Result.Error("Not configured")
        }
    }

    @Test
    fun `invoke should return purchase history for customer`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val purchaseHistory = listOf(
            mapOf<String, Any>(
                "orderId" to "order-001",
                "totalAmount" to 150.00,
                "date" to "2024-01-15",
            ),
            mapOf<String, Any>(
                "orderId" to "order-002",
                "totalAmount" to 75.50,
                "date" to "2024-01-20",
            ),
        )
        mockRepo.getPurchaseHistoryResult = Result.Success(purchaseHistory)

        val useCase = GetCustomerPurchaseHistoryUseCase(mockRepo)

        // When
        val result = useCase("cust-001")

        // Then
        assertIs<Result.Success<List<Map<String, Any>>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("order-001", result.data[0]["orderId"])
        assertEquals(150.00, result.data[0]["totalAmount"])
    }

    @Test
    fun `invoke should return empty list when customer has no purchases`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getPurchaseHistoryResult = Result.Success(emptyList())

        val useCase = GetCustomerPurchaseHistoryUseCase(mockRepo)

        // When
        val result = useCase("cust-002")

        // Then
        assertIs<Result.Success<List<Map<String, Any>>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should return error when customer not found`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getPurchaseHistoryResult = Result.Error(
            message = "Customer not found",
            code = "NOT_FOUND",
        )

        val useCase = GetCustomerPurchaseHistoryUseCase(mockRepo)

        // When
        val result = useCase("cust-999")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Customer not found", result.message)
        assertEquals("NOT_FOUND", result.code)
    }

    @Test
    fun `invoke should pass default pagination parameters`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getPurchaseHistoryResult = Result.Success(emptyList())

        val useCase = GetCustomerPurchaseHistoryUseCase(mockRepo)

        // When
        useCase("cust-001")

        // Then
        assertEquals("cust-001", mockRepo.lastCustomerId)
        assertEquals(1, mockRepo.lastPage)
        assertEquals(50, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should pass custom pagination parameters`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getPurchaseHistoryResult = Result.Success(emptyList())

        val useCase = GetCustomerPurchaseHistoryUseCase(mockRepo)

        // When
        useCase(customerId = "cust-001", page = 3, pageSize = 25)

        // Then
        assertEquals("cust-001", mockRepo.lastCustomerId)
        assertEquals(3, mockRepo.lastPage)
        assertEquals(25, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should return error for database issues`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        mockRepo.getPurchaseHistoryResult = Result.Error(
            message = "Database connection timeout",
            code = "DB_TIMEOUT",
        )

        val useCase = GetCustomerPurchaseHistoryUseCase(mockRepo)

        // When
        val result = useCase("cust-001")

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Database connection timeout", result.message)
        assertEquals("DB_TIMEOUT", result.code)
    }

    @Test
    fun `invoke should return purchase history with product details`() = runTest {
        // Given
        val mockRepo = MockCustomerRepository()
        val purchaseHistory = listOf(
            mapOf<String, Any>(
                "orderId" to "order-001",
                "totalAmount" to 150.00,
                "date" to "2024-01-15",
                "items" to listOf(
                    mapOf("productName" to "Widget A", "quantity" to 2, "price" to 50.00),
                    mapOf("productName" to "Widget B", "quantity" to 1, "price" to 50.00),
                ),
            ),
        )
        mockRepo.getPurchaseHistoryResult = Result.Success(purchaseHistory)

        val useCase = GetCustomerPurchaseHistoryUseCase(mockRepo)

        // When
        val result = useCase("cust-001")

        // Then
        assertIs<Result.Success<List<Map<String, Any>>>>(result)
        assertEquals(1, result.data.size)
        assertTrue(result.data[0].containsKey("items"))
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
