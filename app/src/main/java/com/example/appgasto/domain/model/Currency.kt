package com.example.appgasto.domain.model

enum class Currency(val code: String, val symbol: String) {
    PEN("PEN", "S/."),
    USD("USD", "$"),
    EUR("EUR", "€"),
    JPY("JPY", "¥"),
    GBP("GBP", "£"),
    BRL("BRL", "R$");

    companion object {
        private val BY_CODE = entries.associateBy { it.code }

        fun fromCode(code: String): Currency {
            return BY_CODE[code.uppercase()] ?: PEN
        }

        fun supportedCodes(): List<String> = entries.map { it.code }
    }
}
