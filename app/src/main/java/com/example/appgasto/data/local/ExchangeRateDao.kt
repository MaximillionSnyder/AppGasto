package com.example.appgasto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<ExchangeRateEntity>)

    @Query("SELECT * FROM exchange_rates WHERE code = :code")
    suspend fun getByCode(code: String): ExchangeRateEntity?

    @Query("SELECT * FROM exchange_rates")
    fun getAll(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT COUNT(*) FROM exchange_rates")
    suspend fun count(): Int

    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAll()
}
