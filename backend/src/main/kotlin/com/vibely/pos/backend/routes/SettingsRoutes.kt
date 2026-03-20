@file:Suppress("TooManyFunctions")
package com.vibely.pos.backend.routes

import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.dto.request.UpdateReceiptSettingsRequest
import com.vibely.pos.backend.dto.request.UpdateStoreInfoRequest
import com.vibely.pos.backend.dto.request.UpdateTaxSettingsRequest
import com.vibely.pos.backend.dto.request.UpdateUserPreferencesRequest
import com.vibely.pos.backend.services.SettingsService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

private const val ERROR_KEY = "error"
private const val CLAIM_USER_ID = "userId"

private const val ERROR_MISSING_STORE_NAME = "Missing required field: storeName"
private const val ERROR_MISSING_ADDRESS = "Missing required field: address"
private const val ERROR_MISSING_PHONE = "Missing required field: phone"
private const val ERROR_MISSING_EMAIL = "Missing required field: email"
private const val ERROR_MISSING_HEADER = "Missing required field: header"
private const val ERROR_MISSING_FOOTER = "Missing required field: footer"
private const val ERROR_MISSING_TAX_RATE = "Missing required field: taxRate"
private const val ERROR_MISSING_CURRENCY = "Missing required field: currency"
private const val ERROR_MISSING_LANGUAGE = "Missing required field: language"
private const val ERROR_MISSING_THEME = "Missing required field: theme"
private const val ERROR_UNAUTHORIZED = "User not authenticated"

/**
 * Registers settings management routes.
 * Provides endpoints for retrieving and updating store, receipt, tax, and user preference settings.
 *
 * @param settingsService Service handling settings business logic
 * @param authProvider Environment-specific authentication provider
 */
fun Route.settingsRoutes(settingsService: SettingsService, authProvider: RouteAuthProvider) {
    route("/api/settings") {
        with(authProvider) {
            withAuth { usePaths(settingsService) }
        }
    }
}

private fun Route.usePaths(settingsService: SettingsService) {
    get("/store") { call.handleGetStoreSettings(settingsService) }
    put("/store") { call.handleUpdateStoreSettings(settingsService) }
    get("/receipt") { call.handleGetReceiptSettings(settingsService) }
    put("/receipt") { call.handleUpdateReceiptSettings(settingsService) }
    get("/tax") { call.handleGetTaxSettings(settingsService) }
    put("/tax") { call.handleUpdateTaxSettings(settingsService) }
    get("/preferences") { call.handleGetUserPreferences(settingsService) }
    put("/preferences") { call.handleUpdateUserPreferences(settingsService) }
}

private suspend fun ApplicationCall.handleGetStoreSettings(settingsService: SettingsService) {
    when (val result = settingsService.getStoreSettings()) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateStoreSettings(settingsService: SettingsService) {
    val request = receive<UpdateStoreInfoRequest>()

    val validationError = when {
        request.storeName.isBlank() -> ERROR_MISSING_STORE_NAME
        request.address.isBlank() -> ERROR_MISSING_ADDRESS
        request.phone.isBlank() -> ERROR_MISSING_PHONE
        request.email.isBlank() -> ERROR_MISSING_EMAIL
        else -> null
    }
    if (validationError != null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to validationError))
        return
    }

    when (val result = settingsService.updateStoreSettings(request)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetReceiptSettings(settingsService: SettingsService) {
    when (val result = settingsService.getReceiptSettings()) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateReceiptSettings(settingsService: SettingsService) {
    val request = receive<UpdateReceiptSettingsRequest>()

    if (request.header.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_HEADER))
        return
    }
    if (request.footer.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_FOOTER))
        return
    }

    when (val result = settingsService.updateReceiptSettings(request)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetTaxSettings(settingsService: SettingsService) {
    when (val result = settingsService.getTaxSettings()) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateTaxSettings(settingsService: SettingsService) {
    val request = receive<UpdateTaxSettingsRequest>()

    if (request.taxRate < 0) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_TAX_RATE))
        return
    }
    if (request.currency.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_CURRENCY))
        return
    }

    when (val result = settingsService.updateTaxSettings(request)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetUserPreferences(settingsService: SettingsService) {
    val userId = getUserIdFromPrincipal()
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    when (val result = settingsService.getUserPreferences(userId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateUserPreferences(settingsService: SettingsService) {
    val userId = getUserIdFromPrincipal()
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val request = receive<UpdateUserPreferencesRequest>()

    if (request.language.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_LANGUAGE))
        return
    }
    if (request.theme.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_THEME))
        return
    }

    when (val result = settingsService.updateUserPreferences(userId, request)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private fun ApplicationCall.getUserIdFromPrincipal(): String? {
    val jwtPrincipal = principal<JWTPrincipal>()
    if (jwtPrincipal != null) {
        return jwtPrincipal.payload.getClaim(CLAIM_USER_ID)?.asString()
    }
    
    val userIdPrincipal = principal<UserIdPrincipal>()
    if (userIdPrincipal != null) {
        return userIdPrincipal.name
    }
    
    return null
}
