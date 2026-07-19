package com.example.appgasto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.appgasto.data.repository.PreferencesRepository
import com.example.appgasto.domain.model.FontScale
import com.example.appgasto.domain.model.ThemeMode
import com.example.appgasto.ui.navigation.AppNavigation
import com.example.appgasto.ui.onboarding.OnboardingScreen
import com.example.appgasto.ui.theme.AppGastoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            val preferences by preferencesRepository.preferencesFlow
                .collectAsState(initial = null)

            val isDark = when (preferences?.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.MATRIX -> true
                ThemeMode.HIGH_CONTRAST -> false
                else -> isSystemInDarkTheme()
            }

            val isMatrix = preferences?.themeMode == ThemeMode.MATRIX
            val scope = rememberCoroutineScope()

            AppGastoTheme(
                themeMode = preferences?.themeMode ?: ThemeMode.SYSTEM,
                fontScale = preferences?.fontScale ?: FontScale.NORMAL
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val prefs = preferences
                    val navController = rememberNavController()
                    when {
                        prefs == null -> Unit
                        !prefs.onboardingCompleted -> {
                            OnboardingScreen(
                                onCurrencyConfirmed = { currency ->
                                    scope.launch {
                                        preferencesRepository.completeOnboarding(currency)
                                    }
                                }
                            )
                        }
                        else -> {
                            AppNavigation(
                                navController = navController,
                                isDark = isDark,
                                isMatrix = isMatrix
                            )
                        }
                    }
                }
            }
        }
    }
}
