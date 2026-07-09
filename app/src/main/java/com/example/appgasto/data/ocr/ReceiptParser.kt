package com.example.appgasto.data.ocr

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object ReceiptParser {

    private val CURRENCY_PATTERNS = listOf(
        "S/." to "PEN",
        "R$" to "BRL",
        "€" to "EUR",
        "£" to "GBP",
        "¥" to "JPY",
        "$" to "USD"
    )

    private val DATE_FORMATS = listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MM-yyyy")
        .map { DateTimeFormatter.ofPattern(it, Locale.getDefault()) }

    private val TOTAL_KEYWORDS = listOf("TOTAL", "SUMA", "IMPORTE", "MONTO", "SUBTOTAL")
    private val AMOUNT_REGEX = Regex("""\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{2})?""")
    private val DATE_REGEX = Regex("""\d{1,4}[-/]\d{1,2}[-/]\d{1,4}""")

    fun parse(text: String): ReceiptData {
        return ReceiptData(
            total = detectTotal(text),
            date = detectDate(text),
            merchant = detectMerchant(text),
            currencyCode = detectCurrency(text)
        )
    }

    private fun detectCurrency(text: String): String? {
        for ((symbol, code) in CURRENCY_PATTERNS) {
            if (text.contains(symbol)) return code
        }
        return null
    }

    private fun detectTotal(text: String): String? {
        var best: String? = null
        text.lines().forEach { line ->
            val upper = line.uppercase()
            if (TOTAL_KEYWORDS.any { upper.contains(it) } && !upper.contains("VUELTO")) {
                val match = AMOUNT_REGEX.findAll(line)
                    .map { it.value.replace(",", ".") }
                    .maxByOrNull { it.length }
                if (match != null) best = match
            }
        }
        return best
    }

    private fun detectDate(text: String): LocalDate? {
        val match = DATE_REGEX.find(text) ?: return null
        for (format in DATE_FORMATS) {
            runCatching { LocalDate.parse(match.value, format) }.onSuccess { return it }
        }
        return null
    }

    private fun detectMerchant(text: String): String? {
        return text.lines()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() && it.length >= 3 }
    }
}
