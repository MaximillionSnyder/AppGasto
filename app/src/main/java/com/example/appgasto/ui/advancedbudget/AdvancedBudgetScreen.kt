package com.example.appgasto.ui.advancedbudget

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appgasto.R
import com.example.appgasto.data.local.Category
import com.example.appgasto.data.local.localizedName
import com.example.appgasto.ui.theme.CategoryColors
import com.example.appgasto.ui.theme.Dimens

@Composable
fun AdvancedBudgetScreen(
    isDark: Boolean,
    isMatrix: Boolean = false,
    viewModel: AdvancedBudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.spaceLg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
    ) {
        Spacer(modifier = Modifier.height(Dimens.spaceMd))

        Text(
            text = stringResource(R.string.advanced_budget_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (state.budgetNotConfigured) {
            Spacer(modifier = Modifier.height(Dimens.spaceSm))
            Text(
                text = stringResource(R.string.advanced_budget_not_configured),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(Dimens.spaceSm))

            state.categories.forEach { category ->
                val budgetAmount = state.categoryBudgets[category.id] ?: 0.0
                var textValue by remember(category.id) {
                    mutableStateOf(
                        if (budgetAmount > 0) budgetAmount.toString() else ""
                    )
                }

                LaunchedEffect(budgetAmount) {
                    val formatted = if (budgetAmount == budgetAmount.toLong().toDouble()) {
                        budgetAmount.toLong().toString()
                    } else {
                        budgetAmount.toString()
                    }
                    if (textValue.toDoubleOrNull() != budgetAmount) {
                        textValue = if (budgetAmount > 0) formatted else ""
                    }
                }

                CategoryBudgetRow(
                    category = category,
                    isDark = isDark,
                    isMatrix = isMatrix,
                    value = textValue,
                    onValueChange = { newValue ->
                        val normalized = newValue.replace(',', '.')
                        if (normalized.count { it == '.' } > 1) return@CategoryBudgetRow
                        val parts = normalized.split('.')
                        if (parts.size == 2 && parts[1].length > 4) return@CategoryBudgetRow
                        textValue = normalized
                    },
                    onFocusLost = {
                        val amount = textValue.toDoubleOrNull() ?: 0.0
                        viewModel.updateBudget(category.id, amount)
                        if (amount > 0 && textValue != amount.toString()) {
                            textValue = amount.toString()
                        }
                    },
                    currency = state.baseCurrency
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceSm))

            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.budget_spent),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (state.monthlyBudget > 0)
                                "${((state.totalAllocated / state.monthlyBudget) * 100).toInt().coerceAtMost(999)}%"
                            else "0%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isExceeded) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.spaceSm))

                    val progress = if (state.monthlyBudget > 0)
                        (state.totalAllocated / state.monthlyBudget).toFloat().coerceIn(0f, 1.5f).coerceAtMost(1f)
                    else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.spaceSm),
                        color = if (state.isExceeded) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(Dimens.spaceSm))

                    Text(
                        text = stringResource(
                            R.string.advanced_budget_allocated,
                            state.baseCurrency.format(state.totalAllocated),
                            state.baseCurrency.format(state.monthlyBudget)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (state.isExceeded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimens.iconSm)
                    )
                    Text(
                        text = stringResource(R.string.advanced_budget_total_exceeded),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceLg))
        }
    }
}

@Composable
private fun CategoryBudgetRow(
    category: Category,
    isDark: Boolean,
    isMatrix: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    currency: com.example.appgasto.domain.model.Currency
) {
    val categoryColor = remember(isDark, isMatrix) {
        CategoryColors.getById(category.id, isDark, isMatrix, false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.cardPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spaceMd))

            Text(
                text = category.localizedName(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(0.4f)
            )

            Spacer(modifier = Modifier.width(Dimens.spaceMd))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(0.6f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) onFocusLost()
                    },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = MaterialTheme.shapes.small
            )
        }
    }
}
