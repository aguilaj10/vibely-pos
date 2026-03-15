package com.vibely.pos.backend.services

import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

/**
 * Base class for backend services providing common error handling and pagination utilities.
 */
abstract class BaseService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Calculates pagination range for Supabase queries.
     *
     * @param page Page number (1-indexed)
     * @param pageSize Number of items per page
     * @return Pair of (from, to) indices for range query
     */
    protected fun calculatePaginationRange(page: Int, pageSize: Int): Pair<Long, Long> {
        val from = ((page - 1) * pageSize).toLong()
        val to = (page * pageSize - 1).toLong()
        return Pair(from, to)
    }

    /**
     * Executes a database operation with standardized error handling.
     *
     * Wraps Supabase operations in try-catch for HttpRequestException, RestException and SerializationException.
     *
     * @param T Return type of the operation
     * @param errorMessage Error message prefix for failures
     * @param block Suspending block to execute
     * @return Result.Success with data, or Result.Error with message and cause
     */
    @Suppress("StringLiteralDuplication")
    protected suspend fun <T> executeQuery(
        errorMessage: String,
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: HttpRequestException) {
            logger.error("$errorMessage: ${e.message}", e)
            Result.Error("$errorMessage: ${e.message}", cause = e)
        } catch (e: RestException) {
            logger.error("$errorMessage: ${e.message}", e)
            Result.Error("$errorMessage: ${e.message}", cause = e)
        } catch (e: SerializationException) {
            logger.error("$errorMessage: ${e.message}", e)
            Result.Error("$errorMessage: ${e.message}", cause = e)
        }
    }
}
