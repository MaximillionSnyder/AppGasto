package com.example.appgasto.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.example.appgasto.data.local.AppDatabase
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    private val gson = Gson()

    data class BackupData(
        val version: Int = 1,
        val exportedAt: String = LocalDateTime.now().toString(),
        val categories: List<Category>,
        val expenses: List<Expense>
    )

    suspend fun exportToJson(outputStream: OutputStream): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val categories = database.categoryDao().getAll().first()
                val expenses = database.expenseDao().getAll().first()

                val backupData = BackupData(
                    categories = categories,
                    expenses = expenses
                )

                val json = gson.toJson(backupData)
                outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }

                val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                Result.success("appgasto_backup_$dateStr.json")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun importFromJson(inputStream: InputStream): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val json = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val type = object : TypeToken<BackupData>() {}.type
                val backupData: BackupData = gson.fromJson(json, type)

                database.withTransaction {
                    database.categoryDao().deleteAll()
                    database.expenseDao().deleteAll()

                    for (category in backupData.categories) {
                        database.categoryDao().insert(category)
                    }
                    for (expense in backupData.expenses) {
                        database.expenseDao().insert(expense)
                    }
                }

                Result.success(backupData.expenses.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
