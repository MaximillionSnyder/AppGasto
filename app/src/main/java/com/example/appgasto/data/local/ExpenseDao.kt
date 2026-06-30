package com.example.appgasto.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDateTime

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    suspend fun getAll(): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE createdAt >= :start AND createdAt <= :end ORDER BY createdAt DESC")
    suspend fun getByDateRange(start: LocalDateTime, end: LocalDateTime): List<Expense>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    suspend fun getByCategory(categoryId: Long): List<Expense>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND createdAt >= :start AND createdAt <= :end ORDER BY createdAt DESC")
    suspend fun getByCategoryAndDateRange(categoryId: Long, start: LocalDateTime, end: LocalDateTime): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE createdAt >= :start AND createdAt <= :end")
    suspend fun getTotalForPeriod(start: LocalDateTime, end: LocalDateTime): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE createdAt >= :start")
    suspend fun getTotalSince(start: LocalDateTime): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE createdAt >= :start AND categoryId = :categoryId")
    suspend fun getTotalByCategorySince(categoryId: Long, start: LocalDateTime): Double?

    @Query("SELECT CAST(SUM(amount) AS REAL) FROM expenses WHERE createdAt >= :start AND createdAt <= :end GROUP BY CAST(createdAt AS DATE) ORDER BY createdAt")
    suspend fun getDailyTotals(start: LocalDateTime, end: LocalDateTime): List<Double>

    @Query("SELECT * FROM expenses WHERE createdAt >= :start ORDER BY createdAt DESC")
    suspend fun getExpensesSince(start: LocalDateTime): List<Expense>

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}
