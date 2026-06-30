package com.example.appgasto.data.backup

import android.content.Context
import android.os.Environment
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import com.example.appgasto.data.repository.ExpenseRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseRepository: ExpenseRepository
) {
    private val gson = Gson()

    data class BackupData(
        val version: Int = 1,
        val exportedAt: String = LocalDateTime.now().toString(),
        val categories: List<Category>,
        val expenses: List<Expense>
    )

    suspend fun exportToJson(): Result<File> {
        return try {
            val categories = expenseRepository.getAllCategories()
            val expenses = expenseRepository.getAllExpenses()

            val backupData = BackupData(
                categories = categories,
                expenses = expenses
            )

            val json = gson.toJson(backupData)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val appDir = File(downloadsDir, "AppGasto")
            if (!appDir.exists()) appDir.mkdirs()

            val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val file = File(appDir, "backup_$dateStr.json")
            FileWriter(file).use { it.write(json) }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(fileUri: File): Result<Int> {
        return try {
            val json = FileReader(fileUri).use { it.readText() }
            val type = object : TypeToken<BackupData>() {}.type
            val backupData: BackupData = gson.fromJson(json, type)

            expenseRepository.deleteAllCategories()
            expenseRepository.deleteAllExpenses()

            var count = 0
            for (category in backupData.categories) {
                expenseRepository.insertCategory(category)
            }
            for (expense in backupData.expenses) {
                expenseRepository.insertExpense(expense)
                count++
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
