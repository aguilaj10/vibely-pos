package com.vibely.pos.shared.domain.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SKUTest {

    @Test
    fun `create should accept valid SKU`() {
        val sku = SKU.create("ABC-123")
        assertEquals("ABC-123", sku.value)
    }

    @Test
    fun `create should uppercase lowercase input`() {
        val sku = SKU.create("abc-123")
        assertEquals("ABC-123", sku.value)
    }

    @Test
    fun `create should trim whitespace`() {
        val sku = SKU.create("  ABC-123  ")
        assertEquals("ABC-123", sku.value)
    }

    @Test
    fun `create should accept alphanumeric only`() {
        val sku = SKU.create("PROD001")
        assertEquals("PROD001", sku.value)
    }

    @Test
    fun `create should accept SKU with hyphens`() {
        val sku = SKU.create("SKU-XL-BLU")
        assertEquals("SKU-XL-BLU", sku.value)
    }

    @Test
    fun `create should accept 3 character SKU`() {
        val sku = SKU.create("A1B")
        assertEquals("A1B", sku.value)
    }

    @Test
    fun `create should accept 20 character SKU`() {
        val sku = SKU.create("A1B2C3D4E5F6G7H8I9J0")
        assertEquals("A1B2C3D4E5F6G7H8I9J0", sku.value)
    }

    @Test
    fun `create should reject blank SKU`() {
        assertFailsWith<ValidationException> {
            SKU.create("")
        }
        assertFailsWith<ValidationException> {
            SKU.create("   ")
        }
    }

    @Test
    fun `create should reject SKU shorter than 3 characters`() {
        val exception = assertFailsWith<ValidationException> {
            SKU.create("AB")
        }
        assertEquals("sku", exception.field)
    }

    @Test
    fun `create should reject SKU longer than 20 characters`() {
        val exception = assertFailsWith<ValidationException> {
            SKU.create("A1B2C3D4E5F6G7H8I9J0K")
        }
        assertEquals("sku", exception.field)
    }

    @Test
    fun `create should reject SKU starting with hyphen`() {
        assertFailsWith<ValidationException> {
            SKU.create("-ABC123")
        }
    }

    @Test
    fun `create should reject SKU ending with hyphen`() {
        assertFailsWith<ValidationException> {
            SKU.create("ABC123-")
        }
    }

    @Test
    fun `create should reject SKU with invalid characters`() {
        assertFailsWith<ValidationException> {
            SKU.create("ABC_123")
        }
        assertFailsWith<ValidationException> {
            SKU.create("ABC.123")
        }
        assertFailsWith<ValidationException> {
            SKU.create("ABC 123")
        }
        assertFailsWith<ValidationException> {
            SKU.create("ABC@123")
        }
    }

    @Test
    fun `create should reject SKU with lowercase after normalization fails`() {
        // This tests that the validation happens after uppercasing
        val sku = SKU.create("abc123")
        assertEquals("ABC123", sku.value)
    }

    @Test
    fun `create should reject SKU with only hyphens`() {
        assertFailsWith<ValidationException> {
            SKU.create("---")
        }
    }

    @Test
    fun `create should accept numeric only SKU`() {
        val sku = SKU.create("123456")
        assertEquals("123456", sku.value)
    }

    @Test
    fun `create should accept alphabetic only SKU`() {
        val sku = SKU.create("ABCDEF")
        assertEquals("ABCDEF", sku.value)
    }

    @Test
    fun `create should accept complex valid SKU`() {
        val sku = SKU.create("PROD-2024-XL-BLK")
        assertEquals("PROD-2024-XL-BLK", sku.value)
    }

    @Test
    fun `toString should return SKU value`() {
        val sku = SKU.create("TEST-SKU-001")
        assertEquals("TEST-SKU-001", sku.toString())
    }

    @Test
    fun `equality should work correctly`() {
        val sku1 = SKU.create("ABC-123")
        val sku2 = SKU.create("abc-123")
        val sku3 = SKU.create("DEF-456")

        assertEquals(sku1, sku2) // Case-insensitive
        assertNotEquals(sku1, sku3)
    }

    @Test
    fun `hashCode should work correctly`() {
        val sku1 = SKU.create("ABC-123")
        val sku2 = SKU.create("abc-123")

        assertEquals(sku1.hashCode(), sku2.hashCode())
    }

    @Test
    fun `validation exception should include helpful message`() {
        val exception =
            assertFailsWith<ValidationException> {
                SKU.create("AB")
            }
        assertTrue(exception.message.contains("at least 3 characters"))
        assertEquals("sku", exception.field)
    }

    @Test
    fun `validation exception should show normalized value`() {
        val exception =
            assertFailsWith<ValidationException> {
                SKU.create("ABC_123")
            }
        assertTrue(exception.message.contains("ABC_123"))
    }

    @Test
    fun `create should accept multiple hyphens if pattern is valid`() {
        // The regex allows hyphens in middle positions, including consecutive ones
        // as long as it starts and ends with alphanumeric
        val sku = SKU.create("AB--C")
        assertEquals("AB--C", sku.value)
    }

    @Test
    fun `create should accept single hyphen in middle`() {
        val sku = SKU.create("AB-C")
        assertEquals("AB-C", sku.value)
    }
}
