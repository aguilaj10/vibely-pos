@file:Suppress("UndocumentedPublicFunction", "TooManyFunctions", "StringLiteralDuplication")

package com.vibely.pos.backend.routes

import com.vibely.pos.backend.dto.request.AssignRoleRequest
import com.vibely.pos.backend.dto.request.ChangePasswordRequest
import com.vibely.pos.backend.dto.request.CreateUserRequest
import com.vibely.pos.backend.dto.request.ResetPasswordRequest
import com.vibely.pos.backend.dto.request.UpdateUserRequest
import com.vibely.pos.backend.dto.request.UpdateUserStatusRequest
import com.vibely.pos.backend.services.UserManagementService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

private const val ERROR_KEY = "error"
private const val MESSAGE_KEY = "message"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_ID = "Missing user ID"
private const val ERROR_MISSING_EMAIL = "Missing email"
private const val ERROR_MISSING_PASSWORD = "Missing password"
private const val ERROR_MISSING_NAME = "Missing full name"
private const val ERROR_MISSING_ROLE = "Missing role"
private const val ERROR_MISSING_STATUS = "Missing status"
private const val MSG_PASSWORD_CHANGED = "Password changed successfully"
private const val MSG_PASSWORD_RESET = "Password reset successfully"
private const val MSG_USER_DELETED = "User deleted successfully"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50
private const val PATH_ID = "/{id}"
private const val CLAIM_USER_ID = "userId"

fun Route.userManagementRoutes(userManagementService: UserManagementService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api/users") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                configureUserRoutes(userManagementService)
            }
        } else {
            authenticate("auth-jwt") {
                configureUserRoutes(userManagementService)
            }
        }
    }
}

private fun Route.configureUserRoutes(userManagementService: UserManagementService) {
    get { call.handleGetAllUsers(userManagementService) }
    get("/search") { call.handleSearchUsers(userManagementService) }
    get(PATH_ID) { call.handleGetById(userManagementService) }
    post { call.handleCreateUser(userManagementService) }
    put(PATH_ID) { call.handleUpdateUser(userManagementService) }
    patch("$PATH_ID/status") { call.handleUpdateStatus(userManagementService) }
    patch("$PATH_ID/role") { call.handleAssignRole(userManagementService) }
    post("$PATH_ID/change-password") { call.handleChangePassword(userManagementService) }
    post("$PATH_ID/reset-password") { call.handleResetPassword(userManagementService) }
    delete(PATH_ID) { call.handleDeleteUser(userManagementService) }
}

private suspend fun ApplicationCall.handleGetAllUsers(userManagementService: UserManagementService) {
    val role = request.queryParameters["role"]
    val status = request.queryParameters["status"]
    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    when (val result = userManagementService.getAllUsers(role, status, page, pageSize)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleSearchUsers(userManagementService: UserManagementService) {
    val query = request.queryParameters["q"] ?: ""

    when (val result = userManagementService.searchUsers(query)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.InternalServerError, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleGetById(userManagementService: UserManagementService) {
    val targetUserId = parameters["id"]
    if (targetUserId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    extractUserId() ?: return

    when (val result = userManagementService.getUserById(targetUserId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleCreateUser(userManagementService: UserManagementService) {
    extractUserId() ?: return

    val requestBody = receive<CreateUserRequest>()

    val validationError = validateCreateUserRequest(requestBody)
    if (validationError != null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to validationError))
        return
    }

    when (val result = userManagementService.createUser(requestBody)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private fun validateCreateUserRequest(request: CreateUserRequest): String? = when {
    request.email.isBlank() -> ERROR_MISSING_EMAIL
    request.password.isBlank() -> ERROR_MISSING_PASSWORD
    request.fullName.isBlank() -> ERROR_MISSING_NAME
    request.role.isBlank() -> ERROR_MISSING_ROLE
    else -> null
}

private suspend fun ApplicationCall.handleUpdateUser(userManagementService: UserManagementService) {
    val targetUserId = parameters["id"]
    if (targetUserId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    extractUserId() ?: return

    val requestBody = receive<UpdateUserRequest>()

    when (val result = userManagementService.updateUser(targetUserId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateStatus(userManagementService: UserManagementService) {
    val targetUserId = parameters["id"]
    if (targetUserId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    extractUserId() ?: return

    val requestBody = receive<UpdateUserStatusRequest>()

    if (requestBody.status.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_STATUS))
        return
    }

    when (val result = userManagementService.updateUserStatus(targetUserId, requestBody.status)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleAssignRole(userManagementService: UserManagementService) {
    val targetUserId = parameters["id"]
    if (targetUserId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    extractUserId() ?: return

    val requestBody = receive<AssignRoleRequest>()

    if (requestBody.role.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ROLE))
        return
    }

    when (val result = userManagementService.assignRole(targetUserId, requestBody.role)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleChangePassword(userManagementService: UserManagementService) {
    val targetUserId = parameters["id"]
    if (targetUserId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    extractUserId() ?: return

    val requestBody = receive<ChangePasswordRequest>()

    when (val result = userManagementService.changePassword(targetUserId, requestBody)) {
        is Result.Success -> respond(HttpStatusCode.OK, mapOf(MESSAGE_KEY to MSG_PASSWORD_CHANGED))
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleResetPassword(userManagementService: UserManagementService) {
    val targetUserId = parameters["id"]
    if (targetUserId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    extractUserId() ?: return

    val requestBody = receive<ResetPasswordRequest>()

    when (val result = userManagementService.resetPassword(targetUserId, requestBody.newPassword)) {
        is Result.Success -> respond(HttpStatusCode.OK, mapOf(MESSAGE_KEY to MSG_PASSWORD_RESET))
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleDeleteUser(userManagementService: UserManagementService) {
    val targetUserId = parameters["id"]
    if (targetUserId == null) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_ID))
        return
    }

    extractUserId() ?: return

    when (val result = userManagementService.deleteUser(targetUserId)) {
        is Result.Success -> respond(HttpStatusCode.NoContent, mapOf(MESSAGE_KEY to MSG_USER_DELETED))
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.extractUserId(): String? {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
    }
    return userId
}
