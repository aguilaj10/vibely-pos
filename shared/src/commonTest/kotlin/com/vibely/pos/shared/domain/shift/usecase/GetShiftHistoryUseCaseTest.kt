package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

class GetShiftHistoryUseCaseTest {

    private class MockShiftRepository : ShiftRepository {
        var getHistoryResult: Result<List<Shift>>? = null
        var lastCashierId: String? = null
        var lastPage: Int? = null
        var lastPageSize: Int? = null

        override suspend fun getHistory(cashierId: String?, page: Int, pageSize: Int): Result<List<Shift>> {
            lastCashierId = cashierId
            lastPage = page
            lastPageSize = pageSize
            return getHistoryResult ?: Result.Success(emptyList())
        }

        override suspend fun getCurrentShift(cashierId: String): Result<Shift?> = Result.Success(null)
        override suspend fun getById(id: String): Result<Shift> = Result.Error("Not implemented")
        override suspend fun openShift(cashierId: String, openingBalance: Double): Result<Shift> = Result.Error("Not implemented")
        override suspend fun closeShift(id: String, closingBalance: Double, notes: String?): Result<Shift> = Result.Error("Not implemented")
        override suspend fun getShiftSummary(shiftId: String): Result<ShiftSummary> = Result.Error("Not implemented")
        override suspend fun generateShiftNumber(): Result<String> = Result.Success("SH-001")
    }

    private fun createTestShift(id: String, shiftNumber: String, cashierId: String, openingBalance: Double = 100.0, isOpen: Boolean = false): Shift {
        val now = Clock.System.now()
        return Shift.create(
            id = id,
            shiftNumber = shiftNumber,
            cashierId = cashierId,
            openingBalance = openingBalance,
            cashierName = "Test Cashier",
            totalSales = 500.0,
            totalCash = 300.0,
            totalCard = 150.0,
            totalOther = 50.0,
            openedAt = now,
            closedAt = if (isOpen) null else now,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `invoke should return shift history`() = runTest {
        // Given
        val mockRepo = MockShiftRepository()
        val shifts = listOf(
            createTestShift("shift-001", "SH-001", "cashier-001"),
            createTestShift("shift-002", "SH-002", "cashier-001"),
        )
        mockRepo.getHistoryResult = Result.Success(shifts)

        val useCase = GetShiftHistoryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<Shift>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("SH-001", result.data[0].shiftNumber)
        assertEquals("SH-002", result.data[1].shiftNumber)
    }

    @Test
    fun `invoke should filter by cashier when cashierId provided`() = runTest {
        // Given
        val mockRepo = MockShiftRepository()
        mockRepo.getHistoryResult = Result.Success(emptyList())

        val useCase = GetShiftHistoryUseCase(mockRepo)

        // When
        useCase(cashierId = "cashier-123")

        // Then
        assertEquals("cashier-123", mockRepo.lastCashierId)
    }

    @Test
    fun `invoke should pass pagination parameters to repository`() = runTest {
        // Given
        val mockRepo = MockShiftRepository()
        mockRepo.getHistoryResult = Result.Success(emptyList())

        val useCase = GetShiftHistoryUseCase(mockRepo)

        // When
        useCase(page = 3, pageSize = 20)

        // Then
        assertEquals(3, mockRepo.lastPage)
        assertEquals(20, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should use default pagination values`() = runTest {
        // Given
        val mockRepo = MockShiftRepository()
        mockRepo.getHistoryResult = Result.Success(emptyList())

        val useCase = GetShiftHistoryUseCase(mockRepo)

        // When
        useCase()

        // Then
        assertEquals(null, mockRepo.lastCashierId)
        assertEquals(1, mockRepo.lastPage)
        assertEquals(50, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val mockRepo = MockShiftRepository()
        mockRepo.getHistoryResult = Result.Error(
            message = "Database connection failed",
            code = "DB_ERROR",
        )

        val useCase = GetShiftHistoryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Database connection failed", result.message)
        assertEquals("DB_ERROR", result.code)
    }

    @Test
    fun `invoke should return empty list when no shifts exist`() = runTest {
        // Given
        val mockRepo = MockShiftRepository()
        mockRepo.getHistoryResult = Result.Success(emptyList())

        val useCase = GetShiftHistoryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<Shift>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should return shifts including open shifts`() = runTest {
        // Given
        val mockRepo = MockShiftRepository()
        val shifts = listOf(
            createTestShift("shift-001", "SH-001", "cashier-001", isOpen = true),
            createTestShift("shift-002", "SH-002", "cashier-001", isOpen = false),
        )
        mockRepo.getHistoryResult = Result.Success(shifts)

        val useCase = GetShiftHistoryUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<Shift>>>(result)
        assertEquals(2, result.data.size)
        assertTrue(result.data[0].isOpen)
        assertTrue(result.data[1].isClosed)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
