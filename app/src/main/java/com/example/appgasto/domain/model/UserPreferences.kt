package com.example.appgasto.domain.model

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val monthlyBudget: Double = 0.0,
    val budgetEnabled: Boolean = false,
    val budgetChartStyle: BudgetChartStyle = BudgetChartStyle.CIRCULAR,
    val ratesUpdatedAt: Long = 0L,
    val baseCurrency: Currency = Currency.PEN,
    val fontScale: FontScale = FontScale.NORMAL,
    val onboardingCompleted: Boolean = false
)
