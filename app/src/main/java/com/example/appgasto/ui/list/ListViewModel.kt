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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class ListUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val availableMonths: List<YearMonth> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(Triple<Long?, LocalDate?, LocalDate?>(null, null, null))

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
                    _filterState.flatMapLatest { (categoryId, startDate, endDate) ->
                        expenseRepository.getFilteredExpenses(categoryId, startDate, endDate)
                    }.flowOn(Dispatchers.IO),
                    expenseRepository.getAllCategories().flowOn(Dispatchers.IO),
                    expenseRepository.getAllDates().flowOn(Dispatchers.IO).map { dates ->
                        dates.map { YearMonth.from(it) }.distinct().sortedDescending()
                    }
                ) { expenses, categories, months -> Triple(expenses, categories, months) }
                    .collect { (expenses, categories, months) ->
                        _uiState.value = ListUiState(
                            expenses = expenses,
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
        _filterState.value = Triple(categoryId, startDate, endDate)
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            ExpenseWidget.updateAll(context)
        }
    }
}
