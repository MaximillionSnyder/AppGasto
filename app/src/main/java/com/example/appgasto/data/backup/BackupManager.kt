package com.example.appgasto.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.example.appgasto.data.local.AppDatabase
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import com.example.appgasto.domain.model.Currency
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.time.Month
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type
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
    /**
     * Gson adapter for [LocalDateTime].
     *
     * Exports as ISO-8601 local date-time string (e.g. "2026-07-19T14:30:00").
     * Imports support both the new string format and the legacy object format
     * produced by Gson's default reflection (nested `date` and `time` fields).
     */
    private val localDateTimeAdapter = object : JsonSerializer<LocalDateTime>,
        JsonDeserializer<LocalDateTime> {

        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        override fun serialize(
            src: LocalDateTime?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement = JsonPrimitive(src?.format(formatter))

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): LocalDateTime? = when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> {
                val string = json.asString
                if (string.isNullOrBlank()) null else LocalDateTime.parse(string, formatter)
            }
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val dateObj = obj.getAsJsonObject("date")
                val timeObj = obj.getAsJsonObject("time")
                LocalDateTime.of(
                    dateObj.getInt("year"),
                    dateObj.parseMonth("month"),
                    dateObj.getInt("day"),
                    timeObj.getInt("hour"),
                    timeObj.getInt("minute"),
                    timeObj.getIntOrDefault("second", 0),
                    timeObj.getIntOrDefault("nano", 0)
                )
            }
            else -> null
        }
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, localDateTimeAdapter)
        .create()

    private fun JsonObject.getInt(memberName: String): Int =
        get(memberName).asInt

    private fun JsonObject.getIntOrDefault(memberName: String, default: Int): Int =
        if (has(memberName)) get(memberName).asInt else default

    private fun JsonObject.parseMonth(memberName: String): Int {
        val element = get(memberName)
        return when {
            element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                Month.valueOf(element.asString.uppercase()).value
            }
            else -> element.asInt
        }
    }

    data class BackupData(
        val version: Int = 2,
        val exportedAt: String = LocalDateTime.now().toString(),
        val categories: List<Category> = emptyList(),
        val expenses: List<Expense> = emptyList()
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
                val backupData: BackupData? = gson.fromJson(json, type)

                if (backupData == null) {
                    return@withContext Result.failure(IllegalStateException("Backup vacío o formato inválido"))
                }

                val categories = backupData.categories ?: emptyList()
                val expenses = backupData.expenses ?: emptyList()

                database.withTransaction {
                    database.categoryDao().deleteAll()
                    database.expenseDao().deleteAll()

                    for (category in categories) {
                        database.categoryDao().insert(category)
                    }
                    for (expense in expenses) {
                        val migrated = when (backupData.version) {
                            1 -> expense.copy(
                                currency = Currency.PEN.code,
                                amountInPEN = expense.amount,
                                exchangeRateUsed = 1.0
                            )
                            else -> expense
                        }
                        database.expenseDao().insert(migrated)
                    }
                }

                Result.success(expenses.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
