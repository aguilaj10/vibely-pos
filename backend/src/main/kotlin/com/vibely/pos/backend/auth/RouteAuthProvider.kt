package com.vibely.pos.backend.auth

import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.routing.Route

/**
 * Provides environment-specific authentication wiring for routes and the auth plugin.
 *
 * Implement [configure] to register extra authentication schemes during plugin setup,
 * and [withAuth] to wrap route blocks with the appropriate authenticate() call.
 */
interface RouteAuthProvider {
    /** Called inside [install(Authentication)] to register additional schemes. */
    fun AuthenticationConfig.configure()

    /** Wraps [build] with the correct authenticate() schemes for this environment. */
    fun Route.withAuth(build: Route.() -> Unit)
}
