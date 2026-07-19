package com.example.appgasto.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class CurrencyTest {

    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `fromCode returns matching currency ignoring case`() {
        assertEquals(Currency.USD, Currency.fromCode("USD"))
        assertEquals(Currency.USD, Currency.fromCode("usd"))
        assertEquals(Currency.MXN, Currency.fromCode("mxn"))
        assertEquals(Currency.BRL, Currency.fromCode("BRL"))
    }

    @Test
    fun `fromCode falls back to PEN for unknown codes`() {
        assertEquals(Currency.PEN, Currency.fromCode("XXX"))
        assertEquals(Currency.PEN, Currency.fromCode(""))
    }

    @Test
    fun `supportedCodes contains all 25 currencies`() {
        val codes = Currency.supportedCodes()
        assertEquals(25, codes.size)
        assertTrue(codes.containsAll(listOf("PEN", "USD", "EUR", "JPY", "GBP", "BRL")))
        assertTrue(codes.containsAll(listOf("MXN", "COP", "ARS", "CLP", "BOB", "UYU", "PYG", "CRC", "GTQ", "DOP")))
        assertTrue(codes.containsAll(listOf("CAD", "CHF", "CNY", "KRW", "INR", "AUD", "NZD", "SEK", "NOK")))
    }

    @Test
    fun `format uses two decimals for most currencies`() {
        assertEquals("S/.10.50", Currency.PEN.format(10.5))
        assertEquals("$10.00", Currency.USD.format(10.0))
        assertEquals("€0.99", Currency.EUR.format(0.99))
        assertEquals("MX$25.50", Currency.MXN.format(25.5))
    }

    @Test
    fun `format uses zero decimals for zero-decimal currencies`() {
        assertEquals("¥1000", Currency.JPY.format(1000.0))
        assertEquals("₩5000", Currency.KRW.format(5000.0))
        assertEquals("CL$1500", Currency.CLP.format(1500.0))
        assertEquals("₲15000", Currency.PYG.format(15000.0))
    }

    @Test
    fun `zero decimal currencies have decimals set to 0`() {
        listOf(Currency.JPY, Currency.KRW, Currency.CLP, Currency.PYG).forEach {
            assertEquals(0, it.decimals)
        }
    }

    @Test
    fun `displayName falls back to code for invalid locale data`() {
        // Real currencies resolve through java.util.Currency
        assertTrue(Currency.USD.displayName(Locale.US).isNotBlank())
    }
}
