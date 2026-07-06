package com.example.appgasto.data.currency

import retrofit2.Response
import retrofit2.http.GET

interface ExchangeRateApi {

    /**
     * Fetches current rates expressed as "1 PEN = X other currency".
     * The response body is parsed into [CurrencyApiResponse].
     */
    @GET("v1/currencies/pen.json")
    suspend fun getPenRates(): Response<CurrencyApiResponse>
}
