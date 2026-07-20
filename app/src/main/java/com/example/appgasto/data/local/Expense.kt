package com.example.appgasto.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId"), Index("createdAt")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * Original amount in the currency selected by the user.
     */
    val amount: Double,
    /**
     * ISO 4217 currency code of the original amount.
     */
    val currency: String = "PEN",
    /**
     * Amount converted to PEN at the moment the expense was saved.
     * Immutable: future rate changes do not affect historical expenses.
     */
    val amountInPEN: Double = amount,
    /**
     * Exchange rate used when this expense was saved.
     * Represents "1 PEN = exchangeRateUsed [currency]".
     */
    val exchangeRateUsed: Double = 1.0,
    val categoryId: Long,
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
