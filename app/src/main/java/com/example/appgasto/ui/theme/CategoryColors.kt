package com.example.appgasto.ui.theme

import androidx.compose.ui.graphics.Color

object CategoryColors {
    val food = Color(0xFFFF6D00)
    val transport = Color(0xFF2979FF)
    val leisure = Color(0xFFAA00FF)
    val home = Color(0xFF00C853)
    val health = Color(0xFFFF1744)
    val clothing = Color(0xFFFFAB00)
    val other = Color(0xFF78909C)

    val foodDark = Color(0xFFFF9E40)
    val transportDark = Color(0xFF64B5F6)
    val leisureDark = Color(0xFFCE93D8)
    val homeDark = Color(0xFF69F0AE)
    val healthDark = Color(0xFFFF5252)
    val clothingDark = Color(0xFFFFD740)
    val otherDark = Color(0xFFB0BEC5)

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
