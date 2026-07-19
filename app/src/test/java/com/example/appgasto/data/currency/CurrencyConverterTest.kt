package com.example.appgasto.data.currency

import com.example.appgasto.domain.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyConverterTest {

    private val converter = CurrencyConverter()

    @Test
    fun `toPEN converts using inverse of rateToPen`() {
        // rateToPen(USD) = 0.27 means 1 PEN = 0.27 USD, so 10 USD = 10 / 0.27 PEN
        val result = converter.toPEN(10.0, "USD", 0.27)
        assertEquals(37.037, result, 0.001)
    }

    @Test
    fun `toPEN with PEN returns the same amount`() {
        assertEquals(25.5, converter.toPEN(25.5, "PEN", 0.0), 0.0001)
        assertEquals(25.5, converter.toPEN(25.5, "pen", 1.5), 0.0001)
    }

    @Test
    fun `toPEN with invalid rate returns zero`() {
        assertEquals(0.0, converter.toPEN(10.0, "USD", 0.0), 0.0001)
        assertEquals(0.0, converter.toPEN(10.0, "USD", -1.0), 0.0001)
    }

    @Test
    fun `toPEN with non-positive amount returns zero`() {
        assertEquals(0.0, converter.toPEN(0.0, "USD", 0.27), 0.0001)
        assertEquals(0.0, converter.toPEN(-5.0, "USD", 0.27), 0.0001)
    }

    @Test
    fun `toPEN currency overload matches string overload`() {
        val viaString = converter.toPEN(100.0, "EUR", 0.25)
        val viaEnum = converter.toPEN(100.0, Currency.EUR, 0.25)
        assertEquals(viaString, viaEnum, 0.0001)
    }
}
