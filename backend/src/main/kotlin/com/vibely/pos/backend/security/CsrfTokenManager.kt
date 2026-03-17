package com.vibely.pos.backend.security

import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * CSRF token entry with expiration tracking.
 *
 * @property token The CSRF token value
 * @property expiresAt When the token expires
 */
private data class CsrfTokenEntry(
    val token: String,
    val expiresAt: Instant
)

/**
 * Manages CSRF token generation, validation, and lifecycle.
 *
 * Tokens are cryptographically secure random values stored per user session.
 * Expired tokens are cleaned up periodically to prevent memory leaks.
 */
class CsrfTokenManager {
    private val tokens = ConcurrentHashMap<String, CsrfTokenEntry>()
    private val secureRandom = SecureRandom()
    private var lastCleanup = Instant.now()
    
    /**
     * Generates a new CSRF token for a user session.
     *
     * @param userId Unique user identifier
     * @return The generated CSRF token string
     */
    fun generateToken(userId: String): String {
        cleanupIfNeeded()
        
        val tokenBytes = ByteArray(TOKEN_BYTE_SIZE)
        secureRandom.nextBytes(tokenBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)
        
        val expiresAt = Instant.now().plusSeconds(TOKEN_VALIDITY_SECONDS)
        tokens[userId] = CsrfTokenEntry(token, expiresAt)
        
        return token
    }
    
    /**
     * Validates a CSRF token for a user session.
     *
     * @param userId Unique user identifier
     * @param token Token to validate
     * @return true if token is valid and not expired, false otherwise
     */
    fun validateToken(userId: String, token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        
        val entry = tokens[userId] ?: return false
        val now = Instant.now()
        
        if (now.isAfter(entry.expiresAt)) {
            tokens.remove(userId)
            return false
        }
        
        return entry.token == token
    }
    
    /**
     * Gets the current token for a user without generating a new one.
     *
     * @param userId Unique user identifier
     * @return The current token if exists and valid, null otherwise
     */
    fun getToken(userId: String): String? {
        val entry = tokens[userId] ?: return null
        
        if (Instant.now().isAfter(entry.expiresAt)) {
            tokens.remove(userId)
            return null
        }
        
        return entry.token
    }
    
    /**
     * Invalidates the CSRF token for a user (useful on logout).
     *
     * @param userId Unique user identifier
     */
    fun invalidateToken(userId: String) {
        tokens.remove(userId)
    }
    
    /**
     * Periodically cleans up expired tokens.
     */
    private fun cleanupIfNeeded() {
        val now = Instant.now()
        if (now.epochSecond - lastCleanup.epochSecond < CLEANUP_INTERVAL_SECONDS) {
            return
        }
        
        lastCleanup = now
        tokens.entries.removeIf { (_, entry) -> now.isAfter(entry.expiresAt) }
    }
    
    /**
     * Returns the number of active tokens (for monitoring).
     */
    fun size(): Int = tokens.size
    
    /**
     * Clears all tokens (useful for testing).
     */
    fun clearAll() {
        tokens.clear()
    }
    
    /**
     * CSRF token configuration constants.
     */
    companion object {
        private const val TOKEN_BYTE_SIZE = 32
        private const val TOKEN_VALIDITY_SECONDS = 3600L
        private const val CLEANUP_INTERVAL_SECONDS = 300L
    }
}
