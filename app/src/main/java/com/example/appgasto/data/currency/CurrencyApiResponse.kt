package com.example.appgasto.data.currency

import com.google.gson.annotations.SerializedName

/**
 * Response from fawazahmed0/currency-api for endpoint:
 * https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/pen.json
 *
 * The API returns how much 1 PEN is worth in other currencies.
 * Example: "pen": { "usd": "0.2678", "eur": "0.2451", ... }
 */
data class CurrencyApiResponse(
    val date: String,
    @SerializedName("pen")
    val rates: Map<String, Double?>
)
