package com.example.appgasto.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.BuildConfig
import com.example.appgasto.data.backup.BackupManager
import com.example.appgasto.data.backup.ExpenseCsvExporter
import com.example.appgasto.data.currency.ExchangeRateRepository
import com.example.appgasto.data.repository.ExpenseRepository
import com.example.appgasto.data.repository.PreferencesRepository
import com.example.appgasto.data.updater.GitHubRelease
import com.example.appgasto.domain.model.AppLanguage
import com.example.appgasto.domain.model.Currency
import com.example.appgasto.domain.model.FontScale
import com.example.appgasto.domain.model.ThemeMode
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

sealed class UpdateEvent {
    data object NoUpdate : UpdateEvent()
    data class Available(val release: GitHubRelease) : UpdateEvent()
    data class Error(val message: String) : UpdateEvent()
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val monthlyBudget: Double = 0.0,
    val budgetEnabled: Boolean = false,
    val monthlyExpenseTotal: Double = 0.0,
    val ratesUpdatedAt: Long = 0L,
    val isRefreshingRates: Boolean = false,
    val baseCurrency: Currency = Currency.PEN,
    val fontScale: FontScale = FontScale.NORMAL,
    val isCheckingUpdate: Boolean = false,
    val isDownloading: Boolean = false,
    val updateRelease: GitHubRelease? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val backupManager: BackupManager,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val expenseRepository: ExpenseRepository,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val gson = Gson()
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _updateEvent = Channel<UpdateEvent>(Channel.BUFFERED)
    val updateEvent = _updateEvent.receiveAsFlow()

    fun dismissUpdateDialog() {
        _uiState.value = _uiState.value.copy(updateRelease = null)
    }

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                val rateToBase = if (prefs.baseCurrency == Currency.PEN) 1.0
                    else exchangeRateRepository.getRateToPen(prefs.baseCurrency.code) ?: 1.0
                val monthTotal = expenseRepository.getCurrentMonthTotal() * rateToBase
                _uiState.value = SettingsUiState(
                    themeMode = prefs.themeMode,
                    language = prefs.language,
                    monthlyBudget = prefs.monthlyBudget,
                    budgetEnabled = prefs.budgetEnabled,
                    monthlyExpenseTotal = monthTotal,
                    ratesUpdatedAt = prefs.ratesUpdatedAt,
                    baseCurrency = prefs.baseCurrency,
                    fontScale = prefs.fontScale
                )
            }
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingUpdate = true)
            val result = fetchLatestRelease()
            _uiState.value = _uiState.value.copy(isCheckingUpdate = false)
            when {
                result.isFailure -> {
                    _updateEvent.send(UpdateEvent.Error(
                        result.exceptionOrNull()?.localizedMessage ?: "Error checking update"
                    ))
                }
                result.getOrNull() == null -> {
                    _updateEvent.send(UpdateEvent.NoUpdate)
                }
                else -> {
                    _uiState.value = _uiState.value.copy(updateRelease = result.getOrNull())
                    _updateEvent.send(UpdateEvent.Available(result.getOrNull()!!))
                }
            }
        }
    }

    private suspend fun fetchLatestRelease(): Result<GitHubRelease?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/MaximillionSnyder/AppGasto/releases/latest")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext if (response.code == 404) {
                        Result.success(null)
                    } else {
                        Result.failure(Exception("GitHub API error: ${response.code}"))
                    }
                }
                val release = gson.fromJson(response.body?.string(), GitHubRelease::class.java)
                if (release == null) return@withContext Result.success(null)

                val remoteVersion = release.tagName.removePrefix("v").trim()
                val localVersion = BuildConfig.VERSION_NAME.trim()
                if (compareVersions(remoteVersion, localVersion) <= 0) {
                    return@withContext Result.success(null)
                }
                Result.success(release)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun downloadAndInstall(onComplete: (String) -> Unit) {
        val release = _uiState.value.updateRelease ?: return
        val apkAsset = release.assets?.firstOrNull { it.name.endsWith(".apk") }
        if (apkAsset == null) {
            onComplete("No APK found in release")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true)
            val result = withContext(Dispatchers.IO) {
                downloadApk(apkAsset.browserDownloadUrl)
            }
            _uiState.value = _uiState.value.copy(isDownloading = false)
            if (result.isSuccess) {
                installApk(result.getOrNull()!!)
                onComplete("")
            } else {
                onComplete(result.exceptionOrNull()?.localizedMessage ?: "Download failed")
            }
        }
    }

    private suspend fun downloadApk(url: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Download error: ${response.code}"))
                }
                val file = File(context.cacheDir, "update.apk")
                response.body?.byteStream()?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                Result.success(file)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun installApk(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val a = parts1.getOrElse(i) { 0 }
            val b = parts2.getOrElse(i) { 0 }
            if (a > b) return 1
            if (a < b) return -1
        }
        return 0
    }

    // ── Existing methods ──

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

    fun setFontScale(scale: FontScale) {
        viewModelScope.launch {
            preferencesRepository.setFontScale(scale)
        }
    }

    fun setBaseCurrency(currency: Currency) {
        viewModelScope.launch {
            val oldBase = _uiState.value.baseCurrency
            val budget = _uiState.value.monthlyBudget
            preferencesRepository.setBaseCurrency(currency)
            // The budget is defined in the base currency: reconvert the stored
            // amount so it keeps representing the same value in the new base.
            if (budget > 0 && oldBase != currency) {
                val converted = exchangeRateRepository.convert(budget, oldBase.code, currency.code)
                if (converted != null) {
                    preferencesRepository.setMonthlyBudget(converted)
                }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            expenseRepository.deleteAllExpenses()
            preferencesRepository.setMonthlyBudget(0.0)
            preferencesRepository.setBudgetEnabled(false)
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
