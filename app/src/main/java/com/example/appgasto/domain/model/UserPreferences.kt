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
    val onboardingCompleted: Boolean = false,
    val advancedBudgetEnabled: Boolean = false,
    val isPro: Boolean = false,
    val scanCountMonth: String = "",
    val scanCount: Int = 0,
    val advancedGraceUntil: Long = 0L
)

fun UserPreferences.isProEffective(now: Long = System.currentTimeMillis()): Boolean =
    isPro || now < advancedGraceUntil
