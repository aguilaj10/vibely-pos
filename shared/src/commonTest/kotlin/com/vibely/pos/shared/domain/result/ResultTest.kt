package com.vibely.pos.shared.domain.result

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun `Success should contain data`() {
        val result = Result.Success(42)
        assertEquals(42, result.data)
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
    }

    @Test
    fun `Error should contain message`() {
        val result = Result.Error("Something went wrong")
        assertEquals("Something went wrong", result.message)
        assertTrue(result.isError)
        assertFalse(result.isSuccess)
    }

    @Test
    fun `Error should contain optional code and cause`() {
        val cause = RuntimeException("Root cause")
        val result = Result.Error("Error occurred", "ERR_001", cause)
        assertEquals("Error occurred", result.message)
        assertEquals("ERR_001", result.code)
        assertEquals(cause, result.cause)
    }

    @Test
    fun `getOrNull should return data for Success`() {
        val result = Result.Success("test")
        assertEquals("test", result.getOrNull())
    }

    @Test
    fun `getOrNull should return null for Error`() {
        val result: Result<String> = Result.Error("error")
        assertNull(result.getOrNull())
    }

    @Test
    fun `getOrDefault should return data for Success`() {
        val result = Result.Success(100)
        assertEquals(100, result.getOrDefault(0))
    }

    @Test
    fun `getOrDefault should return default for Error`() {
        val result: Result<Int> = Result.Error("error")
        assertEquals(0, result.getOrDefault(0))
    }

    @Test
    fun `getOrElse should return data for Success`() {
        val result = Result.Success(50)
        assertEquals(50, result.getOrElse { 0 })
    }

    @Test
    fun `getOrElse should invoke function for Error`() {
        val result: Result<Int> = Result.Error("error", "ERR_001")
        val value = result.getOrElse { error ->
            assertEquals("error", error.message)
            assertEquals("ERR_001", error.code)
            999
        }
        assertEquals(999, value)
    }

    @Test
    fun `runCatching should return Success for successful block`() {
        val result = Result.runCatching {
            2 + 2
        }
        assertIs<Result.Success<Int>>(result)
        assertEquals(4, result.data)
    }

    @Test
    fun `runCatching should return Error for throwing block`() {
        val result = Result.runCatching {
            throw IllegalArgumentException("Test exception")
        }
        assertIs<Result.Error>(result)
        assertEquals("Test exception", result.message)
        assertNotNull(result.cause)
    }

    @Test
    fun `map should transform Success data`() {
        val result: Result<Int> = Result.Success(5)
        val mapped = result.map { it * 2 }
        assertIs<Result.Success<Int>>(mapped)
        assertEquals(10, mapped.data)
    }

    @Test
    fun `map should pass through Error unchanged`() {
        val result: Result<Int> = Result.Error("error")
        val mapped = result.map { it * 2 }
        assertIs<Result.Error>(mapped)
        assertEquals("error", mapped.message)
    }

    @Test
    fun `flatMap should chain Success operations`() {
        val result: Result<Int> = Result.Success(5)
        val flatMapped = result.flatMap { value ->
            Result.Success(value * 2)
        }
        assertIs<Result.Success<Int>>(flatMapped)
        assertEquals(10, flatMapped.data)
    }

    @Test
    fun `flatMap should short-circuit on Error in original`() {
        val result: Result<Int> = Result.Error("original error")
        val flatMapped = result.flatMap { value ->
            Result.Success(value * 2)
        }
        assertIs<Result.Error>(flatMapped)
        assertEquals("original error", flatMapped.message)
    }

    @Test
    fun `flatMap should propagate Error from transform`() {
        val result: Result<Int> = Result.Success(5)
        val flatMapped = result.flatMap { _ ->
            Result.Error("transform error")
        }
        assertIs<Result.Error>(flatMapped)
        assertEquals("transform error", flatMapped.message)
    }

    @Test
    fun `onSuccess should execute action for Success`() {
        var executed = false
        val result = Result.Success(42)
        val returned = result.onSuccess { value ->
            executed = true
            assertEquals(42, value)
        }
        assertTrue(executed)
        assertEquals(result, returned) // Should return same result
    }

    @Test
    fun `onSuccess should not execute action for Error`() {
        var executed = false
        val result: Result<Int> = Result.Error("error")
        val returned = result.onSuccess {
            executed = true
        }
        assertFalse(executed)
        assertEquals(result, returned)
    }

    @Test
    fun `onError should execute action for Error`() {
        var executed = false
        val result: Result<Int> = Result.Error("test error", "CODE")
        val returned = result.onError { error ->
            executed = true
            assertEquals("test error", error.message)
            assertEquals("CODE", error.code)
        }
        assertTrue(executed)
        assertEquals(result, returned)
    }

    @Test
    fun `onError should not execute action for Success`() {
        var executed = false
        val result = Result.Success(42)
        val returned = result.onError {
            executed = true
        }
        assertFalse(executed)
        assertEquals(result, returned)
    }

    @Test
    fun `recover should transform Error to Success`() {
        val result: Result<Int> = Result.Error("error")
        val recovered = result.recover { 999 }
        assertIs<Result.Success<Int>>(recovered)
        assertEquals(999, recovered.data)
    }

    @Test
    fun `recover should pass through Success unchanged`() {
        val result = Result.Success(42)
        val recovered = result.recover { 999 }
        assertIs<Result.Success<Int>>(recovered)
        assertEquals(42, recovered.data)
    }

    @Test
    fun `mapError should transform Error`() {
        val result: Result<Int> = Result.Error("original", "CODE1")
        val mapped = result.mapError { error ->
            Result.Error("${error.message} - transformed", "CODE2", error.cause)
        }
        assertIs<Result.Error>(mapped)
        assertEquals("original - transformed", mapped.message)
        assertEquals("CODE2", mapped.code)
    }

    @Test
    fun `mapError should pass through Success unchanged`() {
        val result = Result.Success(42)
        val mapped = result.mapError {
            Result.Error("should not be called")
        }
        assertIs<Result.Success<Int>>(mapped)
        assertEquals(42, mapped.data)
    }

    @Test
    fun `chaining operations should work correctly`() {
        val result: Result<Int> = Result.Success(10)
            .map { it * 2 } // 20
            .flatMap { Result.Success(it + 5) } // 25
            .onSuccess { value -> assertEquals(25, value) }
            .map { it / 5 } // 5

        assertIs<Result.Success<Int>>(result)
        assertEquals(5, result.data)
    }

    @Test
    fun `chaining with error should short-circuit`() {
        var step1 = false
        var step2 = false
        var step3 = false

        val result =
            Result.Success(10)
                .onSuccess { step1 = true }
                .flatMap { _: Int -> Result.Error("error in middle") }
                .onSuccess { step2 = true } // Should not execute
                .onError { step3 = true }

        assertTrue(step1)
        assertFalse(step2)
        assertTrue(step3)
        assertIs<Result.Error>(result)
    }
}
