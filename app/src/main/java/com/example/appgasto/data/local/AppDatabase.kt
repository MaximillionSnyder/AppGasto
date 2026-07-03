package com.example.appgasto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Expense::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
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
                .build()
        }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    """
                    INSERT INTO categories (name, colorHex, iconName, stringKey) VALUES
                    ('Food', '#FF5722', 'restaurant', 'cat_food'),
                    ('Transport', '#2196F3', 'directions_car', 'cat_transport'),
                    ('Leisure', '#9C27B0', 'sports_esports', 'cat_leisure'),
                    ('Home', '#4CAF50', 'home', 'cat_home'),
                    ('Health', '#F44336', 'local_pharmacy', 'cat_health'),
                    ('Clothing', '#FFC107', 'checkroom', 'cat_clothing'),
                    ('Other', '#9E9E9E', 'more_horiz', 'cat_other')
                    """.trimIndent()
                )
            }
        }
    }
}
