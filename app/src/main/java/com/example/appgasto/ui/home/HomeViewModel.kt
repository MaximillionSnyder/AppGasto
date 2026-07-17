package com.example.appgasto.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.data.currency.ExchangeRateRepository
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.CurrencyTotalTuple
import com.example.appgasto.data.local.Expense
import com.example.appgasto.data.repository.ExpenseRepository
import com.example.appgasto.data.repository.PreferencesRepository
import com.example.appgasto.domain.model.Currency
import com.example.appgasto.widget.ExpenseWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val monthCurrencyBreakdown: List<CurrencyTotalTuple> = emptyList(),
    val todayExpenses: List<Expense> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val isLoading: Boolean = true,
    val baseCurrency: Currency = Currency.PEN
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseRepository: ExpenseRepository,
    private val preferencesRepository: PreferencesRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                combine(
                    expenseRepository.getTodayExpenses(),
                    expenseRepository.getAllCategories(),
                    preferencesRepository.preferencesFlow
                ) { expenses, categories, prefs -> Triple(expenses, categories, prefs) }
                    .collect { (expenses, categories, prefs) ->
                        val baseCurrency = prefs.baseCurrency
                        val rateToBase = if (baseCurrency == Currency.PEN) 1.0
                            else exchangeRateRepository.getRateToPen(baseCurrency.code) ?: 1.0

                        val todayPEN = expenseRepository.getTodayTotal()
                        val weekPEN = expenseRepository.getCurrentWeekTotal()
                        val monthPEN = expenseRepository.getCurrentMonthTotal()
                        val monthStart = java.time.LocalDate.now().withDayOfMonth(1)
                        val monthCurrencyBreakdown = expenseRepository.getTotalByCurrencySince(monthStart)

                        _uiState.value = HomeUiState(
                            todayTotal = todayPEN * rateToBase,
                            weekTotal = weekPEN * rateToBase,
                            monthTotal = monthPEN * rateToBase,
                            monthCurrencyBreakdown = monthCurrencyBreakdown,
                            todayExpenses = expenses,
                            categories = categories.associateBy { it.id },
                            isLoading = false,
                            baseCurrency = baseCurrency
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            ExpenseWidget.updateAll(context)
        }
    }
}
