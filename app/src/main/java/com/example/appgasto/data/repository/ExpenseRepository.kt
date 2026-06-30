package com.example.appgasto.data.repository

import com.example.appgasto.data.local.AppDatabase
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    database: AppDatabase
) {
    private val expenseDao = database.expenseDao()
    private val categoryDao = database.categoryDao()

    // -- Expenses --

    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    suspend fun getExpenseById(id: Long) = expenseDao.getById(id)

    suspend fun getAllExpenses() = expenseDao.getAll()

    suspend fun getTodayExpenses(): List<Expense> {
        val start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        val end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        return expenseDao.getByDateRange(start, end)
    }

    suspend fun getExpensesByDateRange(start: LocalDate, end: LocalDate): List<Expense> {
        return expenseDao.getByDateRange(
            LocalDateTime.of(start, LocalTime.MIN),
            LocalDateTime.of(end, LocalTime.MAX)
        )
    }

    suspend fun getTodayTotal(): Double {
        val start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        return expenseDao.getTotalSince(start) ?: 0.0
    }

    suspend fun getCurrentWeekTotal(): Double {
        val today = LocalDate.now()
        val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
        return expenseDao.getTotalSince(LocalDateTime.of(startOfWeek, LocalTime.MIN)) ?: 0.0
    }

    suspend fun getCurrentMonthTotal(): Double {
        val startOfMonth = LocalDate.now().withDayOfMonth(1)
        return expenseDao.getTotalSince(LocalDateTime.of(startOfMonth, LocalTime.MIN)) ?: 0.0
    }

    suspend fun getTotalForPeriod(start: LocalDate, end: LocalDate): Double {
        return expenseDao.getTotalForPeriod(
            LocalDateTime.of(start, LocalTime.MIN),
            LocalDateTime.of(end, LocalTime.MAX)
        ) ?: 0.0
    }

    suspend fun getTotalByCategorySince(categoryId: Long, start: LocalDate): Double {
        return expenseDao.getTotalByCategorySince(
            categoryId,
            LocalDateTime.of(start, LocalTime.MIN)
        ) ?: 0.0
    }

    suspend fun getExpensesSince(date: LocalDate): List<Expense> {
        return expenseDao.getExpensesSince(LocalDateTime.of(date, LocalTime.MIN))
    }

    suspend fun deleteAllExpenses() = expenseDao.deleteAll()

    // -- Categories --

    suspend fun getAllCategories() = categoryDao.getAll()

    suspend fun getCategoryById(id: Long) = categoryDao.getById(id)

    suspend fun insertCategory(category: Category) = categoryDao.insert(category)

    suspend fun deleteAllCategories() = categoryDao.deleteAll()
}
