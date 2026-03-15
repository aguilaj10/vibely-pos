package com.vibely.pos.shared.data.shift.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.shift.datasource.RemoteShiftDataSource
import com.vibely.pos.shared.data.shift.mapper.ShiftMapper
import com.vibely.pos.shared.data.shift.mapper.ShiftSummaryMapper
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.map
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.domain.shift.entity.ShiftSummary
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository

class ShiftRepositoryImpl(private val remoteDataSource: RemoteShiftDataSource) :
    BaseRepository(),
    ShiftRepository {

    override suspend fun getCurrentShift(cashierId: String): Result<Shift?> = remoteDataSource.getCurrentShift().map { dto ->
        dto?.let { ShiftMapper.toDomain(it) }
    }

    override suspend fun getById(id: String): Result<Shift> = mapSingle(
        remoteDataSource.getShiftById(id),
        ShiftMapper::toDomain,
    )

    override suspend fun getHistory(cashierId: String?, page: Int, pageSize: Int): Result<List<Shift>> = mapList(
        remoteDataSource.getShiftHistory(cashierId, page, pageSize),
        ShiftMapper::toDomain,
    )

    override suspend fun openShift(cashierId: String, openingBalance: Double): Result<Shift> = mapSingle(
        remoteDataSource.openShift(openingBalance),
        ShiftMapper::toDomain,
    )

    override suspend fun closeShift(id: String, closingBalance: Double, notes: String?): Result<Shift> = mapSingle(
        remoteDataSource.closeShift(id, closingBalance, notes),
        ShiftMapper::toDomain,
    )

    override suspend fun getShiftSummary(shiftId: String): Result<ShiftSummary> = mapSingle(
        remoteDataSource.getShiftSummary(shiftId),
        ShiftSummaryMapper::toDomain,
    )

    override suspend fun generateShiftNumber(): Result<String> = remoteDataSource.generateShiftNumber()
}
