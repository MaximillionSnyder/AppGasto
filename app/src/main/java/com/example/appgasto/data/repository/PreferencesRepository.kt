package com.example.appgasto.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appgasto.domain.model.AppLanguage
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
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val BUDGET_ENABLED = booleanPreferencesKey("budget_enabled")
        val BUDGET_ALERT_80_MONTH = stringPreferencesKey("budget_alert_80_month")
        val BUDGET_ALERT_100_MONTH = stringPreferencesKey("budget_alert_100_month")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[Keys.THEME_MODE]?.let { safeValueOf<ThemeMode>(it) } ?: ThemeMode.SYSTEM,
            language = prefs[Keys.LANGUAGE]?.let { safeValueOf<AppLanguage>(it) } ?: AppLanguage.SYSTEM,
            monthlyBudget = prefs[Keys.MONTHLY_BUDGET] ?: 0.0,
            budgetEnabled = prefs[Keys.BUDGET_ENABLED] ?: false
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

    suspend fun setBudgetAlertSent(level: Int) {
        context.dataStore.edit { prefs ->
            val month = java.time.YearMonth.now().toString()
            when (level) {
                80 -> prefs[Keys.BUDGET_ALERT_80_MONTH] = month
                100 -> prefs[Keys.BUDGET_ALERT_100_MONTH] = month
            }
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
