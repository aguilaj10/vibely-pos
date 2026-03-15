package com.vibely.pos.shared.domain.shift.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository

class GetShiftHistoryUseCase(private val shiftRepository: ShiftRepository) {

    suspend operator fun invoke(cashierId: String? = null, page: Int = 1, pageSize: Int = 50): Result<List<Shift>> =
        shiftRepository.getHistory(cashierId, page, pageSize)
}
