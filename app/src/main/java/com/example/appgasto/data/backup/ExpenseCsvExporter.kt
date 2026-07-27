package com.example.appgasto.data.backup

import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import java.io.OutputStream
import java.nio.charset.Charsets
import java.time.format.DateTimeFormatter

object ExpenseCsvExporter {

    private val csvDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun export(
        expenses: List<Expense>,
        categories: List<Category>,
        outputStream: OutputStream
    ): Result<String> {
        return try {
            val categoryMap = categories.associateBy { it.id }
            val csv = buildString {
                appendLine("Fecha,Monto,Moneda,Monto_PEN,Categoria,Nota")
                expenses.forEach { expense ->
                    val category = categoryMap[expense.categoryId]
                    val categoryName = category?.name ?: ""
                    val note = expense.note?.replace(",", ";")?.replace("\"", "\"\"") ?: ""
                    val date = expense.createdAt.format(csvDateTimeFormatter)
                    appendLine(
                        "\"$date\"," +
                        "${expense.amount}," +
                        "${expense.currency}," +
                        "${expense.amountInPEN}," +
                        "\"$categoryName\"," +
                        "\"$note\""
                    )
                }
            }
            val bytes = csv.toByteArray(Charsets.UTF_8)
            with(outputStream) {
                write(bytes)
                flush()
            }
            Result.success("CSV exported")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
