package com.example.appgasto.ui.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import com.example.appgasto.data.repository.ExpenseRepository
import com.example.appgasto.widget.ExpenseWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class ListUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val selectedCategoryId: Long? = null,
    val availableMonths: List<YearMonth> = emptyList(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                combine(
                    expenseRepository.getAllExpenses(),
                    expenseRepository.getAllCategories()
                ) { expenses, categories -> expenses to categories }
                    .collect { (expenses, categories) ->
                        val state = _uiState.value
                        val months = expenses.map { YearMonth.from(it.createdAt) }.distinct().sortedDescending()
                        _uiState.value = state.copy(
                            expenses = applyFilters(expenses, state),
                            categories = categories.associateBy { it.id },
                            availableMonths = months,
                            isLoading = false
                        )
                    }
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
            val allExpenses = expenseRepository.getAllExpenses().first()
            _uiState.value = _uiState.value.copy(
                expenses = applyFilters(allExpenses, _uiState.value)
            )
        }
    }

    private fun applyFilters(
        expenses: List<Expense>,
        state: ListUiState
    ): List<Expense> {
        return expenses.filter { expense ->
            val categoryMatch = state.selectedCategoryId?.let { expense.categoryId == it } ?: true
            val startMatch = state.startDate?.let {
                !expense.createdAt.toLocalDate().isBefore(it)
            } ?: true
            val endMatch = state.endDate?.let {
                !expense.createdAt.toLocalDate().isAfter(it)
            } ?: true
            categoryMatch && startMatch && endMatch
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            ExpenseWidget.updateAll(context)
        }
    }
}
