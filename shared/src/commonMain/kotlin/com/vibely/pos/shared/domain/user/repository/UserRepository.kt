package com.vibely.pos.shared.domain.user.repository

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result

interface UserRepository {

    suspend fun getAll(role: UserRole? = null, status: UserStatus? = null, page: Int = 1, pageSize: Int = 50): Result<List<User>>

    suspend fun getById(id: String): Result<User>

    suspend fun getByEmail(email: String): Result<User>

    suspend fun create(email: String, password: String, fullName: String, role: UserRole): Result<User>

    suspend fun update(user: User): Result<User>

    suspend fun updateStatus(id: String, status: UserStatus): Result<User>

    suspend fun assignRole(id: String, role: UserRole): Result<User>

    suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit>

    suspend fun resetPassword(id: String, newPassword: String): Result<Unit>

    suspend fun delete(id: String): Result<Unit>

    suspend fun search(query: String): Result<List<User>>
}
