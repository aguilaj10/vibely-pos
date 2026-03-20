package com.vibely.pos.backend.routes

import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.dto.request.ConvertAmountResponse
import com.vibely.pos.backend.dto.request.CreateExchangeRateRequest
import com.vibely.pos.backend.dto.request.UpdateExchangeRateRequest
import com.vibely.pos.backend.services.CurrencyService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.time.LocalDate

private const val ERROR_KEY = "error"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_ID = "Missing exchange rate ID"
private const val ERROR_INVALID_RATE = "Rate must be positive"
private const val ERROR_INVALID_DATE = "Invalid date format. Use YYYY-MM-DD"
private const val ERROR_SAME_CURRENCY = "Cannot create exchange rate for same currency"
private const val PATH_ID = "/{id}"
private const val CLAIM_USER_ID = "userId"

private data class ConversionParams(
    val amount: Double,
    val fromCurrency: String,
    val toCurrency: String,
    val date: LocalDate?,
)

private suspend fun ApplicationCall.parseConversionParams(): ConversionParams? {
    val amount = request.queryParameters["amount"]?.toDoubleOrNull()
    val fromCurrency = request.queryParameters["from"]
    val toCurrency = request.queryParameters["to"]
    val dateStr = request.queryParameters["date"]

    if (amount == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to "Missing required parameter: amount"))
        return null
    }
    
    if (fromCurrency == null || toCurrency == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to "Missing required parameters: from, to"))
        return null
    }

    val date = dateStr?.let {
        runCatching { LocalDate.parse(it) }.getOrElse {
            respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_DATE))
            return null
        }
    }

    return ConversionParams(amount, fromCurrency, toCurrency, date)
}

/**
 * Configures currency and exchange rate API routes.
 *
 * @param currencyService Service handling currency and exchange rate business logic
 * @param authProvider Environment-specific authentication provider
 */
fun Route.currencyRoutes(currencyService: CurrencyService, authProvider: RouteAuthProvider) {
    route("/api/currencies") {
        with(authProvider) {
            withAuth { usePaths(currencyService) }
        }
    }
}

private fun Route.usePaths(currencyService: CurrencyService) {
    get { call.handleGetAllCurrencies(currencyService) }
    get("/active") { call.handleGetActiveCurrencies(currencyService) }
    
    route("/exchange-rates") {
        get { call.handleGetAllExchangeRates(currencyService) }
        get("/latest") { call.handleGetLatestRate(currencyService) }
        get("/convert") { call.handleConvertAmount(currencyService) }
        post { call.handleCreateExchangeRate(currencyService) }
        put(PATH_ID) { call.handleUpdateExchangeRate(currencyService) }
        delete(PATH_ID) { call.handleDeleteExchangeRate(currencyService) }
    }
}

private suspend fun ApplicationCall.handleGetAllCurrencies(currencyService: CurrencyService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = currencyService.getAllCurrencies()) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetActiveCurrencies(currencyService: CurrencyService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = currencyService.getActiveCurrencies()) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetAllExchangeRates(currencyService: CurrencyService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = currencyService.getAllExchangeRates()) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetLatestRate(currencyService: CurrencyService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val fromCurrency = request.queryParameters["from"]
    val toCurrency = request.queryParameters["to"]

    if (fromCurrency == null || toCurrency == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to "Missing 'from' or 'to' currency parameter"))
        return
    }

    when (val result = currencyService.getLatestExchangeRate(fromCurrency, toCurrency)) {
        is Result.Success -> {
            val data = result.data
            if (data == null) {
                respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to "Exchange rate not found"))
            } else {
                respond(HttpStatusCode.OK, data)
            }
        }
        is Result.Error -> {
            respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
        }
    }
}

private suspend fun ApplicationCall.handleConvertAmount(currencyService: CurrencyService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val params = parseConversionParams() ?: return
    val convertedAmount = currencyService.convertAmount(
        params.amount,
        params.fromCurrency,
        params.toCurrency,
        params.date
    )

    if (convertedAmount == null) {
        respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to "Exchange rate not found for conversion"))
    } else {
        respond(HttpStatusCode.OK, ConvertAmountResponse(
            originalAmount = params.amount,
            fromCurrency = params.fromCurrency,
            toCurrency = params.toCurrency,
            convertedAmount = convertedAmount,
            date = params.date?.toString() ?: LocalDate.now().toString()
        ))
    }
}

private suspend fun ApplicationCall.handleCreateExchangeRate(currencyService: CurrencyService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<CreateExchangeRateRequest>()

    if (requestBody.currencyFrom == requestBody.currencyTo) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_SAME_CURRENCY))
        return
    }

    if (requestBody.rate <= 0) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_RATE))
        return
    }

    runCatching { LocalDate.parse(requestBody.effectiveDate) }.getOrElse {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_DATE))
        return
    }

    when (val result = currencyService.createExchangeRate(
        requestBody.currencyFrom,
        requestBody.currencyTo,
        requestBody.rate,
        requestBody.effectiveDate
    )) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateExchangeRate(currencyService: CurrencyService) {
    val rateId = parameters["id"]
    if (rateId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<UpdateExchangeRateRequest>()

    if (requestBody.rate <= 0) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_RATE))
        return
    }

    runCatching { LocalDate.parse(requestBody.effectiveDate) }.getOrElse {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_INVALID_DATE))
        return
    }

    when (val result = currencyService.updateExchangeRate(rateId, requestBody.rate, requestBody.effectiveDate)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleDeleteExchangeRate(currencyService: CurrencyService) {
    val rateId = parameters["id"]
    if (rateId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = currencyService.deleteExchangeRate(rateId)) {
        is Result.Success -> respond(HttpStatusCode.NoContent, mapOf("message" to "Exchange rate deleted successfully"))
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}
