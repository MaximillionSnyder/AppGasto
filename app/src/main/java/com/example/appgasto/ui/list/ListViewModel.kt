package com.example.appgasto.ui.list

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
import javax.inject.Inject

data class ListUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val selectedCategoryId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val expenses = expenseRepository.getAllExpenses()
                val categories = expenseRepository.getAllCategories()
                    .associateBy { it.id }
                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    categories = categories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun applyFilters(categoryId: Long?, startDate: LocalDate?, endDate: LocalDate?) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            startDate = startDate,
            endDate = endDate
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val filtered = when {
                    categoryId != null && startDate != null && endDate != null ->
                        expenseRepository.getExpensesByDateRange(startDate, endDate)
                            .filter { it.categoryId == categoryId }
                    categoryId != null ->
                        expenseRepository.getAllExpenses()
                            .filter { it.categoryId == categoryId }
                    startDate != null && endDate != null ->
                        expenseRepository.getExpensesByDateRange(startDate, endDate)
                    else -> expenseRepository.getAllExpenses()
                }
                _uiState.value = _uiState.value.copy(
                    expenses = filtered,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            loadAll()
        }
    }
}
