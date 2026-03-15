package com.vibely.pos.shared.data.user.repository

import com.vibely.pos.shared.data.auth.mapper.UserMapper
import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.user.datasource.RemoteUserDataSource
import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.repository.UserRepository

class UserRepositoryImpl(private val remoteDataSource: RemoteUserDataSource) :
    BaseRepository(),
    UserRepository {

    override suspend fun getAll(role: UserRole?, status: UserStatus?, page: Int, pageSize: Int): Result<List<User>> = mapList(
        remoteDataSource.getAllUsers(
            role = role?.name,
            status = status?.name,
            page = page,
            pageSize = pageSize,
        ),
        UserMapper::toDomain,
    )

    override suspend fun getById(id: String): Result<User> = mapSingle(
        remoteDataSource.getUserById(id),
        UserMapper::toDomain,
    )

    override suspend fun getByEmail(email: String): Result<User> = mapSingle(
        remoteDataSource.getUserByEmail(email),
        UserMapper::toDomain,
    )

    override suspend fun create(email: String, password: String, fullName: String, role: UserRole): Result<User> = mapSingle(
        remoteDataSource.createUser(
            email = email,
            password = password,
            fullName = fullName,
            role = role.name,
        ),
        UserMapper::toDomain,
    )

    override suspend fun update(user: User): Result<User> = mapSingle(
        remoteDataSource.updateUser(
            id = user.id,
            email = user.email.value,
            fullName = user.fullName,
        ),
        UserMapper::toDomain,
    )

    override suspend fun updateStatus(id: String, status: UserStatus): Result<User> = mapSingle(
        remoteDataSource.updateUserStatus(id, status.name),
        UserMapper::toDomain,
    )

    override suspend fun assignRole(id: String, role: UserRole): Result<User> = mapSingle(
        remoteDataSource.assignRole(id, role.name),
        UserMapper::toDomain,
    )

    override suspend fun changePassword(id: String, currentPassword: String, newPassword: String): Result<Unit> =
        remoteDataSource.changePassword(id, currentPassword, newPassword)

    override suspend fun resetPassword(id: String, newPassword: String): Result<Unit> = remoteDataSource.resetPassword(id, newPassword)

    override suspend fun delete(id: String): Result<Unit> = remoteDataSource.deleteUser(id)

    override suspend fun search(query: String): Result<List<User>> = mapList(
        remoteDataSource.searchUsers(query),
        UserMapper::toDomain,
    )
}
