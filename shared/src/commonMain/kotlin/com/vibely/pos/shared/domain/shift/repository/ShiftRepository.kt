package com.vibely.pos.shared.domain.shift.repository

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary

interface ShiftRepository {

    suspend fun getCurrentShift(cashierId: String): Result<Shift?>

    suspend fun getById(id: String): Result<Shift>

    suspend fun getHistory(cashierId: String? = null, page: Int = 1, pageSize: Int = 50): Result<List<Shift>>

    suspend fun openShift(cashierId: String, openingBalance: Double): Result<Shift>

    suspend fun closeShift(id: String, closingBalance: Double, notes: String?): Result<Shift>

    suspend fun getShiftSummary(shiftId: String): Result<ShiftSummary>

    suspend fun generateShiftNumber(): Result<String>
}
