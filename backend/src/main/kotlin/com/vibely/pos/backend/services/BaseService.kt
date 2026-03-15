package com.vibely.pos.backend.services

import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.exceptions.RestException
import kotlinx.serialization.SerializationException

/**
 * Base class for backend services providing common error handling and pagination utilities.
 */
abstract class BaseService {

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
     * Wraps Supabase operations in try-catch for RestException and SerializationException.
     *
     * @param T Return type of the operation
     * @param errorMessage Error message prefix for failures
     * @param block Suspending block to execute
     * @return Result.Success with data, or Result.Error with message and cause
     */
    protected suspend fun <T> executeQuery(
        errorMessage: String,
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: RestException) {
            Result.Error("$errorMessage: ${e.message}", cause = e)
        } catch (e: SerializationException) {
            Result.Error("$errorMessage: ${e.message}", cause = e)
        }
    }
}
