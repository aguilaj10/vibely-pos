package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

class CloseShiftUseCaseTest {

    private class MockShiftRepository : ShiftRepository {
        var getByIdResult: Result<Shift>? = null
        var closeShiftResult: Result<Shift>? = null

        override suspend fun getCurrentShift(cashierId: String): Result<Shift?> = Result.Success(null)

        override suspend fun getById(id: String): Result<Shift> = getByIdResult ?: Result.Error("Not configured")

        override suspend fun getHistory(cashierId: String?, page: Int, pageSize: Int): Result<List<Shift>> = Result.Success(emptyList())

        override suspend fun openShift(cashierId: String, openingBalance: Double): Result<Shift> = Result.Error("Not implemented")

        override suspend fun closeShift(id: String, closingBalance: Double, notes: String?): Result<Shift> =
            closeShiftResult ?: Result.Error("Not configured")

        override suspend fun getShiftSummary(shiftId: String): Result<ShiftSummary> = Result.Error("Not implemented")

        override suspend fun generateShiftNumber(): Result<String> = Result.Success("SH-001")
    }

    @Test
    fun `invoke should return success when shift is closed`() = runTest {
        val mockRepo = MockShiftRepository()
        val now = Clock.System.now()
        val openShift = Shift.create(
            id = "shift-001",
            shiftNumber = "SH-001",
            cashierId = "cashier-001",
            openingBalance = 500.0,
            openedAt = now,
            closedAt = null,
            createdAt = now,
            updatedAt = now,
        )
        val closedShift = openShift.close(actualClosingBalance = 1500.0, closeNotes = "All good")
        mockRepo.getByIdResult = Result.Success(openShift)
        mockRepo.closeShiftResult = Result.Success(closedShift)

        val useCase = CloseShiftUseCase(mockRepo)

        val result = useCase("shift-001", 1500.0, "All good")

        assertIs<Result.Success<Shift>>(result)
        assertEquals(1500.0, result.data.closingBalance)
    }

    @Test
    fun `invoke should return error when closing balance is negative`() = runTest {
        val mockRepo = MockShiftRepository()

        val useCase = CloseShiftUseCase(mockRepo)

        val result = useCase("shift-001", -100.0)

        assertIs<Result.Error>(result)
        assertEquals("INVALID_CLOSING_BALANCE", result.code)
    }

    @Test
    fun `invoke should return error when shift not found`() = runTest {
        val mockRepo = MockShiftRepository()
        mockRepo.getByIdResult = Result.Error("Shift not found", "NOT_FOUND")

        val useCase = CloseShiftUseCase(mockRepo)

        val result = useCase("shift-999", 1500.0)

        assertIs<Result.Error>(result)
        assertEquals("NOT_FOUND", result.code)
    }

    @Test
    fun `invoke should return error when shift is already closed`() = runTest {
        val mockRepo = MockShiftRepository()
        val now = Clock.System.now()
        val closedShift = Shift.create(
            id = "shift-001",
            shiftNumber = "SH-001",
            cashierId = "cashier-001",
            openingBalance = 500.0,
            openedAt = now,
            closedAt = now,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.getByIdResult = Result.Success(closedShift)

        val useCase = CloseShiftUseCase(mockRepo)

        val result = useCase("shift-001", 1500.0)

        assertIs<Result.Error>(result)
        assertEquals("SHIFT_ALREADY_CLOSED", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
