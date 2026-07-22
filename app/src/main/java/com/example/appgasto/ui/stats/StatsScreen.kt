package com.example.appgasto.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appgasto.R
import com.example.appgasto.data.local.localizedName
import com.example.appgasto.ui.components.EmptyState
import com.example.appgasto.ui.components.SectionHeader
import com.example.appgasto.ui.components.TotalHeroCard
import com.example.appgasto.ui.theme.CategoryColors
import com.example.appgasto.ui.theme.Dimens
import com.example.appgasto.ui.theme.LocalIsHighContrast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    isDark: Boolean,
    isMatrix: Boolean = false,
    pendingPeriod: StatsPeriod? = null,
    onPeriodConsumed: () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(pendingPeriod) {
        if (pendingPeriod != null) {
            viewModel.loadStats(pendingPeriod)
            onPeriodConsumed()
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.spaceLg)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Dimens.spaceSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
            ) {
                StatsPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = state.period == period,
                        onClick = { viewModel.loadStats(period) },
                        label = {
                            Text(
                                when (period) {
                                    StatsPeriod.DAILY -> stringResource(R.string.daily)
                                    StatsPeriod.WEEKLY -> stringResource(R.string.weekly)
                                    StatsPeriod.MONTHLY -> stringResource(R.string.monthly)
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceLg))

            val formattedTotal = remember(state.totalExpenses, state.baseCurrency) {
                state.baseCurrency.format(state.totalExpenses)
            }
            TotalHeroCard(
                label = stringResource(R.string.total),
                amountText = formattedTotal,
                centered = true
            )

            Spacer(modifier = Modifier.height(Dimens.spaceXl))

            SectionHeader(title = stringResource(R.string.by_category))

            Spacer(modifier = Modifier.height(Dimens.spaceLg))

            if (state.categoryTotals.isNotEmpty() && state.totalExpenses > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isHighContrast = LocalIsHighContrast.current
                        val donutSlices = remember(state.categoryTotals, isDark, isMatrix, isHighContrast) {
                            state.categoryTotals.map { catTotal ->
                                DonutSlice(
                                    color = CategoryColors.getById(
                                        catTotal.category.id,
                                        isDark,
                                        isMatrix,
                                        isHighContrast
                                    ),
                                    percentage = (catTotal.total / state.totalExpenses * 100).toFloat()
                                )
                            }
                        }
                        DonutChart(
                            categoryTotals = donutSlices,
                            modifier = Modifier.size(180.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        state.categoryTotals.zip(donutSlices) { catTotal, slice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = catTotal.category.localizedName(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${String.format("%.1f", slice.percentage)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = Dimens.spaceSm)
                                )
                                val formattedCatTotal = remember(catTotal.total, state.baseCurrency) {
                                    state.baseCurrency.format(catTotal.total)
                                }
                                Text(
                                    text = formattedCatTotal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(slice.percentage / 100f)
                                        .height(6.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    slice.color,
                                                    slice.color.copy(alpha = 0.6f)
                                                )
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(Dimens.spaceXs))
                        }
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Default.BarChart,
                    message = stringResource(R.string.no_expenses)
                )
            }

            if (state.budgetEnabled && state.monthlyBudget > 0) {
                Spacer(modifier = Modifier.height(Dimens.spaceXl))
                BudgetChartSection(
                    spent = state.monthlyExpenseTotal,
                    budget = state.monthlyBudget,
                    currency = state.baseCurrency,
                    style = state.budgetChartStyle
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceXl))
        }
    }
}

data class DonutSlice(
    val color: Color,
    val percentage: Float
)

@Composable
private fun DonutChart(
    categoryTotals: List<DonutSlice>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 32.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        var startAngle = -90f

        categoryTotals.forEach { slice ->
            val sweepAngle = (slice.percentage / 100f) * 360f
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}
