package com.example.appgasto.data.currency

import com.example.appgasto.domain.model.Currency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyConverter @Inject constructor() {

    /**
     * Converts an amount from [sourceCurrency] to PEN.
     *
     * The API returns how much 1 PEN equals in other currencies.
     * Example: rateToPen (USD) = 0.27 means 1 PEN = 0.27 USD,
     * therefore 1 USD = 1 / 0.27 PEN.
     *
     * @param amount amount in the source currency
     * @param sourceCurrency ISO code of the source currency
     * @param rateToPen how much 1 PEN is worth in source currency
     */
    fun toPEN(amount: Double, sourceCurrency: String, rateToPen: Double): Double {
        if (amount <= 0) return 0.0
        if (sourceCurrency.uppercase() == Currency.PEN.code) return amount
        if (rateToPen <= 0) return 0.0
        return amount / rateToPen
    }

    fun toPEN(amount: Double, sourceCurrency: Currency, rateToPen: Double): Double {
        return toPEN(amount, sourceCurrency.code, rateToPen)
    }
}
