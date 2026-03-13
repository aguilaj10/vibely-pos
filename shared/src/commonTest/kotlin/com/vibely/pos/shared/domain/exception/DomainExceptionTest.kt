package com.vibely.pos.shared.domain.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DomainExceptionTest {

    @Test
    fun `ValidationException should contain field and message`() {
        val exception = ValidationException(
            field = "email",
            message = "Invalid email format",
        )
        assertEquals("email", exception.field)
        assertEquals("Invalid email format", exception.message)
        assertEquals("VALIDATION_ERROR", exception.code)
        assertNull(exception.cause)
    }

    @Test
    fun `ValidationException should support custom code`() {
        val exception = ValidationException(
            field = "password",
            message = "Password too short",
            code = "PWD_TOO_SHORT",
        )
        assertEquals("password", exception.field)
        assertEquals("Password too short", exception.message)
        assertEquals("PWD_TOO_SHORT", exception.code)
    }

    @Test
    fun `ValidationException should support cause`() {
        val cause = IllegalArgumentException("Root cause")
        val exception = ValidationException(
            field = "amount",
            message = "Invalid amount",
            cause = cause,
        )
        assertEquals("amount", exception.field)
        assertEquals("Invalid amount", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `ValidationException toString should include all fields`() {
        val exception = ValidationException(
            field = "sku",
            message = "SKU format invalid",
            code = "SKU_INVALID",
        )
        val string = exception.toString()
        assertTrue(string.contains("ValidationException"))
        assertTrue(string.contains("field='sku'"))
        assertTrue(string.contains("message='SKU format invalid'"))
        assertTrue(string.contains("code=SKU_INVALID"))
    }

    @Test
    fun `NotFoundException should contain entity type and identifier`() {
        val exception = NotFoundException(
            entityType = "Product",
            identifier = "PROD-123",
        )
        assertEquals("Product", exception.entityType)
        assertEquals("PROD-123", exception.identifier)
        assertEquals("Product not found with identifier: PROD-123", exception.message)
        assertEquals("NOT_FOUND", exception.code)
    }

    @Test
    fun `NotFoundException should support custom code`() {
        val exception = NotFoundException(
            entityType = "Order",
            identifier = "ORD-999",
            code = "ORDER_NOT_FOUND",
        )
        assertEquals("Order", exception.entityType)
        assertEquals("ORD-999", exception.identifier)
        assertEquals("ORDER_NOT_FOUND", exception.code)
    }

    @Test
    fun `NotFoundException should support cause`() {
        val cause = RuntimeException("Database error")
        val exception = NotFoundException(
            entityType = "Customer",
            identifier = "CUST-456",
            cause = cause,
        )
        assertEquals("Customer", exception.entityType)
        assertEquals("CUST-456", exception.identifier)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NotFoundException toString should include entity and identifier`() {
        val exception = NotFoundException(
            entityType = "Invoice",
            identifier = "INV-777",
        )
        val string = exception.toString()
        assertTrue(string.contains("NotFoundException"))
        assertTrue(string.contains("entityType='Invoice'"))
        assertTrue(string.contains("identifier='INV-777'"))
    }

    @Test
    fun `UnauthorizedException should have default message`() {
        val exception = UnauthorizedException()
        assertEquals("Unauthorized access", exception.message)
        assertEquals("UNAUTHORIZED", exception.code)
    }

    @Test
    fun `UnauthorizedException should support custom message`() {
        val exception = UnauthorizedException("Admin access required")
        assertEquals("Admin access required", exception.message)
        assertEquals("UNAUTHORIZED", exception.code)
    }

    @Test
    fun `UnauthorizedException should support custom code`() {
        val exception = UnauthorizedException(
            message = "Token expired",
            code = "TOKEN_EXPIRED",
        )
        assertEquals("Token expired", exception.message)
        assertEquals("TOKEN_EXPIRED", exception.code)
    }

    @Test
    fun `UnauthorizedException should support cause`() {
        val cause = IllegalStateException("Auth failed")
        val exception = UnauthorizedException(
            message = "Invalid credentials",
            cause = cause,
        )
        assertEquals("Invalid credentials", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `UnauthorizedException toString should include message`() {
        val exception = UnauthorizedException("Invalid token")
        val string = exception.toString()
        assertTrue(string.contains("UnauthorizedException"))
        assertTrue(string.contains("message='Invalid token'"))
    }

    @Test
    fun `BusinessRuleException should contain rule description`() {
        val exception = BusinessRuleException(
            rule = "Cannot delete order with shipped status",
        )
        assertEquals("Cannot delete order with shipped status", exception.rule)
        assertEquals("Business rule violated: Cannot delete order with shipped status", exception.message)
        assertEquals("BUSINESS_RULE_VIOLATION", exception.code)
    }

    @Test
    fun `BusinessRuleException should support custom code`() {
        val exception = BusinessRuleException(
            rule = "Insufficient inventory",
            code = "INSUFFICIENT_INVENTORY",
        )
        assertEquals("Insufficient inventory", exception.rule)
        assertEquals("INSUFFICIENT_INVENTORY", exception.code)
    }

    @Test
    fun `BusinessRuleException should support cause`() {
        val cause = IllegalStateException("Invalid state")
        val exception = BusinessRuleException(
            rule = "Order must have items",
            cause = cause,
        )
        assertEquals("Order must have items", exception.rule)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `BusinessRuleException toString should include rule`() {
        val exception = BusinessRuleException(
            rule = "Price cannot be negative",
        )
        val string = exception.toString()
        assertTrue(string.contains("BusinessRuleException"))
        assertTrue(string.contains("rule='Price cannot be negative'"))
    }

    @Test
    fun `DomainException subclasses should be throwable`() {
        val validation = ValidationException("field", "message")
        assertIs<DomainException>(validation)
        assertIs<Exception>(validation)

        val notFound = NotFoundException("Entity", "id")
        assertIs<DomainException>(notFound)
        assertIs<Exception>(notFound)

        val unauthorized = UnauthorizedException()
        assertIs<DomainException>(unauthorized)
        assertIs<Exception>(unauthorized)

        val businessRule = BusinessRuleException("rule")
        assertIs<DomainException>(businessRule)
        assertIs<Exception>(businessRule)
    }

    @Test
    fun `DomainException should preserve stack trace`() {
        try {
            throw ValidationException("field", "test error")
        } catch (e: DomainException) {
            assertNotNull(e.stackTraceToString())
            assertTrue(e.stackTraceToString().isNotEmpty())
        }
    }

    @Test
    fun `DomainException should support nested causes`() {
        val rootCause = IllegalArgumentException("Root")
        val middleCause = IllegalStateException("Middle", rootCause)
        val exception = ValidationException(
            field = "test",
            message = "Top level",
            cause = middleCause,
        )

        assertEquals(middleCause, exception.cause)
        assertEquals(rootCause, exception.cause?.cause)
    }
}
