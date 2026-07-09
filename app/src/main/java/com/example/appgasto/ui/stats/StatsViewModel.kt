package com.example.appgasto.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.Expense
import com.example.appgasto.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class StatsPeriod { DAILY, WEEKLY, MONTHLY }

data class CategoryTotal(
    val category: Category,
    val total: Double
)

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.MONTHLY,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val dailyTotals: List<Pair<String, Double>> = emptyList(),
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        val initialPeriod = StatsPeriod.valueOf(savedStateHandle.get<String>("period") ?: "MONTHLY")
        loadStats(initialPeriod)
    }

    fun loadStats(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(period = period, isLoading = true)
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            try {
                combine(
                    expenseRepository.getAllExpenses(),
                    expenseRepository.getAllCategories()
                ) { expenses, categories -> expenses to categories }
                    .collect { (expenses, categories) ->
                        val startDate = when (period) {
                            StatsPeriod.DAILY -> LocalDate.now()
                            StatsPeriod.WEEKLY -> LocalDate.now()
                                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            StatsPeriod.MONTHLY -> LocalDate.now().withDayOfMonth(1)
                        }

                        val filtered = expenses.filter {
                            !it.createdAt.toLocalDate().isBefore(startDate)
                        }

                        val categoryMap = categories.associateBy { it.id }
                        val categoryTotals = categories.map { cat ->
                            val total = filtered
                                .filter { it.categoryId == cat.id }
                                .sumOf { it.amountInPEN }
                            CategoryTotal(cat, total)
                        }.sortedByDescending { it.total }

                        val totalExpenses = categoryTotals.sumOf { it.total }

                        _uiState.value = StatsUiState(
                            period = period,
                            categoryTotals = categoryTotals,
                            dailyTotals = emptyList(),
                            totalExpenses = totalExpenses,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
