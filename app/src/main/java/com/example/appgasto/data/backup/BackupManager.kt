package com.example.appgasto.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.example.appgasto.data.local.AppDatabase
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import com.example.appgasto.domain.model.Currency
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
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
    private val localDateTimeAdapter = object : com.google.gson.TypeAdapter<LocalDateTime>() {

        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        override fun write(out: JsonWriter, value: LocalDateTime?) {
            out.value(value?.format(formatter))
        }

        override fun read(`in`: JsonReader): LocalDateTime? {
            return when (`in`.peek()) {
                JsonToken.STRING -> {
                    val string = `in`.nextString()
                    if (string.isNullOrBlank()) null else LocalDateTime.parse(string, formatter)
                }
                JsonToken.BEGIN_OBJECT -> {
                    `in`.beginObject()
                    var year = 0; var month = 0; var day = 0
                    var hour = 0; var minute = 0; var second = 0; var nano = 0
                    while (`in`.hasNext()) {
                        when (`in`.nextName()) {
                            "date" -> {
                                `in`.beginObject()
                                while (`in`.hasNext()) {
                                    when (`in`.nextName()) {
                                        "year" -> year = `in`.nextInt()
                                        "month" -> {
                                            if (`in`.peek() == JsonToken.STRING) {
                                                month = Month.valueOf(`in`.nextString().uppercase()).value
                                            } else {
                                                month = `in`.nextInt()
                                            }
                                        }
                                        "day" -> day = `in`.nextInt()
                                        else -> `in`.skipValue()
                                    }
                                }
                                `in`.endObject()
                            }
                            "time" -> {
                                `in`.beginObject()
                                while (`in`.hasNext()) {
                                    when (`in`.nextName()) {
                                        "hour" -> hour = `in`.nextInt()
                                        "minute" -> minute = `in`.nextInt()
                                        "second" -> second = `in`.nextInt()
                                        "nano" -> nano = `in`.nextInt()
                                        else -> `in`.skipValue()
                                    }
                                }
                                `in`.endObject()
                            }
                            else -> `in`.skipValue()
                        }
                    }
                    `in`.endObject()
                    LocalDateTime.of(year, month, day, hour, minute, second, nano)
                }
                else -> {
                    `in`.skipValue()
                    null
                }
            }
        }
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, localDateTimeAdapter)
        .create()

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

                if (json.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("El archivo está vacío (0 KB)"))
                }
                if (!json.trimStart().startsWith("{")) {
                    return@withContext Result.failure(IllegalStateException("No es un backup JSON válido. Selecciona el archivo appgasto_backup_*.json"))
                }

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
