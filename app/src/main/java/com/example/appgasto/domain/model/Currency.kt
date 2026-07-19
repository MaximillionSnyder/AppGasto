package com.example.appgasto.domain.model

import java.util.Locale

enum class Currency(val code: String, val symbol: String, val decimals: Int = 2) {
    PEN("PEN", "S/."),
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    JPY("JPY", "¥", decimals = 0),
    BRL("BRL", "R$"),
    MXN("MXN", "MX$"),
    COP("COP", "COL$"),
    ARS("ARS", "AR$"),
    CLP("CLP", "CL$", decimals = 0),
    BOB("BOB", "Bs"),
    UYU("UYU", "UY$"),
    PYG("PYG", "₲", decimals = 0),
    CRC("CRC", "₡"),
    GTQ("GTQ", "Q"),
    DOP("DOP", "RD$"),
    CAD("CAD", "CA$"),
    CHF("CHF", "CHF"),
    CNY("CNY", "CN¥"),
    KRW("KRW", "₩", decimals = 0),
    INR("INR", "₹"),
    AUD("AUD", "A$"),
    NZD("NZD", "NZ$"),
    SEK("SEK", "kr"),
    NOK("NOK", "kr");

    /**
     * Formats [amount] with this currency's symbol and its number of
     * decimal places (e.g. 0 for JPY/KRW, 2 for most currencies).
     */
    fun format(amount: Double): String {
        return symbol + String.format("%.${decimals}f", amount)
    }

    /**
     * Localized currency name (e.g. "Sol peruano", "US Dollar") using the
     * device's locale, falling back to the ISO code if unavailable.
     */
    fun displayName(locale: Locale = Locale.getDefault()): String {
        return runCatching {
            java.util.Currency.getInstance(code).getDisplayName(locale)
        }.getOrDefault(code)
    }

    companion object {
        private val BY_CODE = entries.associateBy { it.code }

        fun fromCode(code: String): Currency {
            return BY_CODE[code.uppercase()] ?: PEN
        }

        fun supportedCodes(): List<String> = entries.map { it.code }
    }
}
