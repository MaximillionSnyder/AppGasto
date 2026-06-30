package com.example.appgasto.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats(StatsPeriod.MONTHLY)
    }

    fun loadStats(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(period = period, isLoading = true)
        viewModelScope.launch {
            try {
                val categories = expenseRepository.getAllCategories()
                val today = LocalDate.now()

                val (startDate, _) = when (period) {
                    StatsPeriod.DAILY -> today to today
                    StatsPeriod.WEEKLY -> {
                        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        start to today
                    }
                    StatsPeriod.MONTHLY -> {
                        today.withDayOfMonth(1) to today
                    }
                }

                val categoryTotals = categories.map { cat ->
                    val total = expenseRepository.getTotalByCategorySince(cat.id, startDate)
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
