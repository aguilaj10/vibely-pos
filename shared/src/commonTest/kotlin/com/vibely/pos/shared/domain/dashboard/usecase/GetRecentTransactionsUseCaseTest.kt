package com.vibely.pos.shared.domain.dashboard.usecase

import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.entity.TransactionStatus
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.valueobject.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class GetRecentTransactionsUseCaseTest {

    // Mock repository for testing
    private class MockDashboardRepository : DashboardRepository {
        var transactionsResult: Result<List<RecentTransaction>>? = null
        var lastRequestedLimit: Int? = null

        override suspend fun getDashboardSummary(): Result<DashboardSummary> = Result.Error("Not implemented")

        override suspend fun getRecentTransactions(limit: Int): Result<List<RecentTransaction>> {
            lastRequestedLimit = limit
            return transactionsResult ?: Result.Error("Not configured")
        }

        override suspend fun getLowStockProducts(): Result<List<LowStockProduct>> = Result.Success(emptyList())

        override suspend fun refreshDashboard(): Result<Unit> = Result.Success(Unit)
    }

    @Test
    fun `invoke should return success with transactions when repository succeeds`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val expectedTransactions = listOf(
            RecentTransaction.create(
                id = "txn-1",
                invoiceNumber = "INV-001",
                totalAmount = Money.fromAmount(1500.00, "PHP"),
                status = TransactionStatus.COMPLETED,
                saleDate = Instant.fromEpochMilliseconds(1710320400000),
                customerName = "John Doe",
            ),
            RecentTransaction.create(
                id = "txn-2",
                invoiceNumber = "INV-002",
                totalAmount = Money.fromAmount(2500.00, "PHP"),
                status = TransactionStatus.COMPLETED,
                saleDate = Instant.fromEpochMilliseconds(1710316800000),
                customerName = null,
            ),
        )
        mockRepo.transactionsResult = Result.Success(expectedTransactions)

        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase(limit = 10)

        // Then
        assertIs<Result.Success<List<RecentTransaction>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("txn-1", result.data[0].id)
        assertEquals("INV-001", result.data[0].invoiceNumber)
        assertEquals(10, mockRepo.lastRequestedLimit)
    }

    @Test
    fun `invoke should use default limit when no limit provided`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.transactionsResult = Result.Success(emptyList())

        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<RecentTransaction>>>(result)
        assertEquals(10, mockRepo.lastRequestedLimit)
    }

    @Test
    fun `invoke should return validation error when limit is 0`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase(limit = 0)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("VALIDATION_ERROR", result.code)
        assertTrue(result.message.contains("must be between 1 and 100"))
    }

    @Test
    fun `invoke should return validation error when limit is negative`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase(limit = -1)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("VALIDATION_ERROR", result.code)
        assertTrue(result.message.contains("must be between 1 and 100"))
    }

    @Test
    fun `invoke should return validation error when limit exceeds 100`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase(limit = 101)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("VALIDATION_ERROR", result.code)
        assertTrue(result.message.contains("must be between 1 and 100"))
    }

    @Test
    fun `invoke should accept limit of 1`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.transactionsResult = Result.Success(emptyList())

        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase(limit = 1)

        // Then
        assertIs<Result.Success<List<RecentTransaction>>>(result)
        assertEquals(1, mockRepo.lastRequestedLimit)
    }

    @Test
    fun `invoke should accept limit of 100`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.transactionsResult = Result.Success(emptyList())

        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase(limit = 100)

        // Then
        assertIs<Result.Success<List<RecentTransaction>>>(result)
        assertEquals(100, mockRepo.lastRequestedLimit)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.transactionsResult = Result.Error(
            message = "Network error",
            code = "NETWORK_ERROR",
        )

        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase(limit = 10)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Network error", result.message)
        assertEquals("NETWORK_ERROR", result.code)
    }

    @Test
    fun `invoke should return error when unauthorized`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.transactionsResult = Result.Error(
            message = "User is not authenticated",
            code = "UNAUTHORIZED",
        )

        val useCase = GetRecentTransactionsUseCase(mockRepo)

        // When
        val result = useCase(limit = 10)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("User is not authenticated", result.message)
        assertEquals("UNAUTHORIZED", result.code)
    }

    // Helper function for running suspending tests
    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
