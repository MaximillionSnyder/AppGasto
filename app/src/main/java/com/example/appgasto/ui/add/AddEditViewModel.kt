package com.example.appgasto.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import com.example.appgasto.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class AddEditUiState(
    val categories: List<Category> = emptyList(),
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val isEditing: Boolean = false,
    val expenseId: Long? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun loadExpense(expenseId: Long?) {
        viewModelScope.launch {
            val categories = expenseRepository.getAllCategories()
            _uiState.value = _uiState.value.copy(categories = categories, isLoading = false)

            if (expenseId != null) {
                val expense = expenseRepository.getExpenseById(expenseId)
                if (expense != null) {
                    _uiState.value = _uiState.value.copy(
                        amount = if (expense.amount == expense.amount.toLong().toDouble())
                            expense.amount.toLong().toString()
                        else
                            expense.amount.toString(),
                        selectedCategoryId = expense.categoryId,
                        note = expense.note ?: "",
                        date = expense.createdAt.toLocalDate(),
                        isEditing = true,
                        expenseId = expense.id
                    )
                }
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun updateCategory(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun updateDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(error = "Ingresa un monto válido")
            return
        }
        if (state.selectedCategoryId == null) {
            _uiState.value = state.copy(error = "Selecciona una categoría")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val expense = Expense(
                    id = state.expenseId ?: 0,
                    amount = amount,
                    categoryId = state.selectedCategoryId!!,
                    note = state.note.ifBlank { null },
                    createdAt = LocalDateTime.of(state.date, LocalTime.now())
                )
                if (state.isEditing) {
                    expenseRepository.updateExpense(expense)
                } else {
                    expenseRepository.insertExpense(expense)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
