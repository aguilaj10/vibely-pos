package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository

class GetShiftSummaryUseCase(private val shiftRepository: ShiftRepository) {

    suspend operator fun invoke(shiftId: String): Result<ShiftSummary> = shiftRepository.getShiftSummary(shiftId)
}
