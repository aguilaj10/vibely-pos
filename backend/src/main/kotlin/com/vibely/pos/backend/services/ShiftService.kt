@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "LongParameterList",
    "StringLiteralDuplication",
    "MaxLineLength",
    "FunctionMaxLength"
)

package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.backend.common.ErrorMessages
import com.vibely.pos.backend.dto.request.CloseShiftRequest
import com.vibely.pos.shared.data.shift.dto.ShiftDTO
import com.vibely.pos.shared.data.shift.dto.ShiftSummaryDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ERROR_FETCH_FAILED = "Failed to fetch shifts"
private const val ERROR_OPEN_FAILED = "Failed to open shift"
private const val ERROR_CLOSE_FAILED = "Failed to close shift"
private const val ERROR_SUMMARY_FAILED = "Failed to get shift summary"
private const val ERROR_ALREADY_OPEN = "A shift is already open"
private const val ERROR_ALREADY_CLOSED = "Shift is already closed"
private const val SHIFT_NUMBER_PADDING = 3

class ShiftService(private val supabaseClient: SupabaseClient) : BaseService() {

    suspend fun getCurrentShift(userId: String): Result<ShiftDTO?> {
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient.from(TableNames.SHIFTS)
                .select {
                    filter {
                        eq("cashier_id", userId)
                    }
                }
                .decodeList<ShiftDTO>()
                .firstOrNull()
        }
    }

    suspend fun getShiftById(userId: String, shiftId: String): Result<ShiftDTO> {
        return executeQuery(ErrorMessages.SHIFT_NOT_FOUND) {
            supabaseClient.from(TableNames.SHIFTS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, shiftId)
                        eq("cashier_id", userId)
                    }
                }
                .decodeSingle<ShiftDTO>()
        }
    }

    suspend fun getShiftHistory(
        userId: String,
        cashierId: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<ShiftDTO>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient.from(TableNames.SHIFTS)
                .select {
                    filter {
                        eq("cashier_id", cashierId ?: userId)
                    }
                    order("opened_at", Order.DESCENDING)
                    range(from, to)
                }
                .decodeList<ShiftDTO>()
        }
    }

    suspend fun openShift(userId: String, openingBalance: Double): Result<ShiftDTO> {
        return executeQuery(ERROR_OPEN_FAILED) {
            val existingShift = supabaseClient.from(TableNames.SHIFTS)
                .select {
                    filter {
                        eq("cashier_id", userId)
                    }
                }
                .decodeList<ShiftDTO>()
                .firstOrNull()

            if (existingShift != null) {
                throw IllegalStateException(ERROR_ALREADY_OPEN)
            }

            val shiftNumber = generateNextShiftNumber(userId)

            val data = buildJsonObject {
                put("shift_number", shiftNumber)
                put("cashier_id", userId)
                put("opening_balance", openingBalance)
                put("total_sales", 0.0)
                put("total_cash", 0.0)
                put("total_card", 0.0)
                put("total_other", 0.0)
                put("opened_at", java.time.Instant.now().toString())
            }

            supabaseClient.from(TableNames.SHIFTS)
                .insert(data) { select() }
                .decodeSingle<ShiftDTO>()
        }
    }

    suspend fun closeShift(
        userId: String,
        shiftId: String,
        request: CloseShiftRequest,
    ): Result<ShiftDTO> {
        return executeQuery(ERROR_CLOSE_FAILED) {
            val currentShift = supabaseClient.from(TableNames.SHIFTS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, shiftId)
                        eq("cashier_id", userId)
                    }
                }
                .decodeSingle<ShiftDTO>()

            if (currentShift.closedAt != null) {
                throw IllegalStateException(ERROR_ALREADY_CLOSED)
            }

            val expectedBalance = currentShift.openingBalance + currentShift.totalCash
            val discrepancy = request.closingBalance - expectedBalance

            val data = buildJsonObject {
                put("closing_balance", request.closingBalance)
                put("expected_balance", expectedBalance)
                put("discrepancy", discrepancy)
                put(DatabaseColumns.CLOSED_AT, java.time.Instant.now().toString())
                request.notes?.let { put(DatabaseColumns.NOTES, it) }
            }

            supabaseClient.from(TableNames.SHIFTS)
                .update(data) {
                    filter { eq(DatabaseColumns.ID, shiftId) }
                    select()
                }
                .decodeSingle<ShiftDTO>()
        }
    }

    suspend fun getShiftSummary(userId: String, shiftId: String): Result<ShiftSummaryDTO> {
        return executeQuery(ERROR_SUMMARY_FAILED) {
            val shift = supabaseClient.from(TableNames.SHIFTS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, shiftId)
                        eq("cashier_id", userId)
                    }
                }
                .decodeSingle<ShiftDTO>()

            ShiftSummaryDTO(
                totalSales = shift.totalSales,
                transactionCount = 0,
                cashPayments = shift.totalCash,
                cardPayments = shift.totalCard,
                otherPayments = shift.totalOther,
                expenses = 0.0,
                openingBalance = shift.openingBalance,
                expectedClosingBalance = shift.openingBalance + shift.totalCash,
            )
        }
    }

    suspend fun generateShiftNumber(userId: String): Result<String> {
        return executeQuery("Failed to generate shift number") {
            generateNextShiftNumber(userId)
        }
    }

    private suspend fun generateNextShiftNumber(userId: String): String {
        val today = java.time.LocalDate.now()
        val prefix = "SH-${today.year}${today.monthValue.toString().padStart(2, '0')}${today.dayOfMonth.toString().padStart(2, '0')}"

        val existingShifts = supabaseClient.from(TableNames.SHIFTS)
            .select {
                filter {
                    eq("cashier_id", userId)
                    ilike("shift_number", "$prefix%")
                }
            }
            .decodeList<ShiftDTO>()

        val nextNumber = existingShifts.size + 1
        return "$prefix-${nextNumber.toString().padStart(SHIFT_NUMBER_PADDING, '0')}"
    }
}
