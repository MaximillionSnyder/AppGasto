package com.example.appgasto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.appgasto.ui.theme.Dimens
import com.example.appgasto.ui.theme.GradientEnd
import com.example.appgasto.ui.theme.GradientStart

@Composable
fun TotalHeroCard(
    label: String,
    amountText: String,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        onClickLabel = onClickLabel,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(Dimens.heroPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start
                )
                Spacer(modifier = Modifier.height(Dimens.spaceXs))
                Text(
                    text = amountText,
                    style = if (centered) {
                        MaterialTheme.typography.displayLarge
                    } else {
                        MaterialTheme.typography.displayMedium
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start
                )
            }
        }
    }
}
