package com.example.appgasto.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
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

    val pageTitles = listOf(R.string.home_title, R.string.list_title, R.string.stats_title, R.string.settings_title)
    val pageIcons = listOf(Icons.Default.Home, Icons.Default.FormatListBulleted, Icons.Default.BarChart, Icons.Default.Settings)

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                pageTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        icon = {
                            Icon(
                                pageIcons[index],
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(title),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                LargeFloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
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
