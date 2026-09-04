package com.example.appgasto.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appgasto.domain.model.AppLanguage
import com.example.appgasto.domain.model.BudgetChartStyle
import com.example.appgasto.domain.model.Currency
import com.example.appgasto.domain.model.FontScale
import com.example.appgasto.domain.model.ThemeMode
import com.example.appgasto.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val SCAN_MONTHLY_LIMIT = 10
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val BUDGET_ENABLED = booleanPreferencesKey("budget_enabled")
        val BUDGET_CHART_STYLE = stringPreferencesKey("budget_chart_style")
        val BUDGET_ALERT_80_MONTH = stringPreferencesKey("budget_alert_80_month")
        val BUDGET_ALERT_100_MONTH = stringPreferencesKey("budget_alert_100_month")
        val RATES_UPDATED_AT = longPreferencesKey("rates_updated_at")
        val BASE_CURRENCY = stringPreferencesKey("base_currency")
        val FONT_SCALE = stringPreferencesKey("font_scale")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ADVANCED_BUDGET_ENABLED = booleanPreferencesKey("advanced_budget_enabled")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val SCAN_MONTH = stringPreferencesKey("scan_month")
        val SCAN_COUNT = intPreferencesKey("scan_count")
        val ADVANCED_GRACE_UNTIL = longPreferencesKey("advanced_grace_until")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[Keys.THEME_MODE]?.let { safeValueOf<ThemeMode>(it) } ?: ThemeMode.SYSTEM,
            language = prefs[Keys.LANGUAGE]?.let { safeValueOf<AppLanguage>(it) } ?: AppLanguage.SYSTEM,
            monthlyBudget = prefs[Keys.MONTHLY_BUDGET] ?: 0.0,
            budgetEnabled = prefs[Keys.BUDGET_ENABLED] ?: false,
            budgetChartStyle = prefs[Keys.BUDGET_CHART_STYLE]?.let { safeValueOf<BudgetChartStyle>(it) }
                ?: BudgetChartStyle.CIRCULAR,
            ratesUpdatedAt = prefs[Keys.RATES_UPDATED_AT] ?: 0L,
            baseCurrency = prefs[Keys.BASE_CURRENCY]?.let { Currency.fromCode(it) } ?: Currency.PEN,
            fontScale = prefs[Keys.FONT_SCALE]?.let { safeValueOf<FontScale>(it) } ?: FontScale.NORMAL,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            advancedBudgetEnabled = prefs[Keys.ADVANCED_BUDGET_ENABLED] ?: false,
            isPro = prefs[Keys.IS_PRO] ?: false,
            scanCountMonth = prefs[Keys.SCAN_MONTH] ?: "",
            scanCount = prefs[Keys.SCAN_COUNT] ?: 0,
            advancedGraceUntil = prefs[Keys.ADVANCED_GRACE_UNTIL] ?: 0L
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.name
        }
    }

    suspend fun setMonthlyBudget(budget: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MONTHLY_BUDGET] = budget
        }
    }

    suspend fun setBudgetEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BUDGET_ENABLED] = enabled
        }
    }

    suspend fun setBudgetChartStyle(style: BudgetChartStyle) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BUDGET_CHART_STYLE] = style.name
        }
    }

    suspend fun setBudgetAlertSent(level: Int) {
        context.dataStore.edit { prefs ->
            val month = java.time.YearMonth.now().toString()
            when (level) {
                80 -> prefs[Keys.BUDGET_ALERT_80_MONTH] = month
                100 -> prefs[Keys.BUDGET_ALERT_100_MONTH] = month
            }
        }
    }

    suspend fun setRatesUpdatedAt(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.RATES_UPDATED_AT] = timestamp
        }
    }

    suspend fun setBaseCurrency(currency: Currency) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BASE_CURRENCY] = currency.code
        }
    }

    suspend fun setFontScale(scale: FontScale) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_SCALE] = scale.name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setAdvancedBudgetEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ADVANCED_BUDGET_ENABLED] = enabled
        }
    }

    suspend fun setPro(pro: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PRO] = pro
        }
    }

    suspend fun tryConsumeScanSlot(): Boolean {
        val currentMonth = java.time.YearMonth.now().toString()
        val data = context.dataStore.data.first()
        val storedMonth = data[Keys.SCAN_MONTH] ?: ""
        val count = if (storedMonth == currentMonth) data[Keys.SCAN_COUNT] ?: 0 else 0
        if (count >= SCAN_MONTHLY_LIMIT) return false
        context.dataStore.edit { prefs ->
            prefs[Keys.SCAN_MONTH] = currentMonth
            prefs[Keys.SCAN_COUNT] = count + 1
        }
        return true
    }

    suspend fun migrateLegacyAdvancedUsers() {
        val prefs = context.dataStore.data.first()
        if (prefs[Keys.ADVANCED_BUDGET_ENABLED] == true) {
            context.dataStore.edit {
                it[Keys.ADVANCED_GRACE_UNTIL] = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
            }
        }
    }

    suspend fun completeOnboarding(currency: Currency, themeMode: ThemeMode = ThemeMode.SYSTEM) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BASE_CURRENCY] = currency.code
            prefs[Keys.THEME_MODE] = themeMode.name
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun wasBudgetAlertSentThisMonth(level: Int): Boolean {
        val currentMonth = java.time.YearMonth.now().toString()
        return context.dataStore.data.first().let { prefs ->
            val stored = when (level) {
                80 -> prefs[Keys.BUDGET_ALERT_80_MONTH]
                100 -> prefs[Keys.BUDGET_ALERT_100_MONTH]
                else -> null
            }
            stored == currentMonth
        }
    }

    private inline fun <reified T : Enum<T>> safeValueOf(value: String): T? {
        return try {
            java.lang.Enum.valueOf(T::class.java, value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
