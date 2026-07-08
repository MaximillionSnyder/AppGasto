package com.example.appgasto.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.data.backup.BackupManager
import com.example.appgasto.data.backup.ExpenseCsvExporter
import com.example.appgasto.data.currency.ExchangeRateRepository
import com.example.appgasto.data.repository.ExpenseRepository
import com.example.appgasto.data.repository.PreferencesRepository
import com.example.appgasto.domain.model.AppLanguage
import com.example.appgasto.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val monthlyBudget: Double = 0.0,
    val budgetEnabled: Boolean = false,
    val ratesUpdatedAt: Long = 0L,
    val isRefreshingRates: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val backupManager: BackupManager,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.value = SettingsUiState(
                    themeMode = prefs.themeMode,
                    language = prefs.language,
                    monthlyBudget = prefs.monthlyBudget,
                    budgetEnabled = prefs.budgetEnabled,
                    ratesUpdatedAt = prefs.ratesUpdatedAt
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
            val localeTag = when (language) {
                AppLanguage.SPANISH -> "es"
                AppLanguage.ENGLISH -> "en"
                AppLanguage.PORTUGUESE -> "pt"
                AppLanguage.ITALIAN -> "it"
                AppLanguage.GERMAN -> "de"
                AppLanguage.JAPANESE -> "ja"
                AppLanguage.KOREAN -> "ko"
                AppLanguage.QUECHUA -> "qu"
                AppLanguage.SYSTEM -> ""
            }
            val localeList = if (localeTag.isNotEmpty())
                LocaleListCompat.forLanguageTags(localeTag)
            else
                LocaleListCompat.getEmptyLocaleList()
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    fun setMonthlyBudget(budget: Double) {
        viewModelScope.launch {
            preferencesRepository.setMonthlyBudget(budget)
            preferencesRepository.setBudgetEnabled(true)
        }
    }

    fun setBudgetEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBudgetEnabled(enabled)
        }
    }

    fun refreshRates(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshingRates = true)
            val result = exchangeRateRepository.refreshRates()
            _uiState.value = _uiState.value.copy(isRefreshingRates = false)
            onResult(result.isSuccess)
        }
    }

    suspend fun exportData(outputStream: OutputStream): Result<String> {
        return backupManager.exportToJson(outputStream)
    }

    suspend fun importData(inputStream: InputStream): Result<Int> {
        return backupManager.importFromJson(inputStream)
    }

    suspend fun exportCsv(outputStream: OutputStream): Result<String> {
        return try {
            val expenses = expenseRepository.getAllExpenses().first()
            val categories = expenseRepository.getAllCategoriesSnapshot()
            ExpenseCsvExporter.export(expenses, categories, outputStream)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
