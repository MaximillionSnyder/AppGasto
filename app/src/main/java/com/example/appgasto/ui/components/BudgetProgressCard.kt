package com.example.appgasto.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.appgasto.R
import com.example.appgasto.domain.model.Currency
import com.example.appgasto.ui.theme.Dimens

@Composable
fun BudgetProgressCard(
    spent: Double,
    budget: Double,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    if (budget <= 0.0) return

    val ratio = remember(spent, budget) { (spent / budget).toFloat().coerceIn(0f, 1.5f) }
    val progress = ratio.coerceAtMost(1f)
    val barColor = when {
        ratio >= 1f -> MaterialTheme.colorScheme.error
        ratio >= 0.8f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val spentText = remember(spent, currency) { currency.format(spent) }
    val budgetText = remember(budget, currency) { currency.format(budget) }
    val percentText = remember(ratio) { "${(ratio * 100).toInt().coerceAtMost(999)}%" }

    Card(
        modifier = modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.monthly_budget),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = percentText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spaceSm))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.spaceSm),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSm))
            Text(
                text = "$spentText / $budgetText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
