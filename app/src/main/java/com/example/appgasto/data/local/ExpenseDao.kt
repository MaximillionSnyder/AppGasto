package com.example.appgasto.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

data class CurrencyTotalTuple(
    val currency: String,
    @ColumnInfo(name = "total") val totalOriginal: Double,
    @ColumnInfo(name = "totalInPEN") val totalInPEN: Double
)

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE createdAt >= :start AND createdAt <= :end ORDER BY createdAt DESC")
    fun getByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<Expense>>

    @Query("SELECT SUM(amountInPEN) FROM expenses WHERE createdAt >= :start AND createdAt <= :end")
    suspend fun getTotalForPeriod(start: LocalDateTime, end: LocalDateTime): Double?

    @Query("SELECT SUM(amountInPEN) FROM expenses WHERE createdAt >= :start")
    suspend fun getTotalSince(start: LocalDateTime): Double?

    @Query("SELECT SUM(amountInPEN) FROM expenses WHERE createdAt >= :start AND categoryId = :categoryId")
    suspend fun getTotalByCategorySince(categoryId: Long, start: LocalDateTime): Double?

    @Query("SELECT currency, SUM(amount) as total, SUM(amountInPEN) as totalInPEN FROM expenses WHERE createdAt >= :start GROUP BY currency")
    suspend fun getTotalByCurrencySince(start: LocalDateTime): List<CurrencyTotalTuple>

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}
