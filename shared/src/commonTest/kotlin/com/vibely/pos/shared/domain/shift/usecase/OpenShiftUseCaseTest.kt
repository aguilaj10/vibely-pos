package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

class OpenShiftUseCaseTest {

    private class MockShiftRepository : ShiftRepository {
        var getCurrentShiftResult: Result<Shift?>? = null
        var openShiftResult: Result<Shift>? = null

        override suspend fun getCurrentShift(cashierId: String): Result<Shift?> = getCurrentShiftResult ?: Result.Success(null)

        override suspend fun getById(id: String): Result<Shift> = Result.Error("Not implemented")

        override suspend fun getHistory(cashierId: String?, page: Int, pageSize: Int): Result<List<Shift>> = Result.Success(emptyList())

        override suspend fun openShift(cashierId: String, openingBalance: Double): Result<Shift> = openShiftResult ?: Result.Error("Not configured")

        override suspend fun closeShift(id: String, closingBalance: Double, notes: String?): Result<Shift> = Result.Error("Not implemented")

        override suspend fun getShiftSummary(shiftId: String): Result<ShiftSummary> = Result.Error("Not implemented")

        override suspend fun generateShiftNumber(): Result<String> = Result.Success("SH-001")
    }

    @Test
    fun `invoke should return success when shift is opened`() = runTest {
        val mockRepo = MockShiftRepository()
        val now = Clock.System.now()
        val shift = Shift.create(
            id = "shift-001",
            shiftNumber = "SH-001",
            cashierId = "cashier-001",
            openingBalance = 500.0,
            openedAt = now,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.getCurrentShiftResult = Result.Success(null)
        mockRepo.openShiftResult = Result.Success(shift)

        val useCase = OpenShiftUseCase(mockRepo)

        val result = useCase("cashier-001", 500.0)

        assertIs<Result.Success<Shift>>(result)
        assertEquals("shift-001", result.data.id)
        assertEquals(500.0, result.data.openingBalance)
    }

    @Test
    fun `invoke should return error when opening balance is negative`() = runTest {
        val mockRepo = MockShiftRepository()

        val useCase = OpenShiftUseCase(mockRepo)

        val result = useCase("cashier-001", -100.0)

        assertIs<Result.Error>(result)
        assertEquals("INVALID_OPENING_BALANCE", result.code)
    }

    @Test
    fun `invoke should return error when cashier already has open shift`() = runTest {
        val mockRepo = MockShiftRepository()
        val now = Clock.System.now()
        val existingShift = Shift.create(
            id = "shift-001",
            shiftNumber = "SH-001",
            cashierId = "cashier-001",
            openingBalance = 500.0,
            openedAt = now,
            closedAt = null,
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.getCurrentShiftResult = Result.Success(existingShift)

        val useCase = OpenShiftUseCase(mockRepo)

        val result = useCase("cashier-001", 500.0)

        assertIs<Result.Error>(result)
        assertEquals("SHIFT_ALREADY_OPEN", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
