package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetShiftSummaryUseCaseTest {

    private class MockShiftRepository : ShiftRepository {
        var getShiftSummaryResult: Result<ShiftSummary>? = null

        override suspend fun getCurrentShift(cashierId: String): Result<Shift?> = Result.Success(null)

        override suspend fun getById(id: String): Result<Shift> = Result.Error("Not implemented")

        override suspend fun getHistory(cashierId: String?, page: Int, pageSize: Int): Result<List<Shift>> = Result.Success(emptyList())

        override suspend fun openShift(cashierId: String, openingBalance: Double): Result<Shift> = Result.Error("Not implemented")

        override suspend fun closeShift(id: String, closingBalance: Double, notes: String?): Result<Shift> = Result.Error("Not implemented")

        override suspend fun getShiftSummary(shiftId: String): Result<ShiftSummary> = getShiftSummaryResult ?: Result.Error("Not configured")

        override suspend fun generateShiftNumber(): Result<String> = Result.Success("SH-001")
    }

    @Test
    fun `invoke should return shift summary when found`() = runTest {
        val mockRepo = MockShiftRepository()
        val summary = ShiftSummary(
            totalSales = 1000.0,
            transactionCount = 10,
            cashPayments = 600.0,
            cardPayments = 300.0,
            otherPayments = 100.0,
            expenses = 50.0,
            openingBalance = 500.0,
            expectedClosingBalance = 1050.0,
        )
        mockRepo.getShiftSummaryResult = Result.Success(summary)

        val useCase = GetShiftSummaryUseCase(mockRepo)

        val result = useCase("shift-001")

        assertIs<Result.Success<ShiftSummary>>(result)
        assertEquals(1000.0, result.data.totalSales)
        assertEquals(10, result.data.transactionCount)
    }

    @Test
    fun `invoke should return error when shift not found`() = runTest {
        val mockRepo = MockShiftRepository()
        mockRepo.getShiftSummaryResult = Result.Error("Shift not found", "NOT_FOUND")

        val useCase = GetShiftSummaryUseCase(mockRepo)

        val result = useCase("shift-999")

        assertIs<Result.Error>(result)
        assertEquals("NOT_FOUND", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
