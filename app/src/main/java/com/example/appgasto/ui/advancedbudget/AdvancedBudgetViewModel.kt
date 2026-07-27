package com.example.appgasto.ui.advancedbudget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.CategoryBudget
import com.example.appgasto.data.local.CategoryBudgetDao
import com.example.appgasto.data.repository.ExpenseRepository
import com.example.appgasto.data.repository.PreferencesRepository
import com.example.appgasto.domain.model.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AdvancedBudgetUiState(
    val categories: List<Category> = emptyList(),
    val categoryBudgets: Map<Long, Double> = emptyMap(),
    val monthlyBudget: Double = 0.0,
    val baseCurrency: Currency = Currency.PEN,
    val totalAllocated: Double = 0.0,
    val isExceeded: Boolean = false,
    val budgetNotConfigured: Boolean = true
)

@HiltViewModel
class AdvancedBudgetViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryBudgetDao: CategoryBudgetDao,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvancedBudgetUiState())
    val uiState: StateFlow<AdvancedBudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                expenseRepository.getAllCategories(),
                categoryBudgetDao.getAll(),
                preferencesRepository.preferencesFlow
            ) { categories, budgets, prefs -> Triple(categories, budgets, prefs) }
                .flowOn(Dispatchers.IO)
                .collect { (categories, budgets, prefs) ->
                    withContext(Dispatchers.IO) {
                        val budgetMap = budgets.associate { it.categoryId to it.amount }
                        val total = budgets.sumOf { it.amount }
                        _uiState.value = AdvancedBudgetUiState(
                            categories = categories,
                            categoryBudgets = budgetMap,
                            monthlyBudget = prefs.monthlyBudget,
                            baseCurrency = prefs.baseCurrency,
                            totalAllocated = total,
                            isExceeded = total > prefs.monthlyBudget,
                            budgetNotConfigured = prefs.monthlyBudget <= 0.0
                        )
                    }
                }
        }
    }

    fun updateBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                categoryBudgetDao.upsert(CategoryBudget(categoryId = categoryId, amount = amount))
            }
        }
    }
}
