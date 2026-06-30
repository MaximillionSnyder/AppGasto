package com.example.appgasto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.appgasto.ui.add.AddEditScreen
import com.example.appgasto.ui.home.HomeScreen
import com.example.appgasto.ui.list.ListScreen
import com.example.appgasto.ui.settings.SettingsScreen
import com.example.appgasto.ui.stats.StatsScreen

object Routes {
    const val HOME = "home"
    const val ADD = "add?expenseId={expenseId}"
    const val LIST = "list"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    fun addExpense(expenseId: Long? = null) =
        if (expenseId != null) "add?expenseId=$expenseId" else "add"

    fun editExpense(expenseId: Long) = "add?expenseId=$expenseId"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    isDark: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                isDark = isDark,
                onNavigateToAdd = { navController.navigate(Routes.addExpense()) },
                onNavigateToEdit = { expenseId -> navController.navigate(Routes.editExpense(expenseId)) },
                onNavigateToList = { navController.navigate(Routes.LIST) },
                onNavigateToStats = { navController.navigate(Routes.STATS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.ADD,
            arguments = listOf(
                navArgument("expenseId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: -1L
            AddEditScreen(
                expenseId = if (expenseId == -1L) null else expenseId,
                isDark = isDark,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LIST) {
            ListScreen(
                isDark = isDark,
                onNavigateToEdit = { expenseId -> navController.navigate(Routes.editExpense(expenseId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                isDark = isDark,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                isDark = isDark,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
