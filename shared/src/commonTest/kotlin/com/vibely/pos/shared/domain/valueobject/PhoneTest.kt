package com.vibely.pos.shared.domain.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneTest {

    @Test
    fun `create should accept valid phone number`() {
        val phone = Phone.create("1234567890")
        assertEquals("1234567890", phone.value)
    }

    @Test
    fun `create should trim whitespace`() {
        val phone = Phone.create("  1234567890  ")
        assertEquals("1234567890", phone.value)
    }

    @Test
    fun `create should normalize phone with spaces`() {
        val phone = Phone.create("123 456 7890")
        assertEquals("1234567890", phone.value)
    }

    @Test
    fun `create should normalize phone with hyphens`() {
        val phone = Phone.create("123-456-7890")
        assertEquals("1234567890", phone.value)
    }

    @Test
    fun `create should normalize phone with parentheses`() {
        val phone = Phone.create("(123) 456-7890")
        assertEquals("1234567890", phone.value)
    }

    @Test
    fun `create should normalize phone with dots`() {
        val phone = Phone.create("123.456.7890")
        assertEquals("1234567890", phone.value)
    }

    @Test
    fun `create should accept international format with plus`() {
        val phone = Phone.create("+1234567890")
        assertEquals("+1234567890", phone.value)
        assertTrue(phone.isInternational)
    }

    @Test
    fun `create should normalize international format with spaces`() {
        val phone = Phone.create("+1 234 567 8900")
        assertEquals("+12345678900", phone.value)
        assertTrue(phone.isInternational)
    }

    @Test
    fun `create should normalize international format with formatting`() {
        val phone = Phone.create("+52 (55) 1234-5678")
        assertEquals("+525512345678", phone.value)
        assertTrue(phone.isInternational)
    }

    @Test
    fun `create should accept 7 digit phone`() {
        val phone = Phone.create("5551234")
        assertEquals("5551234", phone.value)
    }

    @Test
    fun `create should accept 15 digit phone`() {
        val phone = Phone.create("123456789012345")
        assertEquals("123456789012345", phone.value)
    }

    @Test
    fun `create should reject blank phone`() {
        assertFailsWith<ValidationException> {
            Phone.create("")
        }
        assertFailsWith<ValidationException> {
            Phone.create("   ")
        }
    }

    @Test
    fun `create should reject phone with less than 7 digits`() {
        val exception = assertFailsWith<ValidationException> {
            Phone.create("123456")
        }
        assertEquals("phone", exception.field)
        assertTrue(exception.message.contains("at least 7 digits"))
    }

    @Test
    fun `create should reject phone with more than 15 digits`() {
        val exception = assertFailsWith<ValidationException> {
            Phone.create("1234567890123456")
        }
        assertEquals("phone", exception.field)
        assertTrue(exception.message.contains("at most 15 digits"))
    }

    // Note: The current implementation normalizes by extracting only digits after the optional leading '+'
    // More complex validation would be needed to catch all edge cases

    @Test
    fun `isInternational should return true for phone with plus`() {
        val phone = Phone.create("+1234567890")
        assertTrue(phone.isInternational)
    }

    @Test
    fun `isInternational should return false for phone without plus`() {
        val phone = Phone.create("1234567890")
        assertFalse(phone.isInternational)
    }

    @Test
    fun `toString should return phone value`() {
        val phone = Phone.create("+52 55 1234 5678")
        assertEquals("+525512345678", phone.toString())
    }

    @Test
    fun `equality should work correctly`() {
        val phone1 = Phone.create("123-456-7890")
        val phone2 = Phone.create("1234567890")
        val phone3 = Phone.create("9876543210")

        assertEquals(phone1, phone2) // Same digits after normalization
        assertTrue(phone1 != phone3)
    }

    @Test
    fun `hashCode should work correctly`() {
        val phone1 = Phone.create("123-456-7890")
        val phone2 = Phone.create("(123) 456-7890")

        assertEquals(phone1.hashCode(), phone2.hashCode())
    }

    @Test
    fun `validation exception should include helpful message`() {
        val exception = assertFailsWith<ValidationException> {
            Phone.create("123")
        }
        assertTrue(exception.message.contains("at least 7 digits"))
        assertEquals("phone", exception.field)
    }

    @Test
    fun `create should handle US format`() {
        val phone = Phone.create("+1 (555) 123-4567")
        assertEquals("+15551234567", phone.value)
        assertTrue(phone.isInternational)
    }

    @Test
    fun `create should handle Mexican format`() {
        val phone = Phone.create("+52 55 1234 5678")
        assertEquals("+525512345678", phone.value)
        assertTrue(phone.isInternational)
    }

    @Test
    fun `create should handle European format`() {
        val phone = Phone.create("+44 20 7123 4567")
        assertEquals("+442071234567", phone.value)
        assertTrue(phone.isInternational)
    }

    @Test
    fun `create should handle local format without country code`() {
        val phone = Phone.create("(555) 123-4567")
        assertEquals("5551234567", phone.value)
        assertFalse(phone.isInternational)
    }

    @Test
    fun `create should reject phone with only formatting characters`() {
        assertFailsWith<ValidationException> {
            Phone.create("()- .")
        }
    }

    // Note: The current implementation normalizes formatting characters
    // These edge cases would normalize to invalid digit counts, not invalid characters

    @Test
    fun `create should accept various valid patterns`() {
        val validPhones = listOf(
            "5551234" to "5551234",
            "555-1234" to "5551234",
            "1234567890" to "1234567890",
            "(123) 456-7890" to "1234567890",
            "+1-234-567-8900" to "+12345678900",
            "+44 20 7123 4567" to "+442071234567",
            "+52 (55) 1234-5678" to "+525512345678",
            "123.456.7890" to "1234567890",
            "+1 (555) 123.4567" to "+15551234567",
        )

        validPhones.forEach { (input, expected) ->
            val phone = Phone.create(input)
            assertEquals(expected, phone.value)
        }
    }

    @Test
    fun `create should normalize mixed formatting`() {
        val phone = Phone.create("+1 (555) 123-4567")
        assertEquals("+15551234567", phone.value)
    }

    @Test
    fun `create should handle edge case with minimum digits`() {
        val phone = Phone.create("1234567")
        assertEquals("1234567", phone.value)
    }

    @Test
    fun `create should handle edge case with maximum digits`() {
        val phone = Phone.create("+12345678901234")
        assertEquals("+12345678901234", phone.value)
    }
}
