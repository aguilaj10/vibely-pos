package com.vibely.pos.debug

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.valueobject.Email
import com.vibely.pos.shared.util.TimeUtil

/**
 * Provides mock user data for debug mode.
 */
object DebugUser {
    /**
     * Creates a mock admin user for development.
     * @return Mock user with admin privileges
     */
    fun createMockUser(): User = User.create(
        id = "debug-user-123",
        email = Email.create("dev@vibely.pos"),
        fullName = "Debug Developer",
        role = UserRole.ADMIN,
        status = UserStatus.ACTIVE,
        createdAt = TimeUtil.now(),
    )

    /**
     * Creates a mock authentication token for development.
     * @return Mock auth token with long expiration
     */
    fun createMockAuthToken(): AuthToken = AuthToken.create(
        accessToken = "debug-access-token",
        refreshToken = "debug-refresh-token",
        expiresInSeconds = 3600,
    )
}
