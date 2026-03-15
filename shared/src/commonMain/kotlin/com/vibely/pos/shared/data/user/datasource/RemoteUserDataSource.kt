package com.vibely.pos.shared.data.user.datasource

import com.vibely.pos.shared.data.auth.dto.UserDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Remote data source for user management operations.
 *
 * Communicates with the backend API to perform CRUD operations on users.
 */
class RemoteUserDataSource(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Gets all users with optional filtering by role and status.
     *
     * @param role Filter by user role (optional).
     * @param status Filter by user status (optional).
     * @param page Page number for pagination.
     * @param pageSize Number of items per page.
     * @return Result containing list of users.
     */
    suspend fun getAllUsers(role: String? = null, status: String? = null, page: Int = 1, pageSize: Int = 50): Result<List<UserDTO>> =
        Result.runCatching {
            httpClient.get("$baseUrl/api/users") {
                role?.let { parameter("role", it) }
                status?.let { parameter("status", it) }
                parameter("page", page)
                parameter("page_size", pageSize)
            }.body<List<UserDTO>>()
        }

    /**
     * Gets a user by their ID.
     *
     * @param id The user ID.
     * @return Result containing the user.
     */
    suspend fun getUserById(id: String): Result<UserDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/users/$id").body<UserDTO>()
    }

    /**
     * Gets a user by their email address.
     *
     * @param email The user's email address.
     * @return Result containing the user.
     */
    suspend fun getUserByEmail(email: String): Result<UserDTO> = Result.runCatching {
        httpClient.get("$baseUrl/api/users/by-email") {
            parameter("email", email)
        }.body<UserDTO>()
    }

    /**
     * Creates a new user.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @param fullName The user's full name.
     * @param role The user's role.
     * @return Result containing the created user.
     */
    suspend fun createUser(email: String, password: String, fullName: String, role: String): Result<UserDTO> = Result.runCatching {
        httpClient.post("$baseUrl/api/users") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "email" to email,
                    "password" to password,
                    "full_name" to fullName,
                    "role" to role,
                ),
            )
        }.body<UserDTO>()
    }

    /**
     * Updates an existing user.
     *
     * @param id The user ID.
     * @param email The updated email address.
     * @param fullName The updated full name.
     * @return Result containing the updated user.
     */
    suspend fun updateUser(id: String, email: String, fullName: String): Result<UserDTO> = Result.runCatching {
        httpClient.put("$baseUrl/api/users/$id") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "email" to email,
                    "full_name" to fullName,
                ),
            )
        }.body<UserDTO>()
    }

    /**
     * Updates a user's status.
     *
     * @param id The user ID.
     * @param status The new status.
     * @return Result containing the updated user.
     */
    suspend fun updateUserStatus(id: String, status: String): Result<UserDTO> = Result.runCatching {
        httpClient.patch("$baseUrl/api/users/$id/status") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status))
        }.body<UserDTO>()
    }

    /**
     * Assigns a role to a user.
     *
     * @param id The user ID.
     * @param role The new role.
     * @return Result containing the updated user.
     */
    suspend fun assignRole(id: String, role: String): Result<UserDTO> = Result.runCatching {
        httpClient.patch("$baseUrl/api/users/$id/role") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("role" to role))
        }.body<UserDTO>()
    }

    /**
     * Changes a user's password (requires current password).
     *
     * @param id The user ID.
     * @param currentPassword The current password.
     * @param newPassword The new password.
     * @return Result indicating success or failure.
     */
    suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit> = Result.runCatching {
        httpClient.post("$baseUrl/api/users/$id/change-password") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "current_password" to currentPassword,
                    "new_password" to newPassword,
                ),
            )
        }
        Unit
    }

    /**
     * Resets a user's password (admin operation).
     *
     * @param id The user ID.
     * @param newPassword The new password.
     * @return Result indicating success or failure.
     */
    suspend fun resetPassword(id: String, newPassword: String): Result<Unit> = Result.runCatching {
        httpClient.post("$baseUrl/api/users/$id/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("new_password" to newPassword))
        }
        Unit
    }

    /**
     * Deletes a user.
     *
     * @param id The user ID.
     * @return Result indicating success or failure.
     */
    suspend fun deleteUser(id: String): Result<Unit> = Result.runCatching {
        httpClient.delete("$baseUrl/api/users/$id")
        Unit
    }

    /**
     * Searches for users by query.
     *
     * @param query The search query.
     * @return Result containing list of matching users.
     */
    suspend fun searchUsers(query: String): Result<List<UserDTO>> = Result.runCatching {
        httpClient.get("$baseUrl/api/users/search") {
            parameter("q", query)
        }.body<List<UserDTO>>()
    }
}
