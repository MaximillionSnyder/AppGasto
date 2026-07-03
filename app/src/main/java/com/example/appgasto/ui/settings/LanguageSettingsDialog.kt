package com.example.appgasto.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appgasto.R
import com.example.appgasto.domain.model.AppLanguage

@Composable
fun LanguageSettingsDialog(
    currentLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                LanguageOption(AppLanguage.SYSTEM, R.string.lang_system, "\uD83C\uDF10", currentLanguage, onSelect)
                LanguageOption(AppLanguage.SPANISH, R.string.lang_es, "\uD83C\uDDEA\uD83C\uDDF8", currentLanguage, onSelect)
                LanguageOption(AppLanguage.ENGLISH, R.string.lang_en, "\uD83C\uDDEC\uD83C\uDDE7", currentLanguage, onSelect)
                LanguageOption(AppLanguage.PORTUGUESE, R.string.lang_pt, "\uD83C\uDDE7\uD83C\uDDF7", currentLanguage, onSelect)
                LanguageOption(AppLanguage.ITALIAN, R.string.lang_it, "\uD83C\uDDEE\uD83C\uDDF9", currentLanguage, onSelect)
                LanguageOption(AppLanguage.GERMAN, R.string.lang_de, "\uD83C\uDDE9\uD83C\uDDEA", currentLanguage, onSelect)
                LanguageOption(AppLanguage.JAPANESE, R.string.lang_ja, "\uD83C\uDDEF\uD83C\uDDF5", currentLanguage, onSelect)
                LanguageOption(AppLanguage.KOREAN, R.string.lang_ko, "\uD83C\uDDF0\uD83C\uDDF7", currentLanguage, onSelect)
                LanguageOption(AppLanguage.QUECHUA, R.string.lang_qu, "\uD83C\uDDF5\uD83C\uDDEA", currentLanguage, onSelect)
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
private fun LanguageOption(
    language: AppLanguage,
    labelRes: Int,
    flag: String,
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit
) {
    val isSelected = current == language
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(language) }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = flag,
            fontSize = 22.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
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
        }
    }
}
