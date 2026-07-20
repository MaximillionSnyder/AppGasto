package com.example.appgasto.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

data class CurrencyTotalTuple(
    val currency: String,
    @ColumnInfo(name = "total") val totalOriginal: Double,
    @ColumnInfo(name = "totalInPEN") val totalInPEN: Double
)

data class PeriodTotals(
    @ColumnInfo(name = "todayTotal") val todayTotal: Double,
    @ColumnInfo(name = "weekTotal") val weekTotal: Double,
    @ColumnInfo(name = "monthTotal") val monthTotal: Double
)

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<Expense>)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query("SELECT COALESCE(SUM(CASE WHEN createdAt >= :todayStart AND createdAt <= :todayEnd THEN amountInPEN ELSE 0 END), 0) as todayTotal, COALESCE(SUM(CASE WHEN createdAt >= :weekStart THEN amountInPEN ELSE 0 END), 0) as weekTotal, COALESCE(SUM(CASE WHEN createdAt >= :monthStart THEN amountInPEN ELSE 0 END), 0) as monthTotal FROM expenses")
    suspend fun getPeriodTotals(todayStart: LocalDateTime, todayEnd: LocalDateTime, weekStart: LocalDateTime, monthStart: LocalDateTime): PeriodTotals

    @Query("SELECT * FROM expenses WHERE (:categoryId IS NULL OR categoryId = :categoryId) AND (:startDate IS NULL OR createdAt >= :startDate) AND (:endDate IS NULL OR createdAt <= :endDate) ORDER BY createdAt DESC")
    fun getFiltered(categoryId: Long?, startDate: LocalDateTime?, endDate: LocalDateTime?): Flow<List<Expense>>

    @Query("SELECT DISTINCT createdAt FROM expenses ORDER BY createdAt DESC")
    fun getAllDates(): Flow<List<LocalDateTime>>

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
