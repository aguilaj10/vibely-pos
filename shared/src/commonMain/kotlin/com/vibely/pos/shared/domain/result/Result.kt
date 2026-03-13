package com.vibely.pos.shared.domain.result

/**
 * A sealed class representing the outcome of an operation that can either succeed or fail.
 *
 * Provides a type-safe alternative to throwing exceptions for expected failures,
 * following the Railway-Oriented Programming pattern.
 *
 * @param T The type of the value contained in a successful result.
 */
sealed class Result<out T> {

    /**
     * Represents a successful outcome containing the [data].
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failed outcome with an error [message] and optional error [code].
     */
    data class Error(val message: String, val code: String? = null, val cause: Throwable? = null) : Result<Nothing>()

    /**
     * Returns `true` if this is a [Success], `false` otherwise.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns `true` if this is an [Error], `false` otherwise.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns the encapsulated value if this is a [Success], or `null` otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the encapsulated value if this is a [Success],
     * or the [defaultValue] otherwise.
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue
    }

    /**
     * Returns the encapsulated value if this is a [Success],
     * or the result of calling [defaultValue] otherwise.
     */
    inline fun getOrElse(defaultValue: (Error) -> @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue(this)
    }

    companion object {
        /**
         * Wraps the execution of [block] in a [Result], catching any thrown exceptions.
         */
        inline fun <T> runCatching(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: Exception) {
            Error(
                message = e.message ?: "Unknown error",
                cause = e,
            )
        }
    }
}

/**
 * Transforms the encapsulated value of a [Result.Success] using [transform].
 * If this is a [Result.Error], it is returned unchanged.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

/**
 * Transforms the encapsulated value of a [Result.Success] using [transform],
 * which itself returns a [Result]. Useful for chaining operations that may fail.
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
    is Result.Success -> transform(data)
    is Result.Error -> this
}

/**
 * Executes [action] if this is a [Result.Success]. Returns the original result unchanged.
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Executes [action] if this is a [Result.Error]. Returns the original result unchanged.
 */
inline fun <T> Result<T>.onError(action: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(this)
    }
    return this
}

/**
 * Transforms a [Result.Error] into a [Result.Success] using [transform].
 * If this is already a [Result.Success], it is returned unchanged.
 */
inline fun <T> Result<T>.recover(transform: (Result.Error) -> T): Result<T> = when (this) {
    is Result.Success -> this
    is Result.Error -> Result.Success(transform(this))
}

/**
 * Maps the error of a [Result.Error] to a different error.
 * If this is a [Result.Success], it is returned unchanged.
 */
inline fun <T> Result<T>.mapError(transform: (Result.Error) -> Result.Error): Result<T> = when (this) {
    is Result.Success -> this
    is Result.Error -> transform(this)
}
