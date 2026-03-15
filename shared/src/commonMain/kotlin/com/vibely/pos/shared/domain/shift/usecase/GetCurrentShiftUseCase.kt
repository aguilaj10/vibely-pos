package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository

class GetCurrentShiftUseCase(private val shiftRepository: ShiftRepository) {

    suspend operator fun invoke(cashierId: String): Result<Shift?> = shiftRepository.getCurrentShift(cashierId)
}
