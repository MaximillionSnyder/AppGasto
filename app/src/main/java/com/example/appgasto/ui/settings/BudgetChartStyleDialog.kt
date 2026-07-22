package com.example.appgasto.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appgasto.R
import com.example.appgasto.domain.model.BudgetChartStyle

@Composable
fun BudgetChartStyleDialog(
    currentStyle: BudgetChartStyle,
    onSelect: (BudgetChartStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.budget_chart_style),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                BudgetChartStyle.entries.forEach { style ->
                    StyleOption(
                        style = style,
                        labelRes = styleLabelRes(style),
                        current = currentStyle,
                        onSelect = onSelect
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun budgetChartStyleLabel(style: BudgetChartStyle): String {
    return stringResource(styleLabelRes(style))
}

private fun styleLabelRes(style: BudgetChartStyle): Int = when (style) {
    BudgetChartStyle.CIRCULAR -> R.string.budget_chart_style_circular
    BudgetChartStyle.BAR -> R.string.budget_chart_style_bar
    BudgetChartStyle.SPEEDOMETER -> R.string.budget_chart_style_speedometer
    BudgetChartStyle.COMPACT -> R.string.budget_chart_style_compact
}

@Composable
private fun StyleOption(
    style: BudgetChartStyle,
    labelRes: Int,
    current: BudgetChartStyle,
    onSelect: (BudgetChartStyle) -> Unit
) {
    val isSelected = current == style
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(style) }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}
