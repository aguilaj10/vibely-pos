@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "LongParameterList",
    "StringLiteralDuplication",
    "MaxLineLength"
)

package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.backend.common.ErrorMessages
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

private const val ERROR_FETCH_FAILED = "Failed to fetch users"
private const val ERROR_CREATE_FAILED = "Failed to create user"
private const val ERROR_UPDATE_FAILED = "Failed to update user"
private const val ERROR_DELETE_FAILED = "Failed to delete user"
private const val ERROR_PASSWORD_FAILED = "Failed to change password"

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
            supabaseClient.from(TableNames.USERS)
                .select {
                    filter {
                        role?.let { eq(DatabaseColumns.ROLE, it) }
                        status?.let { eq(DatabaseColumns.STATUS, it) }
                    }
                    order(DatabaseColumns.FULL_NAME, Order.ASCENDING)
                    range(from, to)
                }
                .decodeList<UserDTO>()
        }
    }

    suspend fun getUserById(userId: String): Result<UserDTO> {
        return executeQuery(ErrorMessages.USER_NOT_FOUND) {
            supabaseClient.from(TableNames.USERS)
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
                throw IllegalStateException(ErrorMessages.EMAIL_EXISTS)
            }

            val passwordHash = userRepository.hashPassword(request.password)

            val data = buildJsonObject {
                put(DatabaseColumns.EMAIL, request.email)
                put(DatabaseColumns.FULL_NAME, request.fullName)
                put(DatabaseColumns.ROLE, request.role)
                put(DatabaseColumns.STATUS, "active")
                put(DatabaseColumns.PASSWORD_HASH, passwordHash)
            }

            supabaseClient.from(TableNames.USERS)
                .insert(data) { select() }
                .decodeSingle<UserDTO>()
        }
    }

    suspend fun updateUser(userId: String, request: UpdateUserRequest): Result<UserDTO> {
        return executeQuery(ERROR_UPDATE_FAILED) {
            val data = buildJsonObject {
                request.fullName?.let { put(DatabaseColumns.FULL_NAME, it) }
                request.role?.let { put(DatabaseColumns.ROLE, it) }
            }

            supabaseClient.from(TableNames.USERS)
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

            supabaseClient.from(TableNames.USERS)
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
                put(DatabaseColumns.ROLE, newRole)
            }

            supabaseClient.from(TableNames.USERS)
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
                ?: throw IllegalStateException(ErrorMessages.USER_NOT_FOUND)

            if (!userRepository.verifyPassword(request.currentPassword, user.passwordHash)) {
                throw IllegalStateException(ErrorMessages.INVALID_PASSWORD)
            }

            val newPasswordHash = userRepository.hashPassword(request.newPassword)

            val data = buildJsonObject {
                put(DatabaseColumns.PASSWORD_HASH, newPasswordHash)
            }

            supabaseClient.from(TableNames.USERS)
                .update(data) {
                    filter { eq(DatabaseColumns.ID, userId) }
                }
        }
    }

    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit> {
        return executeQuery(ERROR_PASSWORD_FAILED) {
            val newPasswordHash = userRepository.hashPassword(newPassword)

            val data = buildJsonObject {
                put(DatabaseColumns.PASSWORD_HASH, newPasswordHash)
            }

            supabaseClient.from(TableNames.USERS)
                .update(data) {
                    filter { eq(DatabaseColumns.ID, userId) }
                }
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return executeQuery(ERROR_DELETE_FAILED) {
            supabaseClient.from(TableNames.USERS)
                .delete {
                    filter { eq(DatabaseColumns.ID, userId) }
                }
        }
    }

    suspend fun searchUsers(query: String): Result<List<UserDTO>> {
        return executeQuery(ERROR_FETCH_FAILED) {
            val searchPattern = "%$query%"
            supabaseClient.from(TableNames.USERS)
                .select {
                    filter {
                        or {
                            ilike(DatabaseColumns.FULL_NAME, searchPattern)
                            ilike(DatabaseColumns.EMAIL, searchPattern)
                        }
                    }
                    order(DatabaseColumns.FULL_NAME, Order.ASCENDING)
                }
                .decodeList<UserDTO>()
        }
    }
}
