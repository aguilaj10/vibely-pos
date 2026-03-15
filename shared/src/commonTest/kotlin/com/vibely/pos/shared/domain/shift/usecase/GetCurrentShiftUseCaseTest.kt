package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Clock

class GetCurrentShiftUseCaseTest {

    private class MockShiftRepository : ShiftRepository {
        var getCurrentShiftResult: Result<Shift?>? = null

        override suspend fun getCurrentShift(cashierId: String): Result<Shift?> = getCurrentShiftResult ?: Result.Success(null)

        override suspend fun getById(id: String): Result<Shift> = Result.Error("Not implemented")

        override suspend fun getHistory(cashierId: String?, page: Int, pageSize: Int): Result<List<Shift>> = Result.Success(emptyList())

        override suspend fun openShift(cashierId: String, openingBalance: Double): Result<Shift> = Result.Error("Not implemented")

        override suspend fun closeShift(id: String, closingBalance: Double, notes: String?): Result<Shift> = Result.Error("Not implemented")

        override suspend fun getShiftSummary(shiftId: String): Result<ShiftSummary> = Result.Error("Not implemented")

        override suspend fun generateShiftNumber(): Result<String> = Result.Success("SH-001")
    }

    @Test
    fun `invoke should return current shift when found`() = runTest {
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
        mockRepo.getCurrentShiftResult = Result.Success(shift)

        val useCase = GetCurrentShiftUseCase(mockRepo)

        val result = useCase("cashier-001")

        assertIs<Result.Success<Shift?>>(result)
        assertEquals("shift-001", result.data?.id)
    }

    @Test
    fun `invoke should return null when no current shift`() = runTest {
        val mockRepo = MockShiftRepository()
        mockRepo.getCurrentShiftResult = Result.Success(null)

        val useCase = GetCurrentShiftUseCase(mockRepo)

        val result = useCase("cashier-001")

        assertIs<Result.Success<Shift?>>(result)
        assertNull(result.data)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
