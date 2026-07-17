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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appgasto.R
import com.example.appgasto.data.local.localizedName
import com.example.appgasto.ui.components.ExpenseItem
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
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
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    val currentMonth = remember { YearMonth.now() }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = if (showFilters) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showFilters) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        stringResource(R.string.filter_category),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilterCategory == null,
                            onClick = {
                                selectedFilterCategory = null
                                viewModel.applyFilters(null, null, null)
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
                                    viewModel.applyFilters(cat.id, null, null)
                                },
                                label = { Text(cat.localizedName()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Por mes",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        if (month == currentMonth) "Este mes"
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

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(onClick = {
                        selectedFilterCategory = null
                        selectedMonth = null
                        viewModel.applyFilters(null, null, null)
                    }) {
                        Text(stringResource(R.string.clear))
                    }
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.no_expenses),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val groupedExpenses = remember(state.expenses) {
                    state.expenses.groupBy { YearMonth.from(it.createdAt) }
                        .toSortedMap(Comparator.reverseOrder())
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(contentType = "spacer_top") { Spacer(modifier = Modifier.height(4.dp)) }
                    groupedExpenses.forEach { (month, expenses) ->
                        item(contentType = "month_header") {
                            Text(
                                text = if (month == currentMonth) "Este mes"
                                       else month.format(monthFormatter),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                        items(expenses, key = { it.id }, contentType = { "expense" }) { expense ->
                            ExpenseItem(
                                expense = expense,
                                category = state.categories[expense.categoryId],
                                isDark = isDark,
                                isMatrix = isMatrix,
                                onEdit = { onNavigateToEdit(expense.id) },
                                onDelete = { viewModel.deleteExpense(expense) }
                            )
                        }
                    }
                    item(contentType = "spacer_bottom") { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
    }
}
