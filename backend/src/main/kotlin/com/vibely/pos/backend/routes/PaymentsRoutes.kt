package com.vibely.pos.backend.routes

import com.vibely.pos.backend.services.PaymentService
import com.vibely.pos.shared.data.sales.dto.CreatePaymentRequest
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

private const val ERROR_KEY = "error"
private const val ERROR_MISSING_SALE_ID = "Missing sale ID"
private const val ERROR_MISSING_AMOUNT = "Missing amount field"
private const val ERROR_MISSING_PAYMENT_TYPE = "Missing payment_type field"

/**
 * Configures payment-related API routes.
 *
 * Routes:
 * - POST /api/payments - Record a payment for a sale
 * - GET /api/sales/{id}/payments - Get all payments for a sale
 *
 * @param paymentService Service for managing payment operations
 */
fun Route.paymentsRoutes(paymentService: PaymentService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                usePaths(paymentService)
            }
        } else {
            authenticate("auth-jwt") {
                usePaths(paymentService)
            }
        }
    }
}

private fun Route.usePaths(paymentService: PaymentService) {
    post("/payments") { call.handleRecordPayment(paymentService) }
    get("/sales/{id}/payments") { call.handleGetPaymentsBySale(paymentService) }
}

private suspend fun ApplicationCall.handleRecordPayment(paymentService: PaymentService) {
    val requestBody = receive<Map<String, String>>()
    val saleId = requestBody["sale_id"]
    val amount = requestBody["amount"]?.toDoubleOrNull()
    val paymentType = requestBody["payment_type"]
    val referenceNumber = requestBody["reference_number"]
    val notes = requestBody["notes"]

    if (saleId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_SALE_ID))
        return
    }

    if (amount == null || amount <= 0) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_AMOUNT))
        return
    }

    if (paymentType == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_PAYMENT_TYPE))
        return
    }

    val request = CreatePaymentRequest(saleId, amount, paymentType, referenceNumber, notes)
    when (val result = paymentService.recordPayment(request)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetPaymentsBySale(paymentService: PaymentService) {
    val saleId = parameters["id"]
    if (saleId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_SALE_ID))
        return
    }

    when (val result = paymentService.getPaymentsBySale(saleId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}
