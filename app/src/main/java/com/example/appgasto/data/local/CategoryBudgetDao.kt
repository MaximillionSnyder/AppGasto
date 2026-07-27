package com.example.appgasto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetDao {

    @Query("SELECT * FROM category_budgets ORDER BY categoryId ASC")
    fun getAll(): Flow<List<CategoryBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: CategoryBudget)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM category_budgets")
    suspend fun getTotalBudget(): Double
}
