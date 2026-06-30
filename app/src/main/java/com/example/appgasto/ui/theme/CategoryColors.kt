package com.example.appgasto.ui.theme

import androidx.compose.ui.graphics.Color

object CategoryColors {
    val food = Color(0xFFFF5722)
    val transport = Color(0xFF2196F3)
    val leisure = Color(0xFF9C27B0)
    val home = Color(0xFF4CAF50)
    val health = Color(0xFFF44336)
    val clothing = Color(0xFFFFC107)
    val other = Color(0xFF9E9E9E)

    val foodDark = Color(0xFFFF8A65)
    val transportDark = Color(0xFF64B5F6)
    val leisureDark = Color(0xFFCE93D8)
    val homeDark = Color(0xFF81C784)
    val healthDark = Color(0xFFE57373)
    val clothingDark = Color(0xFFFFD54F)
    val otherDark = Color(0xFFBDBDBD)

    fun getById(categoryId: Long, isDark: Boolean = false): Color {
        val colors = if (isDark) darkMap() else map()
        return colors[categoryId] ?: other
    }

    private fun map() = mapOf(
        1L to food,
        2L to transport,
        3L to leisure,
        4L to home,
        5L to health,
        6L to clothing,
        7L to other
    )

    private fun darkMap() = mapOf(
        1L to foodDark,
        2L to transportDark,
        3L to leisureDark,
        4L to homeDark,
        5L to healthDark,
        6L to clothingDark,
        7L to otherDark
    )
}
