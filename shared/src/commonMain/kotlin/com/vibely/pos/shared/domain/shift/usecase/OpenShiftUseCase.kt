package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository

class OpenShiftUseCase(private val shiftRepository: ShiftRepository) {

    suspend operator fun invoke(cashierId: String, openingBalance: Double): Result<Shift> {
        if (openingBalance < 0) {
            return Result.Error(
                message = "Opening balance cannot be negative",
                code = "INVALID_OPENING_BALANCE",
            )
        }

        val existingShiftResult = shiftRepository.getCurrentShift(cashierId)

        return when (existingShiftResult) {
            is Result.Error -> existingShiftResult
            is Result.Success -> {
                val existingShift = existingShiftResult.data

                if (existingShift != null && existingShift.isOpen) {
                    Result.Error(
                        message = "Cashier already has an open shift",
                        code = "SHIFT_ALREADY_OPEN",
                    )
                } else {
                    shiftRepository.openShift(cashierId, openingBalance)
                }
            }
        }
    }
}
