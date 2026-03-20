package com.vibely.pos.backend.auth

import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route

/** Uses only the standard JWT scheme. No extra setup required. */
class ProdAuthProvider : RouteAuthProvider {
    override fun AuthenticationConfig.configure() = Unit

    override fun Route.withAuth(build: Route.() -> Unit) {
        authenticate("auth-jwt") { build() }
    }
}
