package com.vibely.pos.backend.routes

import com.vibely.pos.backend.dto.request.CreateCategoryRequest
import com.vibely.pos.backend.dto.request.UpdateCategoryRequest
import com.vibely.pos.backend.services.CategoryService
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
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
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("CategoryRoutes")

private const val ERROR_KEY = "error"
private const val ERROR_UNAUTHORIZED = "User not authenticated"
private const val ERROR_MISSING_ID = "Missing category ID"
private const val ERROR_MISSING_NAME = "Missing required field: name"
private const val DEFAULT_PAGE = 1
private const val DEFAULT_PAGE_SIZE = 50
private const val PATH_ID = "/{id}"
private const val CLAIM_USER_ID = "userId"

/**
 * Configures category-related routes with JWT authentication.
 *
 * @param categoryService Service handling category business logic
 */
fun Route.categoryRoutes(categoryService: CategoryService) {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

    route("/api/categories") {
        if (isDebugMode) {
            authenticate("auth-jwt", "debug-bearer", optional = false) {
                get { call.handleGetAllCategories(categoryService) }
                get(PATH_ID) { call.handleGetById(categoryService) }
                post { call.handleCreateCategory(categoryService) }
                put(PATH_ID) { call.handleUpdateCategory(categoryService) }
                delete(PATH_ID) { call.handleDeleteCategory(categoryService) }
            }
        } else {
            authenticate("auth-jwt") {
                get { call.handleGetAllCategories(categoryService) }
                get(PATH_ID) { call.handleGetById(categoryService) }
                post { call.handleCreateCategory(categoryService) }
                put(PATH_ID) { call.handleUpdateCategory(categoryService) }
                delete(PATH_ID) { call.handleDeleteCategory(categoryService) }
            }
        }
    }
}

private suspend fun ApplicationCall.handleGetAllCategories(categoryService: CategoryService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val isActive = request.queryParameters["is_active"]?.toBooleanStrictOrNull()
    val page = request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
    val pageSize = request.queryParameters["page_size"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE

    when (val result = categoryService.getAllCategories(userId, isActive, page, pageSize)) {
        is Result.Success -> {
            respond(HttpStatusCode.OK, result.data)
        }
        is Result.Error -> {
            logger.error("Failed to fetch categories: ${result.message}", result.cause)
            respond(
                HttpStatusCode.InternalServerError,
                mapOf(ERROR_KEY to result.message)
            )
        }
    }
}

private suspend fun ApplicationCall.handleGetById(categoryService: CategoryService) {
    val categoryId = parameters["id"]
    if (categoryId == null) {
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

    when (val result = categoryService.getCategoryById(userId, categoryId)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleCreateCategory(categoryService: CategoryService) {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(CLAIM_USER_ID)
        ?.asString()

    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR_KEY to ERROR_UNAUTHORIZED))
        return
    }

    val requestBody = receive<CreateCategoryRequest>()

    if (requestBody.name.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to ERROR_MISSING_NAME))
        return
    }

    val createRequest = CreateCategoryRequest(
        name = requestBody.name,
        description = requestBody.description,
        color = requestBody.color,
        icon = requestBody.icon,
        isActive = requestBody.isActive
    )

    when (val result = categoryService.createCategory(userId, createRequest)) {
        is Result.Success -> respond(HttpStatusCode.Created, result.data)
        is Result.Error -> respond(HttpStatusCode.BadRequest, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleUpdateCategory(categoryService: CategoryService) {
    val categoryId = parameters["id"]
    if (categoryId == null) {
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

    val requestBody = receive<UpdateCategoryRequest>()

    val updateRequest = UpdateCategoryRequest(
        name = requestBody.name,
        description = requestBody.description,
        color = requestBody.color,
        icon = requestBody.icon,
        isActive = requestBody.isActive
    )

    when (val result = categoryService.updateCategory(userId, categoryId, updateRequest)) {
        is Result.Success -> respond(HttpStatusCode.OK, result.data)
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}

private suspend fun ApplicationCall.handleDeleteCategory(categoryService: CategoryService) {
    val categoryId = parameters["id"]
    if (categoryId == null) {
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

    when (val result = categoryService.deleteCategory(userId, categoryId)) {
        is Result.Success -> respond(HttpStatusCode.NoContent, mapOf("message" to "Category deleted successfully"))
        is Result.Error -> respond(HttpStatusCode.NotFound, mapOf(ERROR_KEY to result.message))
    }
}
