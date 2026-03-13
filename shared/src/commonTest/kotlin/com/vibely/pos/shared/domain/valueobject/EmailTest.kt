package com.vibely.pos.shared.domain.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EmailTest {

    @Test
    fun `create should accept valid email`() {
        val email = Email.create("user@example.com")
        assertEquals("user@example.com", email.value)
    }

    @Test
    fun `create should lowercase email`() {
        val email = Email.create("User@Example.COM")
        assertEquals("user@example.com", email.value)
    }

    @Test
    fun `create should trim whitespace`() {
        val email = Email.create("  user@example.com  ")
        assertEquals("user@example.com", email.value)
    }

    @Test
    fun `create should accept email with dots in local part`() {
        val email = Email.create("first.last@example.com")
        assertEquals("first.last@example.com", email.value)
    }

    @Test
    fun `create should accept email with plus in local part`() {
        val email = Email.create("user+tag@example.com")
        assertEquals("user+tag@example.com", email.value)
    }

    @Test
    fun `create should accept email with underscore in local part`() {
        val email = Email.create("user_name@example.com")
        assertEquals("user_name@example.com", email.value)
    }

    @Test
    fun `create should accept email with hyphen in local part`() {
        val email = Email.create("user-name@example.com")
        assertEquals("user-name@example.com", email.value)
    }

    @Test
    fun `create should accept email with numbers in local part`() {
        val email = Email.create("user123@example.com")
        assertEquals("user123@example.com", email.value)
    }

    @Test
    fun `create should accept email with subdomain`() {
        val email = Email.create("user@mail.example.com")
        assertEquals("user@mail.example.com", email.value)
    }

    @Test
    fun `create should accept email with multiple subdomains`() {
        val email = Email.create("user@dept.mail.example.com")
        assertEquals("user@dept.mail.example.com", email.value)
    }

    @Test
    fun `create should accept email with short TLD`() {
        val email = Email.create("user@example.co")
        assertEquals("user@example.co", email.value)
    }

    @Test
    fun `create should accept email with long TLD`() {
        val email = Email.create("user@example.technology")
        assertEquals("user@example.technology", email.value)
    }

    @Test
    fun `create should reject blank email`() {
        assertFailsWith<ValidationException> {
            Email.create("")
        }
        assertFailsWith<ValidationException> {
            Email.create("   ")
        }
    }

    @Test
    fun `create should reject email without at sign`() {
        val exception = assertFailsWith<ValidationException> {
            Email.create("userexample.com")
        }
        assertEquals("email", exception.field)
    }

    @Test
    fun `create should reject email without domain`() {
        assertFailsWith<ValidationException> {
            Email.create("user@")
        }
    }

    @Test
    fun `create should reject email without local part`() {
        assertFailsWith<ValidationException> {
            Email.create("@example.com")
        }
    }

    @Test
    fun `create should reject email without TLD`() {
        assertFailsWith<ValidationException> {
            Email.create("user@example")
        }
    }

    @Test
    fun `create should reject email with multiple at signs`() {
        assertFailsWith<ValidationException> {
            Email.create("user@domain@example.com")
        }
    }

    @Test
    fun `create should reject email with spaces`() {
        assertFailsWith<ValidationException> {
            Email.create("user name@example.com")
        }
        assertFailsWith<ValidationException> {
            Email.create("user@example domain.com")
        }
    }

    @Test
    fun `create should reject email too long`() {
        val longLocal = "a".repeat(250)
        val exception = assertFailsWith<ValidationException> {
            Email.create("$longLocal@example.com")
        }
        assertEquals("email", exception.field)
        assertTrue(exception.message.contains("at most 254 characters"))
    }

    @Test
    fun `create should reject email with invalid characters in local part`() {
        assertFailsWith<ValidationException> {
            Email.create("user#name@example.com")
        }
        assertFailsWith<ValidationException> {
            Email.create("user@name@example.com")
        }
    }

    @Test
    fun `create should reject email with invalid characters in domain`() {
        assertFailsWith<ValidationException> {
            Email.create("user@exam ple.com")
        }
        assertFailsWith<ValidationException> {
            Email.create("user@exam_ple.com")
        }
    }

    @Test
    fun `create should reject email with single character TLD`() {
        assertFailsWith<ValidationException> {
            Email.create("user@example.c")
        }
    }

    @Test
    fun `localPart should return correct value`() {
        val email = Email.create("user.name@example.com")
        assertEquals("user.name", email.localPart)
    }

    @Test
    fun `domain should return correct value`() {
        val email = Email.create("user@mail.example.com")
        assertEquals("mail.example.com", email.domain)
    }

    @Test
    fun `toString should return email value`() {
        val email = Email.create("test@example.com")
        assertEquals("test@example.com", email.toString())
    }

    @Test
    fun `equality should work correctly`() {
        val email1 = Email.create("User@Example.com")
        val email2 = Email.create("user@example.com")
        val email3 = Email.create("other@example.com")

        assertEquals(email1, email2) // Case-insensitive
        assertTrue(email1 != email3)
    }

    @Test
    fun `hashCode should work correctly`() {
        val email1 = Email.create("User@Example.com")
        val email2 = Email.create("user@example.com")

        assertEquals(email1.hashCode(), email2.hashCode())
    }

    @Test
    fun `validation exception should include helpful message`() {
        val exception = assertFailsWith<ValidationException> {
            Email.create("invalid-email")
        }
        assertTrue(exception.message.contains("Invalid email format"))
        assertEquals("email", exception.field)
    }

    @Test
    fun `create should accept email with numbers in domain`() {
        val email = Email.create("user@example123.com")
        assertEquals("user@example123.com", email.value)
    }

    @Test
    fun `create should accept email with hyphens in domain`() {
        val email = Email.create("user@my-domain.com")
        assertEquals("user@my-domain.com", email.value)
    }

    // Note: Basic email regex allows these patterns - RFC 5322 is complex
    // and these are edge cases. In production, consider additional validation.

    @Test
    fun `create should accept common email patterns`() {
        val validEmails = listOf(
            "simple@example.com",
            "very.common@example.com",
            "disposable.style.email.with+symbol@example.com",
            "other.email-with-hyphen@example.com",
            "x@example.com",
            "example@s.example",
            "user@example.co.uk",
        )

        validEmails.forEach { emailString ->
            val email = Email.create(emailString)
            assertEquals(emailString.lowercase(), email.value)
        }
    }
}
