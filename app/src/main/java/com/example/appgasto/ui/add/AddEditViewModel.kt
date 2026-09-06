package com.example.appgasto.ui.add

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.R
import com.example.appgasto.data.currency.ExchangeRateRepository
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import com.example.appgasto.data.ocr.ReceiptOcrService
import com.example.appgasto.data.repository.ExpenseRepository
import com.example.appgasto.data.repository.PreferencesRepository
import com.example.appgasto.domain.model.Currency
import com.example.appgasto.widget.ExpenseWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class AddEditUiState(
    val categories: List<Category> = emptyList(),
    val amount: String = "",
    val currency: String = Currency.PEN.code,
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val originalCreatedAt: LocalDateTime? = null,
    val originalAmount: Double? = null,
    val originalCurrency: String? = null,
    val originalAmountInPEN: Double? = null,
    val originalExchangeRateUsed: Double? = null,
    val isEditing: Boolean = false,
    val expenseId: Long? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isScanning: Boolean = false,
    val isPro: Boolean = false,
    val scanCount: Int = 0,
    val hasPendingReceipt: Boolean = false,
    val pendingReceiptPath: String? = null,
    val error: String? = null
)

@HiltViewModel
class AddEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseRepository: ExpenseRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val receiptOcrService: ReceiptOcrService,
    private val preferencesRepository: PreferencesRepository,
    private val receiptRepository: com.example.appgasto.data.receipts.ReceiptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private var pendingReceiptTmp: java.io.File? = null

    fun loadExpense(expenseId: Long?) {
        pendingReceiptTmp?.let { old -> viewModelScope.launch { receiptRepository.discardTemp(old) } }
        pendingReceiptTmp = null
        viewModelScope.launch {
            val categories = expenseRepository.getAllCategories().first()
            val prefs = preferencesRepository.preferencesFlow.first()
            val baseCurrency = prefs.baseCurrency.code
            val currentMonth = java.time.YearMonth.now().toString()
            val scanCount = if (prefs.scanCountMonth == currentMonth) prefs.scanCount else 0
            _uiState.value = _uiState.value.copy(
                categories = categories,
                currency = baseCurrency,
                isPro = prefs.isPro,
                scanCount = scanCount,
                isLoading = false,
                hasPendingReceipt = false,
                pendingReceiptPath = null
            )

            if (expenseId != null) {
                val expense = expenseRepository.getExpenseById(expenseId)
                if (expense != null) {
                    _uiState.value = _uiState.value.copy(
                        amount = if (expense.amount == expense.amount.toLong().toDouble())
                            expense.amount.toLong().toString()
                        else
                            expense.amount.toString(),
                        currency = expense.currency,
                        selectedCategoryId = expense.categoryId,
                        note = expense.note ?: "",
                        date = expense.createdAt.toLocalDate(),
                        originalCreatedAt = expense.createdAt,
                        originalAmount = expense.amount,
                        originalCurrency = expense.currency,
                        originalAmountInPEN = expense.amountInPEN,
                        originalExchangeRateUsed = expense.exchangeRateUsed,
                        isEditing = true,
                        expenseId = expense.id
                    )
                }
            }
        }
    }

    fun updateAmount(amount: String) {
        val normalized = amount.replace(',', '.')
        if (normalized.count { it == '.' } > 1) return
        val parts = normalized.split('.')
        if (parts.size == 2 && parts[1].length > 4) return
        _uiState.value = _uiState.value.copy(amount = normalized)
    }

    fun updateCategory(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun updateCurrency(currencyCode: String) {
        _uiState.value = _uiState.value.copy(currency = currencyCode.uppercase())
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun updateDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun handleScanResult(imageUri: Uri?) {
        if (imageUri == null) return
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            if (!prefs.isPro && !preferencesRepository.tryConsumeScanSlot()) {
                _uiState.value = _uiState.value.copy(
                    error = context.getString(
                        R.string.pro_scan_limit,
                        PreferencesRepository.SCAN_MONTHLY_LIMIT
                    )
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            // Copia privada temporal (la Uri del escáner caduca). El archivo
            // definitivo solo se crea al pulsar Guardar.
            val stagedTmp = runCatching { receiptRepository.stageTemp(imageUri) }.getOrNull()
            if (stagedTmp != null) {
                pendingReceiptTmp?.let { runCatching { receiptRepository.discardTemp(it) } }
                pendingReceiptTmp = stagedTmp
            }
            try {
                val data = receiptOcrService.parseReceiptImage(imageUri)
                val current = _uiState.value
                val scannedAmount = data.total?.let { raw ->
                    val normalized = raw.replace(',', '.')
                    if (normalized.toDoubleOrNull() != null) normalized else null
                }
                val scannedCurrency = data.currencyCode
                    ?.takeIf { it in Currency.supportedCodes() }
                val newPrefs = preferencesRepository.preferencesFlow.first()
                val currentMonth = java.time.YearMonth.now().toString()
                _uiState.value = current.copy(
                    amount = scannedAmount ?: current.amount,
                    currency = scannedCurrency ?: current.currency,
                    date = data.date ?: current.date,
                    note = data.merchant ?: current.note,
                    isPro = prefs.isPro,
                    scanCount = if (newPrefs.scanCountMonth == currentMonth) newPrefs.scanCount else current.scanCount + 1,
                    isScanning = false,
                    hasPendingReceipt = pendingReceiptTmp != null,
                    pendingReceiptPath = pendingReceiptTmp?.absolutePath
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = context.getString(R.string.scan_error)
                )
            }
        }
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(error = context.getString(R.string.error_invalid_amount))
            return
        }
        if (state.selectedCategoryId == null) {
            _uiState.value = state.copy(error = context.getString(R.string.error_select_category))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val createdAt = state.originalCreatedAt?.let { original ->
                    LocalDateTime.of(state.date, original.toLocalTime())
                } ?: LocalDateTime.of(state.date, LocalTime.now())

                val currency = state.currency.uppercase()

                // Preserve immutable conversion for existing expenses unless amount or currency changed.
                val conversionChanged = state.isEditing && (
                    amount != state.originalAmount || currency != state.originalCurrency
                )

                val (amountInPEN, exchangeRateUsed) = if (currency == Currency.PEN.code) {
                    amount to 1.0
                } else if (state.isEditing && !conversionChanged &&
                    state.originalAmountInPEN != null && state.originalExchangeRateUsed != null
                ) {
                    state.originalAmountInPEN to state.originalExchangeRateUsed
                } else {
                    val rate = exchangeRateRepository.getRateToPen(currency)
                    if (rate == null || rate <= 0) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = context.getString(R.string.error_missing_rate, currency)
                        )
                        return@launch
                    }
                    exchangeRateRepository.convertToPEN(amount, currency) to rate
                }

                val expense = Expense(
                    id = state.expenseId ?: 0,
                    amount = amount,
                    currency = currency,
                    amountInPEN = amountInPEN,
                    exchangeRateUsed = exchangeRateUsed,
                    categoryId = state.selectedCategoryId!!,
                    note = state.note.ifBlank { null },
                    createdAt = createdAt
                )
                val savedExpenseId: Long = if (state.isEditing) {
                    expenseRepository.updateExpense(expense)
                    state.expenseId ?: 0L
                } else {
                    expenseRepository.insertExpense(expense)
                }
                // Archivar la foto SOLO al guardar (decisión de producto).
                // La fila de recibo sobrevive al borrado del gasto.
                val tmpToCommit = pendingReceiptTmp
                if (tmpToCommit != null) {
                    runCatching {
                        receiptRepository.commitTemp(
                            tmpToCommit,
                            com.example.appgasto.data.receipts.ReceiptRepository.Snapshot(
                                createdAt = createdAt,
                                amount = amount,
                                currency = currency,
                                merchant = state.note.ifBlank { null },
                                expenseId = savedExpenseId.takeIf { it != 0L }
                            )
                        )
                    }
                    pendingReceiptTmp = null
                }
                ExpenseWidget.updateAll(context)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = true,
                    hasPendingReceipt = false,
                    pendingReceiptPath = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = context.getString(R.string.error_save_detail, e.message ?: "")
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun removePendingReceipt() {
        viewModelScope.launch {
            pendingReceiptTmp?.let { receiptRepository.discardTemp(it) }
            pendingReceiptTmp = null
            _uiState.value = _uiState.value.copy(hasPendingReceipt = false, pendingReceiptPath = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        val tmp = pendingReceiptTmp
        // Si se sale sin guardar, el temporal no se archiva.
        if (tmp != null && _uiState.value.isSaved.not()) {
            // onCleared no puede usar viewModelScope; borrado best-effort.
            runCatching { if (tmp.exists()) tmp.delete() }
        }
    }

    fun setScanError() {
        _uiState.value = _uiState.value.copy(error = context.getString(R.string.scan_error))
    }
}
