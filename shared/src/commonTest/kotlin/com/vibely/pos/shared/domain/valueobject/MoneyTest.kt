package com.vibely.pos.shared.domain.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `fromAmount should create Money with correct cents value`() {
        val money = Money.fromAmount(10.50, "USD")
        assertEquals(1050L, money.amountInCents)
        assertEquals("USD", money.currency)
        assertEquals(10.50, money.amount, 0.01)
    }

    @Test
    fun `fromCents should create Money with correct cents value`() {
        val money = Money.fromCents(2500L, "EUR")
        assertEquals(2500L, money.amountInCents)
        assertEquals("EUR", money.currency)
        assertEquals(25.00, money.amount, 0.01)
    }

    @Test
    fun `zero should create Money with zero amount`() {
        val money = Money.zero("MXN")
        assertEquals(0L, money.amountInCents)
        assertEquals("MXN", money.currency)
        assertTrue(money.isZero)
    }

    @Test
    fun `currency should be uppercased`() {
        val money = Money.fromAmount(100.0, "usd")
        assertEquals("USD", money.currency)
    }

    @Test
    fun `invalid currency should throw ValidationException`() {
        assertFailsWith<ValidationException> {
            Money.fromAmount(100.0, "US")
        }
        assertFailsWith<ValidationException> {
            Money.fromAmount(100.0, "USDD")
        }
        assertFailsWith<ValidationException> {
            Money.fromAmount(100.0, "12A")
        }
        assertFailsWith<ValidationException> {
            Money.fromAmount(100.0, "")
        }
    }

    @Test
    fun `isPositive should return true for positive amounts`() {
        val money = Money.fromCents(100L, "USD")
        assertTrue(money.isPositive)
        assertFalse(money.isNegative)
        assertFalse(money.isZero)
    }

    @Test
    fun `isNegative should return true for negative amounts`() {
        val money = Money.fromCents(-100L, "USD")
        assertTrue(money.isNegative)
        assertFalse(money.isPositive)
        assertFalse(money.isZero)
    }

    @Test
    fun `isZero should return true for zero amounts`() {
        val money = Money.zero("USD")
        assertTrue(money.isZero)
        assertFalse(money.isPositive)
        assertFalse(money.isNegative)
    }

    @Test
    fun `plus should add amounts with same currency`() {
        val money1 = Money.fromAmount(10.50, "USD")
        val money2 = Money.fromAmount(5.25, "USD")
        val result = money1 + money2
        assertEquals(1575L, result.amountInCents)
        assertEquals("USD", result.currency)
    }

    @Test
    fun `plus should throw ValidationException for different currencies`() {
        val money1 = Money.fromAmount(10.0, "USD")
        val money2 = Money.fromAmount(10.0, "EUR")
        assertFailsWith<ValidationException> {
            money1 + money2
        }
    }

    @Test
    fun `minus should subtract amounts with same currency`() {
        val money1 = Money.fromAmount(10.50, "USD")
        val money2 = Money.fromAmount(5.25, "USD")
        val result = money1 - money2
        assertEquals(525L, result.amountInCents)
        assertEquals("USD", result.currency)
    }

    @Test
    fun `minus should throw ValidationException for different currencies`() {
        val money1 = Money.fromAmount(10.0, "USD")
        val money2 = Money.fromAmount(5.0, "EUR")
        assertFailsWith<ValidationException> {
            money1 - money2
        }
    }

    @Test
    fun `times with Int should multiply amount`() {
        val money = Money.fromAmount(10.50, "USD")
        val result = money * 3
        assertEquals(3150L, result.amountInCents)
        assertEquals("USD", result.currency)
    }

    @Test
    fun `times with Double should multiply and round amount`() {
        val money = Money.fromAmount(10.00, "USD")
        val result = money * 1.5
        assertEquals(1500L, result.amountInCents)
        assertEquals("USD", result.currency)
    }

    @Test
    fun `unaryMinus should negate amount`() {
        val money = Money.fromAmount(10.50, "USD")
        val negated = -money
        assertEquals(-1050L, negated.amountInCents)
        assertEquals("USD", negated.currency)
    }

    @Test
    fun `abs should return absolute value`() {
        val money = Money.fromAmount(-10.50, "USD")
        val absolute = money.abs()
        assertEquals(1050L, absolute.amountInCents)
        assertTrue(absolute.isPositive)
    }

    @Test
    fun `compareTo should compare amounts with same currency`() {
        val money1 = Money.fromAmount(10.00, "USD")
        val money2 = Money.fromAmount(5.00, "USD")
        val money3 = Money.fromAmount(10.00, "USD")

        assertTrue(money1 > money2)
        assertTrue(money2 < money1)
        assertTrue(money1 >= money3)
        assertTrue(money1 <= money3)
        assertEquals(0, money1.compareTo(money3))
    }

    @Test
    fun `compareTo should throw ValidationException for different currencies`() {
        val money1 = Money.fromAmount(10.0, "USD")
        val money2 = Money.fromAmount(5.0, "EUR")
        assertFailsWith<ValidationException> {
            money1 > money2
        }
    }

    @Test
    fun `formatDisplay should format positive amounts correctly`() {
        val money = Money.fromAmount(1234.56, "USD")
        assertEquals("1234.56 USD", money.formatDisplay())
    }

    @Test
    fun `formatDisplay should format negative amounts correctly`() {
        val money = Money.fromAmount(-1234.56, "USD")
        assertEquals("-1234.56 USD", money.formatDisplay())
    }

    @Test
    fun `formatDisplay should format zero correctly`() {
        val money = Money.zero("EUR")
        assertEquals("0.00 EUR", money.formatDisplay())
    }

    @Test
    fun `formatDisplay should pad cents with leading zero`() {
        val money = Money.fromCents(1005L, "USD")
        assertEquals("10.05 USD", money.formatDisplay())
    }

    @Test
    fun `toString should return formatted display`() {
        val money = Money.fromAmount(99.99, "MXN")
        assertEquals("99.99 MXN", money.toString())
    }

    @Test
    fun `equality should work correctly`() {
        val money1 = Money.fromAmount(10.50, "USD")
        val money2 = Money.fromCents(1050L, "USD")
        val money3 = Money.fromAmount(10.50, "EUR")
        val money4 = Money.fromAmount(20.00, "USD")

        assertEquals(money1, money2)
        assertTrue(money1 != money3)
        assertTrue(money1 != money4)
    }

    @Test
    fun `hashCode should work correctly`() {
        val money1 = Money.fromAmount(10.50, "USD")
        val money2 = Money.fromCents(1050L, "USD")

        assertEquals(money1.hashCode(), money2.hashCode())
    }

    @Test
    fun `Money should handle large amounts`() {
        val money = Money.fromAmount(999999999.99, "USD")
        assertEquals(99999999999L, money.amountInCents)
        assertEquals("999999999.99 USD", money.formatDisplay())
    }

    @Test
    fun `Money should handle fractional cents by truncating`() {
        val money = Money.fromAmount(10.555, "USD")
        // Should truncate to 10.55 (1055 cents)
        assertEquals(1055L, money.amountInCents)
    }

    @Test
    fun `operations should preserve currency`() {
        val money1 = Money.fromAmount(10.0, "EUR")
        val money2 = Money.fromAmount(5.0, "EUR")

        assertEquals("EUR", (money1 + money2).currency)
        assertEquals("EUR", (money1 - money2).currency)
        assertEquals("EUR", (money1 * 2).currency)
        assertEquals("EUR", (-money1).currency)
        assertEquals("EUR", money1.abs().currency)
    }
}
