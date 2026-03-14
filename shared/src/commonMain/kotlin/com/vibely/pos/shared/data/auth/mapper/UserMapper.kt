package com.vibely.pos.shared.data.auth.mapper

import com.vibely.pos.shared.data.auth.dto.UserDTO
import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.valueobject.Email
import kotlin.time.Instant

/**
 * Mapper for converting between [UserDTO] and [User] domain entity.
 */
object UserMapper {

    /**
     * Maps a [UserDTO] from the backend to a [User] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if role or status cannot be parsed,
     *         or if email is invalid.
     */
    fun toDomain(dto: UserDTO): User {
        val email = Email.create(dto.email)
        val role = UserRole.fromString(dto.role)
            ?: throw IllegalArgumentException("Invalid role: ${dto.role}")
        val status = UserStatus.fromString(dto.status)
            ?: throw IllegalArgumentException("Invalid status: ${dto.status}")
        val createdAt = Instant.parse(dto.createdAt)
        val lastLoginAt = dto.lastLoginAt?.let { Instant.parse(it) }

        return User.create(
            id = dto.id,
            email = email,
            fullName = dto.fullName,
            role = role,
            status = status,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt,
        )
    }

    /**
     * Maps a [User] domain entity to a [UserDTO] for the backend.
     *
     * @param user The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(user: User): UserDTO = UserDTO(
        id = user.id,
        email = user.email.value,
        fullName = user.fullName,
        role = user.role.name,
        status = user.status.name,
        createdAt = user.createdAt.toString(),
        lastLoginAt = user.lastLoginAt?.toString(),
    )
}
