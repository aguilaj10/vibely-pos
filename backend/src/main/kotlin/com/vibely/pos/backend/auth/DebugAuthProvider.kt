package com.vibely.pos.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.routing.Route

private const val DEBUG_BEARER_NAME = "debug-bearer"
private const val DEBUG_TOKEN = "debug-access-token"
private const val DEBUG_USER_ID = "a2259bb8-d02d-4384-bf2f-bbfca16bade5"
private const val DEBUG_EMAIL = "dev@vibely.pos"
private const val DEBUG_ROLE = "admin"
private const val CLAIM_USER_ID = "userId"
private const val CLAIM_EMAIL = "email"
private const val CLAIM_ROLE = "role"

/** Accepts [DEBUG_BEARER_NAME] tokens in addition to JWT; registers the debug bearer scheme. */
class DebugAuthProvider : RouteAuthProvider {
    override fun AuthenticationConfig.configure() {
        println("WARNING: DEBUG MODE ENABLED ON BACKEND - Accepting $DEBUG_TOKEN via $DEBUG_BEARER_NAME auth")
        bearer(DEBUG_BEARER_NAME) {
            authenticate { tokenCredential ->
                if (tokenCredential.token == DEBUG_TOKEN) {
                    val debugPayload = JWT.create()
                        .withClaim(CLAIM_USER_ID, DEBUG_USER_ID)
                        .withClaim(CLAIM_EMAIL, DEBUG_EMAIL)
                        .withClaim(CLAIM_ROLE, DEBUG_ROLE)
                        .sign(Algorithm.none())
                    JWTPrincipal(JWT.decode(debugPayload))
                } else {
                    null
                }
            }
        }
    }

    override fun Route.withAuth(build: Route.() -> Unit) {
        authenticate("auth-jwt", DEBUG_BEARER_NAME) { build() }
    }
}
