@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "LongParameterList",
    "StringLiteralDuplication",
    "MaxLineLength"
)

package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.dto.request.ChangePasswordRequest
import com.vibely.pos.backend.dto.request.CreateUserRequest
import com.vibely.pos.backend.dto.request.UpdateUserRequest
import com.vibely.pos.shared.data.auth.dto.UserDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val TABLE_USERS = "users"
private const val ERROR_FETCH_FAILED = "Failed to fetch users"
private const val ERROR_NOT_FOUND = "User not found"
private const val ERROR_CREATE_FAILED = "Failed to create user"
private const val ERROR_UPDATE_FAILED = "Failed to update user"
private const val ERROR_DELETE_FAILED = "Failed to delete user"
private const val ERROR_PASSWORD_FAILED = "Failed to change password"
private const val ERROR_INVALID_PASSWORD = "Invalid current password"
private const val ERROR_EMAIL_EXISTS = "Email already exists"

class UserManagementService(
    private val supabaseClient: SupabaseClient,
    private val userRepository: UserRepository,
) : BaseService() {

    suspend fun getAllUsers(
        role: String?,
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<UserDTO>> {
        val (from, to) = calculatePaginationRange(page, pageSize)
        return executeQuery(ERROR_FETCH_FAILED) {
            supabaseClient.from(TABLE_USERS)
                .select {
                    filter {
                        role?.let { eq("role", it) }
                        status?.let { eq(DatabaseColumns.STATUS, it) }
                    }
                    order("full_name", Order.ASCENDING)
                    range(from, to)
                }
                .decodeList<UserDTO>()
        }
    }

    suspend fun getUserById(userId: String): Result<UserDTO> {
        return executeQuery(ERROR_NOT_FOUND) {
            supabaseClient.from(TABLE_USERS)
                .select {
                    filter {
                        eq(DatabaseColumns.ID, userId)
                    }
                }
                .decodeSingle<UserDTO>()
        }
    }

    suspend fun createUser(request: CreateUserRequest): Result<UserDTO> {
        return executeQuery(ERROR_CREATE_FAILED) {
            val existingUser = userRepository.getUserByEmail(request.email)
            if (existingUser != null) {
                throw IllegalStateException(ERROR_EMAIL_EXISTS)
            }

            val passwordHash = userRepository.hashPassword(request.password)

            val data = buildJsonObject {
                put("email", request.email)
                put("full_name", request.fullName)
                put("role", request.role)
                put(DatabaseColumns.STATUS, "active")
                put("password_hash", passwordHash)
            }

            supabaseClient.from(TABLE_USERS)
                .insert(data) { select() }
                .decodeSingle<UserDTO>()
        }
    }

    suspend fun updateUser(userId: String, request: UpdateUserRequest): Result<UserDTO> {
        return executeQuery(ERROR_UPDATE_FAILED) {
            val data = buildJsonObject {
                request.fullName?.let { put("full_name", it) }
                request.role?.let { put("role", it) }
            }

            supabaseClient.from(TABLE_USERS)
                .update(data) {
                    filter { eq(DatabaseColumns.ID, userId) }
                    select()
                }
                .decodeSingle<UserDTO>()
        }
    }

    suspend fun updateUserStatus(userId: String, newStatus: String): Result<UserDTO> {
        return executeQuery(ERROR_UPDATE_FAILED) {
            val data = buildJsonObject {
                put(DatabaseColumns.STATUS, newStatus)
            }

            supabaseClient.from(TABLE_USERS)
                .update(data) {
                    filter { eq(DatabaseColumns.ID, userId) }
                    select()
                }
                .decodeSingle<UserDTO>()
        }
    }

    suspend fun assignRole(userId: String, newRole: String): Result<UserDTO> {
        return executeQuery(ERROR_UPDATE_FAILED) {
            val data = buildJsonObject {
                put("role", newRole)
            }

            supabaseClient.from(TABLE_USERS)
                .update(data) {
                    filter { eq(DatabaseColumns.ID, userId) }
                    select()
                }
                .decodeSingle<UserDTO>()
        }
    }

    suspend fun changePassword(userId: String, request: ChangePasswordRequest): Result<Unit> {
        return executeQuery(ERROR_PASSWORD_FAILED) {
            val user = userRepository.getUserById(userId)
                ?: throw IllegalStateException(ERROR_NOT_FOUND)

            if (!userRepository.verifyPassword(request.currentPassword, user.passwordHash)) {
                throw IllegalStateException(ERROR_INVALID_PASSWORD)
            }

            val newPasswordHash = userRepository.hashPassword(request.newPassword)

            val data = buildJsonObject {
                put("password_hash", newPasswordHash)
            }

            supabaseClient.from(TABLE_USERS)
                .update(data) {
                    filter { eq(DatabaseColumns.ID, userId) }
                }
        }
    }

    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit> {
        return executeQuery(ERROR_PASSWORD_FAILED) {
            val newPasswordHash = userRepository.hashPassword(newPassword)

            val data = buildJsonObject {
                put("password_hash", newPasswordHash)
            }

            supabaseClient.from(TABLE_USERS)
                .update(data) {
                    filter { eq(DatabaseColumns.ID, userId) }
                }
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return executeQuery(ERROR_DELETE_FAILED) {
            supabaseClient.from(TABLE_USERS)
                .delete {
                    filter { eq(DatabaseColumns.ID, userId) }
                }
        }
    }

    suspend fun searchUsers(query: String): Result<List<UserDTO>> {
        return executeQuery(ERROR_FETCH_FAILED) {
            val searchPattern = "%$query%"
            supabaseClient.from(TABLE_USERS)
                .select {
                    filter {
                        or {
                            ilike("full_name", searchPattern)
                            ilike("email", searchPattern)
                        }
                    }
                    order("full_name", Order.ASCENDING)
                }
                .decodeList<UserDTO>()
        }
    }
}
