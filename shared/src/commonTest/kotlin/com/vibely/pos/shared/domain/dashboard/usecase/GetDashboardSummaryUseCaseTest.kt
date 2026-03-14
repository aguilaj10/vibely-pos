package com.vibely.pos.shared.domain.dashboard.usecase

import com.vibely.pos.shared.domain.dashboard.entity.ActiveShiftInfo
import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.valueobject.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class GetDashboardSummaryUseCaseTest {

    // Mock repository for testing
    private class MockDashboardRepository : DashboardRepository {
        var summaryResult: Result<DashboardSummary>? = null

        override suspend fun getDashboardSummary(): Result<DashboardSummary> = summaryResult ?: Result.Error("Not configured")

        override suspend fun getRecentTransactions(limit: Int): Result<List<RecentTransaction>> = Result.Success(emptyList())

        override suspend fun getLowStockProducts(): Result<List<LowStockProduct>> = Result.Success(emptyList())

        override suspend fun refreshDashboard(): Result<Unit> = Result.Success(Unit)
    }

    @Test
    fun `invoke should return success when repository returns summary`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val expectedSummary = DashboardSummary.create(
            todaySales = Money.fromAmount(15000.00, "PHP"),
            todayTransactionCount = 42,
            lowStockCount = 5,
            activeShift = ActiveShiftInfo(
                shiftId = "shift-123",
                cashierId = "cashier-456",
                cashierName = "John Doe",
                openedAt = Instant.fromEpochMilliseconds(1710316800000),
                openingBalance = Money.fromAmount(5000.00, "PHP"),
            ),
            generatedAt = Instant.fromEpochMilliseconds(1710320400000),
        )
        mockRepo.summaryResult = Result.Success(expectedSummary)

        val useCase = GetDashboardSummaryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<DashboardSummary>>(result)
        assertEquals(expectedSummary.todaySales, result.data.todaySales)
        assertEquals(expectedSummary.todayTransactionCount, result.data.todayTransactionCount)
        assertEquals(expectedSummary.lowStockCount, result.data.lowStockCount)
        assertEquals(expectedSummary.activeShift?.shiftId, result.data.activeShift?.shiftId)
    }

    @Test
    fun `invoke should return success with null active shift when no shift is open`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val expectedSummary = DashboardSummary.create(
            todaySales = Money.fromAmount(0.00, "PHP"),
            todayTransactionCount = 0,
            lowStockCount = 0,
            activeShift = null,
            generatedAt = Instant.fromEpochMilliseconds(1710320400000),
        )
        mockRepo.summaryResult = Result.Success(expectedSummary)

        val useCase = GetDashboardSummaryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<DashboardSummary>>(result)
        assertEquals(null, result.data.activeShift)
        assertEquals(0, result.data.todayTransactionCount)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.summaryResult = Result.Error(
            message = "Network error",
            code = "NETWORK_ERROR",
        )

        val useCase = GetDashboardSummaryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Network error", result.message)
        assertEquals("NETWORK_ERROR", result.code)
    }

    @Test
    fun `invoke should return error when unauthorized`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.summaryResult = Result.Error(
            message = "User is not authenticated",
            code = "UNAUTHORIZED",
        )

        val useCase = GetDashboardSummaryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("User is not authenticated", result.message)
        assertEquals("UNAUTHORIZED", result.code)
    }

    @Test
    fun `invoke should return error when server error occurs`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.summaryResult = Result.Error(
            message = "Internal server error",
            code = "SERVER_ERROR",
        )

        val useCase = GetDashboardSummaryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Internal server error", result.message)
        assertEquals("SERVER_ERROR", result.code)
    }

    // Helper function for running suspending tests
    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
