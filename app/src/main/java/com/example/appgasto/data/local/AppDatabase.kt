package com.example.appgasto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [Expense::class, Category::class, ExchangeRateEntity::class, CategoryBudget::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao

    companion object {
        const val DATABASE_NAME = "appgasto_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN currency TEXT NOT NULL DEFAULT 'PEN'")
                db.execSQL("ALTER TABLE expenses ADD COLUMN amountInPEN REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN exchangeRateUsed REAL NOT NULL DEFAULT 1.0")
                db.execSQL("UPDATE expenses SET amountInPEN = amount, exchangeRateUsed = 1.0 WHERE currency = 'PEN'")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exchange_rates (
                        code TEXT NOT NULL PRIMARY KEY,
                        rateToPen REAL NOT NULL,
                        fetchedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_created_at ON expenses(createdAt)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS category_budgets (
                        categoryId INTEGER NOT NULL PRIMARY KEY,
                        amount REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun create(context: Context): AppDatabase {
            val appContext = context.applicationContext
            System.loadLibrary("sqlcipher")
            val passphrase = DbKeyStore.getOrCreatePassphraseHex(appContext)
            SqlCipherMigration.migrateIfNeeded(appContext, DATABASE_NAME, passphrase)
            val factory = SupportOpenHelperFactory(passphrase.toByteArray(Charsets.UTF_8))

            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
