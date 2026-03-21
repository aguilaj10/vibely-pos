package com.vibely.pos.backend.data.room.datasource

import com.vibely.pos.shared.domain.result.Result
import kotlinx.coroutines.CancellationException

/**
 * Executes a suspending [block] and wraps the result in [Result].
 *
 * [CancellationException] is re-thrown to preserve structured concurrency.
 * All other throwables are captured as [Result.Error]. This is the suspend-aware
 * counterpart to [Result.runCatching] for use with Room DAO calls.
 */
@Suppress("TooGenericExceptionCaught") // Intentional: DAO errors may be any runtime exception
internal suspend inline fun <T> runCatchingSuspend(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error", cause = e)
    }
