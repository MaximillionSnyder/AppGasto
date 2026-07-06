package com.example.appgasto.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val code: String,
    /**
     * How much 1 PEN is worth in [code] currency.
     * Example: rate = 0.27 means 1 PEN = 0.27 USD.
     */
    val rateToPen: Double,
    val fetchedAt: Long = System.currentTimeMillis()
)
