package com.example.appgasto.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.appgasto.domain.model.FontScale
import com.example.appgasto.domain.model.ThemeMode

/** `true` when the HIGH_CONTRAST theme is active. Read by CategoryColors call sites. */
val LocalIsHighContrast = compositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    surface = LightSurface,
    onSurface = LightOnSurface,
    background = LightBackground,
    onBackground = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    onError = LightOnError,
    outline = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    outline = DarkOutline
)

private val MatrixColorScheme = darkColorScheme(
    primary = MatrixPrimary,
    onPrimary = MatrixOnPrimary,
    primaryContainer = MatrixPrimaryContainer,
    onPrimaryContainer = MatrixOnPrimaryContainer,
    secondary = MatrixSecondary,
    onSecondary = MatrixOnSecondary,
    secondaryContainer = MatrixSecondaryContainer,
    onSecondaryContainer = MatrixOnSecondaryContainer,
    tertiary = MatrixTertiary,
    onTertiary = MatrixOnTertiary,
    tertiaryContainer = MatrixTertiaryContainer,
    onTertiaryContainer = MatrixOnTertiaryContainer,
    surface = MatrixSurface,
    onSurface = MatrixOnSurface,
    background = MatrixBackground,
    onBackground = MatrixOnBackground,
    surfaceVariant = MatrixSurfaceVariant,
    onSurfaceVariant = MatrixOnSurfaceVariant,
    error = MatrixError,
    onError = MatrixOnError,
    outline = MatrixOutline
)

private val HighContrastColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = HighContrastOnPrimary,
    primaryContainer = HighContrastPrimaryContainer,
    onPrimaryContainer = HighContrastOnPrimaryContainer,
    secondary = HighContrastSecondary,
    onSecondary = HighContrastOnSecondary,
    secondaryContainer = HighContrastSecondaryContainer,
    onSecondaryContainer = HighContrastOnSecondaryContainer,
    tertiary = HighContrastTertiary,
    onTertiary = HighContrastOnTertiary,
    tertiaryContainer = HighContrastTertiaryContainer,
    onTertiaryContainer = HighContrastOnTertiaryContainer,
    surface = HighContrastSurface,
    onSurface = HighContrastOnSurface,
    background = HighContrastBackground,
    onBackground = HighContrastOnBackground,
    surfaceVariant = HighContrastSurfaceVariant,
    onSurfaceVariant = HighContrastOnSurfaceVariant,
    error = HighContrastError,
    onError = HighContrastOnError,
    outline = HighContrastOutline
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AppGastoTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontScale: FontScale = FontScale.NORMAL,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.MATRIX -> true
        ThemeMode.HIGH_CONTRAST -> false
    }

    val colorScheme = when (themeMode) {
        ThemeMode.MATRIX -> MatrixColorScheme
        ThemeMode.DARK, ThemeMode.SYSTEM -> if (isDark) DarkColorScheme else LightColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.HIGH_CONTRAST -> HighContrastColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    // Font scale is applied ONLY via LocalDensity: scaling the Typography sizes
    // too would double-apply the factor (Typography uses sp, which density scales).
    val density = LocalDensity.current
    val scaledDensity = remember(density, fontScale) {
        Density(density.density, density.fontScale * fontScale.scale)
    }

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalIsHighContrast provides (themeMode == ThemeMode.HIGH_CONTRAST)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
