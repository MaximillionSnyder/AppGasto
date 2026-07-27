package com.example.appgasto.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appgasto.R
import com.example.appgasto.ui.theme.Dimens

private data class NavDestination(
    @StringRes val titleRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

private val baseDestinations = listOf(
    NavDestination(R.string.home_title, Icons.Filled.Home, Icons.Outlined.Home),
    NavDestination(R.string.list_title, Icons.Filled.FormatListBulleted, Icons.Outlined.FormatListBulleted),
    NavDestination(R.string.stats_title, Icons.Filled.BarChart, Icons.Outlined.BarChart),
    NavDestination(R.string.settings_title, Icons.Filled.Settings, Icons.Outlined.Settings)
)

/**
 * Barra de navegación flotante estilo M3 Expressive.
 *
 * Píldora redondeada despegada de los bordes, con indicador animado,
 * etiqueta solo en el ítem activo e iconos outlined -> filled.
 */
@Composable
fun FloatingNavBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showExtraTab: Boolean = false
) {
    val haptics = LocalHapticFeedback.current

    val destinations = buildList {
        addAll(baseDestinations)
        if (showExtraTab) {
            add(NavDestination(R.string.advanced_budget_tab, Icons.Filled.PieChart, Icons.Outlined.PieChart))
        }
    }

    NavigationBar(
        modifier = modifier
            .padding(horizontal = Dimens.spaceLg, vertical = Dimens.spaceSm)
            .navigationBarsPadding()
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .height(64.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        destinations.forEachIndexed { index, destination ->
            val selected = selectedIndex == index
            val title = stringResource(destination.titleRes)

            NavigationBarItem(
                selected = selected,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onItemSelected(index)
                },
                icon = {
                    Crossfade(
                        targetState = selected,
                        animationSpec = tween(200),
                        label = "navIconCrossfade"
                    ) { isSelected ->
                        Icon(
                            imageVector = if (isSelected) destination.filledIcon else destination.outlinedIcon,
                            contentDescription = title
                        )
                    }
                },
                label = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FloatingNavBarPreview() {
    MaterialTheme {
        FloatingNavBar(selectedIndex = 0, onItemSelected = {})
    }
}
