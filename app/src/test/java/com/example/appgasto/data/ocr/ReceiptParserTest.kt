package com.example.appgasto.data.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReceiptParserTest {

    @Test
    fun `detects original currencies`() {
        assertEquals("PEN", ReceiptParser.parse("TOTAL S/. 25.50").currencyCode)
        assertEquals("USD", ReceiptParser.parse("TOTAL $ 20.00").currencyCode)
        assertEquals("EUR", ReceiptParser.parse("TOTAL € 15.00").currencyCode)
        assertEquals("GBP", ReceiptParser.parse("TOTAL £ 12.00").currencyCode)
        assertEquals("JPY", ReceiptParser.parse("TOTAL ¥ 1500").currencyCode)
        assertEquals("BRL", ReceiptParser.parse("TOTAL R$ 50.00").currencyCode)
    }

    @Test
    fun `detects new latin american currencies`() {
        assertEquals("MXN", ReceiptParser.parse("TOTAL MX$ 150.00").currencyCode)
        assertEquals("COP", ReceiptParser.parse("TOTAL COL$ 8000").currencyCode)
        assertEquals("ARS", ReceiptParser.parse("TOTAL AR$ 3200").currencyCode)
        assertEquals("CLP", ReceiptParser.parse("TOTAL CL$ 5000").currencyCode)
        assertEquals("DOP", ReceiptParser.parse("TOTAL RD$ 600").currencyCode)
        assertEquals("BOB", ReceiptParser.parse("TOTAL Bs 100").currencyCode)
        assertEquals("UYU", ReceiptParser.parse("TOTAL UY$ 450").currencyCode)
        assertEquals("PYG", ReceiptParser.parse("TOTAL ₲ 25000").currencyCode)
        assertEquals("CRC", ReceiptParser.parse("TOTAL ₡ 3500").currencyCode)
    }

    @Test
    fun `detects new international currencies`() {
        assertEquals("CAD", ReceiptParser.parse("TOTAL CA$ 30.00").currencyCode)
        assertEquals("AUD", ReceiptParser.parse("TOTAL A$ 45.00").currencyCode)
        assertEquals("NZD", ReceiptParser.parse("TOTAL NZ$ 22.00").currencyCode)
        assertEquals("KRW", ReceiptParser.parse("TOTAL ₩ 9000").currencyCode)
        assertEquals("INR", ReceiptParser.parse("TOTAL ₹ 800").currencyCode)
    }

    @Test
    fun `more specific symbols win over generic dollar`() {
        // "CA$" must match CAD, not AUD ("A$") or USD ("$")
        assertEquals("CAD", ReceiptParser.parse("CA$ 10").currencyCode)
        // "MX$" must match MXN, not USD
        assertEquals("MXN", ReceiptParser.parse("MX$ 10").currencyCode)
    }

    @Test
    fun `returns null when no known symbol is present`() {
        assertNull(ReceiptParser.parse("TOTAL 25.50").currencyCode)
    }
}
