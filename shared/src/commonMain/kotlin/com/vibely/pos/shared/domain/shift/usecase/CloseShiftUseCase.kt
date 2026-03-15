package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository

class CloseShiftUseCase(private val shiftRepository: ShiftRepository) {

    suspend operator fun invoke(shiftId: String, closingBalance: Double, notes: String? = null): Result<Shift> {
        if (closingBalance < 0) {
            return Result.Error(
                message = "Closing balance cannot be negative",
                code = "INVALID_CLOSING_BALANCE",
            )
        }

        val existingShiftResult = shiftRepository.getById(shiftId)

        return when (existingShiftResult) {
            is Result.Error -> existingShiftResult
            is Result.Success -> {
                val existingShift = existingShiftResult.data

                if (existingShift.isClosed) {
                    Result.Error(
                        message = "Shift is already closed",
                        code = "SHIFT_ALREADY_CLOSED",
                    )
                } else {
                    shiftRepository.closeShift(shiftId, closingBalance, notes)
                }
            }
        }
    }
}
