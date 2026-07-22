package com.example.appgasto.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appgasto.R
import com.example.appgasto.domain.model.BudgetChartStyle
import com.example.appgasto.domain.model.Currency
import com.example.appgasto.ui.components.SectionHeader
import com.example.appgasto.ui.theme.Dimens
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class BudgetDisplayMode { SPENT_REMAINING, AVAILABLE_PERCENT }

@Composable
fun BudgetChartSection(
    spent: Double,
    budget: Double,
    currency: Currency,
    style: BudgetChartStyle,
    modifier: Modifier = Modifier
) {
    if (budget <= 0.0) return

    var displayMode by remember { mutableStateOf(BudgetDisplayMode.SPENT_REMAINING) }
    val ratio = remember(spent, budget) { (spent / budget).toFloat() }
    val clampedProgress = ratio.coerceIn(0f, 1f)
    val accent = budgetAccentColor(ratio)
    val remaining = (budget - spent).coerceAtLeast(0.0)
    val available = remaining
    val percentInt = (ratio * 100).toInt().coerceAtMost(999)

    val spentText = remember(spent, currency) { currency.format(spent) }
    val budgetText = remember(budget, currency) { currency.format(budget) }
    val remainingText = remember(remaining, currency) { currency.format(remaining) }
    val availableText = remember(available, currency) { currency.format(available) }

    val primaryLabel: String
    val primaryValue: String
    val secondaryLabel: String
    val secondaryValue: String
    when (displayMode) {
        BudgetDisplayMode.SPENT_REMAINING -> {
            primaryLabel = stringResource(R.string.budget_spent)
            primaryValue = spentText
            secondaryLabel = stringResource(R.string.budget_remaining)
            secondaryValue = remainingText
        }
        BudgetDisplayMode.AVAILABLE_PERCENT -> {
            primaryLabel = stringResource(R.string.budget_available)
            primaryValue = availableText
            secondaryLabel = stringResource(R.string.budget_percent_used)
            secondaryValue = "$percentInt%"
        }
    }

    val a11y = stringResource(
        R.string.cd_budget_chart,
        spentText,
        budgetText,
        percentInt
    )

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.budget_chart_title))
        Spacer(modifier = Modifier.height(Dimens.spaceMd))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = a11y }
                .clickable {
                    displayMode = when (displayMode) {
                        BudgetDisplayMode.SPENT_REMAINING -> BudgetDisplayMode.AVAILABLE_PERCENT
                        BudgetDisplayMode.AVAILABLE_PERCENT -> BudgetDisplayMode.SPENT_REMAINING
                    }
                },
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spaceLg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (style) {
                    BudgetChartStyle.CIRCULAR -> CircularBudgetGauge(
                        progress = clampedProgress,
                        color = accent,
                        centerLabel = if (displayMode == BudgetDisplayMode.AVAILABLE_PERCENT) {
                            "$percentInt%"
                        } else {
                            primaryValue
                        },
                        centerSub = if (displayMode == BudgetDisplayMode.AVAILABLE_PERCENT) {
                            primaryLabel
                        } else {
                            primaryLabel
                        }
                    )
                    BudgetChartStyle.BAR -> HorizontalBudgetBar(
                        progress = clampedProgress,
                        color = accent,
                        primaryLabel = primaryLabel,
                        primaryValue = primaryValue,
                        secondaryLabel = secondaryLabel,
                        secondaryValue = secondaryValue
                    )
                    BudgetChartStyle.SPEEDOMETER -> SpeedometerBudgetGauge(
                        progress = clampedProgress,
                        color = accent,
                        label = primaryValue,
                        subLabel = primaryLabel
                    )
                    BudgetChartStyle.COMPACT -> CompactBudgetRow(
                        progress = clampedProgress,
                        color = accent,
                        primaryLabel = primaryLabel,
                        primaryValue = primaryValue,
                        secondaryValue = secondaryValue
                    )
                }

                if (style != BudgetChartStyle.BAR && style != BudgetChartStyle.COMPACT) {
                    Spacer(modifier = Modifier.height(Dimens.spaceMd))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = primaryLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = primaryValue,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = secondaryLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = secondaryValue,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceSm))
                Text(
                    text = stringResource(R.string.budget_chart_tap_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun budgetAccentColor(ratio: Float): Color {
    return when {
        ratio >= 1f -> MaterialTheme.colorScheme.error
        ratio >= 0.8f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun CircularBudgetGauge(
    progress: Float,
    color: Color,
    centerLabel: String,
    centerSub: String
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val stroke = 16.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = centerSub,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HorizontalBudgetBar(
    progress: Float,
    color: Color,
    primaryLabel: String,
    primaryValue: String,
    secondaryLabel: String,
    secondaryValue: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = primaryLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = secondaryLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = secondaryValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.spaceMd))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SpeedometerBudgetGauge(
    progress: Float,
    color: Color,
    label: String,
    subLabel: String
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val needleColor = MaterialTheme.colorScheme.onSurface
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(width = 200.dp, height = 110.dp)) {
            val stroke = 14.dp.toPx()
            val diameter = min(size.width, size.height * 2f) - stroke
            val topLeft = Offset((size.width - diameter) / 2f, size.height - diameter / 2f - stroke / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = track,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            val cx = size.width / 2f
            val cy = size.height - stroke / 2f
            val angle = Math.toRadians((180.0 + 180.0 * progress))
            val needleLen = diameter / 2f - stroke
            val end = Offset(
                cx + (cos(angle) * needleLen).toFloat(),
                cy + (sin(angle) * needleLen).toFloat()
            )
            drawLine(
                color = needleColor,
                start = Offset(cx, cy),
                end = end,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = needleColor, radius = 6.dp.toPx(), center = Offset(cx, cy))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompactBudgetRow(
    progress: Float,
    color: Color,
    primaryLabel: String,
    primaryValue: String,
    secondaryValue: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = primaryLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = secondaryValue,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(Dimens.spaceSm))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimens.spaceSm))
        Text(
            text = primaryValue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
