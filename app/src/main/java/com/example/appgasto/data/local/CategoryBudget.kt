package com.example.appgasto.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class CategoryBudget(
    @PrimaryKey val categoryId: Long,
    val amount: Double
)
