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
    val error: String? = null
)

@HiltViewModel
class AddEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseRepository: ExpenseRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val receiptOcrService: ReceiptOcrService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun loadExpense(expenseId: Long?) {
        viewModelScope.launch {
            val categories = expenseRepository.getAllCategories().first()
            _uiState.value = _uiState.value.copy(categories = categories, isLoading = false)

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
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            try {
                val data = receiptOcrService.parseReceiptImage(imageUri)
                val current = _uiState.value
                val scannedAmount = data.total?.let { raw ->
                    val normalized = raw.replace(',', '.')
                    if (normalized.toDoubleOrNull() != null) normalized else null
                }
                val scannedCurrency = data.currencyCode
                    ?.takeIf { it in Currency.supportedCodes() }
                _uiState.value = current.copy(
                    amount = scannedAmount ?: current.amount,
                    currency = scannedCurrency ?: current.currency,
                    date = data.date ?: current.date,
                    note = data.merchant ?: current.note,
                    isScanning = false
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
                if (state.isEditing) {
                    expenseRepository.updateExpense(expense)
                } else {
                    expenseRepository.insertExpense(expense)
                }
                ExpenseWidget.updateAll(context)
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
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

    fun setScanError() {
        _uiState.value = _uiState.value.copy(error = context.getString(R.string.scan_error))
    }
}
