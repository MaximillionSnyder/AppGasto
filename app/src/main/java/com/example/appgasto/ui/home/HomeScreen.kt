package com.example.appgasto.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appgasto.R
import com.example.appgasto.domain.model.Currency
import com.example.appgasto.ui.components.BudgetProgressCard
import com.example.appgasto.ui.components.EmptyState
import com.example.appgasto.ui.components.ExpenseItem
import com.example.appgasto.ui.components.SectionHeader
import com.example.appgasto.ui.components.TotalHeroCard
import com.example.appgasto.ui.stats.StatsPeriod
import com.example.appgasto.ui.theme.Dimens
import com.example.appgasto.ui.theme.GradientTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    isDark: Boolean,
    isMatrix: Boolean = false,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToStats: (StatsPeriod) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { liveRegion = LiveRegionMode.Polite },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
        ) {
            item(contentType = "header") {
                Spacer(modifier = Modifier.height(Dimens.spaceSm))

                val formattedMonthTotal = remember(state.monthTotal, state.baseCurrency) {
                    state.baseCurrency.format(state.monthTotal)
                }
                TotalHeroCard(
                    label = stringResource(R.string.total_month),
                    amountText = formattedMonthTotal,
                    onClickLabel = stringResource(R.string.stats_title),
                    onClick = { onNavigateToStats(StatsPeriod.MONTHLY) }
                )

                if (state.budgetEnabled && state.monthlyBudget > 0) {
                    Spacer(modifier = Modifier.height(Dimens.spaceMd))
                    BudgetProgressCard(
                        spent = state.monthTotal,
                        budget = state.monthlyBudget,
                        currency = state.baseCurrency
                    )
                }

                if (state.monthCurrencyBreakdown.size > 1 ||
                    (state.monthCurrencyBreakdown.isNotEmpty() &&
                        state.monthCurrencyBreakdown.first().currency != state.baseCurrency.code)
                ) {
                    var currencyExpanded by remember { mutableStateOf(false) }
                    Spacer(modifier = Modifier.height(Dimens.spaceSm))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currencyExpanded = !currencyExpanded }
                            .padding(vertical = Dimens.spaceXs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.currency_breakdown),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (currencyExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(
                                if (currencyExpanded) R.string.cd_collapse else R.string.cd_expand
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(visible = currencyExpanded) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
                        ) {
                            state.monthCurrencyBreakdown.forEach { tuple ->
                                val breakdownText = remember(tuple, state.baseCurrency, state.rateToBase) {
                                    val tupleCurrency = Currency.fromCode(tuple.currency)
                                    "${tupleCurrency.format(tuple.totalOriginal)} = ${state.baseCurrency.format(tuple.totalInPEN * state.rateToBase)}"
                                }
                                SuggestionChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = breakdownText,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceSm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniSummaryCard(
                        title = stringResource(R.string.total_today),
                        amount = state.todayTotal,
                        icon = Icons.Default.Today,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToStats(StatsPeriod.DAILY) },
                        currency = state.baseCurrency
                    )
                    MiniSummaryCard(
                        title = stringResource(R.string.total_week),
                        amount = state.weekTotal,
                        icon = Icons.Default.DateRange,
                        color = GradientTertiary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToStats(StatsPeriod.WEEKLY) },
                        currency = state.baseCurrency
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spaceSm))
                SectionHeader(title = stringResource(R.string.today_expenses))
            }

            if (state.todayExpenses.isEmpty()) {
                item(contentType = "empty") {
                    EmptyState(
                        icon = Icons.Default.CalendarToday,
                        message = stringResource(R.string.no_expenses_today)
                    )
                }
            } else {
                items(state.todayExpenses, key = { it.id }, contentType = { "expense" }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        category = state.categories[expense.categoryId],
                        isDark = isDark,
                        isMatrix = isMatrix,
                        onEdit = { onNavigateToEdit(expense.id) },
                        showActions = false
                    )
                }
            }

            item(contentType = "footer") {
                Spacer(modifier = Modifier.height(Dimens.fabClearance))
            }
        }
    }
}

@Composable
private fun MiniSummaryCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    currency: Currency = Currency.PEN
) {
    Card(
        modifier = modifier.clickable(onClickLabel = title, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(Dimens.iconSm)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.spaceSm))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spaceSm))
            Text(
                text = currency.format(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp
            )
        }
    }
}
