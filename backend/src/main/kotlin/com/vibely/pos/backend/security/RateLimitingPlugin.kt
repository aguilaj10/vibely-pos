package com.vibely.pos.backend.security

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

@Suppress("LabeledExpression")
private const val ERROR_KEY = "error"
private const val RETRY_AFTER_HEADER = "Retry-After"
private const val X_RATELIMIT_LIMIT = "X-RateLimit-Limit"
private const val X_RATELIMIT_REMAINING = "X-RateLimit-Remaining"
private const val X_RATELIMIT_RESET = "X-RateLimit-Reset"
private const val ZERO_REMAINING = "0"
private const val SECONDS_PER_MILLISECOND = 1000L

/**
 * Configuration for the RateLimitingPlugin.
 *
 * @property rateLimiter The rate limiter instance to use
 * @property skipPaths List of paths that should not be rate limited
 */
data class RateLimitingConfig(
    val rateLimiter: RateLimiter = RateLimiter(),
    val skipPaths: List<String> = listOf("/health", "/", "/api/test/database")
)

/**
 * Ktor plugin that applies rate limiting to incoming requests.
 *
 * Rate limits are applied per IP address or authenticated user ID.
 * Different limits are used for login endpoints vs general API endpoints:
 * - Login: 5 attempts per 15 minutes
 * - API: 100 requests per minute
 *
 * When rate limit is exceeded, returns HTTP 429 with Retry-After header.
 * All responses include X-RateLimit-* headers for client awareness.
 */
val RateLimitingPlugin = createRouteScopedPlugin(
    name = "RateLimitingPlugin",
    createConfiguration = ::RateLimitingConfig
) {
    val rateLimiter = pluginConfig.rateLimiter
    val skipPaths = pluginConfig.skipPaths
    val logger = LoggerFactory.getLogger("RateLimiting")
    
    onCall { call ->
        val path = call.request.path()
        
        if (skipPaths.any { path.startsWith(it) }) {
            return@onCall
        }
        
        val identifier = call.getRateLimitIdentifier()
        val config = call.getRateLimitConfig()
        
        val (isAllowed, retryAfter) = rateLimiter.isAllowed(identifier, config)
        
        if (!isAllowed) {
            logger.warn("Rate limit exceeded for $identifier on $path")
            
            call.response.header(RETRY_AFTER_HEADER, retryAfter.toString())
            call.response.header(X_RATELIMIT_LIMIT, config.maxRequests.toString())
            call.response.header(X_RATELIMIT_REMAINING, ZERO_REMAINING)
            
            val resetTime = System.currentTimeMillis() / SECONDS_PER_MILLISECOND + (retryAfter ?: 0)
            call.response.header(X_RATELIMIT_RESET, resetTime.toString())
            
            call.respond(
                HttpStatusCode.TooManyRequests,
                mapOf(
                    ERROR_KEY to "Rate limit exceeded. Please try again later.",
                    RETRY_AFTER_HEADER to retryAfter
                )
            )
            return@onCall
        }
        
        val remaining = config.maxRequests - rateLimiter.getAttemptCount(identifier, config)
        call.response.header(X_RATELIMIT_LIMIT, config.maxRequests.toString())
        call.response.header(X_RATELIMIT_REMAINING, remaining.toString())
    }
}

/**
 * Determines the unique identifier for rate limiting.
 * Uses authenticated user ID if available, otherwise falls back to IP address.
 */
private fun ApplicationCall.getRateLimitIdentifier(): String {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("userId")
        ?.asString()
    
    return userId ?: request.local.remoteAddress
}

/**
 * Determines which rate limit configuration to use based on the request path.
 */
private fun ApplicationCall.getRateLimitConfig(): RateLimitConfig {
    val path = request.path()
    
    return when {
        path.contains("/auth/login") -> RateLimiter.LOGIN_LIMIT
        else -> RateLimiter.API_LIMIT
    }
}
