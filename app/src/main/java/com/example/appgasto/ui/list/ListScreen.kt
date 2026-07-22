package com.example.appgasto.ui.list

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appgasto.R
import com.example.appgasto.data.local.Expense
import com.example.appgasto.data.local.localizedName
import com.example.appgasto.ui.components.ConfirmDeleteDialog
import com.example.appgasto.ui.components.EmptyState
import com.example.appgasto.ui.components.ExpenseItem
import com.example.appgasto.ui.theme.Dimens
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    isDark: Boolean,
    isMatrix: Boolean = false,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: ListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilterCategory by remember { mutableStateOf<Long?>(null) }
    var selectedMonth by remember { mutableStateOf<YearMonth?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var expensePendingDelete by remember { mutableStateOf<Expense?>(null) }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    val currentMonth = remember { YearMonth.now() }
    val hasActiveFilters = selectedFilterCategory != null || selectedMonth != null

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spaceSm),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showFilters = !showFilters }) {
                BadgedBox(
                    badge = {
                        if (hasActiveFilters) {
                            Badge()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.cd_filter),
                        tint = if (showFilters || hasActiveFilters) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        if (showFilters) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spaceLg, vertical = Dimens.spaceSm)
            ) {
                Text(
                    stringResource(R.string.filter_category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spaceSm))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
                ) {
                    FilterChip(
                        selected = selectedFilterCategory == null,
                        onClick = {
                            selectedFilterCategory = null
                            viewModel.applyFilters(null, selectedMonth?.atDay(1), selectedMonth?.atEndOfMonth())
                        },
                        label = { Text(stringResource(R.string.filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    state.categories.values.forEach { cat ->
                        FilterChip(
                            selected = selectedFilterCategory == cat.id,
                            onClick = {
                                selectedFilterCategory = cat.id
                                viewModel.applyFilters(
                                    cat.id,
                                    selectedMonth?.atDay(1),
                                    selectedMonth?.atEndOfMonth()
                                )
                            },
                            label = { Text(cat.localizedName()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceMd))

                Text(
                    stringResource(R.string.filter_by_month),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spaceSm))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
                ) {
                    FilterChip(
                        selected = selectedMonth == null,
                        onClick = {
                            selectedMonth = null
                            viewModel.applyFilters(selectedFilterCategory, null, null)
                        },
                        label = { Text(stringResource(R.string.filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    state.availableMonths.forEach { month ->
                        FilterChip(
                            selected = selectedMonth == month,
                            onClick = {
                                selectedMonth = month
                                viewModel.applyFilters(
                                    selectedFilterCategory,
                                    month.atDay(1),
                                    month.atEndOfMonth()
                                )
                            },
                            label = {
                                Text(
                                    if (month == currentMonth) stringResource(R.string.this_month)
                                    else month.format(monthFormatter)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                if (hasActiveFilters) {
                    Spacer(modifier = Modifier.height(Dimens.spaceXs))
                    TextButton(onClick = {
                        selectedFilterCategory = null
                        selectedMonth = null
                        viewModel.applyFilters(null, null, null)
                    }) {
                        Text(stringResource(R.string.clear))
                    }
                }
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { liveRegion = LiveRegionMode.Polite },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.expenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    message = stringResource(
                        if (hasActiveFilters) R.string.no_filter_results else R.string.no_expenses
                    ),
                    actionLabel = if (hasActiveFilters) stringResource(R.string.clear) else null,
                    onAction = if (hasActiveFilters) {
                        {
                            selectedFilterCategory = null
                            selectedMonth = null
                            viewModel.applyFilters(null, null, null)
                        }
                    } else {
                        null
                    }
                )
            }
        } else {
            val groupedExpenses = remember(state.expenses) {
                state.expenses.groupBy { YearMonth.from(it.createdAt) }
                    .toSortedMap(Comparator.reverseOrder())
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.spaceLg),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(contentType = "spacer_top") {
                    Spacer(modifier = Modifier.height(Dimens.spaceXs))
                }
                groupedExpenses.forEach { (month, expenses) ->
                    item(contentType = "month_header") {
                        Text(
                            text = if (month == currentMonth) stringResource(R.string.this_month)
                            else month.format(monthFormatter),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = Dimens.spaceSm, horizontal = Dimens.spaceXs)
                                .semantics { heading() }
                        )
                    }
                    items(expenses, key = { it.id }, contentType = { "expense" }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            category = state.categories[expense.categoryId],
                            isDark = isDark,
                            isMatrix = isMatrix,
                            onEdit = { onNavigateToEdit(expense.id) },
                            onDelete = { expensePendingDelete = expense },
                            showActions = true,
                            showDelete = true
                        )
                    }
                }
                item(contentType = "spacer_bottom") {
                    Spacer(modifier = Modifier.height(Dimens.fabClearance))
                }
            }
        }
    }

    expensePendingDelete?.let { expense ->
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteExpense(expense)
                expensePendingDelete = null
            },
            onDismiss = { expensePendingDelete = null }
        )
    }
}
