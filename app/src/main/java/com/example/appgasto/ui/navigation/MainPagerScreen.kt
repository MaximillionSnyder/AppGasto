package com.example.appgasto.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.appgasto.R
import com.example.appgasto.ui.home.HomeScreen
import com.example.appgasto.ui.list.ListScreen
import com.example.appgasto.ui.settings.SettingsScreen
import com.example.appgasto.ui.stats.StatsPeriod
import com.example.appgasto.ui.stats.StatsScreen
import kotlinx.coroutines.launch

@Composable
fun MainPagerScreen(
    isDark: Boolean,
    isMatrix: Boolean,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    var pendingStatsPeriod by remember { mutableStateOf<StatsPeriod?>(null) }

    val showFab = pagerState.currentPage == 0 || pagerState.currentPage == 1

    Scaffold(
        bottomBar = {
            FloatingNavBar(
                selectedIndex = pagerState.currentPage,
                onItemSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_expense),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    isDark = isDark,
                    isMatrix = isMatrix,
                    onNavigateToEdit = onNavigateToEdit,
                    onNavigateToStats = { period ->
                        pendingStatsPeriod = period
                        scope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
                1 -> ListScreen(
                    isDark = isDark,
                    isMatrix = isMatrix,
                    onNavigateToEdit = onNavigateToEdit
                )
                2 -> StatsScreen(
                    isDark = isDark,
                    isMatrix = isMatrix,
                    pendingPeriod = pendingStatsPeriod,
                    onPeriodConsumed = { pendingStatsPeriod = null }
                )
                3 -> SettingsScreen(
                    isDark = isDark,
                    embeddedInPager = true
                )
            }
        }
    }
}
