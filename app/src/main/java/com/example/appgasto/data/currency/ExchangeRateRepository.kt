package com.example.appgasto.data.currency

import com.example.appgasto.data.local.ExchangeRateDao
import com.example.appgasto.data.local.ExchangeRateEntity
import com.example.appgasto.data.repository.PreferencesRepository
import com.example.appgasto.domain.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateRepository @Inject constructor(
    private val api: ExchangeRateApi,
    private val exchangeRateDao: ExchangeRateDao,
    private val converter: CurrencyConverter,
    private val preferencesRepository: PreferencesRepository
) {

    val ratesFlow: Flow<List<ExchangeRateEntity>> = exchangeRateDao.getAll()

    /**
     * Fetches current rates from the API and caches them locally.
     * Returns Result.success(Unit) on success, Result.failure(exception) otherwise.
     */
    suspend fun refreshRates(): Result<Unit> {
        return try {
            val response = api.getPenRates()
            if (!response.isSuccessful) {
                return Result.failure(Exception("API error: ${response.code()}"))
            }

            val body = response.body()
                ?: return Result.failure(Exception("Empty response body"))

            val entities = body.rates.mapNotNull { (code, rate) ->
                if (rate != null && rate > 0) {
                    ExchangeRateEntity(
                        code = code.uppercase(),
                        rateToPen = rate,
                        fetchedAt = System.currentTimeMillis()
                    )
                } else null
            }

            exchangeRateDao.insertAll(entities)
            preferencesRepository.setRatesUpdatedAt(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns the cached rate for [currencyCode].
     * Returns null if not cached.
     */
    suspend fun getRateToPen(currencyCode: String): Double? {
        return exchangeRateDao.getByCode(currencyCode.uppercase())?.rateToPen
    }

    /**
     * Converts [amount] in [sourceCurrencyCode] to PEN using the cached rate.
     * Falls back to identity if source currency is PEN or no rate is available.
     */
    suspend fun convertToPEN(amount: Double, sourceCurrencyCode: String): Double {
        val code = sourceCurrencyCode.uppercase()
        if (code == Currency.PEN.code) return amount
        val rate = getRateToPen(code)
        if (rate == null || rate <= 0) return 0.0
        return converter.toPEN(amount, Currency.fromCode(code), rate)
    }

    /**
     * Returns true if at least one exchange rate is cached.
     */
    suspend fun hasRates(): Boolean {
        return exchangeRateDao.count() > 0
    }

    /**
     * Best-effort refresh: if network fails but cache exists, consider it acceptable.
     * Useful during app startup / worker runs.
     */
    suspend fun refreshRatesIfNeeded(): Result<Unit> {
        return if (hasRates()) {
            val result = refreshRates()
            // If refresh failed but we have cached data, still success
            if (result.isFailure) Result.success(Unit) else result
        } else {
            refreshRates()
        }
    }
}
