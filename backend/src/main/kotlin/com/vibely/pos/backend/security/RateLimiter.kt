package com.vibely.pos.backend.security

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Rate limiting configuration for different endpoint types.
 *
 * @property maxRequests Maximum number of requests allowed
 * @property windowSeconds Time window in seconds
 */
data class RateLimitConfig(
    val maxRequests: Int,
    val windowSeconds: Long
)

/**
 * Tracks request attempts for a specific identifier (IP or user).
 *
 * @property attempts List of timestamps when requests were made
 */
private data class RateLimitEntry(
    val attempts: MutableList<Instant> = mutableListOf()
)

/**
 * In-memory rate limiter that tracks request counts per identifier within time windows.
 *
 * This implementation uses a sliding window algorithm with concurrent hash maps for thread safety.
 * Expired entries are cleaned up periodically to prevent memory leaks.
 */
class RateLimiter {
    private val storage = ConcurrentHashMap<String, RateLimitEntry>()
    private var lastCleanup = Instant.now()
    
    /**
     * Checks if a request should be allowed based on rate limit configuration.
     *
     * @param identifier Unique identifier (IP address, username, or user ID)
     * @param config Rate limit configuration to apply
     * @return Pair of (isAllowed, retryAfterSeconds). If not allowed, retryAfterSeconds indicates when to retry.
     */
    fun isAllowed(identifier: String, config: RateLimitConfig): Pair<Boolean, Long?> {
        cleanupIfNeeded()
        
        val now = Instant.now()
        val windowStart = now.minusSeconds(config.windowSeconds)
        
        val entry = storage.computeIfAbsent(identifier) { RateLimitEntry() }
        
        synchronized(entry) {
            entry.attempts.removeIf { it.isBefore(windowStart) }
            
            if (entry.attempts.size >= config.maxRequests) {
                val oldestAttempt = entry.attempts.minOrNull() ?: now
                val retryAfter = config.windowSeconds - (now.epochSecond - oldestAttempt.epochSecond)
                return false to maxOf(retryAfter, 0)
            }
            
            entry.attempts.add(now)
            return true to null
        }
    }
    
    /**
     * Explicitly clears rate limit data for an identifier.
     * Useful for administrative actions or when a user successfully authenticates.
     *
     * @param identifier Unique identifier to clear
     */
    fun clear(identifier: String) {
        storage.remove(identifier)
    }
    
    /**
     * Gets current attempt count for an identifier within a time window.
     *
     * @param identifier Unique identifier
     * @param config Rate limit configuration
     * @return Number of attempts in the current window
     */
    fun getAttemptCount(identifier: String, config: RateLimitConfig): Int {
        val now = Instant.now()
        val windowStart = now.minusSeconds(config.windowSeconds)
        
        val entry = storage[identifier] ?: return 0
        
        synchronized(entry) {
            entry.attempts.removeIf { it.isBefore(windowStart) }
            return entry.attempts.size
        }
    }
    
    /**
     * Periodically cleans up expired entries to prevent memory leaks.
     * Called automatically during isAllowed() checks.
     */
    private fun cleanupIfNeeded() {
        val now = Instant.now()
        if (now.epochSecond - lastCleanup.epochSecond < CLEANUP_INTERVAL_SECONDS) {
            return
        }
        
        lastCleanup = now
        
        storage.entries.removeIf { (_, entry) ->
            synchronized(entry) {
                entry.attempts.isEmpty()
            }
        }
    }
    
    /**
     * Returns the total number of tracked identifiers (for monitoring/debugging).
     */
    fun size(): Int = storage.size
    
    /**
     * Clears all rate limit data (useful for testing).
     */
    fun clearAll() {
        storage.clear()
    }
    
    /**
     * Predefined rate limit configurations and cleanup settings.
     */
    companion object {
        // Cleanup interval: every 5 minutes
        private const val CLEANUP_INTERVAL_SECONDS = 300L
        
        /**
         * Login rate limit: 5 attempts per 15 minutes.
         */
        val LOGIN_LIMIT = RateLimitConfig(maxRequests = 5, windowSeconds = 900)
        
        /**
         * API rate limit: 100 requests per minute.
         */
        val API_LIMIT = RateLimitConfig(maxRequests = 100, windowSeconds = 60)
    }
}
