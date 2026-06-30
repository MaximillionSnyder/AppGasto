package com.example.appgasto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Expense::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "appgasto_database"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(SeedCallback())
                .fallbackToDestructiveMigration()
                .build()
        }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    """
                    INSERT INTO categories (name, colorHex, iconName) VALUES
                    ('Comida', '#FF5722', 'restaurant'),
                    ('Transporte', '#2196F3', 'directions_car'),
                    ('Ocio', '#9C27B0', 'sports_esports'),
                    ('Hogar', '#4CAF50', 'home'),
                    ('Salud', '#F44336', 'local_pharmacy'),
                    ('Ropa', '#FFC107', 'checkroom'),
                    ('Otros', '#9E9E9E', 'more_horiz')
                    """.trimIndent()
                )
            }
        }
    }
}
