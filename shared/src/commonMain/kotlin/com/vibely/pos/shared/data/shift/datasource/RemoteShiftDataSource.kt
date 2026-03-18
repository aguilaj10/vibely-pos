package com.vibely.pos.shared.data.shift.datasource

import com.vibely.pos.shared.data.shift.dto.ShiftDTO
import com.vibely.pos.shared.data.shift.dto.ShiftSummaryDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RemoteShiftDataSource(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getCurrentShift(): Result<ShiftDTO?> = Result.runCatching {
        httpClient.get("$baseUrl/api/shifts/current").body<ShiftDTO?>()
    }

    suspend fun getShiftById(id: String): Result<ShiftDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/shifts/$id").body<ShiftDTO>()
    }

    suspend fun getShiftHistory(cashierId: String? = null, page: Int = 1, pageSize: Int = 50): Result<List<ShiftDTO>> = Result.runCatching {
        httpClient
            .get("$baseUrl/api/shifts") {
                cashierId?.let { parameter("cashier_id", it) }
                parameter("page", page)
                parameter("page_size", pageSize)
            }.body<List<ShiftDTO>>()
    }

    suspend fun openShift(openingBalance: Double): Result<ShiftDTO> = Result.runCatching {
        httpClient
            .post("$baseUrl/api/shifts/open") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("opening_balance" to openingBalance))
            }.body<ShiftDTO>()
    }

    suspend fun closeShift(id: String, closingBalance: Double, notes: String?): Result<ShiftDTO> = Result.runCatching {
        httpClient
            .post("$baseUrl/api/shifts/$id/close") {
                contentType(ContentType.Application.Json)
                setBody(
                    buildMap {
                        put("closing_balance", closingBalance)
                        notes?.let { put("notes", it) }
                    },
                )
            }.body<ShiftDTO>()
    }

    suspend fun getShiftSummary(shiftId: String): Result<ShiftSummaryDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/shifts/$shiftId/summary").body<ShiftSummaryDTO>()
    }

    suspend fun generateShiftNumber(): Result<String> = Result.runCatching {
        httpClient.get("$baseUrl/api/shifts/generate-shift-number").body<Map<String, String>>()["shift_number"]
            ?: throw IllegalStateException("Shift number not returned")
    }
}
